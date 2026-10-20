package com.edgeai.reader;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import com.edgeai.reader.databinding.*;
import com.edgeai.reader.model.BookItem;
import com.edgeai.reader.model.ReadingHistoryItem;
import com.edgeai.reader.model.ReadingPreferences;
import com.edgeai.reader.model.ReadingProgress;
import com.edgeai.reader.model.UserProfile;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    private String current="Splash", userName="Reader", userEmail="", selectedBook="";
    private boolean signup=false, focusMode=false, demoTelemetry=true, warmReader=true, highContrast=false, reduceMotion=false;
    private float textScale=1.0f, spacing=1.0f;
    private int sessions=0, minutes=0, wordsRead=0, regressions=0, dailyGoalMinutes=5;
    private TextToSpeech tts;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseManager firebaseManager;
    private final ArrayList<Book> books=new ArrayList<>();
    private final ArrayList<String> history=new ArrayList<>();
    private ActivityResultLauncher<String[]> bookPicker;
    private ActivityResultLauncher<String> imagePicker;
    private ActivityResultLauncher<Intent> mapPicker;
    private String profileAddress="Not provided";
    private String profileMobile="Not provided";
    private String profileAge="Not provided";
    private String profileGender="Prefer not to say";
    private Uri profileUri;

    private static class Book {
        String title, type, content, uri;
        long added;
        Book(String t,String ty,String c,String u){title=t;type=ty;content=c;uri=u;added=System.currentTimeMillis();}
    }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        try {
            FirebaseApp.initializeApp(this);
            auth=FirebaseAuth.getInstance();
            db=FirebaseFirestore.getInstance();
            storage=FirebaseStorage.getInstance();
        } catch(Exception ignored) {}
        firebaseManager=FirebaseManager.getInstance(this);
        if(firebaseManager.isUserSignedIn()){
            userName=firebaseManager.getCurrentUserDisplayName();
            userEmail=firebaseManager.getCurrentUserEmail();
            loadCloudUserData();
        }
        tts=new TextToSpeech(this, status -> {});
        books.add(new Book("The Secret Garden","TXT","Mary Lennox was a sour, contrary, and disagreeable child. But a hidden garden was waiting to change everything. She discovered a small key beneath the earth, and behind an old door, a world full of green life began to wake.","sample"));
        books.add(new Book("A Little Guide to Space","TXT","Space is enormous. Stars are born in clouds of gas and dust. Planets travel around stars, and gravity keeps worlds moving through the universe.","sample"));
        books.add(new Book("Reading with Confidence","TXT","Reading is a skill that grows with practice. Take a breath, follow one line at a time, and celebrate every small step. There is no single right pace for learning.","sample"));
        bookPicker=registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri->{ if(uri!=null) importBook(uri); });
        mapPicker=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result->{
            if(result.getResultCode()==RESULT_OK && result.getData()!=null){
                profileAddress=result.getData().getStringExtra("address");
                toast("Address selected"); show("EditProfile");
            }
        });
        imagePicker=registerForActivityResult(new ActivityResultContracts.GetContent(), uri->{ if(uri!=null){profileUri=uri; show("Profile");}});
        binding.tabHome.setOnClickListener(v->show("Home"));
        binding.tabLibrary.setOnClickListener(v->show("Library"));
        binding.tabProgress.setOnClickListener(v->show("Progress"));
        binding.tabProfile.setOnClickListener(v->show("Profile"));
        binding.btnMore.setOnClickListener(v->{
            PopupMenu p=new PopupMenu(this,binding.btnMore);
            for(String q:new String[]{"Library","Progress","Profile","Settings","Help","About","Privacy","Logout"})p.getMenu().add(q);
            p.setOnMenuItemClickListener(i->{show(i.getTitle().toString());return true;});
            p.show();
        });
        show("Splash");
    }

    private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density);}

    private String displayLabel(String screen){
        switch(screen){
            case "ReaderSettings": return "Reader Settings";
            case "EditProfile": return "Edit Profile";
            default: return screen;
        }
    }

    private void show(String screen) {
        current=screen;
        if(firebaseManager!=null) firebaseManager.logScreenView(screen);
        boolean chrome = !screen.equals("Splash") && !screen.equals("Auth");
        binding.topbar.setVisibility(chrome? View.VISIBLE : View.GONE);
        binding.topbarDivider.setVisibility(chrome? View.VISIBLE : View.GONE);
        binding.bottomNav.setVisibility(chrome? View.VISIBLE : View.GONE);
        if(chrome){
            binding.screenLabel.setText(displayLabel(screen).toUpperCase(Locale.ROOT));
            binding.screenLabel.setContentDescription(screen+" screen");
            updateBottomNav(screen);
        }
        binding.contentContainer.removeAllViews();
        switch(screen) {
            case "Splash": splash(); break;
            case "Auth": authScreen(); break;
            case "Home": home(); break;
            case "Library": library(); break;
            case "Reader": reader(); break;
            case "ReaderSettings": readerSettings(); break;
            case "Gaze": gaze(); break;
            case "Progress": progress(); break;
            case "Insights": insights(); break;
            case "Focus": focus(); break;
            case "Audio": audio(); break;
            case "Upload": upload(); break;
            case "Profile": profile(); break;
            case "EditProfile": editProfile(); break;
            case "Achievements": achievements(); break;
            case "Goals": goals(); break;
            case "History": historyScreen(); break;
            case "Help": help(); break;
            case "About": about(); break;
            case "Privacy": privacy(); break;
            case "Web": web(); break;
            case "Logout": logout(); break;
            case "Settings": settings(); break;
            default: home();
        }
        if(!reduceMotion){
            binding.contentContainer.setAlpha(0f);
            binding.contentContainer.setTranslationY(dp(8));
            binding.contentContainer.animate().alpha(1f).translationY(0f).setDuration(220).start();
        }
    }

    private void updateBottomNav(String screen){
        styleTab(binding.tabHome, binding.tabHomeIcon, binding.tabHomeLabel, screen.equals("Home"), "Home");
        styleTab(binding.tabLibrary, binding.tabLibraryIcon, binding.tabLibraryLabel, screen.equals("Library"), "Library");
        styleTab(binding.tabProgress, binding.tabProgressIcon, binding.tabProgressLabel, screen.equals("Progress"), "Progress");
        styleTab(binding.tabProfile, binding.tabProfileIcon, binding.tabProfileLabel, screen.equals("Profile"), "Profile");
    }

    private void styleTab(android.widget.LinearLayout tab, android.widget.TextView icon, android.widget.TextView label, boolean active, String name){
        tab.setBackgroundResource(active? R.drawable.bg_nav_tab_selected : android.R.color.transparent);
        int color=ContextCompat.getColor(this, active? R.color.primary : R.color.ink_faint);
        icon.setTextColor(color); label.setTextColor(color);
        tab.setContentDescription(name + (active? ", current tab" : " tab"));
    }

    private void styleSegment(android.widget.TextView btn, boolean active){
        if(active){
            btn.setBackgroundResource(R.drawable.bg_segment_selected);
            btn.setTextColor(ContextCompat.getColor(this,R.color.ink));
        } else {
            btn.setBackground(null);
            btn.setTextColor(ContextCompat.getColor(this,R.color.ink_muted));
        }
    }

    // =========================================================================
    // Screens
    // =========================================================================

    private void splash(){
        ScreenSplashBinding sb = ScreenSplashBinding.inflate(getLayoutInflater(), binding.contentContainer, true);
        new Handler(Looper.getMainLooper()).postDelayed(()->{
            if(current.equals("Splash")){
                boolean loggedIn = false;
                try {
                    loggedIn = (firebaseManager != null && firebaseManager.isUserSignedIn())
                            || (auth != null && auth.getCurrentUser() != null);
                } catch(Throwable ignored) {}
                show(loggedIn ? "Home" : "Auth");
            }
        },1500);
        sb.getRoot().setOnClickListener(v->show("Auth"));
    }

    private void authScreen(){
        ScreenAuthBinding ab = ScreenAuthBinding.inflate(getLayoutInflater(), binding.contentContainer, true);
        styleSegment(ab.btnTabLogin, !signup);
        styleSegment(ab.btnTabSignup, signup);
        ab.inputName.setVisibility(signup? View.VISIBLE : View.GONE);
        ab.inputConfirmPassword.setVisibility(signup? View.VISIBLE : View.GONE);
        ab.btnSubmit.setText(signup? R.string.auth_submit_signup : R.string.auth_submit_login);
        ab.btnTabLogin.setOnClickListener(v->{signup=false; show("Auth");});
        ab.btnTabSignup.setOnClickListener(v->{signup=true; show("Auth");});
        ab.btnSubmit.setOnClickListener(v->{
            String em=ab.inputEmail.getText().toString().trim(), pw=ab.inputPassword.getText().toString();
            if(em.isEmpty()||pw.length()<6){toast("Enter an email and a password with at least 6 characters.");return;}
            userEmail=em;
            String enteredName = ab.inputName.getText()!=null ? ab.inputName.getText().toString().trim() : "";
            if(!enteredName.isEmpty()) userName=enteredName;
            if (firebaseManager != null && firebaseManager.isFirebaseAvailable()) {
                if (signup) {
                    firebaseManager.registerWithEmail(em, pw, userName, new FirebaseManager.AuthCallback() {
                        @Override public void onSuccess(String uid, String email, String displayName) {
                            userName = displayName;
                            userEmail = email;
                            loadCloudUserData();
                            show("Home");
                        }
                        @Override public void onFailure(String errorMessage) {
                            toast("Firebase sign-up: " + errorMessage);
                        }
                    });
                } else {
                    firebaseManager.signInWithEmail(em, pw, new FirebaseManager.AuthCallback() {
                        @Override public void onSuccess(String uid, String email, String displayName) {
                            userName = displayName;
                            userEmail = email;
                            loadCloudUserData();
                            show("Home");
                        }
                        @Override public void onFailure(String errorMessage) {
                            toast("Firebase sign-in failed: " + errorMessage);
                        }
                    });
                }
            } else if(auth!=null){
                if(signup)auth.createUserWithEmailAndPassword(em,pw).addOnCompleteListener(task->{if(task.isSuccessful()){if(auth.getCurrentUser()!=null)auth.getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(userName).build());show("Home");}else toast("Firebase sign-in: "+task.getException().getMessage());});
                else auth.signInWithEmailAndPassword(em,pw).addOnCompleteListener(task->{if(task.isSuccessful()){userName=auth.getCurrentUser().getDisplayName()==null?"Reader":auth.getCurrentUser().getDisplayName();show("Home");}else toast("Firebase sign-in failed. Configure Firebase or create a demo session.");});
            } else {
                if(em.contains("@")&&pw.length()>=6){userName=signup&&!enteredName.isEmpty()?enteredName:em.substring(0,em.indexOf('@'));show("Home");}
                else toast("Please check your details.");
            }
        });
        ab.btnGoogle.setOnClickListener(v-> new AlertDialog.Builder(this).setTitle(R.string.google_signin_title).setMessage(R.string.google_signin_body).setPositiveButton(R.string.got_it,null).show());
        ab.getRoot().setOnLongClickListener(v->{new AlertDialog.Builder(this).setTitle(R.string.demo_account_title).setMessage(R.string.demo_account_body).setPositiveButton(R.string.action_ok,null).show();return true;});
    }

    private void home(){
        ScreenHomeBinding hb = ScreenHomeBinding.inflate(getLayoutInflater(), binding.contentContainer, true);
        hb.textGreeting.setText(getString(R.string.home_greeting, userName));
        hb.statMinutes.setText(String.valueOf(minutes));
        hb.statWords.setText(String.format(Locale.US,"%,d",wordsRead));
        hb.statSessions.setText(String.valueOf(sessions));
        hb.btnHeroCta.setOnClickListener(v->{ if(!books.isEmpty()){selectedBook=books.get(0).title;} show("Reader"); });
        hb.booksContainer.removeAllViews();
        for(int i=0;i<Math.min(2,books.size());i++){
            final Book b=books.get(i);
            ItemBookBinding ib = ItemBookBinding.inflate(getLayoutInflater(), hb.booksContainer, true);
            ib.bookTitle.setText("📖  "+b.title);
            ib.bookSubtitle.setText(getString(R.string.label_type_duration, b.type));
            ib.bookPrimaryAction.setText(R.string.action_continue_reading);
            ib.bookPrimaryAction.setOnClickListener(v->{selectedBook=b.title; show("Reader");});
            ib.bookSecondaryAction.setVisibility(View.GONE);
        }
        hb.btnTryFocus.setOnClickListener(v->show("Focus"));
        hb.btnInsights.setOnClickListener(v->show("Insights"));
        hb.btnGaze.setOnClickListener(v->show("Gaze"));
    }

    private void library(){
        ScreenLibraryBinding lb = ScreenLibraryBinding.inflate(getLayoutInflater(), binding.contentContainer, true);
        lb.btnAddBook.setOnClickListener(v->show("Upload"));
        lb.btnCloudSync.setOnClickListener(v->new AlertDialog.Builder(this).setTitle(R.string.cloud_sync_dialog_title).setMessage(R.string.cloud_sync_dialog_body).setPositiveButton(R.string.action_ok,null).show());
        lb.labelBooksCount.setText(getString(R.string.label_books_count, books.size()));
        lb.booksContainer.removeAllViews();
        for(Book b:new ArrayList<>(books)){
            ItemBookBinding ib = ItemBookBinding.inflate(getLayoutInflater(), lb.booksContainer, true);
            ib.bookTitle.setText((b.type.equals("PDF")?"📕  ":"📘  ")+b.title);
            ib.bookSubtitle.setText(b.type+"  •  Added "+new SimpleDateFormat("dd MMM",Locale.getDefault()).format(new Date(b.added)));
            ib.bookPrimaryAction.setText(R.string.action_read_now);
            ib.bookPrimaryAction.setOnClickListener(v->{selectedBook=b.title; show("Reader");});
            ib.bookSecondaryAction.setVisibility(View.VISIBLE);
            ib.bookSecondaryAction.setOnClickListener(v->new AlertDialog.Builder(this).setTitle(b.title).setItems(new String[]{"Read","Export details","Delete from library"},(d,w)->{
                if(w==0){selectedBook=b.title;show("Reader");}
                else if(w==2)new AlertDialog.Builder(this).setTitle(R.string.delete_book_title).setMessage(getString(R.string.delete_book_body,b.title)).setNegativeButton(R.string.action_cancel,null).setPositiveButton(R.string.action_delete,(dd,ww)->{books.remove(b);if(firebaseManager!=null)firebaseManager.deleteBook(b.title,null);show("Library");}).show();
                else toast("Book: "+b.title);
            }).show());
        }
        lb.emptyState.setVisibility(books.isEmpty()? View.VISIBLE : View.GONE);
        lb.btnAddFirstBook.setOnClickListener(v->show("Upload"));
    }

    private Book selected(){for(Book b:books)if(b.title.equals(selectedBook))return b;return books.isEmpty()?new Book("Sample reading","TXT","Take your time. Every word is a step forward.","sample"):books.get(0);}

    private void reader(){
        Book b=selected();
        ScreenReaderBinding rb = ScreenReaderBinding.inflate(getLayoutInflater(), binding.contentContainer, true);
        rb.bookTitle.setText(b.title);
        rb.bookSubtitle.setText(getString(R.string.label_reader_subtitle, b.type));
        if(firebaseManager!=null){
            firebaseManager.logBookOpened(b.title,b.type);
            firebaseManager.logReadingSessionStarted(b.title,b.type);
        }
        rb.passageText.setText(b.content);
        rb.passageText.setTextSize(19*textScale);
        rb.passageText.setLineSpacing(dp(10),Math.max(1f,spacing));
        rb.passageText.setLetterSpacing(Math.max(0,spacing-1)*.08f);
        int bg = warmReader ? (focusMode? R.color.paper_warm_focus : R.color.paper_warm) : (highContrast? R.color.paper_high_contrast_bg : R.color.paper_cool);
        int fg = highContrast && !warmReader ? R.color.paper_high_contrast_text : R.color.ink;
        rb.passageText.setBackgroundColor(ContextCompat.getColor(this,bg));
        rb.passageText.setTextColor(ContextCompat.getColor(this,warmReader? R.color.ink : fg));
        rb.btnListen.setOnClickListener(v->{if(tts!=null)tts.speak(b.content,TextToSpeech.QUEUE_FLUSH,null,"edgeai-read");if(firebaseManager!=null)firebaseManager.logEvent("audio_playback_started","book_title",b.title);show("Audio");});
        rb.btnReaderAppearance.setOnClickListener(v->show("ReaderSettings"));
        rb.btnToggleFocus.setText(focusMode? R.string.action_focus_off : R.string.action_focus_on);
        rb.btnToggleFocus.setOnClickListener(v->{focusMode=!focusMode;if(firebaseManager!=null){firebaseManager.logFocusModeToggled(focusMode);savePreferencesToCloud();}show("Reader");});
        rb.btnSaveSession.setOnClickListener(v->{
            sessions++;minutes+=5;wordsRead+=120;
            String stamp=new SimpleDateFormat("dd MMM, HH:mm",Locale.getDefault()).format(new Date());
            history.add(b.title+" — "+stamp);
            if(firebaseManager!=null){
                ReadingProgress prog=new ReadingProgress(sessions,minutes,wordsRead,regressions,dailyGoalMinutes);
                firebaseManager.saveReadingProgress(prog,null);
                ReadingHistoryItem item=new ReadingHistoryItem(null,b.title,System.currentTimeMillis(),stamp,5,120);
                firebaseManager.addReadingHistory(item,null);
                firebaseManager.logReadingSessionSaved(b.title,5,120);
            }
            toast("Session saved. Nice work!");show("Progress");
        });
        boolean isPdf = b.type.equals("PDF") && !b.uri.equals("sample");
        rb.btnOpenPdf.setVisibility(isPdf? View.VISIBLE : View.GONE);
        if(isPdf) rb.btnOpenPdf.setOnClickListener(v->{try{Intent i=new Intent(this,PdfViewerActivity.class);i.setData(Uri.parse(b.uri));startActivity(i);}catch(Exception e){toast("Could not open PDF.");}});
        rb.btnReadingTools.setOnClickListener(v->show("Insights"));
        rb.pdfNote.setVisibility(b.type.equals("PDF")? View.VISIBLE : View.GONE);
    }

    private void readerSettings(){
        ScreenReaderSettingsBinding rb = ScreenReaderSettingsBinding.inflate(getLayoutInflater(), binding.contentContainer, true);
        rb.labelTextSize.setText(getString(R.string.label_text_size, Math.round(textScale*100)));
        rb.sliderTextSize.setMax(8);
        rb.sliderTextSize.setProgress((int)((textScale-.8f)*10));
        rb.sliderTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar s,int p,boolean u){textScale=.8f+p*.1f;rb.labelTextSize.setText(getString(R.string.label_text_size, Math.round(textScale*100)));savePreferencesToCloud();}
            public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}
        });
        rb.labelLetterSpacing.setText(getString(R.string.label_letter_spacing, spacing));
        rb.sliderLetterSpacing.setMax(10);
        rb.sliderLetterSpacing.setProgress((int)((spacing-1)*10));
        rb.sliderLetterSpacing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar s,int p,boolean u){spacing=1+p*.1f;rb.labelLetterSpacing.setText(getString(R.string.label_letter_spacing, spacing));savePreferencesToCloud();}
            public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}
        });
        rb.btnPreview.setOnClickListener(v->show("Reader"));
        rb.btnPaperTheme.setText(warmReader? R.string.action_cool_theme : R.string.action_warm_theme);
        rb.btnPaperTheme.setOnClickListener(v->{warmReader=!warmReader;savePreferencesToCloud();show("Reader");});
        rb.btnHighContrast.setText(highContrast? R.string.action_contrast_off : R.string.action_contrast_on);
        rb.btnHighContrast.setOnClickListener(v->{highContrast=!highContrast;savePreferencesToCloud();show("ReaderSettings");});
        rb.btnReduceMotion.setText(reduceMotion? R.string.action_enable_motion : R.string.action_reduce_motion);
        rb.btnReduceMotion.setOnClickListener(v->{reduceMotion=!reduceMotion;savePreferencesToCloud();show("ReaderSettings");});
        rb.btnResetDefault.setOnClickListener(v->{textScale=1;spacing=1;warmReader=true;highContrast=false;reduceMotion=false;savePreferencesToCloud();show("ReaderSettings");});
    }

    private void progress(){
        ScreenProgressBinding pb = ScreenProgressBinding.inflate(getLayoutInflater(), binding.contentContainer, true);
        pb.textMinutes.setText(getString(R.string.label_minutes_value, minutes));
        pb.metricSessions.metricLabel.setText(R.string.metric_sessions);
        pb.metricSessions.metricValue.setText(String.valueOf(sessions));
        pb.metricWords.metricLabel.setText(R.string.metric_words);
        pb.metricWords.metricValue.setText(String.format(Locale.US,"%,d",wordsRead));
        pb.metricStreak.metricLabel.setText(R.string.metric_streak);
        pb.metricStreak.metricValue.setText(R.string.value_streak);
        pb.metricCheckins.metricLabel.setText(R.string.metric_checkins);
        pb.metricCheckins.metricValue.setText(R.string.value_checkins);
        pb.navAchievements.rowTitle.setText(R.string.action_achievements);
        pb.navAchievements.rowRoot.setOnClickListener(v->show("Achievements"));
        pb.navGoals.rowTitle.setText(R.string.action_set_goal);
        pb.navGoals.rowRoot.setOnClickListener(v->show("Goals"));
        pb.navHistory.rowTitle.setText(R.string.action_session_history);
        pb.navHistory.rowRoot.setOnClickListener(v->show("History"));
        pb.navHistory.rowDivider.setVisibility(View.GONE);
    }

    private void insights(){
        ScreenInsightsBinding ib = ScreenInsightsBinding.inflate(getLayoutInflater(), binding.contentContainer, true);
        ib.btnTryAnchor.setOnClickListener(v->show("Focus"));
        ib.btnAudioHandoff.setOnClickListener(v->show("Audio"));
        ib.btnLiveCamera.setOnClickListener(v->show("Gaze"));
        ib.btnHowInsights.setOnClickListener(v->show("Privacy"));
    }

    private void focus(){
        ScreenFocusBinding fb = ScreenFocusBinding.inflate(getLayoutInflater(), binding.contentContainer, true);
        String[] lines={"Reading is a skill that grows with practice.","Take a breath, follow one line at a time,","and celebrate every small step.","There is no single right pace for learning."};
        fb.linesContainer.removeAllViews();
        List<android.widget.TextView> lineViews = new ArrayList<>();
        for(String line:lines){
            android.widget.TextView t=new android.widget.TextView(this);
            t.setText(line); t.setTextSize(18); t.setTextColor(ContextCompat.getColor(this,R.color.ink));
            t.setPadding(dp(14),dp(16),dp(14),dp(16));
            t.setBackgroundResource(R.drawable.bg_focus_line);
            android.widget.LinearLayout.LayoutParams lp=new android.widget.LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(8);
            fb.linesContainer.addView(t,lp);
            lineViews.add(t);
            t.setOnClickListener(v->{
                for(android.widget.TextView tv:lineViews){tv.setAlpha(.5f);tv.setBackgroundResource(R.drawable.bg_focus_line);}
                t.setAlpha(1f);t.setBackgroundResource(R.drawable.bg_focus_line_active);
            });
        }
        fb.btnReturnReader.setOnClickListener(v->{focusMode=true;if(firebaseManager!=null){firebaseManager.logFocusModeToggled(true);savePreferencesToCloud();}show("Reader");});
        fb.btnBackNormal.setOnClickListener(v->{focusMode=false;if(firebaseManager!=null){firebaseManager.logFocusModeToggled(false);savePreferencesToCloud();}show("Reader");});
    }

    private void audio(){
        ScreenAudioBinding ab = ScreenAudioBinding.inflate(getLayoutInflater(), binding.contentContainer, true);
        ab.textCurrentPassage.setText(getString(R.string.audio_card_subtitle, selected().title));
        ab.btnPlay.setOnClickListener(v->{if(tts!=null)tts.speak(selected().content,TextToSpeech.QUEUE_FLUSH,null,"edgeai-audio");});
        ab.btnPause.setOnClickListener(v->{if(tts!=null)tts.stop();});
        ab.btnReturnReading.setOnClickListener(v->show("Reader"));
    }

    private void gaze(){
        ScreenGazeBinding gb = ScreenGazeBinding.inflate(getLayoutInflater(), binding.contentContainer, true);
        gb.btnStartCamera.setOnClickListener(v->startActivity(new Intent(this,GazeActivity.class)));
        gb.btnViewInterpretation.setOnClickListener(v->show("Insights"));
    }

    private void upload(){
        ScreenUploadBinding ub = ScreenUploadBinding.inflate(getLayoutInflater(), binding.contentContainer, true);
        ub.btnChooseFile.setOnClickListener(v->bookPicker.launch(new String[]{"text/plain","application/pdf"}));
        ub.btnBackLibrary.setOnClickListener(v->show("Library"));
    }

    private void importBook(Uri uri){
        try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch(Exception ignored) {}
        String name="Imported book";try(android.database.Cursor cur=getContentResolver().query(uri,null,null,null,null)){if(cur!=null&&cur.moveToFirst()){int i=cur.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(i>=0)name=cur.getString(i);}}catch(Exception ignored){}
        String type=name.toLowerCase(Locale.ROOT).endsWith(".pdf")?"PDF":"TXT";String content="This file has been added to your EdgeAI shelf. "+(type.equals("PDF")?"PDF preview is not yet enabled in this starter.":"Select this book to read its extracted text.");
        if(type.equals("TXT")){try(InputStream in=getContentResolver().openInputStream(uri);ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buf=new byte[4096];int n;while((n=in.read(buf))>0)out.write(buf,0,n);content=out.toString("UTF-8");}catch(Exception e){toast("Could not read this file.");}}
        books.add(0,new Book(name,type,content,uri.toString()));
        if(firebaseManager!=null){
            BookItem bItem=new BookItem(null,name,type,content,uri.toString(),System.currentTimeMillis());
            firebaseManager.saveBook(bItem,null);
            firebaseManager.logBookImported(name,type);
        }
        toast("Added to your library");show("Library");
    }

    private void profile(){
        ScreenProfileBinding pb = ScreenProfileBinding.inflate(getLayoutInflater(), binding.contentContainer, true);
        pb.avatarIcon.setText(profileUri==null?"☺":"✓");
        pb.textName.setText(userName);
        pb.textEmail.setText(userEmail.isEmpty()?"reader@example.com":userEmail);
        pb.btnUploadPhoto.setOnClickListener(v->imagePicker.launch("image/*"));
        pb.btnEditDetails.setOnClickListener(v->show("EditProfile"));
        pb.rowReadingAge.rowLabel.setText(R.string.row_reading_age);
        pb.rowReadingAge.rowValue.setText(profileAge);
        pb.rowGender.rowLabel.setText(R.string.row_gender);
        pb.rowGender.rowValue.setText(profileGender);
        pb.rowMobile.rowLabel.setText(R.string.row_mobile);
        pb.rowMobile.rowValue.setText(profileMobile);
        pb.rowAddress.rowLabel.setText(R.string.row_address);
        pb.rowAddress.rowValue.setText(profileAddress);
        pb.navAccountPrefs.rowTitle.setText(R.string.action_account_prefs);
        pb.navAccountPrefs.rowRoot.setOnClickListener(v->show("Settings"));
        pb.navAccountPrefs.rowDivider.setVisibility(View.GONE);
        pb.btnLogOut.setOnClickListener(v->show("Logout"));
    }

    private void editProfile(){
        ScreenEditProfileBinding eb = ScreenEditProfileBinding.inflate(getLayoutInflater(), binding.contentContainer, true);
        eb.inputName.setText(userName);
        eb.inputMobile.setText(profileMobile.equals("Not provided")?"":profileMobile);
        eb.inputAge.setText(profileAge.equals("Not provided")?"":profileAge);
        eb.inputGender.setText(profileGender.equals("Prefer not to say")?"":profileGender);
        eb.inputAddress.setText(profileAddress.equals("Not provided")?"":profileAddress);
        eb.btnChoosePhoto.setOnClickListener(v->imagePicker.launch("image/*"));
        eb.btnSaveChanges.setOnClickListener(v->{
            userName=eb.inputName.getText().toString().trim().isEmpty()?"Reader":eb.inputName.getText().toString().trim();
            profileMobile=eb.inputMobile.getText().toString().trim().isEmpty()?"Not provided":eb.inputMobile.getText().toString().trim();
            profileAge=eb.inputAge.getText().toString().trim().isEmpty()?"Not provided":eb.inputAge.getText().toString().trim();
            profileGender=eb.inputGender.getText().toString().trim().isEmpty()?"Prefer not to say":eb.inputGender.getText().toString().trim();
            profileAddress=eb.inputAddress.getText().toString().trim().isEmpty()?"Not provided":eb.inputAddress.getText().toString().trim();
            if(firebaseManager!=null && firebaseManager.isUserSignedIn()){
                UserProfile p=new UserProfile(firebaseManager.getCurrentUserId(),userName,userEmail);
                p.setMobileNumber(profileMobile);
                p.setReadingAge(profileAge);
                p.setGender(profileGender);
                p.setAddress(profileAddress);
                firebaseManager.saveUserProfile(p,null);
            }
            toast("Profile saved.");
            show("Profile");
        });
        eb.btnPickMapAddress.setOnClickListener(v->mapPicker.launch(new Intent(this,MapPickerActivity.class)));
    }

    private void achievements(){
        ScreenAchievementsBinding ab = ScreenAchievementsBinding.inflate(getLayoutInflater(), binding.contentContainer, true);
        String[][] wins={{"🌱","First chapter","You started reading with EdgeAI."},{"🔥","Finding a rhythm","You returned for three sessions."},{"💜","Kind to yourself","You tried a comfort tool."},{"📚","Curious mind","You explored more than one book."}};
        ab.winsContainer.removeAllViews();
        for(String[] w:wins){
            ItemCardSimpleBinding cb = ItemCardSimpleBinding.inflate(getLayoutInflater(), ab.winsContainer, true);
            cb.cardTitle.setText(w[0]+"  "+w[1]);
            cb.cardSubtitle.setText(w[2]);
        }
        ab.btnSetNextGoal.setOnClickListener(v->show("Goals"));
    }

    private void goals(){
        ScreenGoalsBinding gb = ScreenGoalsBinding.inflate(getLayoutInflater(), binding.contentContainer, true);
        gb.textGoalValue.setText(getString(R.string.label_minutes_today, dailyGoalMinutes));
        gb.sliderGoal.setMax(25);
        gb.sliderGoal.setProgress(dailyGoalMinutes);
        gb.sliderGoal.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar s,int p,boolean u){
                dailyGoalMinutes=Math.max(1,p);
                gb.textGoalValue.setText(getString(R.string.label_minutes_today, dailyGoalMinutes));
            }
            public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}
        });
        gb.btnSaveGoal.setOnClickListener(v->{
            if(firebaseManager!=null){
                ReadingProgress prog=new ReadingProgress(sessions,minutes,wordsRead,regressions,dailyGoalMinutes);
                firebaseManager.saveReadingProgress(prog,null);
                firebaseManager.logGoalUpdated(dailyGoalMinutes);
            }
            toast("Your goal is set. Gentle consistency wins.");
            show("Progress");
        });
    }

    private void historyScreen(){
        ScreenHistoryBinding hb = ScreenHistoryBinding.inflate(getLayoutInflater(), binding.contentContainer, true);
        if(history.isEmpty())history.add("The Secret Garden — sample session");
        hb.historyContainer.removeAllViews();
        for(String h:history){
            ItemCardSimpleBinding cb = ItemCardSimpleBinding.inflate(getLayoutInflater(), hb.historyContainer, true);
            cb.cardTitle.setText("✓  "+h);
            cb.cardSubtitle.setText(R.string.history_item_subtitle);
        }
        hb.btnBackProgress.setOnClickListener(v->show("Progress"));
        hb.btnClearHistory.setOnClickListener(v->new AlertDialog.Builder(this).setTitle(R.string.clear_history_title).setMessage(R.string.clear_history_body).setNegativeButton(R.string.action_cancel,null).setPositiveButton(R.string.action_clear,(d,w)->{
            history.clear();
            if(firebaseManager!=null)firebaseManager.clearReadingHistory(null);
            show("History");
        }).show());
    }

    private void help(){
        ScreenHelpBinding hb = ScreenHelpBinding.inflate(getLayoutInflater(), binding.contentContainer, true);
        String[][] qs={{"How do I add a book?","Open Library → Add a TXT or PDF. Select a file using Android's file picker."},{"Can EdgeAI diagnose dyslexia?","No. Reading patterns are not a diagnosis. Talk to a qualified professional if you need an assessment."},{"Does gaze tracking work now?","The current starter uses illustrative telemetry examples. It does not run a real gaze-estimation model."},{"Can I use it offline?","The sample library, reader, visual focus and Android text-to-speech are local. Firebase features sync when connected."}};
        hb.faqContainer.removeAllViews();
        for(String[] q:qs){
            ItemCardSimpleBinding cb = ItemCardSimpleBinding.inflate(getLayoutInflater(), hb.faqContainer, true);
            cb.cardTitle.setText(q[0]);
            cb.cardSubtitle.setText(q[1]);
        }
        hb.navPrivacy.rowTitle.setText(R.string.action_privacy_data);
        hb.navPrivacy.rowRoot.setOnClickListener(v->show("Privacy"));
        hb.navAbout.rowTitle.setText(R.string.action_about_edgeai);
        hb.navAbout.rowRoot.setOnClickListener(v->show("About"));
        hb.navAbout.rowDivider.setVisibility(View.GONE);
    }

    private void about(){
        ScreenAboutBinding ab = ScreenAboutBinding.inflate(getLayoutInflater(), binding.contentContainer, true);
        ab.btnExploreReader.setOnClickListener(v->show("Reader"));
        ab.btnHelpFaq.setOnClickListener(v->show("Help"));
    }

    private void privacy(){
        ScreenPrivacyBinding pb = ScreenPrivacyBinding.inflate(getLayoutInflater(), binding.contentContainer, true);
        String[][] d={{"Local reading","Sample and imported TXT text is stored in the current app session in this starter."},{"Gaze telemetry","Camera frames are processed locally on the live camera screen and are not saved. True point-of-regard gaze signals are not yet estimated; example insight cards remain illustrative."},{"Cloud services","Firebase Authentication, Cloud Firestore, Analytics, and Crashlytics provide private user sync with strict per-user isolation rules."},{"Your control","Use the library actions to remove books and the history screen to clear local and cloud history."}};
        pb.privacyContainer.removeAllViews();
        for(String[] r:d){
            ItemCardSimpleBinding cb = ItemCardSimpleBinding.inflate(getLayoutInflater(), pb.privacyContainer, true);
            cb.cardTitle.setText(r[0]);
            cb.cardSubtitle.setText(r[1]);
        }
        pb.btnBackSettings.setOnClickListener(v->show("Settings"));
    }

    private void web(){
        ScreenWebBinding wb = ScreenWebBinding.inflate(getLayoutInflater(), binding.contentContainer, true);
        wb.webView.getSettings().setJavaScriptEnabled(false);
        wb.webView.setWebViewClient(new android.webkit.WebViewClient());
        wb.webView.loadUrl("https://www.bdadyslexia.org.uk/");
    }

    private void settings(){
        ScreenSettingsBinding sb = ScreenSettingsBinding.inflate(getLayoutInflater(), binding.contentContainer, true);
        sb.navReadingAppearance.rowTitle.setText(R.string.nav_reading_appearance);
        sb.navReadingAppearance.rowRoot.setOnClickListener(v->show("ReaderSettings"));
        sb.navReadingInsights.rowTitle.setText(R.string.nav_reading_insights);
        sb.navReadingInsights.rowRoot.setOnClickListener(v->show("Insights"));
        sb.navPrivacyData.rowTitle.setText(R.string.nav_privacy_data);
        sb.navPrivacyData.rowRoot.setOnClickListener(v->show("Privacy"));
        sb.navHelpFaq.rowTitle.setText(R.string.nav_help_faq);
        sb.navHelpFaq.rowRoot.setOnClickListener(v->show("Help"));
        sb.navAbout.rowTitle.setText(R.string.nav_about);
        sb.navAbout.rowRoot.setOnClickListener(v->show("About"));
        sb.navWebResources.rowTitle.setText(R.string.nav_web_resources);
        sb.navWebResources.rowRoot.setOnClickListener(v->show("Web"));
        sb.navWebResources.rowDivider.setVisibility(View.GONE);
        sb.btnLogOut.setOnClickListener(v->show("Logout"));
    }

    private void logout(){
        ScreenLogoutBinding lb = ScreenLogoutBinding.inflate(getLayoutInflater(), binding.contentContainer, true);
        lb.btnStay.setOnClickListener(v->show("Home"));
        lb.btnConfirmLogout.setOnClickListener(v->{
            if(firebaseManager!=null) firebaseManager.signOut();
            else if(auth!=null) auth.signOut();
            if(tts!=null) tts.stop();
            userName="Reader";userEmail="";
            profileMobile="Not provided";profileAge="Not provided";profileGender="Prefer not to say";profileAddress="Not provided";
            sessions=0;minutes=0;wordsRead=0;regressions=0;dailyGoalMinutes=5;
            history.clear();
            show("Auth");
        });
    }

    private void savePreferencesToCloud(){
        if(firebaseManager!=null && firebaseManager.isUserSignedIn()){
            ReadingPreferences prefs=new ReadingPreferences(textScale,spacing,warmReader,highContrast,reduceMotion,focusMode);
            firebaseManager.saveReadingPreferences(prefs,null);
            firebaseManager.logPreferenceChanged("theme",warmReader?"warm":"cool");
        }
    }

    private void loadCloudUserData(){
        if(firebaseManager==null || !firebaseManager.isUserSignedIn()) return;
        firebaseManager.loadUserProfile(new FirebaseManager.ProfileCallback(){
            @Override public void onLoaded(UserProfile p){
                if(p!=null){
                    if(p.getDisplayName()!=null && !p.getDisplayName().isEmpty()) userName=p.getDisplayName();
                    if(p.getMobileNumber()!=null) profileMobile=p.getMobileNumber();
                    if(p.getReadingAge()!=null) profileAge=p.getReadingAge();
                    if(p.getGender()!=null) profileGender=p.getGender();
                    if(p.getAddress()!=null) profileAddress=p.getAddress();
                }
            }
            @Override public void onError(String e){}
        });
        firebaseManager.loadReadingPreferences(new FirebaseManager.PreferencesCallback(){
            @Override public void onLoaded(ReadingPreferences p){
                if(p!=null){
                    textScale=p.getTextScale();
                    spacing=p.getSpacing();
                    warmReader=p.isWarmReader();
                    highContrast=p.isHighContrast();
                    reduceMotion=p.isReduceMotion();
                    focusMode=p.isFocusMode();
                }
            }
            @Override public void onError(String e){}
        });
        firebaseManager.loadReadingProgress(new FirebaseManager.ProgressCallback(){
            @Override public void onLoaded(ReadingProgress p){
                sessions=p!=null?p.getSessions():0;
                minutes=p!=null?p.getMinutes():0;
                wordsRead=p!=null?p.getWordsRead():0;
                regressions=p!=null?p.getRegressions():0;
                dailyGoalMinutes=p!=null?p.getDailyGoalMinutes():5;
                if(!current.equals("Splash")&&!current.equals("Auth")) show(current);
            }
            @Override public void onError(String e){}
        });
        firebaseManager.loadReadingHistory(new FirebaseManager.HistoryCallback(){
            @Override public void onLoaded(List<ReadingHistoryItem> items){
                history.clear();
                if(items!=null){
                    for(ReadingHistoryItem item:items){
                        history.add(item.getTitle()+" — "+item.getDisplayDate());
                    }
                }
                if(!current.equals("Splash")&&!current.equals("Auth")) show(current);
            }
            @Override public void onError(String e){}
        });
        firebaseManager.loadBooks(new FirebaseManager.BooksCallback(){
            @Override public void onLoaded(List<BookItem> cloudBooks){
                if(cloudBooks!=null){
                    for(BookItem cb:cloudBooks){
                        boolean exists=false;
                        for(Book b:books){
                            if(b.title.equals(cb.getTitle())){exists=true;break;}
                        }
                        if(!exists){
                            books.add(0,new Book(cb.getTitle(),cb.getType(),cb.getContent(),cb.getUri()));
                        }
                    }
                }
            }
            @Override public void onError(String e){}
        });
    }

    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    @Override public void onBackPressed(){if(current.equals("Home")){new AlertDialog.Builder(this).setTitle(R.string.leave_app_title).setMessage(R.string.leave_app_body).setNegativeButton(R.string.action_stay,null).setPositiveButton(R.string.action_leave,(d,w)->finish()).show();}else if(current.equals("Splash")||current.equals("Auth")){super.onBackPressed();}else show("Home");}
    @Override protected void onDestroy(){if(tts!=null){tts.stop();tts.shutdown();}super.onDestroy();}
}
