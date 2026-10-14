package com.example.tugas5_listview.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tugas5_listview.R;
import com.example.tugas5_listview.data.Mahasiswa;
import com.example.tugas5_listview.util.ImageUtils;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

/**
 * MahasiswaAdapter - Custom ArrayAdapter dengan ViewHolder Pattern (P05) + Foto Kamera (Materi Kamera).
 *
 * Mengoptimalkan daur ulang baris ListView (View Recycling) dengan cache findViewById() via setTag()/getTag().
 * Menampilkan thumbnail foto kamera jika ada, atau monogram inisial nama jika belum berfoto.
 */
public class MahasiswaAdapter extends ArrayAdapter<Mahasiswa> {

    public interface OnMahasiswaItemListener {
        void onItemClick(Mahasiswa mhs);
        void onEditClick(Mahasiswa mhs);
        void onQuickCall(Mahasiswa mhs);
        void onItemLongClick(Mahasiswa mhs);
    }

    private static class ViewHolder {
        TextView tvAvatarInitial;
        ShapeableImageView ivFoto;
        TextView tvNama;
        TextView tvNim;
        TextView tvNoHp;
        TextView tvJurusan;
        TextView tvAngkatan;
        ImageButton btnQuickEdit;
        ImageButton btnQuickCall;
    }

    private final OnMahasiswaItemListener listener;

    public MahasiswaAdapter(@NonNull Context context, @NonNull List<Mahasiswa> objects, OnMahasiswaItemListener listener) {
        super(context, 0, objects);
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Mahasiswa m = getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_mahasiswa, parent, false);

            holder = new ViewHolder();
            holder.tvAvatarInitial = convertView.findViewById(R.id.tv_avatar_initial);
            holder.ivFoto          = convertView.findViewById(R.id.iv_mhs_foto);
            holder.tvNama          = convertView.findViewById(R.id.tv_mhs_nama);
            holder.tvNim           = convertView.findViewById(R.id.tv_mhs_nim);
            holder.tvNoHp          = convertView.findViewById(R.id.tv_mhs_nohp);
            holder.tvJurusan       = convertView.findViewById(R.id.tv_mhs_jurusan);
            holder.tvAngkatan      = convertView.findViewById(R.id.tv_mhs_angkatan);
            holder.btnQuickEdit    = convertView.findViewById(R.id.btn_quick_edit);
            holder.btnQuickCall    = convertView.findViewById(R.id.btn_quick_call);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (m != null) {
            holder.tvNama.setText(m.getNama());
            holder.tvNim.setText(m.getNim());
            holder.tvNoHp.setText(m.getNohp());
            holder.tvJurusan.setText(m.getJurusan());
            holder.tvAngkatan.setText("Angkatan " + m.getAngkatan());

            // Tampilkan foto dari kamera jika tersedia; jika tidak, tampilkan monogram inisial
            if (ImageUtils.isValidPhoto(m.getFotoPath())) {
                Bitmap thumb = ImageUtils.loadThumbnail(m.getFotoPath(), 144);
                if (thumb != null) {
                    holder.ivFoto.setImageBitmap(thumb);
                    holder.ivFoto.setVisibility(View.VISIBLE);
                    holder.tvAvatarInitial.setVisibility(View.GONE);
                } else {
                    holder.ivFoto.setVisibility(View.GONE);
                    holder.tvAvatarInitial.setVisibility(View.VISIBLE);
                }
            } else {
                holder.ivFoto.setVisibility(View.GONE);
                holder.tvAvatarInitial.setVisibility(View.VISIBLE);
            }

            // Monogram inisial nama
            if (m.getNama() != null && !m.getNama().trim().isEmpty()) {
                holder.tvAvatarInitial.setText(m.getNama().trim().substring(0, 1).toUpperCase());
            } else {
                holder.tvAvatarInitial.setText("M");
            }

            convertView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(m);
            });

            convertView.setOnLongClickListener(v -> {
                if (listener != null) listener.onItemLongClick(m);
                return true;
            });

            holder.btnQuickEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(m);
            });

            holder.btnQuickCall.setOnClickListener(v -> {
                if (listener != null) listener.onQuickCall(m);
            });
        }

        return convertView;
    }
}