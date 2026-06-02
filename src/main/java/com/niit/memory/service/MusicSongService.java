package com.niit.memory.service;

import com.niit.memory.entity.MusicSong;
import com.niit.memory.repository.MusicSongRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MusicSongService {

    private final MusicSongRepository repository;

    public MusicSongService(MusicSongRepository repository) {
        this.repository = repository;
    }

    public List<Map<String, Object>> getPlaylist() {
        List<MusicSong> songs = repository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (MusicSong s : songs) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", s.getSongId());
            item.put("name", s.getName());
            item.put("artist", s.getArtist());
            item.put("album", s.getAlbum());
            item.put("duration", s.getDuration());
            item.put("picUrl", s.getPicUrl());
            result.add(item);
        }
        return result;
    }

    public void savePlaylist(List<Map<String, Object>> songs) {
        repository.deleteAll();
        int order = 0;
        for (Map<String, Object> song : songs) {
            MusicSong s = new MusicSong();
            s.setSongId(toLong(song.get("id")));
            s.setName((String) song.getOrDefault("name", ""));
            s.setArtist((String) song.getOrDefault("artist", ""));
            s.setAlbum((String) song.getOrDefault("album", ""));
            s.setDuration(toLong(song.get("duration")));
            s.setPicUrl((String) song.getOrDefault("picUrl", ""));
            s.setSortOrder(order++);
            repository.insert(s);
        }
    }

    private long toLong(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) return ((Number) obj).longValue();
        try {
            return Long.parseLong(obj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
