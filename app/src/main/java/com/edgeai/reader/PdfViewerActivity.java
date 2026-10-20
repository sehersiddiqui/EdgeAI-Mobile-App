
package com.edgeai.reader;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.net.Uri;
import android.view.Gravity;
import android.widget.*;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;

/** Native PDF page renderer using Android PdfRenderer and Storage Access Framework URI. */
public class PdfViewerActivity extends AppCompatActivity {
    private PdfRenderer renderer; private ParcelFileDescriptor descriptor; private int pageIndex=0;
    private ImageView pageImage; private TextView counter;
    @Override public void onCreate(Bundle b){super.onCreate(b);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(12,12,12,12);root.setBackgroundColor(Color.rgb(10,15,29));
        TextView title=new TextView(this);title.setText("EdgeAI · PDF reading room");title.setTextSize(20);title.setTextColor(Color.rgb(239,243,255));root.addView(title);
        counter=new TextView(this);counter.setTextSize(14);counter.setTextColor(Color.rgb(154,166,191));root.addView(counter);
        ScrollView scroll=new ScrollView(this);pageImage=new ImageView(this);pageImage.setAdjustViewBounds(true);pageImage.setScaleType(ImageView.ScaleType.FIT_CENTER);scroll.addView(pageImage);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout controls=new LinearLayout(this);Button prev=new Button(this);prev.setText("← Previous");prev.setTextColor(Color.rgb(10,15,29));prev.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(166,144,255)));prev.setOnClickListener(v->{if(pageIndex>0){pageIndex--;renderPage();}});Button next=new Button(this);next.setText("Next →");next.setTextColor(Color.rgb(10,15,29));next.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(94,226,190)));next.setOnClickListener(v->{if(renderer!=null&&pageIndex<renderer.getPageCount()-1){pageIndex++;renderPage();}});controls.addView(prev,new LinearLayout.LayoutParams(0,-2,1));controls.addView(next,new LinearLayout.LayoutParams(0,-2,1));root.addView(controls);
        setContentView(root);
        try { FirebaseManager.getInstance(this).logScreenView("PdfViewer"); } catch(Throwable ignored) {}
        Uri uri=getIntent().getData();if(uri==null){finish();return;}
        try{
            FirebaseManager.getInstance(this).logBookOpened(uri.getLastPathSegment()!=null?uri.getLastPathSegment():"PDF","PDF");
        }catch(Throwable ignored){}
        try{descriptor=getContentResolver().openFileDescriptor(uri,"r");if(descriptor==null)throw new IOException("Cannot open PDF");renderer=new PdfRenderer(descriptor);renderPage();}
        catch(Exception e){counter.setText("Could not render this PDF: "+e.getMessage());}
    }
    private void renderPage(){if(renderer==null)return;PdfRenderer.Page page=null;try{page=renderer.openPage(pageIndex);int width=Math.min(2048,Math.max(600,page.getWidth()*2)),height=Math.min(2048,Math.max(800,page.getHeight()*2));Bitmap bmp=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);bmp.eraseColor(Color.WHITE);page.render(bmp,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);pageImage.setImageBitmap(bmp);counter.setText("Page "+(pageIndex+1)+" of "+renderer.getPageCount());}catch(Throwable e){counter.setText("Rendering error: "+e.getMessage());}finally{if(page!=null)page.close();}}
    @Override protected void onDestroy(){super.onDestroy();try{if(renderer!=null)renderer.close();if(descriptor!=null)descriptor.close();}catch(Exception ignored){}}
}
