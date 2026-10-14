package com.example.tugas5_listview.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * MahasiswaRepository â€” Lapisan Data Access Object SQLite untuk Buku Kontak.
 * Menjamin parameterized queries (?) dan ContentValues untuk proteksi SQL Injection.
 */
public class MahasiswaRepository {

    private static final String TAG = "MahasiswaRepository";
    private final DbHelper dbHelper;
    private SQLiteDatabase db;

    public MahasiswaRepository(Context context) {
        this.dbHelper = new DbHelper(context.getApplicationContext());
    }

    public synchronized void open() {
        ensureOpen();
    }

    private synchronized void ensureOpen() {
        if (db == null || !db.isOpen()) {
            try {
                db = dbHelper.getWritableDatabase();
            } catch (Exception e) {
                Log.e(TAG, "Gagal membuka database writable, mencoba readable: " + e.getMessage(), e);
                db = dbHelper.getReadableDatabase();
            }
        }
    }

    public synchronized void close() {
        if (db != null && db.isOpen()) {
            db.close();
            db = null;
        }
    }

    // â”€â”€ CREATE â”€â”€
    public synchronized long insert(String nama, String nim, String nohp, String jurusan, int angkatan, String fotoPath) {
        ensureOpen();
        try {
            ContentValues cv = new ContentValues();
            cv.put(DbHelper.COL_NAMA, nama);
            cv.put(DbHelper.COL_NIM, nim);
            cv.put(DbHelper.COL_NOHP, nohp);
            cv.put(DbHelper.COL_JURUSAN, jurusan);
            cv.put(DbHelper.COL_ANGKATAN, angkatan);
            if (fotoPath != null) {
                cv.put(DbHelper.COL_FOTO, fotoPath);
            } else {
                cv.putNull(DbHelper.COL_FOTO);
            }
            long id = db.insert(DbHelper.TABLE_MAHASISWA, null, cv);
            Log.d(TAG, "Insert record result ID: " + id);
            return id;
        } catch (Exception e) {
            Log.e(TAG, "Error saat insert kontak: " + e.getMessage(), e);
            return -1;
        }
    }

