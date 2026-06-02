package com.niit.memory.ui.components;

import android.app.Dialog;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import coil.Coil;
import coil.request.ImageRequest;
import com.niit.memory.R;
import com.niit.memory.data.model.Song;
import com.niit.memory.ui.adapters.PlaylistAdapter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class MusicBarHelper {

    private final AppCompatActivity activity;
    private final MusicPlayerViewModel viewModel;
    private final Handler timeHandler = new Handler(Looper.getMainLooper());
    private Runnable timeUpdater;

    private View musicBarRoot;
    private TextView musicHint;
    private LinearLayout musicInfo;
    private ImageView musicThumb;
    private TextView musicName, musicArtist;
    private ImageButton musicPlayPause, musicNext, musicPlaylistBtn, musicClose;

    private final Observer<List<Song>> playlistObserver;
    private final Observer<Integer> currentIndexObserver;
    private final Observer<Boolean> playingObserver;
    private final Observer<String> errorObserver;

    public MusicBarHelper(AppCompatActivity activity) {
        this.activity = activity;
        this.viewModel = new ViewModelProvider(activity).get(MusicPlayerViewModel.class);

        playlistObserver = list -> {
            if ((list == null || list.isEmpty()) && viewModel.getCurrentSong() == null) {
                musicHint.setVisibility(View.VISIBLE);
                musicInfo.setVisibility(View.GONE);
                musicThumb.setVisibility(View.GONE);
            }
        };

        currentIndexObserver = idx -> {
            Song song = viewModel.getCurrentSong();
            boolean hasSong = song != null && idx != null && idx >= 0;
            musicHint.setVisibility(hasSong ? View.GONE : View.VISIBLE);
            musicInfo.setVisibility(hasSong ? View.VISIBLE : View.GONE);
            musicThumb.setVisibility(hasSong ? View.VISIBLE : View.GONE);
            if (hasSong) {
                musicName.setText(song.getName() != null ? song.getName() : "未知歌曲");
                musicArtist.setText(song.getArtist() != null ? song.getArtist() : "未知歌手");
                String pic = song.getPicUrl();
                if (pic != null && !pic.isEmpty()) {
                    Coil.imageLoader(activity).enqueue(
                        new ImageRequest.Builder(activity)
                            .data(pic)
                            .target(musicThumb)
                            .placeholder(R.drawable.image_placeholder)
                            .error(R.drawable.image_placeholder)
                            .build()
                    );
                } else {
                    musicThumb.setImageResource(R.drawable.image_placeholder);
                }
            }
        };

        playingObserver = playing -> {
            boolean p = playing != null && playing;
            musicPlayPause.setImageResource(p ? R.drawable.ic_pause : R.drawable.ic_play);
            if (p) startTimeUpdater(); else stopTimeUpdater();
        };

        errorObserver = msg -> {
            if (msg != null && !msg.isEmpty()) {
                android.widget.Toast.makeText(activity, msg, android.widget.Toast.LENGTH_SHORT).show();
            }
        };
    }

    public void setup() {
        musicBarRoot = activity.findViewById(R.id.music_player_bar);
        musicHint = activity.findViewById(R.id.music_hint);
        musicInfo = activity.findViewById(R.id.music_info);
        musicThumb = activity.findViewById(R.id.music_thumb);
        musicName = activity.findViewById(R.id.music_name);
        musicArtist = activity.findViewById(R.id.music_artist);
        musicPlayPause = activity.findViewById(R.id.music_play_pause);
        musicNext = activity.findViewById(R.id.music_next);
        musicPlaylistBtn = activity.findViewById(R.id.music_playlist_btn);
        musicClose = activity.findViewById(R.id.music_close);

        viewModel.currentIndex.observeForever(currentIndexObserver);

        viewModel.isPlaying.observeForever(playingObserver);

        viewModel.errorMessage.observeForever(errorObserver);

        viewModel.playlist.observeForever(playlistObserver);

        musicPlayPause.setOnClickListener(v -> viewModel.togglePlayPause());
        musicNext.setOnClickListener(v -> viewModel.playNext());
        musicClose.setOnClickListener(v -> viewModel.stop());
        musicPlaylistBtn.setOnClickListener(v -> showFullPlayerDialog());

        if (musicBarRoot != null) {
            musicBarRoot.setOnClickListener(v -> showFullPlayerDialog());
        }
    }

    public void onDestroy() {
        stopTimeUpdater();
        viewModel.currentIndex.removeObserver(currentIndexObserver);
        viewModel.isPlaying.removeObserver(playingObserver);
        viewModel.errorMessage.removeObserver(errorObserver);
        viewModel.playlist.removeObserver(playlistObserver);
    }

    private void startTimeUpdater() {
        stopTimeUpdater();
        timeUpdater = new Runnable() {
            @Override
            public void run() {
                viewModel.updateCurrentTime();
                timeHandler.postDelayed(this, 1000);
            }
        };
        timeHandler.post(timeUpdater);
    }

    private void stopTimeUpdater() {
        if (timeUpdater != null) {
            timeHandler.removeCallbacks(timeUpdater);
            timeUpdater = null;
        }
    }

    private String formatTime(int seconds) {
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format(Locale.getDefault(), "%d:%02d", m, s);
    }

    private void showFullPlayerDialog() {
        Dialog dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_music_player);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(android.view.Gravity.BOTTOM);
            window.setWindowAnimations(android.R.style.Animation_InputMethod);
        }

        ImageView dialogThumb = dialog.findViewById(R.id.music_dialog_thumb);
        TextView dialogName = dialog.findViewById(R.id.music_dialog_name);
        TextView dialogArtist = dialog.findViewById(R.id.music_dialog_artist);
        LinearLayout nowPlaying = dialog.findViewById(R.id.music_now_playing);
        LinearLayout seekbarRow = dialog.findViewById(R.id.music_seekbar_row);
        SeekBar seekBar = dialog.findViewById(R.id.music_seekbar);
        TextView currentTimeTv = dialog.findViewById(R.id.music_current_time);
        TextView durationTv = dialog.findViewById(R.id.music_duration);
        ImageButton prevBtn = dialog.findViewById(R.id.music_prev_btn);
        ImageButton playPauseBtn = dialog.findViewById(R.id.music_play_pause_btn);
        ImageButton nextBtn = dialog.findViewById(R.id.music_next_btn);
        TextView randomToggle = dialog.findViewById(R.id.music_random_toggle);
        TextView dialogCloseBtn = dialog.findViewById(R.id.music_dialog_close);
        dialogCloseBtn.setOnClickListener(v -> dialog.dismiss());

        EditText searchInput = dialog.findViewById(R.id.music_search_input);
        TextView searchBtn = dialog.findViewById(R.id.music_search_btn);
        TextView sectionLabel = dialog.findViewById(R.id.music_section_label);
        TextView tabToggle = dialog.findViewById(R.id.music_tab_toggle);
        RecyclerView songList = dialog.findViewById(R.id.music_song_list);
        TextView emptyText = dialog.findViewById(R.id.music_empty_text);

        final boolean[] showingSearch = {false};

        PlaylistAdapter playlistAdapter = new PlaylistAdapter(
            index -> {
                if (showingSearch[0]) {
                    List<Song> results = viewModel.searchResults.getValue();
                    if (results != null && index >= 0 && index < results.size()) {
                        viewModel.addToPlaylist(results.get(index));
                        List<Song> pl = viewModel.playlist.getValue();
                        if (pl != null && !pl.isEmpty()) viewModel.play(pl.size() - 1);
                    }
                } else {
                    viewModel.play(index);
                }
            },
            index -> viewModel.removeFromPlaylist(index),
            (from, to) -> viewModel.moveSong(from, to)
        );
        songList.setLayoutManager(new LinearLayoutManager(activity));
        songList.setAdapter(playlistAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                int from = vh.getAdapterPosition();
                int to = target.getAdapterPosition();
                playlistAdapter.moveItem(from, to);
                return true;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {}
            @Override
            public boolean isLongPressDragEnabled() {
                return !showingSearch[0];
            }
        });
        itemTouchHelper.attachToRecyclerView(songList);

        Observer<List<Song>> dialogPlaylistObs = list -> {
            if (!showingSearch[0]) {
                playlistAdapter.submitList(list);
                boolean empty = list == null || list.isEmpty();
                songList.setVisibility(empty ? View.GONE : View.VISIBLE);
                emptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
            }
        };
        viewModel.playlist.observeForever(dialogPlaylistObs);

        final Consumer<List<Song>> onSearchResult = results -> {
            if (!showingSearch[0]) return;

            if (results == null || results.isEmpty()) {
                emptyText.setText("未找到歌曲");
                emptyText.setVisibility(View.VISIBLE);
                songList.setVisibility(View.GONE);
            } else {
                songList.setVisibility(View.VISIBLE);
                emptyText.setVisibility(View.GONE);
                playlistAdapter.submitList(results);
            }
        };

        // Also register as fallback on the ViewModel
        viewModel.setSearchCallback(onSearchResult);


        Observer<Integer> dialogSongObs = idx -> {
            Song song = viewModel.getCurrentSong();
            if (song != null && idx != null && idx >= 0) {
                nowPlaying.setVisibility(View.VISIBLE);
                seekbarRow.setVisibility(View.VISIBLE);
                dialogName.setText(song.getName() != null ? song.getName() : "未知歌曲");
                dialogArtist.setText(song.getArtist() != null ? song.getArtist() : "未知歌手");
                String pic = song.getPicUrl();
                if (pic != null && !pic.isEmpty()) {
                    Coil.imageLoader(activity).enqueue(
                        new ImageRequest.Builder(activity)
                            .data(pic)
                            .target(dialogThumb)
                            .placeholder(R.drawable.image_placeholder)
                            .error(R.drawable.image_placeholder)
                            .build()
                    );
                }
            } else {
                nowPlaying.setVisibility(View.GONE);
                seekbarRow.setVisibility(View.GONE);
            }
        };
        viewModel.currentIndex.observeForever(dialogSongObs);

        Observer<Integer> durationObs = dur -> {
            if (dur != null) {
                seekBar.setMax(dur);
                durationTv.setText(formatTime(dur));
            }
        };
        viewModel.duration.observeForever(durationObs);

        Observer<Integer> timeObs = ct -> {
            if (ct != null) {
                seekBar.setProgress(ct);
                currentTimeTv.setText(formatTime(ct));
            }
        };
        viewModel.currentTime.observeForever(timeObs);

        Observer<Boolean> randomObs = rand -> {
            boolean r = rand != null && rand;
            randomToggle.setText(r ? "🔀 随机 (开)" : "🔀 随机");
            randomToggle.setTextColor(r ? 0xFFFF9A9E : 0xFF9B8B80);
        };
        viewModel.isRandom.observeForever(randomObs);

        Observer<Boolean> playingObs = playing -> {
            boolean p = playing != null && playing;
            playPauseBtn.setImageResource(p ? R.drawable.ic_pause : R.drawable.ic_play);
        };
        viewModel.isPlaying.observeForever(playingObs);

        playPauseBtn.setOnClickListener(v -> viewModel.togglePlayPause());
        nextBtn.setOnClickListener(v -> viewModel.playNext());
        prevBtn.setOnClickListener(v -> viewModel.playPrev());
        randomToggle.setOnClickListener(v -> {
            Boolean cur = viewModel.isRandom.getValue();
            viewModel.isRandom.postValue(cur == null || !cur);
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser) viewModel.seekTo(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        searchBtn.setOnClickListener(v -> {
            String kw = searchInput.getText().toString().trim();
            if (!kw.isEmpty()) {
                showingSearch[0] = true;
                sectionLabel.setText("搜索结果");
                tabToggle.setText("播放列表");
                playlistAdapter.setShowDelete(false);
                emptyText.setText("搜索中...");
                emptyText.setVisibility(View.VISIBLE);
                songList.setVisibility(View.GONE);
                viewModel.search(kw, onSearchResult);
            }
        });
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            String kw = searchInput.getText().toString().trim();
            if (!kw.isEmpty()) {
                showingSearch[0] = true;
                sectionLabel.setText("搜索结果");
                tabToggle.setText("播放列表");
                playlistAdapter.setShowDelete(false);
                emptyText.setText("搜索中...");
                emptyText.setVisibility(View.VISIBLE);
                songList.setVisibility(View.GONE);
                viewModel.search(kw, onSearchResult);
            }
            return true;
        });

        tabToggle.setOnClickListener(v -> {
            showingSearch[0] = !showingSearch[0];
            if (showingSearch[0]) {
                sectionLabel.setText("搜索结果");
                tabToggle.setText("播放列表");
                playlistAdapter.setShowDelete(false);
                playlistAdapter.submitList(viewModel.searchResults.getValue());
            } else {
                sectionLabel.setText("播放列表");
                tabToggle.setText("搜索结果");
                playlistAdapter.setShowDelete(true);
                playlistAdapter.submitList(viewModel.playlist.getValue());
            }
        });

        playlistAdapter.setShowDelete(true);

        dialog.setOnDismissListener(d -> {
            viewModel.setSearchCallback(null);
            viewModel.playlist.removeObserver(dialogPlaylistObs);
            viewModel.currentIndex.removeObserver(dialogSongObs);
            viewModel.duration.removeObserver(durationObs);
            viewModel.currentTime.removeObserver(timeObs);
            viewModel.isRandom.removeObserver(randomObs);
            viewModel.isPlaying.removeObserver(playingObs);
        });

        dialog.show();
    }
}
