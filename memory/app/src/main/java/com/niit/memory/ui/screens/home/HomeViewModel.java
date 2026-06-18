package com.niit.memory.ui.screens.home;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.niit.memory.data.model.*;
import com.niit.memory.data.repository.AuthRepository;
import com.niit.memory.data.repository.HomeRepository;
import com.niit.memory.util.QiniuHelper;
import com.niit.memory.util.TaskExecutor;
import java.io.File;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private static final String TAG = "HomeViewModel";
    private final HomeRepository repository;
    private final AuthRepository authRepository;
    public final MutableLiveData<CoupleResponse> couple = new MutableLiveData<>();
    public final MutableLiveData<List<ImportantDate>> importantDates = new MutableLiveData<>();
    public final MutableLiveData<List<CalendarNote>> calendarNotes = new MutableLiveData<>();
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new HomeRepository(application);
        authRepository = new AuthRepository(application);
    }

    public void loadData() {
        loading.postValue(true);
        TaskExecutor.execute(() -> {
            try {
                CoupleResponse c = repository.getCouple();
                List<ImportantDate> dates = repository.getImportantDates();
                couple.postValue(c);
                importantDates.postValue(dates);
                loading.postValue(false);
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public void createImportantDate(String title, String icon, String eventDate,
                                    String lunarDate, String note, int recurring,
                                    Integer recurringMonth, Integer recurringDay) {
        loading.setValue(true);
        TaskExecutor.execute(() -> {
            try {
                ImportantDate date = new ImportantDate();
                date.setTitle(title);
                date.setIcon(icon);
                date.setEventDate(eventDate);
                date.setLunarDate(lunarDate);
                date.setNote(note);
                date.setRecurring(recurring);
                date.setRecurringMonth(recurringMonth);
                date.setRecurringDay(recurringDay);
                repository.createImportantDate(date);
                loadData();
                loading.postValue(false);
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public void updateImportantDate(long id, String title, String icon, String eventDate,
                                    String lunarDate, String note, int recurring,
                                    Integer recurringMonth, Integer recurringDay) {
        loading.setValue(true);
        TaskExecutor.execute(() -> {
            try {
                ImportantDate date = new ImportantDate();
                date.setId(id);
                date.setTitle(title);
                date.setIcon(icon);
                date.setEventDate(eventDate);
                date.setLunarDate(lunarDate);
                date.setNote(note);
                date.setRecurring(recurring);
                date.setRecurringMonth(recurringMonth);
                date.setRecurringDay(recurringDay);
                repository.updateImportantDate(id, date);
                loadData();
                loading.postValue(false);
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public void deleteImportantDate(long id) {
        loading.setValue(true);
        TaskExecutor.execute(() -> {
            try {
                repository.deleteImportantDate(id);
                loadData();
                loading.postValue(false);
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public void addCalendarNote(String noteDate, String text, String icon, int year, int month) {
        loading.setValue(true);
        TaskExecutor.execute(() -> {
            try {
                CalendarNote note = new CalendarNote();
                note.setNoteDate(noteDate);
                note.setText(text);
                note.setIcon(icon);
                note.setYear(year);
                note.setMonth(month);
                repository.createCalendarNote(note);
                loadCalendarNotes(year, month);
                loading.postValue(false);
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public void loadCalendarNotes(int year, int month) {
        TaskExecutor.execute(() -> {
            try {
                List<CalendarNote> notes = repository.getCalendarNotes(year, month);
                calendarNotes.postValue(notes);
            } catch (Exception e) {
                Log.e(TAG, "Error loading calendar notes", e);
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public String uploadImage(File file) throws Exception {
        return QiniuHelper.uploadImage(getApplication(), file);
    }

    public void updateAvatar(String avatarUrl) {
        TaskExecutor.execute(() -> {
            try {
                authRepository.updateAvatar(avatarUrl);
            } catch (Exception e) {
                Log.e(TAG, "Error updating avatar", e);
                errorMessage.postValue(e.getMessage());
            }
        });
    }
}
