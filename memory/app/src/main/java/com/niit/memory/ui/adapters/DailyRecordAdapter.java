package com.niit.memory.ui.adapters;

import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import coil.Coil;
import coil.request.ImageRequest;
import com.niit.memory.R;
import com.niit.memory.data.model.DailyRecord;
import java.util.ArrayList;
import java.util.List;

public class DailyRecordAdapter extends RecyclerView.Adapter<DailyRecordAdapter.ViewHolder> {

    private List<DailyRecord> items = new ArrayList<>();
    private final OnDeleteListener deleteListener;
    private final OnEditListener editListener;
    private final OnClickListener clickListener;

    public interface OnDeleteListener { void onDelete(long id); }
    public interface OnEditListener { void onEdit(DailyRecord record); }
    public interface OnClickListener { void onClick(DailyRecord record); }

    public DailyRecordAdapter(OnDeleteListener delListener, OnEditListener edtListener,
                              OnClickListener clkListener) {
        this.deleteListener = delListener;
        this.editListener = edtListener;
        this.clickListener = clkListener;
    }

    public void submitList(List<DailyRecord> list) {
        items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_daily_record, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        DailyRecord item = items.get(pos);
        holder.author.setText(item.getAuthor() != null ? item.getAuthor() : "");
        holder.date.setText(item.getRecordDate() != null ? item.getRecordDate() : "");
        holder.title.setText(item.getTitle() != null ? item.getTitle() : "");
        holder.content.setText(item.getContent() != null ? item.getContent() : "");
        holder.mood.setText((item.getMoodIcon() != null ? item.getMoodIcon() + " " : "")
            + (item.getMood() != null ? item.getMood() : ""));
        holder.location.setText(item.getLocation() != null ? item.getLocation() : "");

        // Load images
        holder.imagesContainer.removeAllViews();
        holder.imagesContainer.setVisibility(View.GONE);
        String imageUrls = item.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            holder.imagesContainer.setVisibility(View.VISIBLE);
            for (String url : imageUrls.split(",")) {
                String trimmed = url.trim();
                if (!trimmed.isEmpty()) {
                    ImageView iv = new ImageView(holder.itemView.getContext());
                    int size = (int) (100 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
                    lp.setMargins(0, 0, 8, 0);
                    iv.setLayoutParams(lp);
                    iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    iv.setBackgroundResource(R.drawable.image_placeholder);
                    ImageRequest req = new ImageRequest.Builder(holder.itemView.getContext())
                        .data(trimmed)
                        .target(iv)
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_placeholder)
                        .build();
                    Coil.imageLoader(holder.itemView.getContext()).enqueue(req);

                    final String imgUrl = trimmed;
                    iv.setOnClickListener(v -> {
                        AlertDialog.Builder b = new AlertDialog.Builder(holder.itemView.getContext());
                        ImageView full = new ImageView(holder.itemView.getContext());
                        full.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        full.setBackgroundColor(0xFF000000);
                        ImageRequest fullReq = new ImageRequest.Builder(holder.itemView.getContext())
                            .data(imgUrl)
                            .target(full)
                            .build();
                        Coil.imageLoader(holder.itemView.getContext()).enqueue(fullReq);
                        AlertDialog dlg = b.setView(full).create();
                        dlg.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT);
                        dlg.getWindow().setBackgroundDrawableResource(android.R.color.black);
                        full.setOnClickListener(ff -> dlg.dismiss());
                        dlg.show();
                    });
                    holder.imagesContainer.addView(iv);
                }
            }
        }

        // Edit button
        holder.editBtn.setOnClickListener(v -> {
            if (editListener != null) editListener.onEdit(item);
        });

        // Delete button
        holder.deleteBtn.setOnClickListener(v -> {
            if (deleteListener != null && item.getId() != null) {
                deleteListener.onDelete(item.getId());
            }
        });

        // Click to navigate to detail page
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onClick(item);
        });

        // Long press delete (keep as alternative)
        holder.itemView.setOnLongClickListener(v -> {
            if (deleteListener != null && item.getId() != null) {
                deleteListener.onDelete(item.getId());
            }
            return true;
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView author, date, title, content, mood, location, editBtn, deleteBtn;
        LinearLayout imagesContainer;
        ViewHolder(View v) {
            super(v);
            author = v.findViewById(R.id.record_author);
            date = v.findViewById(R.id.record_date);
            title = v.findViewById(R.id.record_title);
            content = v.findViewById(R.id.record_content);
            mood = v.findViewById(R.id.record_mood);
            location = v.findViewById(R.id.record_location);
            editBtn = v.findViewById(R.id.record_edit_btn);
            deleteBtn = v.findViewById(R.id.record_delete_btn);
            imagesContainer = v.findViewById(R.id.record_images_container);
        }
    }
}
