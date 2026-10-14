package com.example.tugas5_listview.data;

/**
 * Model data Mahasiswa / Kontak Mahasiswa ITS.
 * Merepresentasikan satu entitas record pada tabel SQLite "mahasiswa".
 */
public class Mahasiswa {

    private long id;
    private String nama;
    private String nim;
    private String nohp;
    private String jurusan;
    private int angkatan;
    private String timestamp;
    private String fotoPath;

    public Mahasiswa() {}

    public Mahasiswa(long id, String nama, String nim, String nohp, String jurusan, int angkatan, String timestamp) {
        this.id = id;
        this.nama = nama;
        this.nim = nim;
        this.nohp = nohp;
        this.jurusan = jurusan;
        this.angkatan = angkatan;
        this.timestamp = timestamp;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getNim() { return nim; }
    public void setNim(String nim) { this.nim = nim; }

    public String getNohp() { return nohp; }
    public void setNohp(String nohp) { this.nohp = nohp; }

    public String getJurusan() { return jurusan; }
    public void setJurusan(String jurusan) { this.jurusan = jurusan; }

    public int getAngkatan() { return angkatan; }
    public void setAngkatan(int angkatan) { this.angkatan = angkatan; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    /** Path absolut file foto profil hasil kamera (null bila belum ada foto). */
    public String getFotoPath() { return fotoPath; }
    public void setFotoPath(String fotoPath) { this.fotoPath = fotoPath; }
}