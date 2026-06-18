package com.niit.memory.data.repository;

import android.content.Context;
import com.niit.memory.data.api.*;
import com.niit.memory.data.model.*;
import java.util.List;

public class MemoryRepository {
    private final MemoryService service;

    public MemoryRepository(Context context) {
        this.service = ApiClient.getInstance(context).create(MemoryService.class);
    }

    public List<MemoryAlbum> getAlbums() throws Exception {
        return ApiResponse.parseResponse(service.getAlbums().execute());
    }

    public MemoryAlbum getAlbum(long id) throws Exception {
        return ApiResponse.parseResponse(service.getAlbum(id).execute());
    }

    public void createAlbum(MemoryAlbum album) throws Exception {
        ApiResponse.validateResponse(service.createAlbum(album).execute());
    }

    public MemoryAlbum updateAlbum(long id, MemoryAlbum album) throws Exception {
        return ApiResponse.parseResponse(service.updateAlbum(id, album).execute());
    }

    public void deleteAlbum(long id) throws Exception {
        ApiResponse.validateResponse(service.deleteAlbum(id).execute());
    }

    public List<TimelineGroup> getMoments() throws Exception {
        List<TimelineGroup> groups = ApiResponse.parseResponse(service.getMoments().execute());
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
        return ApiResponse.parseResponse(service.getMoment(id).execute());
    }

    public void createMoment(MemoryMoment moment) throws Exception {
        ApiResponse.validateResponse(service.createMoment(moment).execute());
    }

    public MemoryMoment updateMoment(long id, MemoryMoment moment) throws Exception {
        return ApiResponse.parseResponse(service.updateMoment(id, moment).execute());
    }

    public void deleteMoment(long id) throws Exception {
        ApiResponse.validateResponse(service.deleteMoment(id).execute());
    }

    public List<VisitedLocation> getLocations() throws Exception {
        return ApiResponse.parseResponse(service.getLocations().execute());
    }

    public VisitedLocation createLocation(VisitedLocation loc) throws Exception {
        return ApiResponse.parseResponse(service.createLocation(loc).execute());
    }

    public VisitedLocation updateLocation(long id, VisitedLocation loc) throws Exception {
        return ApiResponse.parseResponse(service.updateLocation(id, loc).execute());
    }

    public void deleteLocation(long id) throws Exception {
        ApiResponse.validateResponse(service.deleteLocation(id).execute());
    }
}