    // â”€â”€ READ ALL with Sort â”€â”€
    public synchronized List<Mahasiswa> getAll(String sortOrder) {
        ensureOpen();
        List<Mahasiswa> list = new ArrayList<>();
        String orderBy;
        if ("oldest".equals(sortOrder)) {
            orderBy = DbHelper.COL_TIMESTAMP + " ASC, " + DbHelper.COL_ID + " ASC";
        } else if ("name_asc".equals(sortOrder)) {
            orderBy = DbHelper.COL_NAMA + " COLLATE NOCASE ASC";
        } else if ("nim_asc".equals(sortOrder)) {
            orderBy = DbHelper.COL_NIM + " ASC";
        } else {
            orderBy = DbHelper.COL_TIMESTAMP + " DESC, " + DbHelper.COL_ID + " DESC";
        }

        try {
            Cursor cursor = db.rawQuery(
                    "SELECT " + DbHelper.COL_ID + ", "
                    + DbHelper.COL_NAMA + ", "
                    + DbHelper.COL_NIM + ", "
                    + DbHelper.COL_NOHP + ", "
                    + DbHelper.COL_JURUSAN + ", "
                    + DbHelper.COL_ANGKATAN + ", "
                    + DbHelper.COL_FOTO + ", "
                    + "datetime(" + DbHelper.COL_TIMESTAMP + ", 'localtime') AS ts "
                    + "FROM " + DbHelper.TABLE_MAHASISWA + " "
                    + "ORDER BY " + orderBy,
                    null
            );

            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        do {
                            list.add(cursorToMahasiswa(cursor));
                        } while (cursor.moveToNext());
                    }
                } finally {
                    cursor.close();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saat getAll: " + e.getMessage(), e);
        }
        return list;
    }

    // â”€â”€ READ BY ID â”€â”€
    public synchronized Mahasiswa getById(long id) {
        ensureOpen();
        Mahasiswa mhs = null;
        try {
            Cursor cursor = db.rawQuery(
                    "SELECT " + DbHelper.COL_ID + ", "
                    + DbHelper.COL_NAMA + ", "
                    + DbHelper.COL_NIM + ", "
                    + DbHelper.COL_NOHP + ", "
                    + DbHelper.COL_JURUSAN + ", "
                    + DbHelper.COL_ANGKATAN + ", "
                    + DbHelper.COL_FOTO + ", "
                    + "datetime(" + DbHelper.COL_TIMESTAMP + ", 'localtime') AS ts "
                    + "FROM " + DbHelper.TABLE_MAHASISWA + " "
                    + "WHERE " + DbHelper.COL_ID + " = ?",
                    new String[]{String.valueOf(id)}
            );

            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        mhs = cursorToMahasiswa(cursor);
                    }
                } finally {
                    cursor.close();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saat getById: " + e.getMessage(), e);
        }
        return mhs;
    }

    // â”€â”€ SEARCH (Nama, NIM, NoHP, atau Jurusan) â”€â”€
    public synchronized List<Mahasiswa> search(String query, String sortOrder) {
        ensureOpen();
        List<Mahasiswa> list = new ArrayList<>();
        String orderBy;
        if ("oldest".equals(sortOrder)) {
            orderBy = DbHelper.COL_TIMESTAMP + " ASC, " + DbHelper.COL_ID + " ASC";
        } else if ("name_asc".equals(sortOrder)) {
            orderBy = DbHelper.COL_NAMA + " COLLATE NOCASE ASC";
        } else if ("nim_asc".equals(sortOrder)) {
            orderBy = DbHelper.COL_NIM + " ASC";
        } else {
            orderBy = DbHelper.COL_TIMESTAMP + " DESC, " + DbHelper.COL_ID + " DESC";
        }

        String pattern = "%" + query + "%";
        try {
            Cursor cursor = db.rawQuery(
                    "SELECT " + DbHelper.COL_ID + ", "
                    + DbHelper.COL_NAMA + ", "
                    + DbHelper.COL_NIM + ", "
                    + DbHelper.COL_NOHP + ", "
                    + DbHelper.COL_JURUSAN + ", "
                    + DbHelper.COL_ANGKATAN + ", "
                    + DbHelper.COL_FOTO + ", "
                    + "datetime(" + DbHelper.COL_TIMESTAMP + ", 'localtime') AS ts "
                    + "FROM " + DbHelper.TABLE_MAHASISWA + " "
                    + "WHERE " + DbHelper.COL_NAMA + " LIKE ? "
                    + "OR " + DbHelper.COL_NIM + " LIKE ? "
                    + "OR " + DbHelper.COL_NOHP + " LIKE ? "
                    + "OR " + DbHelper.COL_JURUSAN + " LIKE ? "
                    + "ORDER BY " + orderBy,
                    new String[]{pattern, pattern, pattern, pattern}
            );

            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        do {
                            list.add(cursorToMahasiswa(cursor));
                        } while (cursor.moveToNext());
                    }
                } finally {
                    cursor.close();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saat search: " + e.getMessage(), e);
        }
        return list;
    }

    // â”€â”€ UPDATE â”€â”€
    public synchronized int update(long id, String nama, String nim, String nohp, String jurusan, int angkatan, String fotoPath) {
        ensureOpen();
        try {
            ContentValues cv = new ContentValues();
            cv.put(DbHelper.COL_NAMA, nama);
            cv.put(DbHelper.COL_NIM, nim);
            cv.put(DbHelper.COL_NOHP, nohp);
            cv.put(DbHelper.COL_JURUSAN, jurusan);
            cv.put(DbHelper.COL_ANGKATAN, angkatan);
            if (fotoPath != null) {
                cv.put(DbHelper.COL_FOTO, fotoPath);
            } else {
                cv.putNull(DbHelper.COL_FOTO);
            }
            int rows = db.update(
                    DbHelper.TABLE_MAHASISWA,
                    cv,
                    DbHelper.COL_ID + " = ?",
                    new String[]{String.valueOf(id)}
            );
            Log.d(TAG, "Update record result rows: " + rows);
            return rows;
        } catch (Exception e) {
            Log.e(TAG, "Error saat update kontak: " + e.getMessage(), e);
            return -1;
        }
    }

    // â”€â”€ DELETE â”€â”€
    public synchronized int delete(long id) {
        ensureOpen();
        try {
            int rows = db.delete(
                    DbHelper.TABLE_MAHASISWA,
                    DbHelper.COL_ID + " = ?",
                    new String[]{String.valueOf(id)}
            );
            Log.d(TAG, "Delete record result rows: " + rows);
            return rows;
        } catch (Exception e) {
            Log.e(TAG, "Error saat delete kontak: " + e.getMessage(), e);
            return -1;
        }
    }

    private Mahasiswa cursorToMahasiswa(Cursor cursor) {
        Mahasiswa m = new Mahasiswa();
        m.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DbHelper.COL_ID)));
        m.setNama(cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_NAMA)));
        m.setNim(cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_NIM)));
        m.setNohp(cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_NOHP)));
        m.setJurusan(cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_JURUSAN)));
        m.setAngkatan(cursor.getInt(cursor.getColumnIndexOrThrow(DbHelper.COL_ANGKATAN)));
        m.setFotoPath(cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_FOTO)));
        m.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow("ts")));
        return m;
    }
}