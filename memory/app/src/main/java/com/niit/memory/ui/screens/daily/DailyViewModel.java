package com.niit.memory.ui.screens.daily;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.niit.memory.data.api.ApiClient;
import com.niit.memory.data.api.QiniuService;
import com.niit.memory.data.model.ApiResponse;
import com.niit.memory.data.model.DailyRecord;
import com.niit.memory.data.repository.DailyRepository;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

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
        new Thread(() -> {
            try {
                List<DailyRecord> list = repository.getRecords(year, month);
                records.postValue(list != null ? list : new ArrayList<>());
                loadStats();
                loadLoveDays();
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        }).start();
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
            // Love days is optional, don't show error
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
        new Thread(() -> {
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
        }).start();
    }

    public String uploadImage(File file) throws Exception {
        QiniuService qiniu = ApiClient.getInstance(getApplication()).create(QiniuService.class);
        Response<ApiResponse<Map<String, String>>> tokenResp = qiniu.getUploadToken().execute();
        ApiResponse<Map<String, String>> tokenBody = tokenResp.body();
        if (tokenBody == null || !tokenBody.isSuccess() || tokenBody.getData() == null) {
            throw new Exception("获取上传token失败");
        }
        String token = tokenBody.getData().get("token");
        String domain = tokenBody.getData().get("domain");

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

    public void updateRecord(long id, String title, String content, String author,
                             String location, String mood, String moodIcon,
                             String recordDate, String imageUrls) {
        loading.setValue(true);
        new Thread(() -> {
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
        }).start();
    }

    public void deleteRecord(long id) {
        new Thread(() -> {
            try {
                repository.deleteRecord(id);
                loadRecords(currentYear.getValue(), currentMonth.getValue());
            } catch (Exception e) {
                errorMessage.postValue(e.getMessage());
            }
        }).start();
    }
}
