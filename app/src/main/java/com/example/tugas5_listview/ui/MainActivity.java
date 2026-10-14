package com.example.tugas5_listview.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.tugas5_listview.R;
import com.example.tugas5_listview.data.Mahasiswa;
import com.example.tugas5_listview.data.MahasiswaRepository;
import com.example.tugas5_listview.util.CameraPermissionHelper;
import com.example.tugas5_listview.util.ImageUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity - Capstone Buku Kontak Mahasiswa ITS + Fitur Kamera Profil.
 *
 * Mengintegrasikan ListView, Custom ArrayAdapter + ViewHolder, Input Dialog (AlertDialog),
 * Quick Intent (Dial/SMS), Foto Profil dari Kamera (FileProvider + ACTION_IMAGE_CAPTURE),
 * dan persistensi SQLite penuh.
 */
public class MainActivity extends AppCompatActivity implements MahasiswaAdapter.OnMahasiswaItemListener {

    private static final String PREF_NAME     = "mhs_prefs";
    private static final String PREF_SORT_KEY = "sort_order";

    private ListView listView;
    private LinearLayout layoutEmpty;
    private MaterialCardView cardHero;
    private TextView tvHeroCounter;
    private ExtendedFloatingActionButton fabAdd;

    private MahasiswaAdapter adapter;
    private final List<Mahasiswa> listData = new ArrayList<>();
    private MahasiswaRepository repository;
    private SharedPreferences prefs;

    private String currentSort = "newest";
    private String currentSearch = "";

    // State kamera untuk dialog form aktif
    private String formPhotoPath = null;
    private String initialFormPhotoPath = null;
    private ShapeableImageView formImageView = null;
    private TextView formInitialView = null;
    private MaterialButton formBtnRemovePhoto = null;

    private ActivityResultLauncher<Uri> formCameraLauncher;
    private ActivityResultLauncher<String> formPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        listView      = findViewById(R.id.listView);
        layoutEmpty   = findViewById(R.id.layout_empty);
        cardHero      = findViewById(R.id.card_hero);
        tvHeroCounter = findViewById(R.id.tv_hero_counter);
        fabAdd        = findViewById(R.id.fab_add);

        prefs       = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        currentSort = prefs.getString(PREF_SORT_KEY, "newest");
        repository  = new MahasiswaRepository(this);

