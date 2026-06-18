package com.niit.memory.ui.components;

import android.app.Application;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.niit.memory.data.model.Song;
import com.niit.memory.data.repository.MusicRepository;
import com.niit.memory.util.TaskExecutor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MusicPlayerViewModel extends AndroidViewModel {

    private static final String TAG = "MusicPlayerViewModel";
    private final MusicRepository repository;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private MediaPlayer mediaPlayer;
    private volatile int playGeneration;
    private Consumer<List<Song>> searchCallback;

    public final MutableLiveData<List<Song>> playlist = new MutableLiveData<>(new ArrayList<>());
    public final MutableLiveData<List<Song>> searchResults = new MutableLiveData<>();
    public final MutableLiveData<Integer> currentIndex = new MutableLiveData<>(-1);
    public final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);
    public final MutableLiveData<Boolean> isRandom = new MutableLiveData<>(false);
    public final MutableLiveData<Integer> currentTime = new MutableLiveData<>(0);
    public final MutableLiveData<Integer> duration = new MutableLiveData<>(0);
    public final MutableLiveData<Boolean> panelExpanded = new MutableLiveData<>(false);
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public final MutableLiveData<Boolean> searching = new MutableLiveData<>(false);

    public MusicPlayerViewModel(@NonNull Application application) {
        super(application);
        repository = new MusicRepository(application);
        loadPlaylist();
    }

    public void loadPlaylist() {
        TaskExecutor.execute(() -> {
            try {
                List<Song> songs = repository.getPlaylist();
                playlist.postValue(songs != null ? songs : new ArrayList<>());
            } catch (Exception e) {
                Log.e(TAG, "Error loading playlist", e);
                errorMessage.postValue("加载播放列表失败");
            }
        });
    }

    public void setSearchCallback(Consumer<List<Song>> callback) {
        this.searchCallback = callback;
    }

    public void search(String keyword) {
        search(keyword, searchCallback);
    }

    public void search(String keyword, Consumer<List<Song>> callback) {
        TaskExecutor.execute(() -> {
            try {
                List<Song> results = repository.search(keyword, 20);
                List<Song> safe = results != null ? results : new ArrayList<>();
                mainHandler.post(() -> {
                    searchResults.setValue(safe);
                    if (callback != null) {
                        callback.accept(safe);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "搜索异常: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    errorMessage.setValue("搜索失败: " + e.getMessage());
                });
            }
        });
    }

    public void addToPlaylist(Song song) {
        List<Song> list = playlist.getValue();
        if (list == null) list = new ArrayList<>();
        for (Song s : list) {
            if (s.getId() != null && s.getId().equals(song.getId())) return;
        }
        list.add(song);
        playlist.postValue(list);
        persistPlaylist(list);
    }

    public void removeFromPlaylist(int index) {
        List<Song> list = playlist.getValue();
        if (list == null) return;
        if (index >= 0 && index < list.size()) {
            Integer curr = currentIndex.getValue();
            list.remove(index);
            playlist.postValue(list);
            if (curr != null && index == curr) {
                stop();
            } else if (curr != null && index < curr) {
                currentIndex.postValue(curr - 1);
            }
            persistPlaylist(list);
        }
    }

    public void moveSong(int from, int to) {
        List<Song> list = playlist.getValue();
        if (list == null || from < 0 || to < 0 || from >= list.size() || to >= list.size()) return;
        Song song = list.remove(from);
        list.add(to, song);
        playlist.postValue(list);

        Integer curr = currentIndex.getValue();
        if (curr != null) {
            if (from == curr) currentIndex.postValue(to);
            else if (from < curr && to >= curr) currentIndex.postValue(curr - 1);
            else if (from > curr && to <= curr) currentIndex.postValue(curr + 1);
        }
        persistPlaylist(list);
    }

    private void persistPlaylist(List<Song> songs) {
        TaskExecutor.execute(() -> {
            try {
                List<Map<String, Object>> data = new ArrayList<>();
                for (Song s : songs) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", s.getId());
                    m.put("name", s.getName());
                    m.put("artist", s.getArtist());
                    m.put("album", s.getAlbum());
                    m.put("picUrl", s.getPicUrl());
                    m.put("duration", s.getDuration());
                    data.add(m);
                }
                repository.savePlaylist(data);
            } catch (Exception e) {
                Log.e(TAG, "Error saving playlist", e);
                errorMessage.postValue("保存播放列表失败");
            }
        });
    }

    public void play(int index) {
        List<Song> list = playlist.getValue();
        if (list == null || index < 0 || index >= list.size()) return;

        currentIndex.postValue(index);
        Song song = list.get(index);
        Long neteaseId = song.getId();
        String url = "https://music.163.com/song/media/outer/url?id=" + neteaseId + ".mp3";

        stopMediaPlayer();

        final int generation = ++playGeneration;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.setOnPreparedListener(mp -> {
                if (generation != playGeneration) return;
                duration.postValue(mp.getDuration() / 1000);
                mp.start();
                isPlaying.postValue(true);
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                if (generation == playGeneration) playNext();
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                if (generation == playGeneration) {
                    errorMessage.postValue("播放失败");
                }
                return true;
            });
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            errorMessage.postValue("无法播放: " + e.getMessage());
            stopMediaPlayer();
        }
    }

    public void togglePlayPause() {
        MediaPlayer mp = mediaPlayer;
        if (mp == null) {
            List<Song> list = playlist.getValue();
            if (list != null && !list.isEmpty()) {
                play(0);
            }
            return;
        }
        try {
            if (mp.isPlaying()) {
                mp.pause();
                isPlaying.postValue(false);
            } else {
                mp.start();
                isPlaying.postValue(true);
            }
        } catch (Exception e) {
            Log.e(TAG, "toggle error", e);
        }
    }

    public void playNext() {
        List<Song> list = playlist.getValue();
        if (list == null || list.isEmpty()) return;
        Integer curr = currentIndex.getValue();
        int next;
        if (curr == null || curr < 0) {
            next = 0;
        } else if (Boolean.TRUE.equals(isRandom.getValue())) {
            next = (int) (Math.random() * list.size());
        } else {
            next = (curr + 1) % list.size();
        }
        play(next);
    }

    public void playPrev() {
        List<Song> list = playlist.getValue();
        if (list == null || list.isEmpty()) return;
        Integer curr = currentIndex.getValue();
        if (curr == null || curr <= 0) {
            play(list.size() - 1);
        } else {
            play(curr - 1);
        }
    }

    public void stop() {
        stopMediaPlayer();
        currentIndex.postValue(-1);
        isPlaying.postValue(false);
        currentTime.postValue(0);
        duration.postValue(0);
    }

    public void seekTo(int seconds) {
        MediaPlayer mp = mediaPlayer;
        if (mp != null) {
            try {
                mp.seekTo(seconds * 1000);
                currentTime.postValue(seconds);
            } catch (Exception e) {
                Log.e(TAG, "seek error", e);
            }
        }
    }

    public void updateCurrentTime() {
        MediaPlayer mp = mediaPlayer;
        if (mp != null) {
            try {
                if (mp.isPlaying()) {
                    currentTime.postValue(mp.getCurrentPosition() / 1000);
                }
            } catch (Exception e) {
                Log.e(TAG, "updateCurrentTime error", e);
            }
        }
    }

    public void togglePanel() {
        Boolean expanded = panelExpanded.getValue();
        panelExpanded.postValue(expanded == null || !expanded);
    }

    public Song getCurrentSong() {
        List<Song> list = playlist.getValue();
        Integer idx = currentIndex.getValue();
        if (list != null && idx != null && idx >= 0 && idx < list.size()) {
            return list.get(idx);
        }
        return null;
    }

    private void stopMediaPlayer() {
        MediaPlayer mp = mediaPlayer;
        if (mp != null) {
            mediaPlayer = null;
            try {
                if (mp.isPlaying()) mp.stop();
                mp.release();
            } catch (Exception e) {
                Log.e(TAG, "stopMediaPlayer error", e);
            }
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopMediaPlayer();
    }
}
