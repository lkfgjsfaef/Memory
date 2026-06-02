package com.niit.memory.ui.screens.home;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.niit.memory.data.api.ApiClient;
import com.niit.memory.data.api.QiniuService;
import com.niit.memory.data.model.*;
import com.niit.memory.data.repository.AuthRepository;
import com.niit.memory.data.repository.HomeRepository;
import java.io.File;
import java.util.List;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

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
        new Thread(() -> {
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
        }).start();
    }

    public void createImportantDate(String title, String icon, String eventDate,
                                    String lunarDate, String note, int recurring,
                                    Integer recurringMonth, Integer recurringDay) {
        loading.setValue(true);
        new Thread(() -> {
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
        }).start();
    }

    public void updateImportantDate(long id, String title, String icon, String eventDate,
                                    String lunarDate, String note, int recurring,
                                    Integer recurringMonth, Integer recurringDay) {
        loading.setValue(true);
        new Thread(() -> {
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
        }).start();
    }

    public void deleteImportantDate(long id) {
        loading.setValue(true);
        new Thread(() -> {
            try {
                repository.deleteImportantDate(id);
                loadData();
                loading.postValue(false);
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        }).start();
    }

    public void addCalendarNote(String noteDate, String text, String icon, int year, int month) {
        loading.setValue(true);
        new Thread(() -> {
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
        }).start();
    }

    public void loadCalendarNotes(int year, int month) {
        new Thread(() -> {
            try {
                List<CalendarNote> notes = repository.getCalendarNotes(year, month);
                calendarNotes.postValue(notes);
            } catch (Exception e) {
                Log.e(TAG, "Error loading calendar notes", e);
                errorMessage.postValue(e.getMessage());
            }
        }).start();
    }

    public String uploadImage(File file) throws Exception {
        // Get upload token
        QiniuService qiniu = ApiClient.getInstance(getApplication()).create(QiniuService.class);
        Response<ApiResponse<Map<String, String>>> tokenResp = qiniu.getUploadToken().execute();
        ApiResponse<Map<String, String>> tokenBody = tokenResp.body();
        if (tokenBody == null || !tokenBody.isSuccess() || tokenBody.getData() == null) {
            throw new Exception("获取上传token失败");
        }
        String token = tokenBody.getData().get("token");
        String domain = tokenBody.getData().get("domain");

        // Upload file to Qiniu
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build();
        RequestBody fileBody = RequestBody.create(file, MediaType.parse("image/*"));
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), fileBody);

        okhttp3.Request uploadReq = new okhttp3.Request.Builder()
            .url("https://upload-as0.qiniup.com")
            .post(new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(part)
                .addFormDataPart("token", token)
                .build())
            .build();

        okhttp3.Response uploadResp = client.newCall(uploadReq).execute();
        try {
            if (!uploadResp.isSuccessful()) throw new Exception("上传失败: " + uploadResp.code());
            String respBody = uploadResp.body() != null ? uploadResp.body().string() : "";
            com.google.gson.Gson gson = new com.google.gson.Gson();
            Map<String, Object> result = gson.fromJson(respBody, Map.class);
            if (result == null) throw new Exception("上传响应解析失败");
            String key = (String) result.get("key");
            return domain + "/" + key;
        } finally {
            uploadResp.close();
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
        }
    }

    public void updateAvatar(String avatarUrl) {
        new Thread(() -> {
            try {
                authRepository.updateAvatar(avatarUrl);
            } catch (Exception e) {
                Log.e(TAG, "Error updating avatar", e);
                errorMessage.postValue(e.getMessage());
            }
        }).start();
    }
}
