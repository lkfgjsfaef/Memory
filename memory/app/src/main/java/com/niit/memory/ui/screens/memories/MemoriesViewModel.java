package com.niit.memory.ui.screens.memories;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.niit.memory.data.model.*;
import com.niit.memory.data.repository.MemoryRepository;
import com.niit.memory.util.QiniuHelper;
import com.niit.memory.util.TaskExecutor;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MemoriesViewModel extends AndroidViewModel {

    private final MemoryRepository repository;
    public final MutableLiveData<List<MemoryAlbum>> albums = new MutableLiveData<>();
    public final MutableLiveData<List<TimelineGroup>> moments = new MutableLiveData<>();
    public final MutableLiveData<List<VisitedLocation>> locations = new MutableLiveData<>();
    public final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public MemoriesViewModel(@NonNull Application application) {
        super(application);
        repository = new MemoryRepository(application);
    }

    public void loadAlbums() {
        loading.postValue(true);
        TaskExecutor.execute(() -> {
            try {
                List<MemoryAlbum> list = repository.getAlbums();
                albums.postValue(list != null ? list : new ArrayList<>());
                loading.postValue(false);
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public void loadMoments() {
        loading.postValue(true);
        TaskExecutor.execute(() -> {
            try {
                List<TimelineGroup> list = repository.getMoments();
                moments.postValue(list != null ? list : new ArrayList<>());
                loading.postValue(false);
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public void loadLocations() {
        loading.postValue(true);
        TaskExecutor.execute(() -> {
            try {
                List<VisitedLocation> list = repository.getLocations();
                locations.postValue(list != null ? list : new ArrayList<>());
                loading.postValue(false);
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public void createAlbum(String location, String albumDate, String emoji,
                            String coverUrl, String photoUrls, Integer isPrivate) {
        loading.setValue(true);
        TaskExecutor.execute(() -> {
            try {
                MemoryAlbum album = new MemoryAlbum();
                album.setLocation(location);
                album.setAlbumDate(albumDate);
                album.setEmoji(emoji);
                album.setCoverUrl(coverUrl);
                album.setPhotoUrls(photoUrls);
                album.setIsPrivate(isPrivate);
                repository.createAlbum(album);
                loading.postValue(false);
                loadAlbums();
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public void updateAlbum(long id, String location, String albumDate, String emoji,
                            String coverUrl, String photoUrls, Integer isPrivate) {
        loading.setValue(true);
        TaskExecutor.execute(() -> {
            try {
                MemoryAlbum album = new MemoryAlbum();
                album.setLocation(location);
                album.setAlbumDate(albumDate);
                album.setEmoji(emoji);
                album.setCoverUrl(coverUrl);
                album.setPhotoUrls(photoUrls);
                album.setIsPrivate(isPrivate);
                repository.updateAlbum(id, album);
                loading.postValue(false);
                loadAlbums();
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public void deleteAlbum(long id) {
        TaskExecutor.execute(() -> {
            try {
                repository.deleteAlbum(id);
                loadAlbums();
            } catch (Exception e) {
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public void createMoment(String title, String momentDate, String location,
                             String emoji, String photoUrls) {
        loading.setValue(true);
        TaskExecutor.execute(() -> {
            try {
                MemoryMoment moment = new MemoryMoment();
                moment.setTitle(title);
                moment.setMomentDate(momentDate);
                moment.setLocation(location);
                moment.setEmoji(emoji);
                moment.setPhotoUrls(photoUrls);
                repository.createMoment(moment);
                loading.postValue(false);
                loadMoments();
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public void updateMoment(long id, String title, String momentDate, String location,
                             String emoji, String photoUrls) {
        loading.setValue(true);
        TaskExecutor.execute(() -> {
            try {
                MemoryMoment moment = new MemoryMoment();
                moment.setTitle(title);
                moment.setMomentDate(momentDate);
                moment.setLocation(location);
                moment.setEmoji(emoji);
                moment.setPhotoUrls(photoUrls);
                repository.updateMoment(id, moment);
                loading.postValue(false);
                loadMoments();
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public void deleteMoment(long id) {
        TaskExecutor.execute(() -> {
            try {
                repository.deleteMoment(id);
                loadMoments();
            } catch (Exception e) {
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public void createLocation(String name, String province, String visitDate,
                               String title, String imageUrl, Double lat, Double lng) {
        loading.setValue(true);
        TaskExecutor.execute(() -> {
            try {
                VisitedLocation loc = new VisitedLocation();
                loc.setName(name);
                loc.setProvince(province);
                loc.setVisitDate(visitDate);
                loc.setTitle(title);
                loc.setImageUrl(imageUrl);
                loc.setLat(lat);
                loc.setLng(lng);
                repository.createLocation(loc);
                loading.postValue(false);
                loadLocations();
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public void updateLocation(long id, String name, String province, String visitDate,
                               String title, String imageUrl, Double lat, Double lng) {
        loading.setValue(true);
        TaskExecutor.execute(() -> {
            try {
                VisitedLocation loc = new VisitedLocation();
                loc.setName(name);
                loc.setProvince(province);
                loc.setVisitDate(visitDate);
                loc.setTitle(title);
                loc.setImageUrl(imageUrl);
                loc.setLat(lat);
                loc.setLng(lng);
                repository.updateLocation(id, loc);
                loading.postValue(false);
                loadLocations();
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public void deleteLocation(long id) {
        TaskExecutor.execute(() -> {
            try {
                repository.deleteLocation(id);
                loadLocations();
            } catch (Exception e) {
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public String uploadImage(File file) throws Exception {
        return QiniuHelper.uploadImage(getApplication(), file);
    }
}
