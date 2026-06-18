package com.niit.memory.ui.adapters;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import coil.Coil;
import coil.request.ImageRequest;
import com.niit.memory.R;
import com.niit.memory.data.model.DailyRecord;
import com.niit.memory.util.ImageSaveUtil;
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

        // Load images — only first 2, +N for the rest
        holder.imagesContainer.removeAllViews();
        holder.imagesContainer.setVisibility(View.GONE);
        String imageUrls = item.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            List<String> validUrls = new ArrayList<>();
            for (String url : imageUrls.split(",")) {
                String t = url.trim();
                if (!t.isEmpty()) validUrls.add(t);
            }
            int total = validUrls.size();
            if (total > 0) {
                holder.imagesContainer.setVisibility(View.VISIBLE);
                Context ctx = holder.itemView.getContext();
                int size = (int) (100 * ctx.getResources().getDisplayMetrics().density);

                int showCount = Math.min(total, 2);
                for (int i = 0; i < showCount; i++) {
                    final String imgUrl = validUrls.get(i);
                    ImageView iv = new ImageView(ctx);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
                    lp.setMargins(0, 0, 8, 0);
                    iv.setLayoutParams(lp);
                    iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    iv.setBackgroundResource(R.drawable.image_placeholder);
                    Coil.imageLoader(ctx).enqueue(new ImageRequest.Builder(ctx)
                        .data(imgUrl).target(iv)
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_placeholder).build());

                    iv.setOnClickListener(v -> showFullscreenDialog(ctx, imgUrl));
                    holder.imagesContainer.addView(iv);
                }

                // Show +N overlay on 3rd image position if more than 2
                if (total > 2) {
                    int remaining = total - 2;
                    FrameLayout fl = new FrameLayout(ctx);
                    LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(size, size);
                    flp.setMargins(0, 0, 8, 0);
                    fl.setLayoutParams(flp);

                    ImageView bg = new ImageView(ctx);
                    bg.setLayoutParams(new FrameLayout.LayoutParams(size, size));
                    bg.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    bg.setBackgroundResource(R.drawable.image_placeholder);
                    bg.setAlpha(0.35f);
                    Coil.imageLoader(ctx).enqueue(new ImageRequest.Builder(ctx)
                        .data(validUrls.get(2)).target(bg)
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_placeholder).build());
                    fl.addView(bg);

                    TextView plusN = new TextView(ctx);
                    plusN.setText("+" + remaining);
                    plusN.setTextColor(0xFF000000);
                    plusN.setTextSize(22);
                    plusN.getPaint().setFakeBoldText(true);
                    plusN.setGravity(Gravity.CENTER);
                    FrameLayout.LayoutParams tp = new FrameLayout.LayoutParams(size, size);
                    fl.addView(plusN, tp);

                    fl.setOnClickListener(v -> {
                        if (clickListener != null) clickListener.onClick(item);
                    });
                    holder.imagesContainer.addView(fl);
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

    private void showFullscreenDialog(Context ctx, String imgUrl) {
        AlertDialog.Builder b = new AlertDialog.Builder(ctx);
        ImageView full = new ImageView(ctx);
        full.setScaleType(ImageView.ScaleType.FIT_CENTER);
        full.setBackgroundColor(0xFF000000);
        Coil.imageLoader(ctx).enqueue(new ImageRequest.Builder(ctx)
            .data(imgUrl).target(full).allowHardware(false).build());
        AlertDialog dlg = b.setView(full).create();
        dlg.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);
        dlg.getWindow().setBackgroundDrawableResource(android.R.color.black);
        full.setOnClickListener(ff -> dlg.dismiss());
        full.setOnLongClickListener(v -> {
            ImageSaveUtil.saveViewToGallery(ctx, full);
            return true;
        });
        dlg.show();
    }

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
