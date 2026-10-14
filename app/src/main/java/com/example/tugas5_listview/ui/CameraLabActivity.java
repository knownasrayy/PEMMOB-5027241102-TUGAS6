package com.example.tugas5_listview.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.tugas5_listview.R;
import com.example.tugas5_listview.util.CameraPermissionHelper;
import com.example.tugas5_listview.util.ImageUtils;

import java.io.File;
import java.io.IOException;

/**
 * CameraLabActivity - demo setiap konsep Materi Android Kamera.
 *
 *  Mode A : Intent kamera TANPA EXTRA_OUTPUT  -> hanya thumbnail (data.getExtras().get("data"))   [Bagian 2-4, 6]
 *  Mode B : Intent kamera DENGAN EXTRA_OUTPUT -> foto full-size ke file, dibaca via decodeFile      [Bagian 5, 7, 9, 10]
 *  Mode C : Salin hasil Mode B ke Galeri lewat MediaStore (+ runtime permission di API < 29)       [Bagian 8, 14]
 *  Mode D : CameraX (pengganti Camera / Camera2 API)                                               [Bagian 11-13]
 *
 * Catatan adaptasi API modern: startActivityForResult + onActivityResult (request code "kodekamera")
 * digantikan registerForActivityResult; Uri.fromFile digantikan FileProvider.
 */
public class CameraLabActivity extends AppCompatActivity {

    private static final String STATE_NM_FILE = "state_nm_file";
    private static final int MODE_A = 1;
    private static final int MODE_B = 2;

    private ImageView ivModeA;
    private ImageView ivModeB;
    private TextView tvInfoA;
    private TextView tvInfoB;
    private Button btnSaveGallery;

    /** Path lengkap file foto Mode B - variabel "nmFile" yang dideklarasikan global pada materi (Bagian 9). */
    private String nmFile;
    private int pendingMode = 0;

