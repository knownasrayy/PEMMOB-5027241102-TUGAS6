package com.example.tugas5_listview.ui;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.tugas5_listview.R;
import com.example.tugas5_listview.util.CameraPermissionHelper;
import com.example.tugas5_listview.util.ImageUtils;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * CameraXActivity - pratinjau kamera langsung (live preview) memakai CameraX.
 *
 * Materi Bagian 11-12 membahas Camera API lama (deprecated) dan Camera2 (kompleks, asynchronous:
 * CameraManager -> CameraDevice -> CaptureRequest -> CameraCaptureSession -> CaptureCallback).
 * CameraX (Jetpack) membungkus alur tersebut dengan use case sederhana: Preview + ImageCapture
 * yang terikat ke lifecycle Activity.
 */
public class CameraXActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ImageView ivLastPhoto;

    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

    private ActivityResultLauncher<String> cameraPermissionLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camerax);

        previewView = findViewById(R.id.preview_view);
        ivLastPhoto = findViewById(R.id.iv_last_photo);

        cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) {
                startCamera();
            } else {
                Toast.makeText(this, "Izin kamera diperlukan untuk CameraX", Toast.LENGTH_LONG).show();
                if (!CameraPermissionHelper.shouldShowRationale(this)) {
                    CameraPermissionHelper.showSettingsDialog(this);
                }
            }
        });

        findViewById(R.id.btn_shutter).setOnClickListener(v -> takePhoto());
        findViewById(R.id.btn_flip).setOnClickListener(v -> flipCamera());
        findViewById(R.id.btn_close_camerax).setOnClickListener(v -> finish());

        if (CameraPermissionHelper.hasCameraPermission(this)) {
            startCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    /** ProcessCameraProvider diperoleh secara asynchronous lewat ListenableFuture. */
    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                cameraProvider = future.get();
                bindUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Gagal memulai kamera: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindUseCases() {
        if (cameraProvider == null) return;
        try {
            cameraProvider.unbindAll();

            Preview preview = new Preview.Builder().build();
            preview.setSurfaceProvider(previewView.getSurfaceProvider());

            imageCapture = new ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build();

            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
        } catch (Exception e) {
            Toast.makeText(this, "Kamera tidak dapat dibuka: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void flipCamera() {
        if (cameraProvider == null) return;
        CameraSelector target = (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                ? CameraSelector.DEFAULT_FRONT_CAMERA
                : CameraSelector.DEFAULT_BACK_CAMERA;
        try {
            if (!cameraProvider.hasCamera(target)) {
                Toast.makeText(this, "Kamera tujuan tidak tersedia di perangkat ini", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (CameraInfoUnavailableException e) {
            Toast.makeText(this, "Informasi kamera tidak tersedia", Toast.LENGTH_SHORT).show();
            return;
        }
        cameraSelector = target;
        bindUseCases();
    }

    private void takePhoto() {
        if (imageCapture == null) {
            Toast.makeText(this, "Kamera belum siap", Toast.LENGTH_SHORT).show();
            return;
        }
        final File photo;
        try {
            photo = ImageUtils.createImageFile(this);
        } catch (IOException e) {
            Toast.makeText(this, "Gagal menyiapkan file foto: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(photo).build();
        imageCapture.takePicture(options, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults results) {
                        Bitmap thumb = ImageUtils.loadThumbnail(photo.getAbsolutePath(), 168);
                        if (thumb != null) {
                            ivLastPhoto.setImageBitmap(thumb);
                            ivLastPhoto.setVisibility(View.VISIBLE);
                        }
                        Toast.makeText(CameraXActivity.this,
                                "Tersimpan: " + photo.getName() + " (" + ImageUtils.formatSize(photo.length()) + ")",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        ImageUtils.deleteQuietly(photo.getAbsolutePath());
                        Toast.makeText(CameraXActivity.this,
                                "Gagal mengambil foto: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}