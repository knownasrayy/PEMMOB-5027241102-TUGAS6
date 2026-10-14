# 📸 Buku Kontak Mahasiswa ITS & Lab Kamera (Tugas 6 Pemrograman Mobile)

[![Platform](https://img.shields.io/badge/Platform-Android%20(API%2024%2B)-green.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Java-orange.svg)](https://www.java.com)
[![Camera Engine](https://img.shields.io/badge/Camera-Intent%20%2B%20Jetpack%20CameraX-red.svg)](https://developer.android.com/training/camerax)
[![Storage](https://img.shields.io/badge/Storage-FileProvider%20%26%20MediaStore-yellow.svg)](https://developer.android.com/reference/androidx/core/content/FileProvider)
[![Database](https://img.shields.io/badge/Database-SQLite-blue.svg)](https://www.sqlite.org)
[![UI/UX](https://img.shields.io/badge/Design-Material%20Design%203%20(M3)-007BC0.svg)](https://m3.material.io)
[![Brand](https://img.shields.io/badge/Brand-ITS%20Brand%20Guidelines-013880.svg)](https://www.its.ac.id)

Aplikasi manajemen buku kontak mahasiswa berbasis Android yang diperluas dengan **integrasi fungsionalitas kamera penuh**, arsitektur **Jetpack CameraX**, dan **Lab Kamera Interaktif**. Proyek ini dikembangkan sebagai pemenuhan **Tugas 6: Android Kamera** pada mata kuliah Pemrograman Perangkat Bergerak, Departemen Teknologi Informasi, FTEIC – Institut Teknologi Sepuluh Nopember (ITS).

---

## 👨‍💻 Identitas Mahasiswa

- **Nama:** Rayhan Agnan Kusuma
- **NRP:** 5027241102
- **Mata Kuliah:** Pemrograman Perangkat Bergerak (Pemrograman Mobile)
- **Dosen Pengampu:** Dr. Dwi Sunaryono, S.Kom., M.Kom.
- **Departemen:** Teknologi Informasi
- **Fakultas:** Fakultas Teknologi Elektro dan Informatika Cerdas (FTEIC)
- **Institusi:** Institut Teknologi Sepuluh Nopember (ITS) Surabaya

---

## 🎯 Capstone & Pemenuhan Materi Android Kamera

Proyek ini mengimplementasikan seluruh silabus materi perkuliahan **Android Kamera** secara komprehensif, mencakup metode klasik hingga arsitektur modern Android Jetpack:

| No | Modul Materi Kuliah | Implementasi dalam Proyek Ini | Lokasi Kode / File |
|:--:|---|---|---|
| **1** | **Intent Kamera (Dasar)** | Memanggil kamera bawaan via `MediaStore.ACTION_IMAGE_CAPTURE` untuk menangkap foto thumbnail tanpa `EXTRA_OUTPUT`. | [`CameraLabActivity.java`](file:///app/src/main/java/com/example/tugas5_listview/ui/CameraLabActivity.java) (Mode A) |
| **2** | **Simpan Foto Resolusi Penuh** | Mengirimkan `MediaStore.EXTRA_OUTPUT` dengan target file URI agar kamera menyimpan foto beresolusi penuh asli sensor. | [`CameraLabActivity.java`](file:///app/src/main/java/com/example/tugas5_listview/ui/CameraLabActivity.java) (Mode B), [`MainActivity.java`](file:///app/src/main/java/com/example/tugas5_listview/ui/MainActivity.java) |
| **3** | **FileProvider & Keamanan URI** | Menghindari `FileUriExposedException` (Android 7.0+) dengan membagikan `content://` URI aman melalui `androidx.core.content.FileProvider`. | [`AndroidManifest.xml`](file:///app/src/main/AndroidManifest.xml), [`file_paths.xml`](file:///app/src/main/res/xml/file_paths.xml), [`ImageUtils.java`](file:///app/src/main/java/com/example/tugas5_listview/util/ImageUtils.java) |
| **4** | **Runtime Permission Handling** | Penanganan izin `android.permission.CAMERA` & `WRITE_EXTERNAL_STORAGE` (API <= 28) dengan alur Material 3 Rationale & Settings fallback. | [`CameraPermissionHelper.java`](file:///app/src/main/java/com/example/tugas5_listview/util/CameraPermissionHelper.java) |
| **5** | **Simpan ke Galeri (MediaStore)** | Menyimpan foto ke galeri publik sistem menggunakan `MediaStore.Images.Media` sehingga langsung terbaca oleh aplikasi Google Photos/Galeri. | [`CameraLabActivity.java`](file:///app/src/main/java/com/example/tugas5_listview/ui/CameraLabActivity.java) (Mode C), [`ImageUtils.java`](file:///app/src/main/java/com/example/tugas5_listview/util/ImageUtils.java) |
| **6** | **Jetpack CameraX (Pengganti Camera/Camera2)** | Pratinjau kamera langsung (*live viewfinder*) menggunakan `PreviewView`, lifecycle-aware binding, dan pengambilan foto instan dengan tombol flip kamera. | [`CameraXActivity.java`](file:///app/src/main/java/com/example/tugas5_listview/ui/CameraXActivity.java), [`activity_camerax.xml`](file:///app/src/main/res/layout/activity_camerax.xml) |
| **7** | **Integrasi SQLite & UI Kontak** | Penyimpanan path file foto ke database SQLite (`foto_path TEXT`), thumbnail rounded pada list kontak, dan pratinjau besar pada dialog detail. | [`DbHelper.java`](file:///app/src/main/java/com/example/tugas5_listview/data/DbHelper.java), [`MahasiswaAdapter.java`](file:///app/src/main/java/com/example/tugas5_listview/ui/MahasiswaAdapter.java), [`MainActivity.java`](file:///app/src/main/java/com/example/tugas5_listview/ui/MainActivity.java) |

---

## 🌟 Fitur Utama Aplikasi

### 1. Lab Kamera Interaktif (`CameraLabActivity`)
Dapat diakses langsung dari tombol menu **"Lab Kamera"** pada toolbar atas. Layar ini didesain sebagai laboratorium pengujian langsung seluruh materi kamera:
- **Mode A (Thumbnail / Tanpa EXTRA_OUTPUT):**
  Mengambil foto cepat dan mengekstrak thumbnail Bitmap berukuran rendah dari `Intent.getExtras().get("data")`. Disertai info ukuran Bitmap dan alokasi memori.
- **Mode B (Full Resolution + FileProvider):**
  Mengambil foto resolusi tinggi ke direktori *app-specific storage* (`getExternalFilesDir(Environment.DIRECTORY_PICTURES)`). Hasil dibaca via downsampled decoding untuk mencegah `OutOfMemoryError`.
- **Mode C (Ekspor ke Galeri Publik):**
  Menyalin hasil foto Mode B ke direktori DCIM/Pictures publik Android lewat ContentResolver MediaStore sehingga langsung muncul di galeri ponsel.
- **Mode D (Peluncur CameraX Live):**
  Pintasan langsung untuk membuka layar CameraX Live Viewfinder.

### 2. Viewfinder Kamera Mandiri dengan CameraX (`CameraXActivity`)
Implementasi kamera modern berbasis Google Jetpack CameraX:
- **Live Preview:** Tampilan feed kamera real-time mulus pada `androidx.camera.view.PreviewView`.
- **ImageCapture Use Case:** Pengambilan foto resolusi tinggi terikat siklus hidup (*lifecycle-bound*).
- **Flip Camera:** Mendukung perpindahan kamera belakang (*back camera*) dan kamera depan (*front/selfie camera*) secara instan.
- **Thumbnail Callback:** Menampilkan pratinjau foto terakhir yang diambil pada sudut layar secara interaktif.

### 3. Foto Profil Kontak Mahasiswa Terintegrasi
- **Ambil Foto di Form Dialog:** Saat menambah atau mengedit kontak, pengguna dapat langsung memotret wajah mahasiswa via kamera.
- **Preview & Hapus Foto:** Foto yang baru diambil langsung dipratinjau dalam bentuk avatar bulat di form input, lengkap dengan tombol untuk menghapus/mengulang foto.
- **Pembersihan Berkas Otomatis (*Garbage Cleanup*):** Berkas foto sementara (*temporary file*) otomatis dihapus jika pengguna membatalkan dialog form atau mengganti foto sebelum disimpan.
- **Penghapusan Bersih dari Disk:** Saat sebuah kontak dihapus dari SQLite, berkas fisik fotonya di disk turut dihapus secara bersih untuk menghemat kapasitas penyimpanan.
- **Fallback Monogram Cerdas:** Jika kontak tidak memiliki foto, sistem otomatis menampilkan inisial monogram nama berlatar belakang warna resmi ITS.

---

## 🏗️ Arsitektur & Struktur Direktori

```
app/src/main/
├── AndroidManifest.xml                        # Manifest aplikasi, izin kamera, FileProvider & queries
├── java/com/example/tugas5_listview/
│   ├── data/
│   │   ├── DbHelper.java                     # SQLite OpenHelper (tabel mahasiswa + kolom foto_path)
│   │   ├── Mahasiswa.java                    # Model data POJO Mahasiswa (id, nama, nim, nohp, jurusan, angkatan, fotoPath)
│   │   └── MahasiswaRepository.java          # DAO CRUD SQLite dengan parameterized queries
│   ├── ui/
│   │   ├── MainActivity.java                 # Activity utama buku kontak + dialog form foto profil
│   │   ├── MahasiswaAdapter.java             # Custom ArrayAdapter + ViewHolder pattern + visual foto
│   │   ├── CameraLabActivity.java            # Lab demo: Intent thumbnail vs full-size vs MediaStore
│   │   └── CameraXActivity.java              # Viewfinder kamera real-time dengan AndroidX CameraX
│   └── util/
│       ├── CameraPermissionHelper.java       # Helper pengecekan izin kamera & dialog rationale M3
│       └── ImageUtils.java                   # Helper pembuatan file temp, FileProvider URI, decode sampling, & MediaStore
└── res/
    ├── layout/
    │   ├── activity_main.xml                 # Layar utama buku kontak
    │   ├── activity_camera_lab.xml           # Layar laboratorium kamera
    │   ├── activity_camerax.xml              # Layar live viewfinder CameraX
    │   ├── dialog_form_mahasiswa.xml         # Form input dialog + komponen kamera avatar
    │   ├── dialog_detail_mahasiswa.xml       # Dialog profil mahasiswa + foto profil
    │   └── item_mahasiswa.xml                # Item baris ListView
    ├── xml/
    │   └── file_paths.xml                    # Konfigurasi path FileProvider (app-specific external pictures)
    ├── menu/
    │   └── menu_main.xml                     # Menu aksi toolbar: Lab Kamera, Urutkan, Cari
    └── values/
        ├── colors.xml                        # Palet warna resmi ITS & Material 3
        ├── strings.xml                       # String resources bahasa Indonesia
        ├── styles_camera.xml                 # Style tampilan lab kamera
        └── themes.xml                        # Material 3 Day/Night NoActionBar
```

---

## 🔐 Keamanan & Kompatibilitas Android Modern

Aplikasi ini dirancang sesuai standar Android SDK terbaru (API level 24 hingga 36/37):

1. **Package Visibility (`<queries>`):**
   Android 11 (API 30+) membatasi visibilitas paket aplikasi lain. Ditambahkan deklarasi `<queries>` untuk intent `android.media.action.IMAGE_CAPTURE` agar `resolveActivity()` dapat mendeteksi aplikasi kamera bawaan secara tepat.
2. **FileProvider (`androidx.core.content.FileProvider`):**
   Menggantikan pemanggilan usang `Uri.fromFile(file)` yang memicu `FileUriExposedException` pada Android 7.0+. URI disajikan dalam skema aman `content://...fileprovider/...`.
3. **Scoped Storage Compliance:**
   Penyimpanan berkas foto diletakkan pada direktori khusus aplikasi (`getExternalFilesDir(Environment.DIRECTORY_PICTURES)`), sehingga tidak memerlukan izin penyimpanan global pada Android 10+ (API 29+).
4. **Modern Activity Result API:**
   Menggantikan metode lama `startActivityForResult()` dan `onActivityResult()` yang telah *deprecated* dengan `registerForActivityResult(new ActivityResultContracts.TakePicture())` dan `RequestPermission()`.

---

## 🚀 Panduan Menjalankan Proyek

1. **Clone Repositori:**
   ```bash
   git clone https://github.com/knownasrayy/PEMMOB-5027241102-TUGAS6.git
   cd PEMMOB-5027241102-TUGAS6
   ```

2. **Buka di Android Studio:**
   - Buka project menggunakan **Android Studio (Ladybug / Koala atau lebih baru)**.
   - Biarkan Gradle melakukan sinkronisasi otomatis (*Sync Project with Gradle Files*).

3. **Kompilasi & Build APK:**
   - Jalankan via terminal:
     ```bash
     ./gradlew assembleDebug
     ```
   - Berkas APK siap diinstal pada: `app/build/outputs/apk/debug/app-debug.apk`.

4. **Menjalankan di Emulator / Device Fisik:**
   - Pastikan perangkat emulator memiliki konfigurasi kamera aktif (*VirtualScene* atau *Webcam*).
   - Tekan tombol **Run (Shift + F10)** di Android Studio.

---

## 📚 Dokumen Pendukung & Catatan Kuliah

- Ringkasan teoritis lengkap materi kuliah oleh dosen pengampu tersedia pada berkas terpisah:  
  👉 [**`README - Materi Android Kamera.md`**](file:///README%20-%20Materi%20Android%20Kamera.md)

---
*Dibuat dengan dedikasi untuk mata kuliah Pemrograman Perangkat Bergerak – Departemen Teknologi Informasi, FTEIC ITS.*