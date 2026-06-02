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
import com.niit.memory.data.model.VisitedLocation;
import java.util.ArrayList;
import java.util.List;

public class VisitedLocationAdapter extends RecyclerView.Adapter<VisitedLocationAdapter.ViewHolder> {

    private List<VisitedLocation> items = new ArrayList<>();
    private OnDeleteListener deleteListener;
    private OnEditListener editListener;
    private OnNavigateListener navigateListener;

    public interface OnDeleteListener { void onDelete(long id); }
    public interface OnEditListener { void onEdit(VisitedLocation loc); }
    public interface OnNavigateListener { void onNavigate(VisitedLocation loc); }

    public void setOnDeleteListener(OnDeleteListener listener) { this.deleteListener = listener; }
    public void setOnEditListener(OnEditListener listener) { this.editListener = listener; }
    public void setOnNavigateListener(OnNavigateListener listener) { this.navigateListener = listener; }

    public void submitList(List<VisitedLocation> list) {
        items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_location, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        VisitedLocation item = items.get(pos);
        Log.d("VisitedLocationAdapter", "onBindViewHolder pos=" + pos
            + " id=" + item.getId()
            + " name=" + item.getName()
            + " imageUrl=" + item.getImageUrl()
            + " lat=" + item.getLat()
            + " lng=" + item.getLng()
            + " mapX=" + item.getMapX()
            + " mapY=" + item.getMapY());

        holder.name.setText(item.getName() != null ? item.getName() : "");
        holder.province.setText(item.getProvince() != null ? item.getProvince() : "");
        holder.title.setText(item.getTitle() != null ? item.getTitle() : "");
        holder.date.setText(item.getVisitDate() != null ? item.getVisitDate() : "");

        String imgUrl = item.getImageUrl();
        if (imgUrl != null && !imgUrl.isEmpty()) {
            Log.d("VisitedLocationAdapter", "Loading image: " + imgUrl);
            holder.image.setVisibility(View.VISIBLE);
            ImageRequest req = new ImageRequest.Builder(holder.itemView.getContext())
                .data(imgUrl)
                .target(holder.image)
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_placeholder)
                .build();
            Coil.imageLoader(holder.itemView.getContext()).enqueue(req);
        } else {
            holder.image.setVisibility(View.GONE);
        }

        // Navigate via card click (image area, card itself, or nav button)
        View.OnClickListener navClick = v -> {
            Log.d("VisitedLocationAdapter", "NAV CLICK from " + v.getId() + ": id=" + item.getId()
                + " name=" + item.getName() + " lat=" + item.getLat() + " lng=" + item.getLng()
                + " mapX=" + item.getMapX() + " mapY=" + item.getMapY()
                + " navigateListener=" + (navigateListener != null));
            if (navigateListener != null) {
                navigateListener.onNavigate(item);
            } else {
                Log.w("VisitedLocationAdapter", "NAV CLICK: navigateListener is NULL!");
            }
        };

        holder.image.setOnClickListener(navClick);
        holder.itemView.setOnClickListener(navClick);
        holder.navBtn.setOnClickListener(navClick);

        holder.deleteBtn.setOnClickListener(v -> {
            Log.d("VisitedLocationAdapter", "delete click: id=" + item.getId() + " name=" + item.getName());
            if (deleteListener != null && item.getId() != null) {
                deleteListener.onDelete(item.getId());
            }
        });

        holder.editBtn.setOnClickListener(v -> {
            Log.d("VisitedLocationAdapter", "edit click: id=" + item.getId() + " name=" + item.getName()
                + " imageUrl=" + item.getImageUrl());
            if (editListener != null) editListener.onEdit(item);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, province, title, date, deleteBtn, editBtn, navBtn;
        ImageView image;
        ViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.location_image);
            name = v.findViewById(R.id.location_name);
            province = v.findViewById(R.id.location_province);
            title = v.findViewById(R.id.location_title);
            date = v.findViewById(R.id.location_date);
            deleteBtn = v.findViewById(R.id.location_delete_btn);
            editBtn = v.findViewById(R.id.location_edit_btn);
            navBtn = v.findViewById(R.id.location_nav_btn);
        }
    }
}
