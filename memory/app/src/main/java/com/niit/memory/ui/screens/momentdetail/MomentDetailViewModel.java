package com.niit.memory.ui.screens.momentdetail;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.niit.memory.data.model.ApiResponse;
import com.niit.memory.data.model.MemoryMoment;
import com.niit.memory.data.repository.MemoryRepository;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import com.google.gson.Gson;
import com.niit.memory.data.api.ApiClient;
import com.niit.memory.data.api.QiniuService;

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
        new Thread(() -> {
            try {
                MemoryMoment m = repository.getMoment(id);
                moment.postValue(m);
                loading.postValue(false);
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        }).start();
    }

    public void updateMoment(long id, String title, String momentDate, String location,
                             String emoji, String photoUrls) {
        loading.postValue(true);
        new Thread(() -> {
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
        }).start();
    }

    private okhttp3.OkHttpClient uploadClient;

    private okhttp3.OkHttpClient getUploadClient() {
        if (uploadClient == null) {
            uploadClient = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        }
        return uploadClient;
    }

    public String uploadImage(File file) throws Exception {
        QiniuService qiniu = ApiClient.getInstance(getApplication()).create(QiniuService.class);
        retrofit2.Response<ApiResponse<Map<String, String>>> tokenResp = qiniu.getUploadToken().execute();
        ApiResponse<Map<String, String>> tokenBody = tokenResp.body();
        if (tokenBody == null || !tokenBody.isSuccess() || tokenBody.getData() == null) {
            throw new IOException("获取上传token失败");
        }
        String token = tokenBody.getData().get("token");
        String domain = tokenBody.getData().get("domain");

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
        okhttp3.Response uploadResp = getUploadClient().newCall(uploadReq).execute();
        if (!uploadResp.isSuccessful()) {
            String errBody = uploadResp.body() != null ? uploadResp.body().string() : "";
            uploadResp.close();
            throw new IOException("上传失败: " + uploadResp.code() + " " + errBody);
        }
        String uploadBodyStr = uploadResp.body() != null ? uploadResp.body().string() : "";
        uploadResp.close();
        Map<String, Object> result = new Gson().fromJson(uploadBodyStr, Map.class);
        if (result == null) throw new IOException("上传响应解析失败");
        String key = (String) result.get("key");
        return domain + "/" + key;
    }
}
