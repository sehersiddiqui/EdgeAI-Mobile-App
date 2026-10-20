
package com.edgeai.reader;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Size;
import android.view.Gravity;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.OptIn;

/**
 * Live face/eye-landmark signal demo. It does NOT claim to estimate point-of-regard.
 * Head pose and eye-open probabilities are proxies only until a validated gaze model
 * and per-device calibration are integrated.
 */
@OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
public class GazeActivity extends AppCompatActivity {
    private PreviewView preview;
    private TextView status, metrics, note;
    private ExecutorService cameraExecutor;
    private FaceDetector detector;
    private ImageAnalysis analysis;
    private final AtomicBoolean busy = new AtomicBoolean(false);
    private long frameCount=0, lastUi=0;
    private double yawSum=0, eyeLeftSum=0, eyeRightSum=0;
    private int validFaces=0;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        cameraExecutor=Executors.newSingleThreadExecutor();
        FaceDetectorOptions options=new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .enableTracking().build();
        detector=FaceDetection.getClient(options);
        LinearLayout page=new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL); page.setPadding(28,22,28,22); page.setBackgroundColor(Color.rgb(10,15,29));
        TextView title=new TextView(this); title.setText("EdgeAI · Live camera signals"); title.setTextSize(23); title.setTextColor(Color.rgb(239,243,255)); title.setTypeface(null,android.graphics.Typeface.BOLD); page.addView(title);
        TextView intro=new TextView(this); intro.setText("On-device face and eye-landmark demo. Keep your face inside the frame; camera frames are processed locally and not saved."); intro.setTextSize(14); intro.setTextColor(Color.rgb(154,166,191)); intro.setPadding(0,10,0,14); page.addView(intro);
        preview=new PreviewView(this); page.addView(preview,new LinearLayout.LayoutParams(-1,0,1));
        status=new TextView(this); status.setText("Waiting for camera permission…"); status.setTextSize(17); status.setTextColor(Color.rgb(166,144,255)); status.setPadding(0,14,0,6); page.addView(status);
        metrics=new TextView(this); metrics.setText("Frames analysed: 0"); metrics.setTextSize(14); metrics.setTextColor(Color.rgb(239,243,255)); page.addView(metrics);
        note=new TextView(this); note.setText("Important: eye-open scores and head yaw are not gaze coordinates and cannot diagnose dyslexia."); note.setTextSize(12); note.setTextColor(Color.rgb(154,166,191)); note.setPadding(0,10,0,10); page.addView(note);
        Button done=new Button(this); done.setText("Finish session"); done.setTextColor(Color.rgb(10,15,29)); done.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(166,144,255)));
        done.setOnClickListener(v->{
            try { FirebaseManager.getInstance(this).logGazeSession(frameCount, validFaces); } catch(Throwable ignored) {}
            finish();
        }); page.addView(done);
        setContentView(page);
        try { FirebaseManager.getInstance(this).logScreenView("GazeActivity"); } catch(Throwable ignored) {}
        permissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted->{if(granted)startCamera();else status.setText("Camera permission denied. No camera data was collected.");});
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED) startCamera();
        else permissionLauncher.launch(Manifest.permission.CAMERA);
    }
    @androidx.camera.core.ExperimentalGetImage
    private void startCamera(){
        ListenableFuture<ProcessCameraProvider> future=ProcessCameraProvider.getInstance(this);
        future.addListener(()->{
            try {
                ProcessCameraProvider provider=future.get();
                Preview p=new Preview.Builder().build(); p.setSurfaceProvider(preview.getSurfaceProvider());
                analysis=new ImageAnalysis.Builder().setTargetResolution(new Size(640,480)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
                analysis.setAnalyzer(cameraExecutor,imageProxy->{
                    if(!busy.compareAndSet(false,true)){imageProxy.close();return;}
                    android.media.Image media=imageProxy.getImage();
                    if(media==null){busy.set(false);imageProxy.close();return;}
                    try {
                        InputImage input=InputImage.fromMediaImage(media,imageProxy.getImageInfo().getRotationDegrees());
                        detector.process(input).addOnSuccessListener(faces->{
                            frameCount++;
                            if(!faces.isEmpty()){
                                Face f=faces.get(0); validFaces++; yawSum+=f.getHeadEulerAngleY();
                                Float le=f.getLeftEyeOpenProbability(), re=f.getRightEyeOpenProbability();
                                if(le!=null)eyeLeftSum+=le;if(re!=null)eyeRightSum+=re;
                                long now=System.currentTimeMillis();
                                if(now-lastUi>350){
                                    runOnUiThread(()->{
                                        status.setText("Face detected · tracking ID "+(f.getTrackingId()==null?"—":f.getTrackingId()));
                                        metrics.setText(String.format(java.util.Locale.US,
                                            "Frames analysed: %d\nHead yaw: %.1f°\nLeft-eye open probability: %s\nRight-eye open probability: %s\nValid face frames: %d",
                                            frameCount,f.getHeadEulerAngleY(),le==null?"not available":String.format(java.util.Locale.US,"%.2f",le),
                                            re==null?"not available":String.format(java.util.Locale.US,"%.2f",re),validFaces));
                                    });lastUi=now;
                                }
                            } else {
                                runOnUiThread(()->status.setText("No face detected · adjust lighting or position"));
                            }
                        }).addOnFailureListener(e->runOnUiThread(()->status.setText("Signal processing issue: "+e.getLocalizedMessage())))
                        .addOnCompleteListener(t->{busy.set(false);imageProxy.close();});
                    } catch(Throwable e){
                        busy.set(false);
                        imageProxy.close();
                    }
                });
                provider.unbindAll();provider.bindToLifecycle(this,CameraSelector.DEFAULT_FRONT_CAMERA,p,analysis);
                status.setText("Camera running · looking for face landmarks");
            } catch(Exception e){status.setText("Unable to start camera: "+e.getMessage());}
        },ContextCompat.getMainExecutor(this));
    }
    @Override protected void onDestroy(){super.onDestroy();if(analysis!=null)analysis.clearAnalyzer();if(cameraExecutor!=null)cameraExecutor.shutdown();if(detector!=null)detector.close();}
}
