package com.niit.memory.data.repository;

import android.content.Context;
import com.niit.memory.data.api.*;
import com.niit.memory.data.model.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import retrofit2.Response;

public class MusicRepository {
    private final MusicService service;

    public MusicRepository(Context context) {
        this.service = ApiClient.getInstance(context).create(MusicService.class);
    }

    public List<Song> search(String keyword, int limit) throws Exception {
        android.util.Log.w("MusicSearch", "STEP5-HTTP请求: " + keyword);
        Response<ApiResponse<List<Song>>> resp = service.search(keyword, limit).execute();
        android.util.Log.w("MusicSearch", "STEP5-HTTP响应: code=" + resp.code() + ", success=" + resp.isSuccessful());
        if (!resp.isSuccessful()) throw new IOException("Network error: " + resp.code());
        ApiResponse<List<Song>> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        List<Song> data = body.getData();
        android.util.Log.w("MusicSearch", "STEP5-解析完成: " + (data != null ? data.size() : 0) + " 首歌");
        return data;
    }

    public List<Song> getPlaylist() throws Exception {
        Response<ApiResponse<List<Song>>> resp = service.getPlaylist().execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<List<Song>> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        return body.getData();
    }

    public void savePlaylist(List<Map<String, Object>> songs) throws Exception {
        Response<ApiResponse<Void>> resp = service.savePlaylist(songs).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<Void> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
    }
}
