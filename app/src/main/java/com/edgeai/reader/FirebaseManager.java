package com.edgeai.reader;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.edgeai.reader.model.BookItem;
import com.edgeai.reader.model.ReadingHistoryItem;
import com.edgeai.reader.model.ReadingPreferences;
import com.edgeai.reader.model.ReadingProgress;
import com.edgeai.reader.model.UserProfile;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized, production-grade manager for Firebase services:
 * - Firebase Authentication (registration, login, profile name, state)
 * - Cloud Firestore (user-isolated profiles, preferences, progress, history, books)
 * - Firebase Analytics (reading session events, content interactions, screen views)
 * - Firebase Crashlytics (crash reporting, user IDs, custom attributes, breadcrumbs)
 *
 * Designed with safe fallback: if Firebase configuration (google-services.json)
 * is not yet attached, operations gracefully invoke error/fallback callbacks
 * without crashing, preserving local-only and offline-first functionality.
 */
public final class FirebaseManager {
    private static final String TAG = "FirebaseManager";

    // Firestore Collection / Subcollection Paths
    private static final String COL_USERS = "users";
    private static final String COL_PREFERENCES = "preferences";
    private static final String DOC_SETTINGS = "settings";
    private static final String COL_PROGRESS = "progress";
    private static final String DOC_STATS = "stats";
    private static final String COL_HISTORY = "history";
    private static final String COL_BOOKS = "books";

    private static volatile FirebaseManager instance;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseAnalytics analytics;
    private FirebaseCrashlytics crashlytics;
    private boolean available = false;

    public interface AuthCallback {
        void onSuccess(String uid, String email, String displayName);
        void onFailure(String errorMessage);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public interface ProfileCallback {
        void onLoaded(UserProfile profile);
        void onError(String errorMessage);
    }

    public interface PreferencesCallback {
        void onLoaded(ReadingPreferences preferences);
        void onError(String errorMessage);
    }

    public interface ProgressCallback {
        void onLoaded(ReadingProgress progress);
        void onError(String errorMessage);
    }

    public interface HistoryCallback {
        void onLoaded(List<ReadingHistoryItem> historyItems);
        void onError(String errorMessage);
    }

    public interface BooksCallback {
        void onLoaded(List<BookItem> books);
        void onError(String errorMessage);
    }

    private FirebaseManager(Context context) {
        init(context);
    }

    public static FirebaseManager getInstance(Context context) {
        if (instance == null) {
            synchronized (FirebaseManager.class) {
                if (instance == null) {
                    instance = new FirebaseManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    /** Re-attempts initialization if configuration changes. */
    public synchronized void init(Context context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context);
            }
            auth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            try { analytics = FirebaseAnalytics.getInstance(context); } catch (Throwable ignored) {}
            try { crashlytics = FirebaseCrashlytics.getInstance(); } catch (Throwable ignored) {}
            available = true;
            Log.i(TAG, "Firebase initialized successfully.");

            // If user is currently signed in, attach Crashlytics ID
            if (auth.getCurrentUser() != null) {
                setCrashlyticsUserId(auth.getCurrentUser().getUid());
            }
        } catch (Throwable t) {
            available = false;
            Log.w(TAG, "Firebase unavailable or not configured yet: " + t.getMessage());
        }
    }

    public boolean isFirebaseAvailable() {
        return available && auth != null && db != null;
    }

    public boolean isUserSignedIn() {
        return isFirebaseAvailable() && auth.getCurrentUser() != null;
    }

    public FirebaseUser getCurrentUser() {
        return isFirebaseAvailable() ? auth.getCurrentUser() : null;
    }

    public String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public String getCurrentUserEmail() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getEmail() : "";
    }

    public String getCurrentUserDisplayName() {
        FirebaseUser user = getCurrentUser();
        if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            return user.getDisplayName();
        }
        return "Reader";
    }

    // =========================================================================
    // Authentication
    // =========================================================================

