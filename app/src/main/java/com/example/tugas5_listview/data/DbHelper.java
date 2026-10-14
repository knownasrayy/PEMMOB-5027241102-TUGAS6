package com.example.tugas5_listview.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DbHelper â€” SQLiteOpenHelper untuk Buku Kontak Mahasiswa ITS (Pertemuan 5).
 * Database: kontak_mahasiswa_v2.db (Memastikan skema bersih dan terisolasi dari sisa cache)
 * Tabel: mahasiswa
 */
public class DbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME    = "kontak_mahasiswa_v2.db";
    public static final int    DATABASE_VERSION = 3;

    public static final String TABLE_MAHASISWA  = "mahasiswa";
    public static final String COL_ID           = "_id"; // Sesuai konvensi P5
    public static final String COL_NAMA         = "nama";
    public static final String COL_NIM          = "nim";
    public static final String COL_NOHP         = "nohp";
    public static final String COL_JURUSAN      = "jurusan";
    public static final String COL_ANGKATAN     = "angkatan";
    public static final String COL_TIMESTAMP    = "timestamp";
    public static final String COL_FOTO         = "foto_path"; // path file foto profil dari kamera (nullable)

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_MAHASISWA + " (" +
            COL_ID        + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_NAMA      + " TEXT NOT NULL, " +
            COL_NIM       + " TEXT NOT NULL UNIQUE, " +
            COL_NOHP      + " TEXT NOT NULL, " +
            COL_JURUSAN   + " TEXT NOT NULL, " +
            COL_ANGKATAN  + " INTEGER NOT NULL, " +
            COL_FOTO      + " TEXT, " +
            COL_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
            ")";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);

        // Seed data dummy resmi ITS lengkap dengan nomor telepon kontak
        db.execSQL("INSERT INTO " + TABLE_MAHASISWA + " (" +
                COL_NAMA + ", " + COL_NIM + ", " + COL_NOHP + ", " + COL_JURUSAN + ", " + COL_ANGKATAN + ") VALUES " +
                "('Rayhan Agnan Kusuma', '5027241102', '081234567890', 'Teknologi Informasi', 2024), " +
                "('Tiffany Viriya Chu', '5027241088', '081298765432', 'Teknologi Informasi', 2024)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Migrasi bertahap: data lama tetap aman (tidak lagi DROP TABLE)
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_MAHASISWA + " ADD COLUMN " + COL_FOTO + " TEXT");
        }
    }
}