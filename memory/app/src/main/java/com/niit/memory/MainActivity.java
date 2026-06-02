package com.niit.memory;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import coil.Coil;
import coil.request.ImageRequest;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.niit.memory.data.model.Song;
import com.niit.memory.databinding.ActivityMainBinding;
import com.niit.memory.ui.adapters.PlaylistAdapter;
import com.niit.memory.ui.components.MusicPlayerViewModel;
import com.niit.memory.ui.screens.login.LoginActivity;
import com.niit.memory.util.SessionManager;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private MusicPlayerViewModel musicViewModel;

    // Music bar views
    private View musicBarRoot;
    private TextView musicHint;
    private LinearLayout musicInfo;
    private ImageView musicThumb;
    private TextView musicName, musicArtist;
    private ImageButton musicPlayPause, musicNext, musicPlaylistBtn, musicClose;

    // Time update handler
    private final Handler timeHandler = new Handler(Looper.getMainLooper());
    private Runnable timeUpdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager session = SessionManager.getInstance(this);
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
            .findFragmentById(R.id.nav_host_fragment);
        navController = navHost.getNavController();

        BottomNavigationView bottomNav = binding.bottomNav;
        NavigationUI.setupWithNavController(bottomNav, navController);


        navController.addOnDestinationChangedListener((controller, dest, args) -> {
            int id = dest.getId();
            boolean showNav = id == R.id.nav_home || id == R.id.nav_daily
                || id == R.id.nav_calendar || id == R.id.nav_wishlist
                || id == R.id.nav_memories;
            bottomNav.setVisibility(showNav ? android.view.View.VISIBLE : android.view.View.GONE);
        });

        initMusicPlayer();
    }

    private void initMusicPlayer() {
        musicViewModel = new ViewModelProvider(this).get(MusicPlayerViewModel.class);

        // Find bar views (they're part of the included layout)
        musicBarRoot = findViewById(R.id.music_player_bar);
        musicHint = findViewById(R.id.music_hint);
        musicInfo = findViewById(R.id.music_info);
        musicThumb = findViewById(R.id.music_thumb);
        musicName = findViewById(R.id.music_name);
        musicArtist = findViewById(R.id.music_artist);
        musicPlayPause = findViewById(R.id.music_play_pause);
        musicNext = findViewById(R.id.music_next);
        musicPlaylistBtn = findViewById(R.id.music_playlist_btn);
        musicClose = findViewById(R.id.music_close);

        // Observe current song for bar display
        musicViewModel.currentIndex.observe(this, idx -> {
            Song song = musicViewModel.getCurrentSong();
            boolean hasSong = song != null && idx != null && idx >= 0;
            musicHint.setVisibility(hasSong ? View.GONE : View.VISIBLE);
            musicInfo.setVisibility(hasSong ? View.VISIBLE : View.GONE);
            musicThumb.setVisibility(hasSong ? View.VISIBLE : View.GONE);
            if (hasSong) {
                musicName.setText(song.getName() != null ? song.getName() : "未知歌曲");
                musicArtist.setText(song.getArtist() != null ? song.getArtist() : "未知歌手");
                String pic = song.getPicUrl();
                if (pic != null && !pic.isEmpty()) {
                    ImageRequest req = new ImageRequest.Builder(this)
                        .data(pic)
                        .target(musicThumb)
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_placeholder)
                        .build();
                    Coil.imageLoader(this).enqueue(req);
                } else {
                    musicThumb.setImageResource(R.drawable.image_placeholder);
                }
            }
        });

        // Observe play state
        musicViewModel.isPlaying.observe(this, playing -> {
            boolean p = playing != null && playing;
            musicPlayPause.setImageResource(p ? R.drawable.ic_pause : R.drawable.ic_play);
            if (p) startTimeUpdater(); else stopTimeUpdater();
        });

        // Observe errors
        musicViewModel.errorMessage.observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        // Button clicks
        musicPlayPause.setOnClickListener(v -> musicViewModel.togglePlayPause());
        musicNext.setOnClickListener(v -> musicViewModel.playNext());
        musicClose.setOnClickListener(v -> musicViewModel.stop());
        musicPlaylistBtn.setOnClickListener(v -> showFullPlayerDialog());

        // Tapping the bar also opens the full player
        musicBarRoot.setOnClickListener(v -> showFullPlayerDialog());

        // Ensure hint state when playlist is empty
        musicViewModel.playlist.observe(this, list -> {
            if ((list == null || list.isEmpty()) && musicViewModel.getCurrentSong() == null) {
                musicHint.setVisibility(View.VISIBLE);
                musicInfo.setVisibility(View.GONE);
                musicThumb.setVisibility(View.GONE);
            }
        });
    }

    private void startTimeUpdater() {
        stopTimeUpdater();
        timeUpdater = new Runnable() {
            @Override
            public void run() {
                musicViewModel.updateCurrentTime();
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
        Dialog dialog = new Dialog(this, android.R.style.Theme_DeviceDefault_Light_NoActionBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_music_player);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(android.view.Gravity.BOTTOM);
            window.setWindowAnimations(android.R.style.Animation_InputMethod);
        }

        // Dialog views
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

        // Track current mode: playlist vs search results
        final boolean[] showingSearch = {false};

        // Playlist adapter — mode-aware play/add callbacks, with drag-to-reorder
        PlaylistAdapter playlistAdapter = new PlaylistAdapter(
            index -> {
                // PLAY: play song (add then play in search mode, direct play in playlist mode)
                if (showingSearch[0]) {
                    List<Song> results = musicViewModel.searchResults.getValue();
                    if (results != null && index >= 0 && index < results.size()) {
                        musicViewModel.addToPlaylist(results.get(index));
                        List<Song> pl = musicViewModel.playlist.getValue();
                        if (pl != null && !pl.isEmpty()) musicViewModel.play(pl.size() - 1);
                    }
                } else {
                    musicViewModel.play(index);
                }
            },
            index -> musicViewModel.removeFromPlaylist(index),
            (from, to) -> musicViewModel.moveSong(from, to),
            index -> {
                // ADD: add to playlist without playing
                List<Song> results = musicViewModel.searchResults.getValue();
                if (results != null && index >= 0 && index < results.size()) {
                    musicViewModel.addToPlaylist(results.get(index));
                    android.widget.Toast.makeText(this,
                        "已添加到播放列表", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        );
        songList.setLayoutManager(new LinearLayoutManager(this));
        songList.setAdapter(playlistAdapter);

        // Drag-to-reorder support
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

        // Observe playlist
        Observer<List<Song>> playlistObserver = list -> {
            if (!showingSearch[0]) {
                playlistAdapter.submitList(list);
                boolean empty = list == null || list.isEmpty();
                songList.setVisibility(empty ? View.GONE : View.VISIBLE);
                emptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
            }
        };
        musicViewModel.playlist.observeForever(playlistObserver);

        // Observe search results
        Observer<List<Song>> searchObserver = results -> {
            if (showingSearch[0]) {
                playlistAdapter.submitList(results);
                boolean empty = results == null || results.isEmpty();
                songList.setVisibility(empty ? View.GONE : View.VISIBLE);
                emptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
                if (empty && musicViewModel.searching.getValue() != Boolean.TRUE) {
                    emptyText.setText("未找到歌曲");
                }
            }
        };
        musicViewModel.searchResults.observeForever(searchObserver);

        // Observe searching state
        Observer<Boolean> searchingObserver = searching -> {
            if (searching != null && searching) {
                emptyText.setText("搜索中...");
                emptyText.setVisibility(View.VISIBLE);
                songList.setVisibility(View.GONE);
            }
        };
        musicViewModel.searching.observeForever(searchingObserver);

        // Observe current song for dialog display
        Observer<Integer> dialogSongObserver = idx -> {
            Song song = musicViewModel.getCurrentSong();
            if (song != null && idx != null && idx >= 0) {
                nowPlaying.setVisibility(View.VISIBLE);
                seekbarRow.setVisibility(View.VISIBLE);
                dialogName.setText(song.getName() != null ? song.getName() : "未知歌曲");
                dialogArtist.setText(song.getArtist() != null ? song.getArtist() : "未知歌手");
                String pic = song.getPicUrl();
                if (pic != null && !pic.isEmpty()) {
                    ImageRequest req = new ImageRequest.Builder(this)
                        .data(pic)
                        .target(dialogThumb)
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_placeholder)
                        .build();
                    Coil.imageLoader(this).enqueue(req);
                }
            } else {
                nowPlaying.setVisibility(View.GONE);
                seekbarRow.setVisibility(View.GONE);
            }
        };
        musicViewModel.currentIndex.observeForever(dialogSongObserver);

        // Observe duration
        Observer<Integer> durationObserver = dur -> {
            if (dur != null) {
                seekBar.setMax(dur);
                durationTv.setText(formatTime(dur));
            }
        };
        musicViewModel.duration.observeForever(durationObserver);

        // Observe current time
        Observer<Integer> timeObserver = ct -> {
            if (ct != null) {
                seekBar.setProgress(ct);
                currentTimeTv.setText(formatTime(ct));
            }
        };
        musicViewModel.currentTime.observeForever(timeObserver);

        // Observe random mode
        Observer<Boolean> randomObserver = rand -> {
            boolean r = rand != null && rand;
            randomToggle.setText(r ? "🔀 随机 (开)" : "🔀 随机");
            randomToggle.setTextColor(r ? 0xFFFF9A9E : 0xFF666666);
        };
        musicViewModel.isRandom.observeForever(randomObserver);

        // Play state for dialog button
        Observer<Boolean> playingObserver = playing -> {
            boolean p = playing != null && playing;
            playPauseBtn.setImageResource(p ? R.drawable.ic_pause : R.drawable.ic_play);
        };
        musicViewModel.isPlaying.observeForever(playingObserver);

        // Button handlers
        playPauseBtn.setOnClickListener(v -> musicViewModel.togglePlayPause());
        nextBtn.setOnClickListener(v -> musicViewModel.playNext());
        prevBtn.setOnClickListener(v -> musicViewModel.playPrev());
        randomToggle.setOnClickListener(v -> {
            Boolean cur = musicViewModel.isRandom.getValue();
            musicViewModel.isRandom.postValue(cur == null || !cur);
        });

        // SeekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser) musicViewModel.seekTo(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        // Search
        searchBtn.setOnClickListener(v -> {
            String kw = searchInput.getText().toString().trim();
            if (!kw.isEmpty()) musicViewModel.search(kw);
        });
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            String kw = searchInput.getText().toString().trim();
            if (!kw.isEmpty()) musicViewModel.search(kw);
            return true;
        });

        // Toggle between playlist and search results
        tabToggle.setOnClickListener(v -> {
            showingSearch[0] = !showingSearch[0];
            if (showingSearch[0]) {
                sectionLabel.setText("搜索结果");
                tabToggle.setText("播放列表");
                playlistAdapter.setShowDelete(false);
                playlistAdapter.setShowAdd(true);
                List<Song> results = musicViewModel.searchResults.getValue();
                playlistAdapter.submitList(results);
            } else {
                sectionLabel.setText("播放列表");
                tabToggle.setText("搜索结果");
                playlistAdapter.setShowDelete(true);
                playlistAdapter.setShowAdd(false);
                List<Song> pl = musicViewModel.playlist.getValue();
                playlistAdapter.submitList(pl);
            }
        });

        // Set initial playlist state with delete visible
        playlistAdapter.setShowDelete(true);
        playlistAdapter.setShowAdd(false);

        dialog.setOnDismissListener(d -> {
            musicViewModel.playlist.removeObserver(playlistObserver);
            musicViewModel.searchResults.removeObserver(searchObserver);
            musicViewModel.searching.removeObserver(searchingObserver);
            musicViewModel.currentIndex.removeObserver(dialogSongObserver);
            musicViewModel.duration.removeObserver(durationObserver);
            musicViewModel.currentTime.removeObserver(timeObserver);
            musicViewModel.isRandom.removeObserver(randomObserver);
            musicViewModel.isPlaying.removeObserver(playingObserver);
        });

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            new AlertDialog.Builder(this)
                .setTitle("退出登录")
                .setMessage("确定要退出登录吗？")
                .setPositiveButton("退出", (d, w) -> {
                    SessionManager.getInstance(this).logout();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("取消", null)
                .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimeUpdater();
    }
}
