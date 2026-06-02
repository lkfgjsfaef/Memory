package com.niit.memory.ui.adapters;

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
import com.niit.memory.data.model.MemoryMoment;
import com.niit.memory.data.model.TimelineGroup;
import java.util.ArrayList;
import java.util.List;

public class MomentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private final List<Object> items = new ArrayList<>();
    private final OnClickListener clickListener;
    private final OnEditListener editListener;
    private final OnDeleteListener deleteListener;

    public interface OnClickListener { void onClick(long id); }
    public interface OnEditListener { void onEdit(MemoryMoment moment); }
    public interface OnDeleteListener { void onDelete(long id); }

    public MomentAdapter(OnClickListener click, OnEditListener edit, OnDeleteListener del) {
        this.clickListener = click;
        this.editListener = edit;
        this.deleteListener = del;
    }

    public void submitGroups(List<TimelineGroup> groups) {
        items.clear();
        if (groups != null) {
            for (TimelineGroup g : groups) {
                if (g.getMoments() != null && !g.getMoments().isEmpty()) {
                    items.add(g);
                    items.addAll(g.getMoments());
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof TimelineGroup ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timeline_header, parent, false);
            return new HeaderViewHolder(v);
        }
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_moment, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
        if (holder instanceof HeaderViewHolder) {
            TimelineGroup g = (TimelineGroup) items.get(pos);
            HeaderViewHolder h = (HeaderViewHolder) holder;
            h.label.setText(g.getLabel() != null ? g.getLabel() : "");
            h.icon.setText(g.getMoments() != null && !g.getMoments().isEmpty() ? "⭐" : "");
            if (g.getMoments() != null) {
                h.badge.setText(String.valueOf(g.getMoments().size()));
            }
        } else {
            bindItem((ViewHolder) holder, (MemoryMoment) items.get(pos));
        }
    }

    private void bindItem(@NonNull ViewHolder holder, MemoryMoment item) {
        holder.title.setText(item.getTitle());
        holder.date.setText(item.getMomentDate());
        holder.location.setText(item.getLocation());
        holder.emoji.setText(item.getEmoji() != null ? item.getEmoji() : "💫");

        holder.photosContainer.removeAllViews();
        holder.photosContainer.setVisibility(View.GONE);
        String photoUrls = item.getPhotoUrls();
        if (photoUrls != null && !photoUrls.isEmpty()) {
            String[] urls = photoUrls.split(",");
            // Show first photo as cover
            String firstUrl = urls[0].trim();
            if (!firstUrl.isEmpty()) {
                holder.cover.setVisibility(View.VISIBLE);
                holder.emojiPlaceholder.setVisibility(View.GONE);
                ImageRequest coverReq = new ImageRequest.Builder(holder.itemView.getContext())
                    .data(firstUrl)
                    .target(holder.cover)
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_placeholder)
                    .build();
                Coil.imageLoader(holder.itemView.getContext()).enqueue(coverReq);
            } else {
                holder.cover.setVisibility(View.GONE);
                holder.emojiPlaceholder.setVisibility(View.VISIBLE);
                holder.emojiPlaceholder.setText(item.getEmoji() != null ? item.getEmoji() : "💫");
            }

        } else {
            holder.cover.setVisibility(View.GONE);
            holder.emojiPlaceholder.setVisibility(View.VISIBLE);
            holder.emojiPlaceholder.setText(item.getEmoji() != null ? item.getEmoji() : "💫");
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

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView icon, label, badge;
        HeaderViewHolder(View v) {
            super(v);
            icon = v.findViewById(R.id.timeline_icon);
            label = v.findViewById(R.id.timeline_label);
            badge = v.findViewById(R.id.timeline_badge);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, location, emoji, emojiPlaceholder, editBtn, deleteBtn;
        ImageView cover;
        LinearLayout photosContainer;
        ViewHolder(View v) {
            super(v);
            cover = v.findViewById(R.id.moment_cover);
            title = v.findViewById(R.id.moment_title);
            date = v.findViewById(R.id.moment_date);
            location = v.findViewById(R.id.moment_location);
            emoji = v.findViewById(R.id.moment_emoji);
            emojiPlaceholder = v.findViewById(R.id.moment_emoji_placeholder);
            photosContainer = v.findViewById(R.id.moment_photos_container);
            editBtn = v.findViewById(R.id.moment_edit_btn);
            deleteBtn = v.findViewById(R.id.moment_delete_btn);
        }
    }
}
