package com.example.tugas5_listview.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * CameraPermissionHelper - alur Runtime Permission kamera (Materi Bagian 8 dan 14).
 *
 * Sejak Android 6.0 (API 23) izin diminta saat aplikasi berjalan. Karena manifest mendeklarasikan
 * CAMERA, izin ini WAJIB diberikan sebelum membuka kamera (baik lewat Intent maupun CameraX).
 */
public final class CameraPermissionHelper {

    private CameraPermissionHelper() {}

    public static boolean hasCameraPermission(Context ctx) {
        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean shouldShowRationale(Activity activity) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA);
    }

    /** Dialog penjelasan sebelum meminta izin lagi setelah pengguna pernah menolak. */
    public static void showRationaleDialog(Activity activity, Runnable onContinue) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle("Izin Kamera Diperlukan")
                .setMessage("Aplikasi memerlukan akses kamera untuk mengambil foto profil mahasiswa. "
                        + "Foto hanya disimpan di penyimpanan aplikasi ini.")
                .setPositiveButton("Lanjutkan", (d, w) -> onContinue.run())
                .setNegativeButton("Batal", (d, w) -> d.dismiss())
                .show();
    }

    /** Dipakai saat izin ditolak permanen ("Jangan tanya lagi"): arahkan ke pengaturan aplikasi. */
    public static void showSettingsDialog(Activity activity) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle("Izin Kamera Ditolak")
                .setMessage("Izin kamera ditolak permanen. Aktifkan melalui Pengaturan > Izin aplikasi "
                        + "agar fitur foto dapat digunakan.")
                .setPositiveButton("Buka Pengaturan", (d, w) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
                    activity.startActivity(intent);
                })
                .setNegativeButton("Batal", (d, w) -> d.dismiss())
                .show();
    }
}