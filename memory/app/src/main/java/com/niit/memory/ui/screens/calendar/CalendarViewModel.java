package com.niit.memory.ui.screens.calendar;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.niit.memory.data.model.CalendarMood;
import com.niit.memory.data.model.CalendarNote;
import com.niit.memory.data.model.ImportantDate;
import com.niit.memory.data.repository.CalendarRepository;
import com.niit.memory.data.repository.HomeRepository;
import java.util.ArrayList;
import java.util.List;

public class CalendarViewModel extends AndroidViewModel {

    private final CalendarRepository repository;
    private final HomeRepository homeRepository;
    public final MutableLiveData<Integer> currentYear = new MutableLiveData<>();
    public final MutableLiveData<Integer> currentMonth = new MutableLiveData<>();
    public final MutableLiveData<List<CalendarNote>> notes = new MutableLiveData<>();
    public final MutableLiveData<List<CalendarMood>> monthMoods = new MutableLiveData<>();
    public final MutableLiveData<List<ImportantDate>> importantDates = new MutableLiveData<>();
    public final MutableLiveData<String> selectedDate = new MutableLiveData<>();
    public final MutableLiveData<String> todayMood = new MutableLiveData<>();
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public CalendarViewModel(@NonNull Application application) {
        super(application);
        repository = new CalendarRepository(application);
        homeRepository = new HomeRepository(application);
    }

    public void loadNotes(int year, int month) {
        // Use setValue when on the main thread so values are visible before the worker starts
        currentYear.setValue(year);
        currentMonth.setValue(month);
        loading.setValue(true);
        new Thread(() -> {
            try {
                List<CalendarNote> list = repository.getNotes(year, month);
                notes.postValue(list != null ? list : new ArrayList<>());

                List<CalendarMood> moods = repository.getMoods(year, month);
                monthMoods.postValue(moods != null ? moods : new ArrayList<>());

                List<ImportantDate> dates = homeRepository.getImportantDates();
                importantDates.postValue(dates != null ? dates : new ArrayList<>());

                loading.postValue(false);
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        }).start();
    }

    private void reloadNotesInBackground(int year, int month) {
        try {
            List<CalendarNote> list = repository.getNotes(year, month);
            notes.postValue(list != null ? list : new ArrayList<>());

            List<CalendarMood> moods = repository.getMoods(year, month);
            monthMoods.postValue(moods != null ? moods : new ArrayList<>());

            List<ImportantDate> dates = homeRepository.getImportantDates();
            importantDates.postValue(dates != null ? dates : new ArrayList<>());
        } catch (Exception e) {
            errorMessage.postValue(e.getMessage());
        }
    }

    public List<CalendarMood> getMoodsForDate(String date) {
        List<CalendarMood> all = monthMoods.getValue();
        List<CalendarMood> result = new ArrayList<>();
        if (all != null) {
            for (CalendarMood m : all) {
                if (m.getMoodDate() != null && m.getMoodDate().equals(date)) {
                    result.add(m);
                }
            }
        }
        return result;
    }

    public void addNote(String date, String text, String icon) {
        loading.setValue(true);
        new Thread(() -> {
            try {
                int y = currentYear.getValue() != null ? currentYear.getValue() : 0;
                int m = currentMonth.getValue() != null ? currentMonth.getValue() : 0;
                CalendarNote note = new CalendarNote();
                note.setNoteDate(date);
                note.setText(text);
                note.setIcon(icon);
                note.setYear(y);
                note.setMonth(m);
                repository.createNote(note);
                reloadNotesInBackground(y, m);
                loading.postValue(false);
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        }).start();
    }

    public void deleteNote(long id) {
        loading.setValue(true);
        new Thread(() -> {
            try {
                repository.deleteNote(id);
                int year = currentYear.getValue() != null ? currentYear.getValue() : 0;
                int month = currentMonth.getValue() != null ? currentMonth.getValue() : 0;
                reloadNotesInBackground(year, month);
                loading.postValue(false);
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        }).start();
    }

    public void setMood(String date, String mood, String moodIcon) {
        new Thread(() -> {
            try {
                CalendarMood cm = new CalendarMood();
                cm.setMoodDate(date);
                cm.setMood(mood);
                cm.setMoodIcon(moodIcon);
                repository.upsertMood(cm);
                todayMood.postValue(moodIcon);
                // Reload moods so calendar cells update immediately
                int y = currentYear.getValue() != null ? currentYear.getValue() : 0;
                int m = currentMonth.getValue() != null ? currentMonth.getValue() : 0;
                List<CalendarMood> moods = repository.getMoods(y, m);
                monthMoods.postValue(moods != null ? moods : new ArrayList<>());
            } catch (Exception e) {
                errorMessage.postValue(e.getMessage());
            }
        }).start();
    }

    public void deleteImportantDate(long id) {
        loading.setValue(true);
        new Thread(() -> {
            try {
                homeRepository.deleteImportantDate(id);
                List<ImportantDate> dates = homeRepository.getImportantDates();
                importantDates.postValue(dates != null ? dates : new ArrayList<>());
                loading.postValue(false);
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        }).start();
    }

    public List<CalendarNote> getNotesForDate(String date) {
        List<CalendarNote> all = notes.getValue();
        List<CalendarNote> result = new ArrayList<>();
        if (all != null) {
            for (CalendarNote n : all) {
                if (n.getNoteDate() != null && n.getNoteDate().equals(date)) {
                    result.add(n);
                }
            }
        }
        return result;
    }
}
