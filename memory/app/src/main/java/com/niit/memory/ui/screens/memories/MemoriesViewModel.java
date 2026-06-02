package com.niit.memory.ui.screens.memories;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.niit.memory.data.api.ApiClient;
import com.niit.memory.data.api.QiniuService;
import com.niit.memory.data.model.*;
import com.niit.memory.data.repository.MemoryRepository;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

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
        new Thread(() -> {
            try {
                List<MemoryAlbum> list = repository.getAlbums();
                albums.postValue(list != null ? list : new ArrayList<>());
                loading.postValue(false);
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        }).start();
    }

    public void loadMoments() {
        loading.postValue(true);
        new Thread(() -> {
            try {
                List<TimelineGroup> list = repository.getMoments();
                moments.postValue(list != null ? list : new ArrayList<>());
                loading.postValue(false);
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        }).start();
    }

    public void loadLocations() {
        loading.postValue(true);
        new Thread(() -> {
            try {
                List<VisitedLocation> list = repository.getLocations();
                locations.postValue(list != null ? list : new ArrayList<>());
                loading.postValue(false);
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        }).start();
    }

    public void createAlbum(String location, String albumDate, String emoji,
                            String coverUrl, String photoUrls, Integer isPrivate) {
        loading.setValue(true);
        new Thread(() -> {
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
        }).start();
    }

    public void updateAlbum(long id, String location, String albumDate, String emoji,
                            String coverUrl, String photoUrls, Integer isPrivate) {
        loading.setValue(true);
        new Thread(() -> {
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
        }).start();
    }

    public void deleteAlbum(long id) {
        new Thread(() -> {
            try {
                repository.deleteAlbum(id);
                loadAlbums();
            } catch (Exception e) {
                errorMessage.postValue(e.getMessage());
            }
        }).start();
    }

    public void createMoment(String title, String momentDate, String location,
                             String emoji, String photoUrls) {
        loading.setValue(true);
        new Thread(() -> {
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
        }).start();
    }

    public void updateMoment(long id, String title, String momentDate, String location,
                             String emoji, String photoUrls) {
        loading.setValue(true);
        new Thread(() -> {
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
        }).start();
    }

    public void deleteMoment(long id) {
        new Thread(() -> {
            try {
                repository.deleteMoment(id);
                loadMoments();
            } catch (Exception e) {
                errorMessage.postValue(e.getMessage());
            }
        }).start();
    }

    public void createLocation(String name, String province, String visitDate,
                               String title, String imageUrl, Double lat, Double lng) {
        loading.setValue(true);
        new Thread(() -> {
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
        }).start();
    }

    public void updateLocation(long id, String name, String province, String visitDate,
                               String title, String imageUrl, Double lat, Double lng) {
        loading.setValue(true);
        new Thread(() -> {
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
        }).start();
    }

    public void deleteLocation(long id) {
        new Thread(() -> {
            try {
                repository.deleteLocation(id);
                loadLocations();
            } catch (Exception e) {
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
        Log.d("MemoriesVM", "uploadImage: file=" + file.getAbsolutePath() + " size=" + file.length());
        QiniuService qiniu = ApiClient.getInstance(getApplication()).create(QiniuService.class);
        Response<ApiResponse<Map<String, String>>> tokenResp = qiniu.getUploadToken().execute();
        ApiResponse<Map<String, String>> tokenBody = tokenResp.body();
        if (tokenBody == null || !tokenBody.isSuccess() || tokenBody.getData() == null) {
            Log.e("MemoriesVM", "uploadImage: failed to get upload token, body=" + (tokenBody != null ? tokenBody.getMessage() : "null"));
            throw new Exception("获取上传token失败");
        }
        String token = tokenBody.getData().get("token");
        String domain = tokenBody.getData().get("domain");
        Log.d("MemoriesVM", "uploadImage: got token, domain=" + domain);

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
            Log.e("MemoriesVM", "uploadImage: Qiniu upload failed code=" + uploadResp.code() + " body=" + errBody);
            throw new Exception("上传失败: " + uploadResp.code() + " " + errBody);
        }
        String respBody = uploadResp.body() != null ? uploadResp.body().string() : "";
        uploadResp.close();
        Log.d("MemoriesVM", "uploadImage: Qiniu response=" + respBody);
        Map<String, Object> result = new com.google.gson.Gson().fromJson(respBody, Map.class);
        if (result == null) throw new Exception("上传响应解析失败");
        String key = (String) result.get("key");
        String finalUrl = domain + "/" + key;
        Log.d("MemoriesVM", "uploadImage: SUCCESS, finalUrl=" + finalUrl);
        return finalUrl;
    }
}
