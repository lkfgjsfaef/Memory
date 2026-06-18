package com.niit.memory.data.repository;

import android.content.Context;
import com.niit.memory.data.api.*;
import com.niit.memory.data.model.*;
import java.util.List;
import java.util.Map;

public class MusicRepository {
    private final MusicService service;

    public MusicRepository(Context context) {
        this.service = ApiClient.getInstance(context).create(MusicService.class);
    }

    public List<Song> search(String keyword, int limit) throws Exception {
        return ApiResponse.parseResponse(service.search(keyword, limit).execute());
    }

    public List<Song> getPlaylist() throws Exception {
        return ApiResponse.parseResponse(service.getPlaylist().execute());
    }

    public void savePlaylist(List<Map<String, Object>> songs) throws Exception {
        ApiResponse.validateResponse(service.savePlaylist(songs).execute());
    }
}
