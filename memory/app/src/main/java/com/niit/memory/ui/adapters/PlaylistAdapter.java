package com.niit.memory.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import coil.Coil;
import coil.request.ImageRequest;
import com.niit.memory.R;
import com.niit.memory.data.model.Song;
import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    private List<Song> items = new ArrayList<>();
    private final OnPlayListener playListener;
    private final OnDeleteListener deleteListener;
    private final OnAddListener addListener;
    private final OnMoveListener moveListener;
    private boolean showDelete = false;
    private boolean showAdd = false;

    public interface OnPlayListener { void onPlay(int index); }
    public interface OnDeleteListener { void onDelete(int index); }
    public interface OnAddListener { void onAdd(int index); }
    public interface OnMoveListener { void onMove(int from, int to); }

    public PlaylistAdapter(OnPlayListener play, OnDeleteListener del) {
        this(play, del, null, null);
    }

    public PlaylistAdapter(OnPlayListener play, OnDeleteListener del, OnMoveListener move) {
        this(play, del, move, null);
    }

    public PlaylistAdapter(OnPlayListener play, OnDeleteListener del, OnMoveListener move,
                           OnAddListener add) {
        this.playListener = play;
        this.deleteListener = del;
        this.moveListener = move;
        this.addListener = add;
    }

    public void submitList(List<Song> list) {
        items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setShowDelete(boolean show) {
        showDelete = show;
        notifyDataSetChanged();
    }

    public void setShowAdd(boolean show) {
        showAdd = show;
        notifyDataSetChanged();
    }

    public void moveItem(int from, int to) {
        if (from < 0 || to < 0 || from >= items.size() || to >= items.size()) return;
        Song song = items.remove(from);
        items.add(to, song);
        notifyItemMoved(from, to);
        if (moveListener != null) moveListener.onMove(from, to);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_playlist_song, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        Song song = items.get(pos);
        holder.name.setText(song.getName() != null ? song.getName() : "未知歌曲");
        holder.artist.setText(song.getArtist() != null ? song.getArtist() : "未知歌手");

        String picUrl = song.getPicUrl();
        if (picUrl != null && !picUrl.isEmpty()) {
            ImageRequest req = new ImageRequest.Builder(holder.itemView.getContext())
                .data(picUrl)
                .target(holder.thumb)
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_placeholder)
                .build();
            Coil.imageLoader(holder.itemView.getContext()).enqueue(req);
        } else {
            holder.thumb.setImageResource(R.drawable.image_placeholder);
        }

        holder.deleteBtn.setVisibility(showDelete ? View.VISIBLE : View.GONE);
        holder.addBtn.setVisibility(showAdd ? View.VISIBLE : View.GONE);

        holder.playBtn.setOnClickListener(v -> {
            if (playListener != null) playListener.onPlay(holder.getAdapterPosition());
        });

        holder.deleteBtn.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(holder.getAdapterPosition());
        });

        holder.addBtn.setOnClickListener(v -> {
            if (addListener != null) addListener.onAdd(holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumb;
        TextView name, artist;
        ImageButton playBtn, deleteBtn, addBtn;
        ViewHolder(View v) {
            super(v);
            thumb = v.findViewById(R.id.pl_song_thumb);
            name = v.findViewById(R.id.pl_song_name);
            artist = v.findViewById(R.id.pl_song_artist);
            playBtn = v.findViewById(R.id.pl_song_play);
            deleteBtn = v.findViewById(R.id.pl_song_delete);
            addBtn = v.findViewById(R.id.pl_song_add);
        }
    }
}
