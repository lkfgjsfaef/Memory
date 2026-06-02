package com.niit.memory.ui.screens.wishlist;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.niit.memory.data.api.ApiClient;
import com.niit.memory.data.api.QiniuService;
import com.niit.memory.data.model.ApiResponse;
import com.niit.memory.data.model.Wish;
import com.niit.memory.data.repository.WishRepository;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

public class WishlistViewModel extends AndroidViewModel {

    private final WishRepository repository;
    public final MutableLiveData<List<Wish>> wishes = new MutableLiveData<>();
    public final MutableLiveData<Integer> total = new MutableLiveData<>(0);
    public final MutableLiveData<Integer> completed = new MutableLiveData<>(0);
    public final MutableLiveData<Integer> pending = new MutableLiveData<>(0);
    public final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private String currentStatus = null;
    private String currentCategory = null;
    private String currentOwner = "all";

    public WishlistViewModel(@NonNull Application application) {
        super(application);
        repository = new WishRepository(application);
    }

    public void loadWishes(String status, String category) {
        currentStatus = status;
        currentCategory = category;
        loading.postValue(true);
        new Thread(() -> {
            try {
                List<Wish> list = repository.getWishes(status, category);
                wishes.postValue(list != null ? list : new ArrayList<>());
                loadStats();
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        }).start();
    }

    public void setOwnerFilter(String owner) {
        currentOwner = owner;
        // Owner filtering is client-side; re-trigger with current data without server call
        List<Wish> current = wishes.getValue();
        if (current != null) wishes.postValue(current);
    }

    public String getCurrentOwner() { return currentOwner; }

    private void loadStats() {
        try {
            List<Wish> all = repository.getWishes(null, null);
            if (all != null) {
                total.postValue(all.size());
                int comp = 0, pend = 0;
                for (Wish w : all) {
                    if ("completed".equals(w.getStatus())) comp++;
                    else pend++;
                }
                completed.postValue(comp);
                pending.postValue(pend);
            }
        } catch (Exception e) {
            Log.e("WishlistViewModel", "Error loading stats", e);
        }
        loading.postValue(false);
    }

    public void createWish(String title, String description, String category,
                           String status, String author, String startDate, String imageUrls) {
        loading.setValue(true);
        new Thread(() -> {
            try {
                Wish wish = new Wish();
                wish.setTitle(title);
                wish.setDescription(description);
                wish.setCategory(category);
                wish.setStatus(status);
                wish.setAuthor(author);
                wish.setStartDate(startDate);
                wish.setImageUrls(imageUrls);
                repository.createWish(wish);
                loading.postValue(false);
                loadWishes(currentStatus, currentCategory);
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        }).start();
    }

    public void updateWish(long id, String title, String description, String category,
                           String status, String author, String startDate, String imageUrls) {
        loading.setValue(true);
        new Thread(() -> {
            try {
                Wish wish = new Wish();
                wish.setTitle(title);
                wish.setDescription(description);
                wish.setCategory(category);
                wish.setStatus(status);
                wish.setAuthor(author);
                wish.setStartDate(startDate);
                wish.setImageUrls(imageUrls);
                repository.updateWish(id, wish);
                loading.postValue(false);
                loadWishes(currentStatus, currentCategory);
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        }).start();
    }

    public void updateWishStatus(long id, String status) {
        new Thread(() -> {
            try {
                repository.updateStatus(id, status);
                loadWishes(currentStatus, currentCategory);
            } catch (Exception e) {
                errorMessage.postValue(e.getMessage());
            }
        }).start();
    }

    public void deleteWish(long id) {
        new Thread(() -> {
            try {
                repository.deleteWish(id);
                loadWishes(currentStatus, currentCategory);
            } catch (Exception e) {
                errorMessage.postValue(e.getMessage());
            }
        }).start();
    }

    public String uploadImage(File file) throws Exception {
        Log.d("WishVM", "uploadImage: file=" + file.getAbsolutePath() + " size=" + file.length());
        QiniuService qiniu = ApiClient.getInstance(getApplication()).create(QiniuService.class);
        Response<ApiResponse<Map<String, String>>> tokenResp = qiniu.getUploadToken().execute();
        ApiResponse<Map<String, String>> tokenBody = tokenResp.body();
        if (tokenBody == null || !tokenBody.isSuccess() || tokenBody.getData() == null) {
            Log.e("WishVM", "uploadImage: failed to get token, body=" + (tokenBody != null ? tokenBody.getMessage() : "null"));
            throw new Exception("获取上传token失败");
        }
        String token = tokenBody.getData().get("token");
        String domain = tokenBody.getData().get("domain");
        Log.d("WishVM", "uploadImage: got token, domain=" + domain);

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
            if (!uploadResp.isSuccessful()) {
                String errBody = uploadResp.body() != null ? uploadResp.body().string() : "";
                Log.e("WishVM", "uploadImage: Qiniu upload failed code=" + uploadResp.code() + " body=" + errBody);
                throw new Exception("上传失败: " + uploadResp.code());
            }
            String respBody = uploadResp.body() != null ? uploadResp.body().string() : "";
            Log.d("WishVM", "uploadImage: Qiniu response=" + respBody);
            Map<String, Object> result = new com.google.gson.Gson().fromJson(respBody, Map.class);
            if (result == null) throw new Exception("上传响应解析失败");
            String key = (String) result.get("key");
            String finalUrl = domain + "/" + key;
            Log.d("WishVM", "uploadImage: SUCCESS, finalUrl=" + finalUrl);
            return finalUrl;
        } finally {
            uploadResp.close();
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
        }
    }
}
