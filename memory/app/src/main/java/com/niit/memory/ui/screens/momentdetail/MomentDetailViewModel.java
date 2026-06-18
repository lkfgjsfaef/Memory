package com.niit.memory.ui.screens.momentdetail;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.niit.memory.data.model.MemoryMoment;
import com.niit.memory.data.repository.MemoryRepository;
import com.niit.memory.util.QiniuHelper;
import com.niit.memory.util.TaskExecutor;
import java.io.File;

public class MomentDetailViewModel extends AndroidViewModel {

    private final MemoryRepository repository;
    public final MutableLiveData<MemoryMoment> moment = new MutableLiveData<>();
    public final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public MomentDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new MemoryRepository(application);
    }

    public void loadMoment(long id) {
        loading.setValue(true);
        TaskExecutor.execute(() -> {
            try {
                MemoryMoment m = repository.getMoment(id);
                moment.postValue(m);
                loading.postValue(false);
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public void updateMoment(long id, String title, String momentDate, String location,
                             String emoji, String photoUrls) {
        loading.postValue(true);
        TaskExecutor.execute(() -> {
            try {
                MemoryMoment m = new MemoryMoment();
                m.setTitle(title);
                m.setMomentDate(momentDate);
                m.setLocation(location);
                m.setEmoji(emoji);
                m.setPhotoUrls(photoUrls);
                MemoryMoment updated = repository.updateMoment(id, m);
                moment.postValue(updated);
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
