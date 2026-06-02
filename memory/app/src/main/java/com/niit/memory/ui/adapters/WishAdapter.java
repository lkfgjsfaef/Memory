package com.niit.memory.ui.adapters;

import android.graphics.Paint;
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
import com.niit.memory.data.model.Wish;
import java.util.ArrayList;
import java.util.List;

public class WishAdapter extends RecyclerView.Adapter<WishAdapter.ViewHolder> {

    private List<Wish> items = new ArrayList<>();
    private final OnDeleteListener deleteListener;
    private final OnToggleListener toggleListener;
    private final OnEditListener editListener;

    public interface OnDeleteListener { void onDelete(long id); }
    public interface OnToggleListener { void onToggle(long id, String status); }
    public interface OnEditListener { void onEdit(Wish wish); }

    public WishAdapter(OnDeleteListener del, OnToggleListener tog, OnEditListener edt) {
        this.deleteListener = del;
        this.toggleListener = tog;
        this.editListener = edt;
    }

    public void submitList(List<Wish> list) {
        items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_wish, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        Wish item = items.get(pos);
        String status = item.getStatus();
        boolean isCompleted = "completed".equals(status);

        // Avatar
        holder.avatar.setText(item.getUserId() != null && item.getUserId() == 2 ? "👧" : "👦");

        // Title with strikethrough for completed
        holder.title.setText(item.getTitle());
        if (isCompleted) {
            holder.title.setPaintFlags(holder.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.title.setTextColor(0xFFC8BFB5);
            holder.itemView.setAlpha(0.75f);
        } else {
            holder.title.setPaintFlags(holder.title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.title.setTextColor(0xFF3D3028);
            holder.itemView.setAlpha(1.0f);
        }

        holder.description.setText(item.getDescription());
        holder.author.setText(item.getAuthor());

        // Category badge with colors
        String cat = item.getCategory();
        holder.category.setText(cat != null ? cat : "");
        holder.category.setBackground(getCategoryBg(cat));

        // Date
        holder.date.setText("发起于: " + (item.getStartDate() != null ? item.getStartDate() : ""));

        // Status badge
        if (isCompleted) {
            holder.status.setText("已完成 ✨");
            holder.status.setTextColor(0xFF81C784);
        } else {
            holder.status.setText("点我标记完成~");
            holder.status.setTextColor(0xFFD4A853);
        }

        // Images — always show container, display hint when no images
        holder.imagesContainer.removeAllViews();
        String imageUrls = item.getImageUrls();
        android.util.Log.d("WishAdapter", "onBind pos=" + pos + " id=" + item.getId()
            + " imageUrls='" + imageUrls + "'");
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String url : imageUrls.split(",")) {
                String trimmed = url.trim();
                if (!trimmed.isEmpty()) {
                    ImageView iv = new ImageView(holder.itemView.getContext());
                    int size = (int) (80 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
                    lp.setMargins(0, 0, 8, 0);
                    iv.setLayoutParams(lp);
                    iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    ImageRequest req = new ImageRequest.Builder(holder.itemView.getContext())
                        .data(trimmed)
                        .target(iv)
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_placeholder)
                        .build();
                    Coil.imageLoader(holder.itemView.getContext()).enqueue(req);
                    holder.imagesContainer.addView(iv);
                }
            }
        } else {
            // Show hint that images can be added via edit — tap to open edit
            TextView hint = new TextView(holder.itemView.getContext());
            hint.setText("📷 点击添加配图");
            hint.setTextColor(0xFFBBB0A5);
            hint.setTextSize(12);
            hint.setPadding(0, 4, 0, 4);
            hint.setClickable(true);
            hint.setFocusable(true);
            android.util.TypedValue outValue = new android.util.TypedValue();
            holder.itemView.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            hint.setBackgroundResource(outValue.resourceId);
            hint.setOnClickListener(v -> {
                android.util.Log.d("WishAdapter", "hint click: id=" + item.getId() + " editListener=" + (editListener != null));
                if (editListener != null) editListener.onEdit(item);
            });
            holder.imagesContainer.addView(hint);
            // Also make the container clickable as fallback
            holder.imagesContainer.setClickable(true);
            holder.imagesContainer.setOnClickListener(v -> {
                android.util.Log.d("WishAdapter", "container click: id=" + item.getId() + " editListener=" + (editListener != null));
                if (editListener != null) editListener.onEdit(item);
            });
        }
        holder.imagesContainer.setVisibility(View.VISIBLE);

        // Click listeners
        holder.status.setOnClickListener(v -> {
            if (toggleListener != null && item.getId() != null && !isCompleted) {
                toggleListener.onToggle(item.getId(), "completed");
            }
        });

        holder.editBtn.setOnClickListener(v -> {
            if (editListener != null) editListener.onEdit(item);
        });

        holder.deleteBtn.setOnClickListener(v -> {
            if (deleteListener != null && item.getId() != null) {
                deleteListener.onDelete(item.getId());
            }
        });
    }

    private android.graphics.drawable.GradientDrawable getCategoryBg(String cat) {
        int[] colors;
        if ("旅行计划".equals(cat)) {
            colors = new int[]{0xFF81D4FA, 0xFFB3E5FC};
        } else if ("生活目标".equals(cat)) {
            colors = new int[]{0xFFA5D6A7, 0xFFC8E6C9};
        } else {
            colors = new int[]{0xFFCE93D8, 0xFFE1BEE7};
        }
        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setColors(colors);
        gd.setCornerRadius(24);
        gd.setOrientation(android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT);
        return gd;
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView avatar, title, status, description, category, author, date;
        TextView editBtn, deleteBtn;
        LinearLayout imagesContainer;
        ViewHolder(View v) {
            super(v);
            avatar = v.findViewById(R.id.wish_avatar);
            title = v.findViewById(R.id.wish_title);
            status = v.findViewById(R.id.wish_status);
            description = v.findViewById(R.id.wish_description);
            category = v.findViewById(R.id.wish_category);
            author = v.findViewById(R.id.wish_author);
            date = v.findViewById(R.id.wish_date);
            editBtn = v.findViewById(R.id.wish_edit_btn);
            deleteBtn = v.findViewById(R.id.wish_delete_btn);
            imagesContainer = v.findViewById(R.id.wish_images_container);
        }
    }
}
