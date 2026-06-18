package com.niit.memory.ui.screens.daily;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.niit.memory.data.model.DailyRecord;
import com.niit.memory.data.repository.DailyRepository;
import com.niit.memory.util.QiniuHelper;
import com.niit.memory.util.TaskExecutor;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DailyViewModel extends AndroidViewModel {

    private final DailyRepository repository;
    public final MutableLiveData<List<DailyRecord>> records = new MutableLiveData<>();
    public final MutableLiveData<Integer> total = new MutableLiveData<>(0);
    public final MutableLiveData<Integer> monthCount = new MutableLiveData<>(0);
    public final MutableLiveData<Integer> streak = new MutableLiveData<>(0);
    public final MutableLiveData<Long> loveDays = new MutableLiveData<>(0L);
    public final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public final MutableLiveData<Integer> currentYear = new MutableLiveData<>();
    public final MutableLiveData<Integer> currentMonth = new MutableLiveData<>();

    public DailyViewModel(@NonNull Application application) {
        super(application);
        repository = new DailyRepository(application);
    }

    public void loadRecords(Integer year, Integer month) {
        currentYear.postValue(year);
        currentMonth.postValue(month);
        loading.postValue(true);
        TaskExecutor.execute(() -> {
            try {
                List<DailyRecord> list = repository.getRecords(year, month);
                records.postValue(list != null ? list : new ArrayList<>());
                loadStats();
                loadLoveDays();
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    private void loadLoveDays() {
        try {
            com.niit.memory.data.repository.HomeRepository homeRepo =
                new com.niit.memory.data.repository.HomeRepository(getApplication());
            com.niit.memory.data.model.CoupleResponse cr = homeRepo.getCouple();
            if (cr != null && cr.getLoveDays() != null) {
                loveDays.postValue(cr.getLoveDays());
            }
        } catch (Exception e) {
            android.util.Log.w("DailyViewModel", "Failed to load love days", e);
        }
    }

    private void loadStats() {
        try {
            List<DailyRecord> all = repository.getRecords(null, null);
            total.postValue(all != null ? all.size() : 0);

            Integer month = currentMonth.getValue();
            if (month != null) {
                List<DailyRecord> monthly = repository.getRecords(null, month);
                monthCount.postValue(monthly != null ? monthly.size() : 0);
            } else {
                monthCount.postValue(all != null ? all.size() : 0);
            }
        } catch (Exception e) {
            errorMessage.postValue(e.getMessage());
        }
        loading.postValue(false);
    }

    public void createRecord(String title, String content, String author,
                             String location, String mood, String moodIcon,
                             String recordDate, String imageUrls) {
        loading.setValue(true);
        TaskExecutor.execute(() -> {
            try {
                DailyRecord record = new DailyRecord();
                record.setTitle(title);
                record.setContent(content);
                record.setAuthor(author);
                record.setLocation(location);
                record.setMood(mood);
                record.setMoodIcon(moodIcon);
                record.setRecordDate(recordDate);
                record.setImageUrls(imageUrls);
                repository.createRecord(record);
                loading.postValue(false);
                loadRecords(currentYear.getValue(), currentMonth.getValue());
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public String uploadImage(File file) throws Exception {
        return QiniuHelper.uploadImage(getApplication(), file);
    }

    public void updateRecord(long id, String title, String content, String author,
                             String location, String mood, String moodIcon,
                             String recordDate, String imageUrls) {
        loading.setValue(true);
        TaskExecutor.execute(() -> {
            try {
                DailyRecord record = new DailyRecord();
                record.setTitle(title);
                record.setContent(content);
                record.setAuthor(author);
                record.setLocation(location);
                record.setMood(mood);
                record.setMoodIcon(moodIcon);
                record.setRecordDate(recordDate);
                record.setImageUrls(imageUrls);
                repository.updateRecord(id, record);
                loading.postValue(false);
                loadRecords(currentYear.getValue(), currentMonth.getValue());
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public void deleteRecord(long id) {
        TaskExecutor.execute(() -> {
            try {
                repository.deleteRecord(id);
                loadRecords(currentYear.getValue(), currentMonth.getValue());
            } catch (Exception e) {
                errorMessage.postValue(e.getMessage());
            }
        });
    }
}
