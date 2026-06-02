package com.niit.memory.ui.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import coil.Coil;
import coil.request.ImageRequest;
import com.niit.memory.R;
import com.niit.memory.data.model.MemoryAlbum;
import java.util.ArrayList;
import java.util.List;

public class MemoryAlbumAdapter extends RecyclerView.Adapter<MemoryAlbumAdapter.ViewHolder> {

    private List<MemoryAlbum> items = new ArrayList<>();
    private final OnClickListener clickListener;
    private final OnEditListener editListener;
    private final OnDeleteListener deleteListener;

    public interface OnClickListener { void onClick(long id); }
    public interface OnEditListener { void onEdit(MemoryAlbum album); }
    public interface OnDeleteListener { void onDelete(long id); }

    public MemoryAlbumAdapter(OnClickListener click, OnEditListener edit, OnDeleteListener del) {
        this.clickListener = click;
        this.editListener = edit;
        this.deleteListener = del;
    }

    public void submitList(List<MemoryAlbum> list) {
        items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_memory_album, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        MemoryAlbum item = items.get(pos);
        Log.d("MemoryAlbumAdapter", "onBindViewHolder pos=" + pos
            + " id=" + item.getId()
            + " location=" + item.getLocation()
            + " coverUrl=" + item.getCoverUrl()
            + " photoUrls=" + (item.getPhotoUrls() != null ? item.getPhotoUrls().length() + " chars" : "null"));

        holder.location.setText(item.getLocation());
        holder.date.setText(item.getAlbumDate());

        String coverUrl = item.getCoverUrl();
        // Fallback: if no cover, use first photo from photoUrls
        if ((coverUrl == null || coverUrl.isEmpty()) && item.getPhotoUrls() != null && !item.getPhotoUrls().isEmpty()) {
            String firstPhoto = item.getPhotoUrls().split(",")[0].trim();
            if (!firstPhoto.isEmpty()) coverUrl = firstPhoto;
        }
        if (coverUrl != null && !coverUrl.isEmpty()) {
            ImageRequest req = new ImageRequest.Builder(holder.itemView.getContext())
                .data(coverUrl)
                .target(holder.cover)
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_placeholder)
                .build();
            Coil.imageLoader(holder.itemView.getContext()).enqueue(req);
        } else {
            holder.cover.setImageResource(R.drawable.image_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null && item.getId() != null) clickListener.onClick(item.getId());
        });

        holder.editBtn.setOnClickListener(v -> {
            if (editListener != null) editListener.onEdit(item);
        });

        holder.deleteBtn.setOnClickListener(v -> {
            if (deleteListener != null && item.getId() != null) deleteListener.onDelete(item.getId());
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView location, date, editBtn, deleteBtn;
        ViewHolder(View v) {
            super(v);
            cover = v.findViewById(R.id.album_cover);
            location = v.findViewById(R.id.album_location);
            date = v.findViewById(R.id.album_date);
            editBtn = v.findViewById(R.id.album_edit_btn);
            deleteBtn = v.findViewById(R.id.album_delete_btn);
        }
    }
}
