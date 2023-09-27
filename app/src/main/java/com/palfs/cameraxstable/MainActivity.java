package com.palfs.cameraxstable;

import android.content.ContentValues;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.PendingRecording;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;

import com.google.common.util.concurrent.ListenableFuture;
import com.palfs.cameraxstable.databinding.ActivityMainBinding;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    ProcessCameraProvider cameraProvider;

    ActivityMainBinding mainBinding;
    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private Recorder recorder;
    private Recording recording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());

        mainBinding.bCapturePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                capturePhoto();
            }
        });

        mainBinding.bCaptureVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mainBinding.bCaptureVideo.getText().equals("Stop Capture")) {

                    if (recording == null) return;

                    recording.stop();
                    recording = null;

                    mainBinding.bCaptureVideo.setText("Capture Video");

                } else {
                    captureVideo();
                }
            }
        });

        ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderListenableFuture.addListener(() -> {

            try {
                cameraProvider = cameraProviderListenableFuture.get();

                startCameraX(cameraProvider);

            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }


        }, ContextCompat.getMainExecutor(this));


    }

    private void captureVideo() {
        if (videoCapture == null) return;

        String name = System.currentTimeMillis() + "";

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Movies/CameraXStableVideo");
        }

        MediaStoreOutputOptions mediaStoreOutputOptions = new MediaStoreOutputOptions.Builder(
                getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI).build();

        PendingRecording pendingRecording = videoCapture.getOutput().prepareRecording(this, mediaStoreOutputOptions);

        recording = pendingRecording.start(ContextCompat.getMainExecutor(this), new Consumer<VideoRecordEvent>() {
            @Override
            public void accept(VideoRecordEvent videoRecordEvent) {
                if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                    mainBinding.bCaptureVideo.setText("Stop Capture");
                } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                    if (((VideoRecordEvent.Finalize) videoRecordEvent).hasError()) {
                        if (recording != null) recording.stop();
                        recording = null;
                        return;
                    }

                    Toast.makeText(MainActivity.this, "Video capture success: " + ((VideoRecordEvent.Finalize) videoRecordEvent).getOutputResults().getOutputUri(), Toast.LENGTH_SHORT).show();

                }

            }
        });

    }

    private void capturePhoto() {
        if (imageCapture == null) return;

        String name = System.currentTimeMillis() + "";

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraXStable");
        }

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(
                getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        ).build();

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Toast.makeText(MainActivity.this, "Image captured: " + outputFileResults.getSavedUri(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                exception.printStackTrace();
            }
        });

    }

    private void startCameraX(ProcessCameraProvider cameraProvider) {
        CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;


        Preview preview = new Preview.Builder().build();

        preview.setSurfaceProvider(mainBinding.pvPreview.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder().build();

        recorder = new Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HIGHEST)).build();


        videoCapture = VideoCapture.withOutput(recorder);

        try {

            cameraProvider.unbindAll();

            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, videoCapture);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}