    private ActivityResultLauncher<Void> previewLauncher;
    private ActivityResultLauncher<Uri> fullPhotoLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<String> writePermissionLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_lab);

        Toolbar toolbar = findViewById(R.id.toolbar_lab);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ivModeA        = findViewById(R.id.iv_mode_a);
        ivModeB        = findViewById(R.id.iv_mode_b);
        tvInfoA        = findViewById(R.id.tv_info_a);
        tvInfoB        = findViewById(R.id.tv_info_b);
        btnSaveGallery = findViewById(R.id.btn_save_gallery);

        registerLaunchers();

        findViewById(R.id.btn_mode_a).setOnClickListener(v -> startMode(MODE_A));
        findViewById(R.id.btn_mode_b).setOnClickListener(v -> startMode(MODE_B));
        btnSaveGallery.setOnClickListener(v -> onSaveToGalleryClicked());
        findViewById(R.id.btn_mode_camerax).setOnClickListener(v ->
                startActivity(new Intent(this, CameraXActivity.class)));

        if (savedInstanceState != null) {
            nmFile = savedInstanceState.getString(STATE_NM_FILE);
            if (ImageUtils.isValidPhoto(nmFile)) {
                showFullResult();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_NM_FILE, nmFile);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // ---------------------------------------------------------------- Launcher (pengganti onActivityResult)

    private void registerLaunchers() {
        // Mode A: tanpa putExtra -> hasil berupa Bitmap thumbnail (Materi Bagian 6.3)
        previewLauncher = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bm -> {
            if (bm != null) {
                ivModeA.setImageBitmap(bm);
                tvInfoA.setText("Thumbnail: " + bm.getWidth() + " x " + bm.getHeight()
                        + " px\n(hanya pratinjau kecil - tidak ada file yang disimpan)");
                Toast.makeText(this, "Data Telah Terload ke ImageView", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Pengambilan foto dibatalkan", Toast.LENGTH_SHORT).show();
            }
        });

        // Mode B: dengan putExtra(EXTRA_OUTPUT) -> foto tersimpan di nmFile, extras kosong (Materi Bagian 9)
        fullPhotoLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success) {
                showFullResult();
                Toast.makeText(this, "Data Telah Terload ke ImageView\n" + nmFile, Toast.LENGTH_SHORT).show();
            } else {
                ImageUtils.deleteQuietly(nmFile);
                nmFile = null;
                Toast.makeText(this, "Pengambilan foto dibatalkan", Toast.LENGTH_SHORT).show();
            }
        });

        // Runtime permission CAMERA (Materi Bagian 8 dan 14)
        cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) {
                launchMode(pendingMode);
            } else if (!CameraPermissionHelper.shouldShowRationale(this)) {
                CameraPermissionHelper.showSettingsDialog(this);
            } else {
                Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show();
            }
        });

        // Izin tulis hanya diperlukan API 24-28 untuk menyalin ke Galeri
        writePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) {
                saveToGallery();
            } else {
                Toast.makeText(this, "Izin penyimpanan ditolak", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------------------------------------------------------------- Alur kamera

    private void startMode(int mode) {
        pendingMode = mode;
        if (CameraPermissionHelper.hasCameraPermission(this)) {
            launchMode(mode);
        } else if (CameraPermissionHelper.shouldShowRationale(this)) {
            CameraPermissionHelper.showRationaleDialog(this,
                    () -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA));
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchMode(int mode) {
        Intent probe = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (probe.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, "Tidak ada aplikasi kamera di perangkat ini", Toast.LENGTH_LONG).show();
            return;
        }

        if (mode == MODE_A) {
            previewLauncher.launch(null);
        } else if (mode == MODE_B) {
            try {
                File image = ImageUtils.createImageFile(this);
                // hapus foto Mode B sebelumnya agar tidak menumpuk
                if (nmFile != null && !nmFile.equals(image.getAbsolutePath())) {
                    ImageUtils.deleteQuietly(nmFile);
                }
                nmFile = image.getAbsolutePath();
                fullPhotoLauncher.launch(ImageUtils.getUriForFile(this, image));
            } catch (IOException e) {
                Toast.makeText(this, "Gagal menyiapkan file foto: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    /** Membaca file hasil foto lalu menayangkannya di ImageView (Materi Bagian 9 dan 10). */
    private void showFullResult() {
        int[] bounds = ImageUtils.readBounds(nmFile);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = ImageUtils.calculateInSampleSize(bounds[0], bounds[1], 1080, 1080);
        Bitmap bm = BitmapFactory.decodeFile(nmFile, options);
        bm = ImageUtils.fixOrientation(nmFile, bm);

        if (bm == null) {
            tvInfoB.setText("Gagal membaca file: " + nmFile);
            return;
        }
        ivModeB.setImageBitmap(bm);
        btnSaveGallery.setVisibility(View.VISIBLE);
        tvInfoB.setText("File   : " + nmFile
                + "\nAsli   : " + bounds[0] + " x " + bounds[1] + " px"
                + "\nSample : inSampleSize = " + options.inSampleSize
                + " (tampil " + bm.getWidth() + " x " + bm.getHeight() + " px)"
                + "\nUkuran : " + ImageUtils.formatSize(new File(nmFile).length()));
    }

    // ---------------------------------------------------------------- Mode C: Galeri

    private void onSaveToGalleryClicked() {
        if (!ImageUtils.isValidPhoto(nmFile)) {
            Toast.makeText(this, "Ambil foto Mode B terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean needLegacyWrite = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
        if (needLegacyWrite) {
            writePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else {
            saveToGallery();
        }
    }

    private void saveToGallery() {
        try {
            Uri uri = ImageUtils.saveFileToGallery(this, new File(nmFile));
            Toast.makeText(this, "Tersimpan di Galeri (Pictures/BukuKontakITS)\n" + uri, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Gagal menyimpan ke Galeri: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}