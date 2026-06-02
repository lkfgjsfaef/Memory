package com.niit.memory.data.repository;

import android.content.Context;
import android.util.Log;
import com.niit.memory.data.api.*;
import com.niit.memory.data.model.*;
import java.io.IOException;
import java.util.List;
import retrofit2.Response;

public class MemoryRepository {
    private final MemoryService service;

    public MemoryRepository(Context context) {
        this.service = ApiClient.getInstance(context).create(MemoryService.class);
    }

    // Albums
    public List<MemoryAlbum> getAlbums() throws Exception {
        Response<ApiResponse<List<MemoryAlbum>>> resp = service.getAlbums().execute();
        Log.d("MemoryRepository", "getAlbums: httpCode=" + resp.code());
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<List<MemoryAlbum>> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        List<MemoryAlbum> list = body.getData();
        if (list != null) {
            for (MemoryAlbum a : list) {
                Log.d("MemoryRepository", "  album: id=" + a.getId() + " location=" + a.getLocation()
                    + " coverUrl=" + a.getCoverUrl()
                    + " emoji=" + a.getEmoji());
            }
        }
        return list;
    }

    public MemoryAlbum getAlbum(long id) throws Exception {
        Response<ApiResponse<MemoryAlbum>> resp = service.getAlbum(id).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<MemoryAlbum> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        return body.getData();
    }

    public void createAlbum(MemoryAlbum album) throws Exception {
        Response<ApiResponse<Void>> resp = service.createAlbum(album).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<Void> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
    }

    public MemoryAlbum updateAlbum(long id, MemoryAlbum album) throws Exception {
        Response<ApiResponse<MemoryAlbum>> resp = service.updateAlbum(id, album).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<MemoryAlbum> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        return body.getData();
    }

    public void deleteAlbum(long id) throws Exception {
        Response<ApiResponse<Void>> resp = service.deleteAlbum(id).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<Void> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
    }

    // Moments
    public List<TimelineGroup> getMoments() throws Exception {
        Response<ApiResponse<List<TimelineGroup>>> resp = service.getMoments().execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<List<TimelineGroup>> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        List<TimelineGroup> groups = body.getData();
        return groups != null ? groups : new java.util.ArrayList<>();
    }

    public List<MemoryMoment> getMomentsFlat() throws Exception {
        List<TimelineGroup> groups = getMoments();
        List<MemoryMoment> flat = new java.util.ArrayList<>();
        for (TimelineGroup g : groups) {
            if (g.getMoments() != null) flat.addAll(g.getMoments());
        }
        return flat;
    }

    public MemoryMoment getMoment(long id) throws Exception {
        Response<ApiResponse<MemoryMoment>> resp = service.getMoment(id).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<MemoryMoment> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        return body.getData();
    }

    public void createMoment(MemoryMoment moment) throws Exception {
        Response<ApiResponse<Void>> resp = service.createMoment(moment).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<Void> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
    }

    public MemoryMoment updateMoment(long id, MemoryMoment moment) throws Exception {
        Response<ApiResponse<MemoryMoment>> resp = service.updateMoment(id, moment).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<MemoryMoment> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        return body.getData();
    }

    public void deleteMoment(long id) throws Exception {
        Response<ApiResponse<Void>> resp = service.deleteMoment(id).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<Void> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
    }

    // Locations
    public List<VisitedLocation> getLocations() throws Exception {
        Response<ApiResponse<List<VisitedLocation>>> resp = service.getLocations().execute();
        Log.d("MemoryRepository", "getLocations: httpCode=" + resp.code());
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<List<VisitedLocation>> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        List<VisitedLocation> list = body.getData();
        if (list != null) {
            for (VisitedLocation loc : list) {
                Log.d("MemoryRepository", "  loc: id=" + loc.getId() + " name=" + loc.getName()
                    + " imageUrl=" + loc.getImageUrl()
                    + " lat=" + loc.getLat() + " lng=" + loc.getLng());
            }
        }
        return list;
    }

    public VisitedLocation createLocation(VisitedLocation loc) throws Exception {
        Response<ApiResponse<VisitedLocation>> resp = service.createLocation(loc).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<VisitedLocation> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        return body.getData();
    }

    public VisitedLocation updateLocation(long id, VisitedLocation loc) throws Exception {
        Response<ApiResponse<VisitedLocation>> resp = service.updateLocation(id, loc).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<VisitedLocation> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
        return body.getData();
    }

    public void deleteLocation(long id) throws Exception {
        Response<ApiResponse<Void>> resp = service.deleteLocation(id).execute();
        if (!resp.isSuccessful()) throw new IOException("Network error");
        ApiResponse<Void> body = resp.body();
        if (body == null || !body.isSuccess()) throw new Exception(body != null ? body.getMessage() : "服务器返回数据异常");
    }
}