    public void registerWithEmail(String email, String password, String displayName, final AuthCallback callback) {
        if (!isFirebaseAvailable()) {
            if (callback != null) callback.onFailure("Firebase is not configured. Demo mode is active.");
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        String name = (displayName != null && !displayName.trim().isEmpty()) ? displayName.trim() : "Reader";
                        user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(name).build());
                        setCrashlyticsUserId(user.getUid());
                        logEvent(FirebaseAnalytics.Event.SIGN_UP, "method", "email");

                        // Create initial profile and default collections in Firestore
                        UserProfile profile = new UserProfile(user.getUid(), name, user.getEmail());
                        saveUserProfile(profile, null);
                        saveReadingPreferences(new ReadingPreferences(), null);
                        saveReadingProgress(new ReadingProgress(), null);

                        if (callback != null) {
                            callback.onSuccess(user.getUid(), user.getEmail(), name);
                        }
                    } else {
                        if (callback != null) callback.onFailure("User account could not be created.");
                    }
                })
                .addOnFailureListener(e -> {
                    recordException(e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void signInWithEmail(String email, String password, final AuthCallback callback) {
        if (!isFirebaseAvailable()) {
            if (callback != null) callback.onFailure("Firebase is not configured. Demo mode is active.");
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        setCrashlyticsUserId(user.getUid());
                        logEvent(FirebaseAnalytics.Event.LOGIN, "method", "email");
                        String name = user.getDisplayName() != null ? user.getDisplayName() : "Reader";
                        if (callback != null) {
                            callback.onSuccess(user.getUid(), user.getEmail(), name);
                        }
                    } else {
                        if (callback != null) callback.onFailure("Sign-in succeeded but user details unavailable.");
                    }
                })
                .addOnFailureListener(e -> {
                    recordException(e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void signOut() {
        if (isFirebaseAvailable()) {
            try {
                logEvent("user_sign_out", "uid", getCurrentUserId());
                auth.signOut();
            } catch (Throwable t) {
                recordException(t);
            }
        }
        setCrashlyticsUserId("");
    }

    // =========================================================================
    // Firestore: User Profile (/users/{userId})
    // =========================================================================

    public void saveUserProfile(UserProfile profile, final SimpleCallback callback) {
        String uid = getCurrentUserId();
        if (!isFirebaseAvailable() || uid == null) {
            if (callback != null) callback.onFailure("User not authenticated or Firebase unavailable");
            return;
        }
        profile.setUid(uid);
        db.collection(COL_USERS).document(uid)
                .set(profile.toMap(), SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    logEvent("profile_updated", "uid", uid);
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    recordException(e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void loadUserProfile(final ProfileCallback callback) {
        String uid = getCurrentUserId();
        if (!isFirebaseAvailable() || uid == null) {
            if (callback != null) callback.onError("User not authenticated or Firebase unavailable");
            return;
        }

        db.collection(COL_USERS).document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.getData() != null) {
                        UserProfile profile = UserProfile.fromMap(documentSnapshot.getData());
                        if (callback != null) callback.onLoaded(profile);
                    } else {
                        // Document doesn't exist yet, return fresh fallback
                        UserProfile fallback = new UserProfile(uid, getCurrentUserDisplayName(), getCurrentUserEmail());
                        if (callback != null) callback.onLoaded(fallback);
                    }
                })
                .addOnFailureListener(e -> {
                    recordException(e);
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    // =========================================================================
    // Firestore: Reading Preferences (/users/{userId}/preferences/settings)
    // =========================================================================

    public void saveReadingPreferences(ReadingPreferences preferences, final SimpleCallback callback) {
        String uid = getCurrentUserId();
        if (!isFirebaseAvailable() || uid == null) {
            if (callback != null) callback.onFailure("User not authenticated or Firebase unavailable");
            return;
        }

        db.collection(COL_USERS).document(uid)
                .collection(COL_PREFERENCES).document(DOC_SETTINGS)
                .set(preferences.toMap(), SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    recordException(e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void loadReadingPreferences(final PreferencesCallback callback) {
        String uid = getCurrentUserId();
        if (!isFirebaseAvailable() || uid == null) {
            if (callback != null) callback.onError("User not authenticated or Firebase unavailable");
            return;
        }

        db.collection(COL_USERS).document(uid)
                .collection(COL_PREFERENCES).document(DOC_SETTINGS)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.getData() != null) {
                        ReadingPreferences prefs = ReadingPreferences.fromMap(documentSnapshot.getData());
                        if (callback != null) callback.onLoaded(prefs);
                    } else {
                        if (callback != null) callback.onLoaded(new ReadingPreferences());
                    }
                })
                .addOnFailureListener(e -> {
                    recordException(e);
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    // =========================================================================
    // Firestore: Reading Progress (/users/{userId}/progress/stats)
    // =========================================================================

    public void saveReadingProgress(ReadingProgress progress, final SimpleCallback callback) {
        String uid = getCurrentUserId();
        if (!isFirebaseAvailable() || uid == null) {
            if (callback != null) callback.onFailure("User not authenticated or Firebase unavailable");
            return;
        }

        db.collection(COL_USERS).document(uid)
                .collection(COL_PROGRESS).document(DOC_STATS)
                .set(progress.toMap(), SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    recordException(e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void loadReadingProgress(final ProgressCallback callback) {
        String uid = getCurrentUserId();
        if (!isFirebaseAvailable() || uid == null) {
            if (callback != null) callback.onError("User not authenticated or Firebase unavailable");
            return;
        }

        db.collection(COL_USERS).document(uid)
                .collection(COL_PROGRESS).document(DOC_STATS)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.getData() != null) {
                        ReadingProgress progress = ReadingProgress.fromMap(documentSnapshot.getData());
                        if (callback != null) callback.onLoaded(progress);
                    } else {
                        if (callback != null) callback.onLoaded(new ReadingProgress());
                    }
                })
                .addOnFailureListener(e -> {
                    recordException(e);
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    // =========================================================================
    // Firestore: Reading History (/users/{userId}/history/{sessionId})
    // =========================================================================

    public void addReadingHistory(ReadingHistoryItem item, final SimpleCallback callback) {
        String uid = getCurrentUserId();
        if (!isFirebaseAvailable() || uid == null) {
            if (callback != null) callback.onFailure("User not authenticated or Firebase unavailable");
            return;
        }

        DocumentReference docRef;
        if (item.getId() != null && !item.getId().isEmpty()) {
            docRef = db.collection(COL_USERS).document(uid).collection(COL_HISTORY).document(item.getId());
        } else {
            docRef = db.collection(COL_USERS).document(uid).collection(COL_HISTORY).document();
            item.setId(docRef.getId());
        }

        docRef.set(item.toMap(), SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    recordException(e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void loadReadingHistory(final HistoryCallback callback) {
        String uid = getCurrentUserId();
        if (!isFirebaseAvailable() || uid == null) {
            if (callback != null) callback.onError("User not authenticated or Firebase unavailable");
            return;
        }

        db.collection(COL_USERS).document(uid)
                .collection(COL_HISTORY)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ReadingHistoryItem> list = new ArrayList<>();
                    for (QueryDocumentSnapshot snap : queryDocumentSnapshots) {
                        list.add(ReadingHistoryItem.fromMap(snap.getData()));
                    }
                    if (callback != null) callback.onLoaded(list);
                })
                .addOnFailureListener(e -> {
                    recordException(e);
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    public void clearReadingHistory(final SimpleCallback callback) {
        String uid = getCurrentUserId();
        if (!isFirebaseAvailable() || uid == null) {
            if (callback != null) callback.onFailure("User not authenticated or Firebase unavailable");
            return;
        }

        db.collection(COL_USERS).document(uid)
                .collection(COL_HISTORY)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }
                    logEvent("history_cleared", "uid", uid);
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    recordException(e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    // =========================================================================
    // Firestore: User Library Books (/users/{userId}/books/{bookId})
    // =========================================================================

    public void saveBook(BookItem book, final SimpleCallback callback) {
        String uid = getCurrentUserId();
        if (!isFirebaseAvailable() || uid == null) {
            if (callback != null) callback.onFailure("User not authenticated or Firebase unavailable");
            return;
        }

        DocumentReference docRef;
        if (book.getId() != null && !book.getId().isEmpty()) {
            docRef = db.collection(COL_USERS).document(uid).collection(COL_BOOKS).document(book.getId());
        } else {
            docRef = db.collection(COL_USERS).document(uid).collection(COL_BOOKS).document();
            book.setId(docRef.getId());
        }

        docRef.set(book.toMap(), SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    recordException(e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void loadBooks(final BooksCallback callback) {
        String uid = getCurrentUserId();
        if (!isFirebaseAvailable() || uid == null) {
            if (callback != null) callback.onError("User not authenticated or Firebase unavailable");
            return;
        }

        db.collection(COL_USERS).document(uid)
                .collection(COL_BOOKS)
                .orderBy("addedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<BookItem> list = new ArrayList<>();
                    for (QueryDocumentSnapshot snap : queryDocumentSnapshots) {
                        list.add(BookItem.fromMap(snap.getData()));
                    }
                    if (callback != null) callback.onLoaded(list);
                })
                .addOnFailureListener(e -> {
                    recordException(e);
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    public void deleteBook(String bookTitle, final SimpleCallback callback) {
        String uid = getCurrentUserId();
        if (!isFirebaseAvailable() || uid == null) {
            if (callback != null) callback.onFailure("User not authenticated or Firebase unavailable");
            return;
        }

        db.collection(COL_USERS).document(uid)
                .collection(COL_BOOKS)
                .whereEqualTo("title", bookTitle)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }
                    logEvent("book_deleted", "title", bookTitle);
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    recordException(e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    // =========================================================================
    // Firebase Analytics
    // =========================================================================

    public void logEvent(String eventName, Bundle bundle) {
        if (analytics != null) {
            try {
                analytics.logEvent(eventName, bundle);
            } catch (Throwable t) {
                Log.w(TAG, "Analytics logEvent failed: " + t.getMessage());
            }
        }
    }

    public void logEvent(String eventName, String paramKey, String paramValue) {
        Bundle bundle = new Bundle();
        bundle.putString(paramKey, paramValue != null ? paramValue : "");
        logEvent(eventName, bundle);
    }

    public void logScreenView(String screenName) {
        logBreadcrumb("Navigated to screen: " + screenName);
        setCrashlyticsCustomKey("current_screen", screenName);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName);
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity");
        logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
    }

    public void logReadingSessionStarted(String bookTitle, String bookType) {
        Bundle bundle = new Bundle();
        bundle.putString("book_title", bookTitle != null ? bookTitle : "");
        bundle.putString("book_type", bookType != null ? bookType : "TXT");
        logEvent("reading_session_started", bundle);
    }

    public void logReadingSessionSaved(String bookTitle, int durationMinutes, int wordsRead) {
        Bundle bundle = new Bundle();
        bundle.putString("book_title", bookTitle != null ? bookTitle : "");
        bundle.putInt("duration_minutes", durationMinutes);
        bundle.putInt("words_read", wordsRead);
        logEvent("reading_session_saved", bundle);
    }

    public void logBookOpened(String bookTitle, String bookType) {
        Bundle bundle = new Bundle();
        bundle.putString("book_title", bookTitle != null ? bookTitle : "");
        bundle.putString("book_type", bookType != null ? bookType : "TXT");
        logEvent("book_opened", bundle);
    }

    public void logBookImported(String bookTitle, String bookType) {
        Bundle bundle = new Bundle();
        bundle.putString("book_title", bookTitle != null ? bookTitle : "");
        bundle.putString("book_type", bookType != null ? bookType : "TXT");
        logEvent("book_imported", bundle);
    }

    public void logPreferenceChanged(String settingKey, String value) {
        Bundle bundle = new Bundle();
        bundle.putString("setting_key", settingKey);
        bundle.putString("setting_value", value);
        logEvent("preference_changed", bundle);
    }

    public void logGoalUpdated(int dailyGoalMinutes) {
        Bundle bundle = new Bundle();
        bundle.putInt("goal_minutes", dailyGoalMinutes);
        logEvent("goal_updated", bundle);
    }

    public void logFocusModeToggled(boolean enabled) {
        setCrashlyticsCustomKey("focus_mode_active", enabled);
        Bundle bundle = new Bundle();
        bundle.putBoolean("enabled", enabled);
        logEvent("focus_mode_toggled", bundle);
    }

    public void logGazeSession(long frameCount, int validFaces) {
        Bundle bundle = new Bundle();
        bundle.putLong("frames_analysed", frameCount);
        bundle.putInt("valid_faces", validFaces);
        logEvent("gaze_session_completed", bundle);
    }

    // =========================================================================
    // Firebase Crashlytics
    // =========================================================================

    public void setCrashlyticsUserId(String uid) {
        if (crashlytics != null) {
            try {
                crashlytics.setUserId(uid != null ? uid : "");
            } catch (Throwable ignored) {}
        }
    }

    public void setCrashlyticsCustomKey(String key, String value) {
        if (crashlytics != null) {
            try {
                crashlytics.setCustomKey(key, value != null ? value : "");
            } catch (Throwable ignored) {}
        }
    }

    public void setCrashlyticsCustomKey(String key, boolean value) {
        if (crashlytics != null) {
            try {
                crashlytics.setCustomKey(key, value);
            } catch (Throwable ignored) {}
        }
    }

    public void setCrashlyticsCustomKey(String key, int value) {
        if (crashlytics != null) {
            try {
                crashlytics.setCustomKey(key, value);
            } catch (Throwable ignored) {}
        }
    }

    public void logBreadcrumb(String message) {
        if (crashlytics != null) {
            try {
                crashlytics.log(message);
            } catch (Throwable ignored) {}
        }
    }

    public void recordException(Throwable throwable) {
        if (crashlytics != null && throwable != null) {
            try {
                crashlytics.recordException(throwable);
            } catch (Throwable ignored) {}
        }
    }
}
