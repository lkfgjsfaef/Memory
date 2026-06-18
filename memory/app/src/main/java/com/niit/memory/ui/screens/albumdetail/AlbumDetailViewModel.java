package com.niit.memory.ui.screens.albumdetail;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.niit.memory.data.model.MemoryAlbum;
import com.niit.memory.data.repository.MemoryRepository;
import com.niit.memory.util.QiniuHelper;
import com.niit.memory.util.TaskExecutor;
import java.io.File;

public class AlbumDetailViewModel extends AndroidViewModel {

    private final MemoryRepository repository;
    public final MutableLiveData<MemoryAlbum> album = new MutableLiveData<>();
    public final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AlbumDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new MemoryRepository(application);
    }

    public void loadAlbum(long id) {
        loading.setValue(true);
        TaskExecutor.execute(() -> {
            try {
                MemoryAlbum a = repository.getAlbum(id);
                album.postValue(a);
                loading.postValue(false);
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public void updateAlbum(long id, String location, String albumDate, String emoji,
                            String coverUrl, String photoUrls, Integer isPrivate) {
        loading.postValue(true);
        TaskExecutor.execute(() -> {
            try {
                MemoryAlbum a = new MemoryAlbum();
                a.setLocation(location);
                a.setAlbumDate(albumDate);
                a.setEmoji(emoji);
                a.setCoverUrl(coverUrl);
                a.setPhotoUrls(photoUrls);
                a.setIsPrivate(isPrivate);
                MemoryAlbum updated = repository.updateAlbum(id, a);
                album.postValue(updated);
                loading.postValue(false);
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public String uploadImage(File file) throws Exception {
        return QiniuHelper.uploadImage(getApplication(), file);
    }
}