        adapter = new MahasiswaAdapter(this, listData, this);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Mahasiswa mhs = adapter.getItem(position);
            if (mhs != null) {
                showDetailActionDialog(mhs);
            }
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Mahasiswa mhs = adapter.getItem(position);
            if (mhs != null) {
                showDeleteConfirmationDialog(mhs);
            }
            return true;
        });

        fabAdd.setOnClickListener(v -> showFormDialog(null));

        View btnEmptyAdd = findViewById(R.id.btn_empty_add);
        if (btnEmptyAdd != null) {
            btnEmptyAdd.setOnClickListener(v -> showFormDialog(null));
        }

        registerFormCameraLaunchers();
    }

    private void registerFormCameraLaunchers() {
        // Menangkap hasil kamera untuk dialog form
        formCameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success && formPhotoPath != null) {
                updateFormPhotoUi(formPhotoPath);
                Toast.makeText(this, "Foto profil berhasil diambil dari kamera", Toast.LENGTH_SHORT).show();
            } else {
                // Pengambilan foto dibatalkan: bersihkan file kosong baru
                if (formPhotoPath != null && !formPhotoPath.equals(initialFormPhotoPath)) {
                    ImageUtils.deleteQuietly(formPhotoPath);
                }
                formPhotoPath = initialFormPhotoPath;
                updateFormPhotoUi(formPhotoPath);
            }
        });

        formPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) {
                launchFormCamera();
            } else if (!CameraPermissionHelper.shouldShowRationale(this)) {
                CameraPermissionHelper.showSettingsDialog(this);
            } else {
                Toast.makeText(this, "Izin kamera diperlukan untuk mengambil foto profil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        repository.open();
        muatDataDariDatabase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        repository.open();
        muatDataDariDatabase();
    }

    @Override
    protected void onStop() {
        super.onStop();
        repository.close();
    }

    @Override
    public void onItemClick(Mahasiswa mhs) {
        showDetailActionDialog(mhs);
    }

    @Override
    public void onEditClick(Mahasiswa mhs) {
        showFormDialog(mhs);
    }

    @Override
    public void onQuickCall(Mahasiswa mhs) {
        makePhoneCall(mhs);
    }

    @Override
    public void onItemLongClick(Mahasiswa mhs) {
        showDeleteConfirmationDialog(mhs);
    }

    private void muatDataDariDatabase() {
        List<Mahasiswa> result;
        if (currentSearch.isEmpty()) {
            result = repository.getAll(currentSort);
        } else {
            result = repository.search(currentSearch, currentSort);
        }

        adapter.clear();
        if (result != null && !result.isEmpty()) {
            adapter.addAll(result);
        }
        adapter.notifyDataSetChanged();

        if (adapter.isEmpty()) {
            listView.setVisibility(View.GONE);
            if (cardHero != null) cardHero.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle("0 Kontak");
            }
        } else {
            listView.setVisibility(View.VISIBLE);
            if (cardHero != null) cardHero.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
            String countText = adapter.getCount() + " Kontak Mahasiswa";
            if (tvHeroCounter != null) {
                tvHeroCounter.setText(countText);
            }
            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle(getString(R.string.institution_name));
            }
        }
    }

    // ---------------------------------------------------------------- Dialog Form Input + Foto Kamera

    public void showFormDialog(final Mahasiswa existingMhs) {
        final boolean isEdit = (existingMhs != null);

        View viewInput = LayoutInflater.from(this).inflate(R.layout.dialog_form_mahasiswa, null);
        final TextView tvSubtitle         = viewInput.findViewById(R.id.tv_dialog_subtitle);
        final TextInputLayout tilNama     = viewInput.findViewById(R.id.til_dialog_nama);
        final TextInputLayout tilNim      = viewInput.findViewById(R.id.til_dialog_nim);
        final TextInputLayout tilNoHp     = viewInput.findViewById(R.id.til_dialog_nohp);
        final TextInputLayout tilJurusan  = viewInput.findViewById(R.id.til_dialog_jurusan);
        final TextInputLayout tilAngkatan = viewInput.findViewById(R.id.til_dialog_angkatan);

        final EditText etNama     = viewInput.findViewById(R.id.et_dialog_nama);
        final EditText etNim      = viewInput.findViewById(R.id.et_dialog_nim);
        final EditText etNoHp     = viewInput.findViewById(R.id.et_dialog_nohp);
        final EditText etJurusan  = viewInput.findViewById(R.id.et_dialog_jurusan);
        final EditText etAngkatan = viewInput.findViewById(R.id.et_dialog_angkatan);

        // Bind widget foto form
        formImageView       = viewInput.findViewById(R.id.iv_form_foto);
        formInitialView     = viewInput.findViewById(R.id.tv_form_avatar_initial);
        formBtnRemovePhoto  = viewInput.findViewById(R.id.btn_form_remove_photo);
        MaterialButton btnTake = viewInput.findViewById(R.id.btn_form_take_photo);

        initialFormPhotoPath = isEdit ? existingMhs.getFotoPath() : null;
        formPhotoPath        = initialFormPhotoPath;
        updateFormPhotoUi(formPhotoPath);

        btnTake.setOnClickListener(v -> onFormTakePhotoClicked());
        formBtnRemovePhoto.setOnClickListener(v -> {
            // Pengguna melepas foto profil
            if (formPhotoPath != null && !formPhotoPath.equals(initialFormPhotoPath)) {
                ImageUtils.deleteQuietly(formPhotoPath);
            }
            formPhotoPath = null;
            updateFormPhotoUi(null);
        });

        addClearErrorWatcher(etNama, tilNama);
        addClearErrorWatcher(etNim, tilNim);
        addClearErrorWatcher(etNoHp, tilNoHp);
        addClearErrorWatcher(etJurusan, tilJurusan);
        addClearErrorWatcher(etAngkatan, tilAngkatan);

        etNama.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                if (formPhotoPath == null && formInitialView != null) {
                    String str = s != null ? s.toString().trim() : "";
                    formInitialView.setText(!str.isEmpty() ? str.substring(0, 1).toUpperCase() : "M");
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        if (isEdit) {
            tvSubtitle.setText("Perbarui informasi kontak mahasiswa di bawah ini lalu klik Simpan.");
            etNama.setText(existingMhs.getNama());
            etNim.setText(existingMhs.getNim());
            etNoHp.setText(existingMhs.getNohp());
            etJurusan.setText(existingMhs.getJurusan());
            etAngkatan.setText(String.valueOf(existingMhs.getAngkatan()));
        } else {
            tvSubtitle.setText("Masukkan informasi kontak mahasiswa ke database SQLite ITS.");
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(isEdit ? R.string.title_edit_mhs : R.string.title_add_mhs)
                .setView(viewInput)
                .setCancelable(false)
                .setPositiveButton(R.string.btn_save, null)
                .setNegativeButton(R.string.dialog_no, (d, w) -> {
                    // Batalkan: hapus foto baru jika belum disimpan
                    if (formPhotoPath != null && !formPhotoPath.equals(initialFormPhotoPath)) {
                        ImageUtils.deleteQuietly(formPhotoPath);
                    }
                    d.dismiss();
                })
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String nama     = etNama.getText() != null ? etNama.getText().toString().trim() : "";
                String nim      = etNim.getText() != null ? etNim.getText().toString().trim() : "";
                String nohp     = etNoHp.getText() != null ? etNoHp.getText().toString().trim() : "";
                String jurusan  = etJurusan.getText() != null ? etJurusan.getText().toString().trim() : "";
                String angkStr  = etAngkatan.getText() != null ? etAngkatan.getText().toString().trim() : "";

                boolean valid = true;
                if (nama.isEmpty()) {
                    tilNama.setError(getString(R.string.error_nama_empty));
                    valid = false;
                }
                if (nim.isEmpty()) {
                    tilNim.setError(getString(R.string.error_nim_empty));
                    valid = false;
                }
                if (nohp.isEmpty()) {
                    tilNoHp.setError(getString(R.string.error_nohp_empty));
                    valid = false;
                }
                if (jurusan.isEmpty()) {
                    tilJurusan.setError(getString(R.string.error_jurusan_empty));
                    valid = false;
                }
                if (angkStr.isEmpty()) {
                    tilAngkatan.setError(getString(R.string.error_angkatan_empty));
                    valid = false;
                }

                if (!valid) return;

                int angkatan;
                try {
                    angkatan = Integer.parseInt(angkStr);
                    if (angkatan < 1950 || angkatan > 2100) {
                        tilAngkatan.setError(getString(R.string.error_angkatan_invalid));
                        return;
                    }
                } catch (NumberFormatException e) {
                    tilAngkatan.setError(getString(R.string.error_angkatan_invalid));
                    return;
                }

                // Jika foto lama diganti, hapus file foto lama
                if (initialFormPhotoPath != null && !initialFormPhotoPath.equals(formPhotoPath)) {
                    ImageUtils.deleteQuietly(initialFormPhotoPath);
                }

                if (isEdit) {
                    int rows = repository.update(existingMhs.getId(), nama, nim, nohp, jurusan, angkatan, formPhotoPath);
                    if (rows > 0) {
                        Toast.makeText(MainActivity.this, R.string.toast_update_success, Toast.LENGTH_SHORT).show();
                        muatDataDariDatabase();
                        dialog.dismiss();
                    } else if (rows == -1) {
                        tilNim.setError("NRP/NIM sudah terdaftar untuk mahasiswa lain!");
                        Toast.makeText(MainActivity.this, "Gagal memperbarui: NRP sudah digunakan", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, R.string.toast_update_failed, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    long newId = repository.insert(nama, nim, nohp, jurusan, angkatan, formPhotoPath);
                    if (newId != -1) {
                        Toast.makeText(MainActivity.this, R.string.toast_save_success, Toast.LENGTH_SHORT).show();
                        muatDataDariDatabase();
                        dialog.dismiss();
                    } else {
                        tilNim.setError("NRP/NIM sudah terdaftar!");
                        Toast.makeText(MainActivity.this, R.string.toast_save_failed, Toast.LENGTH_LONG).show();
                    }
                }
            });
        });

        dialog.show();
    }

    private void updateFormPhotoUi(String path) {
        if (formImageView == null || formInitialView == null) return;
        if (ImageUtils.isValidPhoto(path)) {
            Bitmap bm = ImageUtils.decodeSampled(path, 192, 192);
            if (bm != null) {
                formImageView.setImageBitmap(bm);
                formImageView.setVisibility(View.VISIBLE);
                formInitialView.setVisibility(View.GONE);
                if (formBtnRemovePhoto != null) formBtnRemovePhoto.setVisibility(View.VISIBLE);
                return;
            }
        }
        formImageView.setVisibility(View.GONE);
        formInitialView.setVisibility(View.VISIBLE);
        if (formBtnRemovePhoto != null) formBtnRemovePhoto.setVisibility(View.GONE);
    }

    private void onFormTakePhotoClicked() {
        if (CameraPermissionHelper.hasCameraPermission(this)) {
            launchFormCamera();
        } else if (CameraPermissionHelper.shouldShowRationale(this)) {
            CameraPermissionHelper.showRationaleDialog(this, () ->
                    formPermissionLauncher.launch(Manifest.permission.CAMERA));
        } else {
            formPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchFormCamera() {
        try {
            File photo = ImageUtils.createImageFile(this);
            // hapus temporary sebelumnya bila ada
            if (formPhotoPath != null && !formPhotoPath.equals(initialFormPhotoPath)) {
                ImageUtils.deleteQuietly(formPhotoPath);
            }
            formPhotoPath = photo.getAbsolutePath();
            Uri uri = ImageUtils.getUriForFile(this, photo);
            formCameraLauncher.launch(uri);
        } catch (IOException e) {
            Toast.makeText(this, "Gagal menyiapkan file foto: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ---------------------------------------------------------------- Dialog Profil Detail

    public void showDetailActionDialog(final Mahasiswa mhs) {
        View viewDetail = LayoutInflater.from(this).inflate(R.layout.dialog_detail_mahasiswa, null);

        TextView tvAvatar            = viewDetail.findViewById(R.id.tv_detail_avatar);
        ShapeableImageView ivDetailFoto = viewDetail.findViewById(R.id.iv_detail_foto);
        TextView tvNama              = viewDetail.findViewById(R.id.tv_detail_nama);
        TextView tvNim               = viewDetail.findViewById(R.id.tv_detail_nim);
        TextView tvSub               = viewDetail.findViewById(R.id.tv_detail_sub);
        TextView tvNoHp              = viewDetail.findViewById(R.id.tv_detail_nohp);

        // Tampilkan foto kamera jika ada, atau monogram fallback
        if (ImageUtils.isValidPhoto(mhs.getFotoPath())) {
            Bitmap bm = ImageUtils.decodeSampled(mhs.getFotoPath(), 256, 256);
            if (bm != null) {
                ivDetailFoto.setImageBitmap(bm);
                ivDetailFoto.setVisibility(View.VISIBLE);
                tvAvatar.setVisibility(View.GONE);
            } else {
                ivDetailFoto.setVisibility(View.GONE);
                tvAvatar.setVisibility(View.VISIBLE);
            }
        } else {
            ivDetailFoto.setVisibility(View.GONE);
            tvAvatar.setVisibility(View.VISIBLE);
        }

        if (mhs.getNama() != null && !mhs.getNama().trim().isEmpty()) {
            tvAvatar.setText(mhs.getNama().trim().substring(0, 1).toUpperCase());
        }
        tvNama.setText(mhs.getNama());
        tvNim.setText("NRP: " + mhs.getNim());
        tvSub.setText(mhs.getJurusan() + " â€¢ Angkatan " + mhs.getAngkatan());
        tvNoHp.setText(mhs.getNohp());

        AlertDialog detailDialog = new MaterialAlertDialogBuilder(this)
                .setView(viewDetail)
                .create();

        viewDetail.findViewById(R.id.btn_action_call).setOnClickListener(v -> {
            detailDialog.dismiss();
            makePhoneCall(mhs);
        });

        viewDetail.findViewById(R.id.btn_action_message).setOnClickListener(v -> {
            detailDialog.dismiss();
            sendSmsMessage(mhs);
        });

        viewDetail.findViewById(R.id.btn_action_edit).setOnClickListener(v -> {
            detailDialog.dismiss();
            showFormDialog(mhs);
        });

        viewDetail.findViewById(R.id.btn_action_delete).setOnClickListener(v -> {
            detailDialog.dismiss();
            showDeleteConfirmationDialog(mhs);
        });

        detailDialog.show();
    }

    // ---------------------------------------------------------------- Dialog Hapus Kontak

    public void showDeleteConfirmationDialog(final Mahasiswa mhs) {
        String msg = getString(R.string.dialog_delete_message, mhs.getNama(), mhs.getNim());
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(msg)
                .setPositiveButton(R.string.dialog_yes, (d, w) -> {
                    // Hapus file foto dari disk bila ada
                    ImageUtils.deleteQuietly(mhs.getFotoPath());

                    int rows = repository.delete(mhs.getId());
                    if (rows > 0) {
                        Toast.makeText(this, R.string.toast_delete_success, Toast.LENGTH_SHORT).show();
                        muatDataDariDatabase();
                    } else {
                        Toast.makeText(this, R.string.toast_delete_failed, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.dialog_no, (d, w) -> d.dismiss())
                .show();
    }

    private void makePhoneCall(Mahasiswa mhs) {
        if (mhs != null && mhs.getNohp() != null && !mhs.getNohp().isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mhs.getNohp()));
            startActivity(intent);
        } else {
            Toast.makeText(this, "Nomor telepon tidak valid", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSmsMessage(Mahasiswa mhs) {
        if (mhs != null && mhs.getNohp() != null && !mhs.getNohp().isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + mhs.getNohp()));
            startActivity(intent);
        } else {
            Toast.makeText(this, "Nomor telepon tidak valid", Toast.LENGTH_SHORT).show();
        }
    }

    private void addClearErrorWatcher(EditText et, TextInputLayout til) {
        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                if (til.getError() != null) til.setError(null);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            SearchView sv = (SearchView) searchItem.getActionView();
            if (sv != null) {
                sv.setQueryHint("Cari nama, NRP, no HP...");
                sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String q) {
                        currentSearch = q != null ? q.trim() : "";
                        muatDataDariDatabase();
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String text) {
                        currentSearch = text != null ? text.trim() : "";
                        muatDataDariDatabase();
                        return true;
                    }
                });
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sort_newest) {
            setSortOrder("newest");
            return true;
        } else if (id == R.id.action_sort_oldest) {
            setSortOrder("oldest");
            return true;
        } else if (id == R.id.action_sort_name_asc) {
            setSortOrder("name_asc");
            return true;
        } else if (id == R.id.action_sort_nim_asc) {
            setSortOrder("nim_asc");
            return true;
        } else if (id == R.id.action_camera_lab) {
            // Demo interaktif Materi Android Kamera
            startActivity(new Intent(this, CameraLabActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setSortOrder(String sort) {
        currentSort = sort;
        prefs.edit().putString(PREF_SORT_KEY, sort).apply();
        muatDataDariDatabase();
    }
}