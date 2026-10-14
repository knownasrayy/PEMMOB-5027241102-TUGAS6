# Android Kamera

> Ringkasan lengkap materi **Pemrograman Perangkat Bergerak** — Program Studi Sarjana Informatika, Departemen Teknik Informatika, Institut Teknologi Sepuluh Nopember (ITS).
> Dosen pengampu: Dr. Dwi Sunaryono, S.Kom., M.Kom.

---

## Daftar Isi

1. [Pendahuluan](#1-pendahuluan)
2. [Menggunakan Aplikasi Kamera Android (Intent)](#2-menggunakan-aplikasi-kamera-android-intent)
3. [Pemanggilan Intent](#3-pemanggilan-intent)
4. [Pembuatan Aplikasi Sederhana](#4-pembuatan-aplikasi-sederhana)
5. [Menyimpan Foto ke Tempat Lain](#5-menyimpan-foto-ke-tempat-lain)
6. [Memakai onActivityResult()](#6-memakai-onactivityresult)
7. [Menyimpan Foto ke File Lain (Tanpa putExtra)](#7-menyimpan-foto-ke-file-lain-tanpa-putextra)
8. [Akses Permission Write Storage (Runtime Permission)](#8-akses-permission-write-storage-runtime-permission)
9. [Membaca File Hasil Foto](#9-membaca-file-hasil-foto)
10. [Program Selengkapnya](#10-program-selengkapnya)
11. [Memakai API Camera](#11-memakai-api-camera)
12. [Android Working with Camera2 API](#12-android-working-with-camera2-api)
13. [Perbandingan Pendekatan](#13-perbandingan-pendekatan)
14. [Catatan Tambahan (Pembaruan Android Modern)](#14-catatan-tambahan-pembaruan-android-modern)

---

## 1. Pendahuluan

Terdapat **dua cara** menggunakan kamera dalam Android:

1. Menggunakan **aplikasi kamera** bawaan Android di dalam aplikasi kita (lewat `Intent`).
2. Menggunakan **Camera API** yang disediakan oleh Android di dalam aplikasi kita.

---

## 2. Menggunakan Aplikasi Kamera Android (Intent)

Aplikasi kamera bawaan dipanggil menggunakan `MediaStore.ACTION_IMAGE_CAPTURE`:

```java
Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
```

### Beberapa data di MediaStore

| No | Jenis maksud (intent) | Deskripsi |
|----|----------------------|-----------|
| 1 | `ACTION_IMAGE_CAPTURE_SECURE` | Mengembalikan gambar yang diambil dari kamera, saat perangkat sedang diproteksi (terkunci). |
| 2 | `ACTION_VIDEO_CAPTURE` | Memanggil aplikasi video yang ada di Android untuk menangkap video. |
| 3 | `EXTRA_SCREEN_ORIENTATION` | Mengatur orientasi layar: vertikal (portrait) atau landscape. |
| 4 | `EXTRA_FULL_SCREEN` | Mengontrol user interface dari ViewImage. |
| 5 | `INTENT_ACTION_VIDEO_CAMERA` | Meluncurkan kamera dalam modus video. |
| 6 | `EXTRA_SIZE_LIMIT` | Menentukan batas ukuran video atau ukuran pengambilan gambar. |

---

## 3. Pemanggilan Intent

```java
Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
startActivityForResult(intent, 0);
```

Method `startActivityForResult()` didefinisikan di dalam class **Activity**. Pada contoh ini method dipanggil dari *main activity*. Terdapat method lain di class Activity yang fungsinya sama, tetapi dipakai ketika pemanggilan **tidak** dilakukan dari activity itu sendiri, melainkan dari tempat lain.

### Pilihan menjalankan aktivitas

| No | Fungsi | Deskripsi |
|----|--------|-----------|
| 1 | `startActivityForResult(Intent intent, int requestCode, Bundle options)` | Memulai sebuah activity, dan dapat membawa tambahan *bundle* berisi opsi. |
| 2 | `startActivityFromChild(Activity child, Intent intent, int requestCode)` | Menjalankan activity ketika activity kita merupakan *child* dari activity lain. |
| 3 | `startActivityFromChild(Activity child, Intent intent, int requestCode, Bundle options)` | Sama seperti di atas, tetapi dapat membawa nilai tambahan dalam bentuk bundle. |
| 4 | `startActivityFromFragment(Fragment fragment, Intent intent, int requestCode)` | Menjalankan activity dari fragment yang sedang aktif. |
| 5 | `startActivityFromFragment(Fragment fragment, Intent intent, int requestCode, Bundle options)` | Tidak hanya menjalankan activity dari fragment, tetapi juga dapat membawa nilai tambahan. |

---

## 4. Pembuatan Aplikasi Sederhana

Aplikasi terdiri dari satu **Button** ("AmbilFoto") dan satu **ImageView**. Nama package pada contoh: `dwi.anif.kamera`.

### 4.1 Layout XML (`activity_main.xml`)

```xml
<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="dwi.anif.kamera.MainActivity">

    <Button
        android:id="@+id/button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="AmbilFoto"
        tools:layout_editor_absoluteX="138dp"
        tools:layout_editor_absoluteY="456dp"
        tools:ignore="MissingConstraints" />

    <ImageView
        android:id="@+id/imageView"
        android:layout_width="372dp"
        android:layout_height="514dp"
        app:srcCompat="@mipmap/ic_launcher"
        tools:layout_editor_absoluteY="-58dp"
        android:layout_marginRight="8dp"
        app:layout_constraintRight_toRightOf="parent"
        tools:ignore="MissingConstraints" />
</android.support.constraint.ConstraintLayout>
```

### 4.2 `MainActivity.java`

```java
package dwi.anif.kamera;

import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    Button b1;
    ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b1 = (Button) findViewById(R.id.button);
        iv = (ImageView) findViewById(R.id.imageView);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(it, 0);
            }
        });
    }
}
```

### 4.3 Hasil

Jalankan program, maka akan muncul sebuah button. Jika ditekan, fungsi kamera akan aktif. Jika dilakukan pengambilan gambar, hasilnya dapat dilihat pada file di **gallery**.

---

## 5. Menyimpan Foto ke Tempat Lain

Dengan program pada bagian 4, sebenarnya hasil foto sudah tersimpan di gallery. Jika hasil foto ingin diletakkan di folder atau file lain, maka **tempat penyimpanan harus ditentukan**. Penyimpanan dilakukan dengan memakai **`putExtra`** (`MediaStore.EXTRA_OUTPUT`) sebelum `startActivityForResult`.

### 5.1 Menyimpan ke direktori DCIM

```java
public class MainActivity extends AppCompatActivity {
    Button b1;
    ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b1 = (Button) findViewById(R.id.button);
        iv = (ImageView) findViewById(R.id.imageView);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File imagesFolder = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM);
                Date d = new Date();
                CharSequence s = DateFormat.format("MM-dd-yy hh-mm-ss", d.getTime());
                File image = new File(imagesFolder, s.toString() + ".jpg");
                Uri uriSavedImage = Uri.fromFile(image);
                it.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
                startActivityForResult(it, 0);
            }
        });
    }
}
```

> Catatan: pada slide tertulis "DICM", yang dimaksud adalah direktori **DCIM** (`Environment.DIRECTORY_DCIM`).

### 5.2 Menyimpan ke direktori non-default ("HasilFoto")

Menyimpan hasil foto di direktori yang bukan merupakan default Android, misalnya direktori `HasilFoto`:

```java
public class MainActivity extends AppCompatActivity {
    Button b1;
    ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b1 = (Button) findViewById(R.id.button);
        iv = (ImageView) findViewById(R.id.imageView);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File imagesFolder = new File(
                        Environment.getExternalStorageDirectory(), "HasilFoto");
                imagesFolder.mkdirs();
                Date d = new Date();
                CharSequence s = DateFormat.format("MM-dd-yy hh-mm-ss", d.getTime());
                File image = new File(imagesFolder, s.toString() + ".jpg");
                Uri uriSavedImage = Uri.fromFile(image);
                it.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
                startActivityForResult(it, 0);
            }
        });
    }
}
```

Poin penting:

- `imagesFolder.mkdirs()` membuat folder `HasilFoto` jika belum ada.
- Nama file dibuat dari tanggal dan waktu (`DateFormat.format(...)`) agar unik.
- `Uri.fromFile(image)` mengubah `File` menjadi `Uri`, lalu dikirim lewat `putExtra(MediaStore.EXTRA_OUTPUT, ...)`.

---

## 6. Memakai onActivityResult()

Untuk mendeteksi hasil dari activity dipakai method **`onActivityResult()`**, yang menangkap aksi dari `startActivityForResult`. Karena itu diperlukan **kode unik (request code)** agar saat intent diaktifkan dan hasilnya dideteksi, keduanya dapat dikenali dengan kode yang sama. Hal ini dapat dipakai untuk melakukan operasi pada hasil capture foto, misalnya ditayangkan ulang di `ImageView`.

### 6.1 Mendefinisikan kode unik

```java
private static final int kodekamera = 222;
```

### 6.2 Meluncurkan kamera dengan Intent khusus

```java
b1.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(it, kodekamera);
    }
});
```

### 6.3 Menangkap hasil dengan unique request code

Request code menandakan bahwa aplikasi kitalah yang meminta kamera.

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == Activity.RESULT_OK) {
        switch (requestCode) {
            case (kodekamera): prosesKamera(data); break;
        }
    }
}

private void prosesKamera(Intent datanya) {
    Bitmap bm;
    bm = (Bitmap) datanya.getExtras().get("data");
    iv.setImageBitmap(bm);
    Toast.makeText(this, "Data Telah Terload ke ImageView",
            Toast.LENGTH_SHORT).show();
}
```

### 6.4 Program lengkap (tanpa putExtra)

```java
package dwi.anif.kamera;

public class MainActivity extends AppCompatActivity {
    Button b1;
    ImageView iv;
    private static final int kodekamera = 222;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b1 = (Button) findViewById(R.id.button);
        iv = (ImageView) findViewById(R.id.imageView);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(it, kodekamera);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case (kodekamera): prosesKamera(data); break;
            }
        }
    }

    private void prosesKamera(Intent datanya) {
        Bitmap bm;
        bm = (Bitmap) datanya.getExtras().get("data");
        iv.setImageBitmap(bm);
        Toast.makeText(this, "Data Telah Terload ke ImageView",
                Toast.LENGTH_SHORT).show();
    }
}
```

**Hasil:** program berjalan sama seperti sebelumnya, hanya saja hasil capture kamera **ditayangkan di ImageView**. Program ini **tidak memakai `putExtra`**.

---

## 7. Menyimpan Foto ke File Lain (Tanpa putExtra)

Dengan program sebelumnya, hasil foto sebenarnya sudah ada di gallery. Jika ingin hasilnya berada di folder atau file lain, tempat penyimpanan harus ditentukan. Pada pendekatan ini, bitmap hasil capture (thumbnail) dikompres lalu ditulis ke file secara manual.

```java
private void prosesKamera(Intent datanya) throws IOException {
    Bitmap bm;
    bm = (Bitmap) datanya.getExtras().get("data");
    iv.setImageBitmap(bm);

    ByteArrayOutputStream stream = new ByteArrayOutputStream();

    bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
    byte[] byteArray = stream.toByteArray(); // convert camera photo to byte array

    // save it in your external storage.
    File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
    File output = new File(dir, "simpan.png");
    FileOutputStream fo = new FileOutputStream(output);
    fo.write(byteArray);
    fo.flush();
    fo.close();

    Toast.makeText(this, "Data Telah Terload ke ImageView" + output,
            Toast.LENGTH_LONG).show();
}
```

### Permission di `AndroidManifest.xml`

**Jangan lupa menambahkan permission menulis file** di `AndroidManifest.xml`, atau periksa di pengaturan izin aplikasi:

```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

---

## 8. Akses Permission Write Storage (Runtime Permission)

**Requesting Permissions at Run Time**

Mulai Android 6.0 (**API level 23**), pengguna memberikan izin kepada aplikasi **saat aplikasi sedang berjalan**, bukan saat aplikasi diinstal. Pendekatan ini menyederhanakan proses instalasi karena pengguna tidak perlu memberi izin saat install atau update. Pendekatan ini juga memberi pengguna kontrol lebih besar atas fungsi aplikasi; misalnya, pengguna dapat memberi aplikasi kamera akses ke kamera tetapi tidak ke lokasi perangkat.

Pada saat dijalankan, akan muncul dialog: *"Izinkan Kamera untuk mengakses foto, media, dan file di perangkat?"* dengan pilihan **TOLAK** dan **IZINKAN**.

### Deklarasi konstanta dan pemanggilan di `onCreate`

```java
public class MainActivity extends AppCompatActivity {
    Button b1;
    ImageView iv;
    private static final int kodekamera = 222;
    private static final int MY_PERMISSIONS_REQUEST_WRITE = 223;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        askWritePermission();
        setContentView(R.layout.activity_main);
        // ...
    }
}
```

### Method `askWritePermission()`

```java
private void askWritePermission() {
    if (android.os.Build.VERSION.SDK_INT >= 23) {
        int cameraPermission = this.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE);
        }
    }
}
```

Alur singkat:

1. Cek versi SDK (`SDK_INT >= 23`).
2. Cek apakah izin sudah diberikan dengan `checkSelfPermission(...)`.
3. Jika belum (`!= PERMISSION_GRANTED`), minta izin dengan `requestPermissions(...)` memakai kode `MY_PERMISSIONS_REQUEST_WRITE`.

---

## 9. Membaca File Hasil Foto

Program sebelumnya memakai `intent.putExtra` untuk menyimpan hasil foto ke direktori dan nama file lain, dan dipanggil **sebelum** `startActivityForResult`:

```java
Uri uriSavedImage = Uri.fromFile(image);
it.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
startActivityForResult(it, kodekamera);
```

Pada method `onActivityResult`, hasil intent **tidak dapat dipakai langsung** seperti sebelumnya:

```java
bm = (Bitmap) datanya.getExtras().get("data");   // tidak ada data
iv.setImageBitmap(bm);
```

**Alasannya:** tidak ada data pada extras karena foto sudah disimpan ke file lewat `putExtra`.

Program harus diubah untuk **membaca file hasil foto berdasarkan path dan nama file**, baru kemudian di-set ke `ImageView`:

```java
private void prosesKamera(Intent datanya) {
    Bitmap bm;
    //bm = (Bitmap) datanya.getExtras().get("data");
    BitmapFactory.Options options;
    options = new BitmapFactory.Options();
    options.inSampleSize = 2;
    bm = BitmapFactory.decodeFile(nmFile, options);
    iv.setImageBitmap(bm); // Set imageview to image that was captured
    Toast.makeText(this, "Data Telah Terload ke ImageView" + nmFile,
            Toast.LENGTH_SHORT).show();
}
```

- `nmFile` adalah nama file (path lengkap) yang **dideklarasikan secara global** (variabel class).
- `options.inSampleSize = 2` memperkecil ukuran gambar (skala). Hasil `decodeFile` bisa **selalu `null`** jika gambar terlalu besar, sehingga ukurannya harus dikecilkan.

---

## 10. Program Selengkapnya

### Bagian 1 — Deklarasi, `onCreate`, dan Intent

```java
public class MainActivity extends AppCompatActivity {
    Button b1;
    ImageView iv;
    private static final int kodekamera = 222;
    String nmFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b1 = (Button) findViewById(R.id.button);
        iv = (ImageView) findViewById(R.id.imageView);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File imagesFolder = new File(
                        Environment.getExternalStorageDirectory(), "HasilFoto");
                imagesFolder.mkdirs();
                Date d = new Date();
                CharSequence s = DateFormat.format("yyyyMMdd-hh-mm-ss", d.getTime());
                nmFile = imagesFolder + File.separator + s.toString() + ".jpg";
                File image = new File(nmFile);

                Uri uriSavedImage = Uri.fromFile(image);
                it.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
                startActivityForResult(it, kodekamera);
            }
        });
    }
```

### Bagian 2 — `onActivityResult` dan `prosesKamera`

```java
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case (kodekamera): prosesKamera(data); break;
            }
        }
    }

    private void prosesKamera(Intent datanya) {
        Bitmap bm;
        //bm = (Bitmap) datanya.getExtras().get("data");
        BitmapFactory.Options options;
        options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        bm = BitmapFactory.decodeFile(nmFile, options);
        iv.setImageBitmap(bm); // Set imageview to image that was captured
        Toast.makeText(this, "Data Telah Terload ke ImageView" + nmFile,
                Toast.LENGTH_SHORT).show();
    }
}
```

---

## 11. Memakai API Camera

API Camera disediakan oleh **kelas `Camera`**:

```java
Camera object = null;
object = Camera.open();
```

### Fungsi-fungsi yang disediakan kelas Camera

| No | Method | Deskripsi |
|----|--------|-----------|
| 1 | `getCameraInfo(int cameraId, Camera.CameraInfo cameraInfo)` | Mengembalikan informasi tentang kamera tertentu. |
| 2 | `getNumberOfCameras()` | Mengembalikan bilangan bulat yang menyatakan jumlah kamera yang tersedia di perangkat. |
| 3 | `lock()` | Mengunci kamera sehingga tidak ada aplikasi lain yang dapat mengaksesnya. |
| 4 | `release()` | Melepas kunci pada kamera sehingga aplikasi lain dapat mengaksesnya. |
| 5 | `open(int cameraId)` | Membuka kamera tertentu ketika perangkat mendukung banyak kamera. |
| 6 | `enableShutterSound(boolean enabled)` | Mengaktifkan/menonaktifkan suara shutter default saat pengambilan gambar. |

---

## 12. Android Working with Camera2 API

Dibandingkan Camera API lama, **Camera2** (`android.hardware.camera2`) jauh lebih kompleks karena melibatkan banyak **pemanggilan asynchronous**. Camera2 diperkenalkan pada **Android API 21**.

- Jika bekerja pada **API di bawah 21** → gunakan Camera API lama (`android.hardware.camera`).
- Untuk **API di atas 21** → gunakan Camera2, karena API lama sudah *deprecated*.

### Komponen utama Camera2

| Komponen | Fungsi |
|----------|--------|
| **CameraManager** | Mengambil semua kamera yang tersedia di perangkat, masing-masing memiliki `cameraId` (misalnya kamera depan dan belakang). Propertinya dapat diperoleh dari kelas `CameraCharacteristics`. *(Tahap: pilih kamera dan buat `CameraDevice`.)* |
| **CameraDevice** | Mengambil perangkat kamera dari `CameraManager` berdasarkan id dan melakukan operasi terkait. *(Tahap: buat `CaptureRequest` dan buat `CameraCaptureSession`.)* |
| **CaptureRequest** | Dibuat dari `CameraDevice` untuk menangkap gambar. *(Tahap: hubungkan `Surface` untuk tampilan dan buat `CaptureRequest`.)* |
| **CameraCaptureSession** | Dibuat untuk menerima `CaptureRequest` dari Camera Device. *(Tahap: tangkap gambar kamera dan letakkan hasilnya pada `Surface` yang didaftarkan di `CaptureRequest`.)* |
| **CameraCaptureSession.CaptureCallback** | Menyediakan seluruh hasil dari capture session. |

### Alur kerja (ringkas)

```
CameraManager  ->  CameraDevice  ->  CaptureRequest  ->  CameraCaptureSession  ->  CaptureCallback
 (pilih kamera)   (buka kamera)     (siapkan request)    (jalankan capture)       (terima hasil)
```

### Mengapa Camera2 terasa kompleks?

Sebagian besar proses *set-up* dilakukan secara **asynchronous**. Contohnya, saat membuat `CameraDevice`, kita lebih dulu mendaftarkan **fungsi callback** pada `CameraManager`. Setelah `CameraDevice` selesai dibuat dan callback dipanggil, barulah kita mendapatkan `CameraDevice` di dalam kode callback tersebut.

---

## 13. Perbandingan Pendekatan

| Aspek | Intent `ACTION_IMAGE_CAPTURE` | Camera API (lama) | Camera2 API |
|-------|-------------------------------|-------------------|-------------|
| Paket | `android.provider.MediaStore` | `android.hardware.camera` | `android.hardware.camera2` |
| Cara kerja | Memanggil aplikasi kamera bawaan | Mengontrol kamera langsung lewat kelas `Camera` | Mengontrol kamera langsung lewat `CameraManager`, `CameraDevice`, dsb. |
| Tingkat kompleksitas | Paling sederhana | Menengah | Paling kompleks (asynchronous) |
| Versi API | Semua | Dipakai untuk API < 21 | Mulai API 21 |
| Status | Umum dipakai | Deprecated | Direkomendasikan oleh materi untuk API > 21 |

### Ringkasan penyimpanan hasil foto lewat Intent

| Pendekatan | Pakai `putExtra`? | Hasil di `onActivityResult` | Cara menampilkan di ImageView |
|------------|-------------------|-----------------------------|-------------------------------|
| Tanpa `EXTRA_OUTPUT` | Tidak | `data.getExtras().get("data")` berisi bitmap (thumbnail) | `iv.setImageBitmap(bm)` |
| Dengan `EXTRA_OUTPUT` | Ya | Tidak ada data di extras | `BitmapFactory.decodeFile(nmFile, options)` lalu `iv.setImageBitmap(bm)` |

---

## 14. Catatan Tambahan (Pembaruan Android Modern)

> Bagian ini **bukan dari slide**. Ditambahkan sebagai pelengkap karena beberapa kode pada materi memakai API lama.

- **`android.support.*`** (misalnya `android.support.v7.app.AppCompatActivity` dan `android.support.constraint.ConstraintLayout`) sudah digantikan **AndroidX**: `androidx.appcompat.app.AppCompatActivity` dan `androidx.constraintlayout.widget.ConstraintLayout`.
- **`startActivityForResult()` / `onActivityResult()`** sudah *deprecated*. Penggantinya adalah **Activity Result API** (`registerForActivityResult` dengan `ActivityResultContracts.TakePicture` atau `TakePicturePreview`).
- **`Uri.fromFile()`** untuk `EXTRA_OUTPUT` menyebabkan `FileUriExposedException` pada Android 7.0 (API 24) ke atas. Gunakan **`FileProvider`** (`FileProvider.getUriForFile(...)`).
- **`WRITE_EXTERNAL_STORAGE`** dan `Environment.getExternalStorageDirectory()` dibatasi oleh *Scoped Storage* mulai Android 10/11. Gunakan `MediaStore` atau direktori khusus aplikasi (`getExternalFilesDir(...)`).
- Jika aplikasi mengakses kamera langsung (Camera/Camera2/CameraX), tambahkan juga izin kamera di manifest dan minta saat runtime:
  ```xml
  <uses-permission android:name="android.permission.CAMERA" />
  ```
- **Camera API lama** sudah deprecated. Untuk pengembangan baru, **CameraX** (Jetpack) adalah pilihan yang lebih sederhana dibanding Camera2.

---

## Ringkasan Poin Penting

1. Ada dua cara memakai kamera: **aplikasi kamera bawaan (Intent)** dan **Camera API**.
2. Intent kamera: `MediaStore.ACTION_IMAGE_CAPTURE` + `startActivityForResult()`.
3. Gunakan **request code unik** (contoh `kodekamera = 222`) dan tangkap hasil di `onActivityResult()`.
4. Tanpa `putExtra`, hasil berupa **thumbnail** di `data.getExtras().get("data")`.
5. Dengan `putExtra(MediaStore.EXTRA_OUTPUT, uri)`, foto disimpan ke **path yang ditentukan**; baca kembali dengan `BitmapFactory.decodeFile()` dan perkecil dengan `inSampleSize` agar tidak `null`.
6. Menulis ke storage memerlukan **permission** di manifest dan **runtime permission** mulai API 23.
7. **Camera2** (API 21+) bersifat asynchronous: `CameraManager` → `CameraDevice` → `CaptureRequest` → `CameraCaptureSession` → `CaptureCallback`.
