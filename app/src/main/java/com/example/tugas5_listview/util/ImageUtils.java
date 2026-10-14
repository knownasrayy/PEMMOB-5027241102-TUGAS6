package com.example.tugas5_listview.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.LruCache;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ImageUtils - utilitas foto untuk Materi Android Kamera.
 *
 * Merangkum konsep materi:
 *  - Bagian 5/7  : menentukan file tujuan foto (EXTRA_OUTPUT) dengan nama unik berbasis waktu
 *  - Bagian 9    : membaca file hasil foto dengan BitmapFactory + inSampleSize agar tidak null / OOM
 *  - Bagian 14   : FileProvider (pengganti Uri.fromFile) dan penyimpanan Scoped Storage
 */
public final class ImageUtils {

    /** Cache thumbnail kecil supaya ListView tidak men-decode ulang file saat di-scroll. */
    private static final LruCache<String, Bitmap> THUMB_CACHE =
            new LruCache<String, Bitmap>(4 * 1024 * 1024) {
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getByteCount();
                }
            };

    private ImageUtils() {}

    // ---------------------------------------------------------------- File tujuan foto

    /**
     * Membuat file kosong bernama unik (IMG_yyyyMMdd_HHmmss_xxx.jpg) di folder foto khusus aplikasi.
     * Folder khusus aplikasi tidak butuh izin WRITE_EXTERNAL_STORAGE (Scoped Storage).
     */
    public static File createImageFile(Context ctx) throws IOException {
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File dir = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (dir == null) {
            dir = new File(ctx.getFilesDir(), "Pictures");
        }
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Gagal membuat folder foto: " + dir);
        }
        return File.createTempFile("IMG_" + stamp + "_", ".jpg", dir);
    }

    /** content:// Uri aman untuk EXTRA_OUTPUT (pengganti Uri.fromFile yang memicu FileUriExposedException). */
    public static Uri getUriForFile(Context ctx, File file) {
        return FileProvider.getUriForFile(ctx, ctx.getPackageName() + ".fileprovider", file);
    }

    public static void deleteQuietly(String path) {
        if (path == null) return;
        File f = new File(path);
        if (f.exists() && !f.delete()) {
            f.deleteOnExit();
        }
    }

    public static boolean isValidPhoto(String path) {
        if (path == null) return false;
        File f = new File(path);
        return f.exists() && f.length() > 0;
    }

    // ---------------------------------------------------------------- Membaca / decode foto

    /** Membaca dimensi gambar tanpa memuat pikselnya ke memori (inJustDecodeBounds). */
    public static int[] readBounds(String path) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, o);
        return new int[]{o.outWidth, o.outHeight};
    }

    /** Pangkat dua terbesar untuk inSampleSize agar hasil decode masih >= ukuran yang diminta. */
    public static int calculateInSampleSize(int width, int height, int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * Decode file foto dengan skala yang aman (Materi Bagian 9: hasil decodeFile bisa null /
     * OutOfMemory bila gambar terlalu besar, sehingga ukurannya harus dikecilkan).
     */
    public static Bitmap decodeSampled(String path, int reqWidth, int reqHeight) {
        if (!isValidPhoto(path)) return null;
        int[] bounds = readBounds(path);
        if (bounds[0] <= 0 || bounds[1] <= 0) return null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(bounds[0], bounds[1], reqWidth, reqHeight);
        Bitmap bm = BitmapFactory.decodeFile(path, options);
        return fixOrientation(path, bm);
    }

    /** Memutar bitmap sesuai tag EXIF agar foto dari kamera tidak tampil miring. */
    public static Bitmap fixOrientation(String path, Bitmap bm) {
        if (bm == null) return null;
        try {
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int degrees;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:  degrees = 90;  break;
                case ExifInterface.ORIENTATION_ROTATE_180: degrees = 180; break;
                case ExifInterface.ORIENTATION_ROTATE_270: degrees = 270; break;
                default: return bm;
            }
            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);
            Bitmap rotated = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
            if (rotated != bm) {
                bm.recycle();
            }
            return rotated;
        } catch (IOException e) {
            return bm;
        }
    }

    /** Thumbnail kecil ber-cache untuk baris ListView. */
    public static Bitmap loadThumbnail(String path, int sizePx) {
        if (path == null) return null;
        String key = path + "@" + sizePx;
        Bitmap cached = THUMB_CACHE.get(key);
        if (cached != null && !cached.isRecycled()) return cached;

        Bitmap bm = decodeSampled(path, sizePx, sizePx);
        if (bm != null) {
            THUMB_CACHE.put(key, bm);
        }
        return bm;
    }

    // ---------------------------------------------------------------- Galeri (MediaStore)

    /**
     * Menyalin file foto ke Galeri lewat MediaStore (pengganti menulis langsung ke DCIM,
     * yang dibatasi Scoped Storage mulai Android 10).
     */
    public static Uri saveFileToGallery(Context ctx, File src) throws IOException {
        ContentResolver cr = ctx.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, src.getName());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BukuKontakITS");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
        }

        Uri uri = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            throw new IOException("MediaStore menolak membuat entri baru");
        }

        try (InputStream in = new FileInputStream(src); OutputStream out = cr.openOutputStream(uri)) {
            if (out == null) {
                throw new IOException("Tidak dapat membuka output stream Galeri");
            }
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) > 0) {
                out.write(buffer, 0, n);
            }
        } catch (IOException e) {
            cr.delete(uri, null, null);
            throw e;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues done = new ContentValues();
            done.put(MediaStore.Images.Media.IS_PENDING, 0);
            cr.update(uri, done, null, null);
        }
        return uri;
    }

    public static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format(Locale.US, "%.1f KB", bytes / 1024f);
        return String.format(Locale.US, "%.2f MB", bytes / (1024f * 1024f));
    }
}