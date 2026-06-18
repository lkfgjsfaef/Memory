package com.niit.memory.util;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.niit.memory.data.api.ApiClient;
import com.niit.memory.data.api.QiniuService;
import com.niit.memory.data.model.ApiResponse;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Response;

public class QiniuHelper {

    private static final String TAG = "QiniuHelper";
    private static final String UPLOAD_URL = "https://upload-as0.qiniup.com";
    private static final Gson GSON = new Gson();

    private static volatile OkHttpClient uploadClient;

    private static OkHttpClient getUploadClient() {
        if (uploadClient == null) {
            synchronized (QiniuHelper.class) {
                if (uploadClient == null) {
                    uploadClient = new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .writeTimeout(60, TimeUnit.SECONDS)
                        .build();
                }
            }
        }
        return uploadClient;
    }

    /**
     * Upload an image file to Qiniu cloud storage.
     * @return the CDN URL of the uploaded image
     */
    public static String uploadImage(Context context, File file) throws Exception {
        QiniuService qiniu = ApiClient.getInstance(context).create(QiniuService.class);
        Response<ApiResponse<Map<String, String>>> tokenResp = qiniu.getUploadToken().execute();
        ApiResponse<Map<String, String>> tokenBody = tokenResp.body();
        if (tokenBody == null || !tokenBody.isSuccess() || tokenBody.getData() == null) {
            throw new IOException("获取上传token失败");
        }
        String token = tokenBody.getData().get("token");
        String domain = tokenBody.getData().get("domain");

        RequestBody fileBody = RequestBody.create(file, MediaType.parse("image/*"));
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), fileBody);

        okhttp3.Request uploadReq = new okhttp3.Request.Builder()
            .url(UPLOAD_URL)
            .post(new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(part)
                .addFormDataPart("token", token)
                .build())
            .build();

        okhttp3.Response uploadResp = getUploadClient().newCall(uploadReq).execute();
        try {
            if (!uploadResp.isSuccessful()) {
                String errBody = uploadResp.body() != null ? uploadResp.body().string() : "";
                throw new IOException("上传失败: " + uploadResp.code() + " " + errBody);
            }
            String respBody = uploadResp.body() != null ? uploadResp.body().string() : "";
            Map<String, Object> result = GSON.fromJson(respBody, Map.class);
            if (result == null) throw new IOException("上传响应解析失败");
            String key = (String) result.get("key");
            return domain + "/" + key;
        } finally {
            uploadResp.close();
        }
    }

    /**
     * Silently delete an image from Qiniu cloud storage.
     * Runs on a background thread, does not block or show errors.
     */
    public static void deleteImageSilently(Context context, String url) {
        if (url == null || url.isEmpty()) return;
        TaskExecutor.execute(() -> {
            try {
                QiniuService service = ApiClient.getInstance(context).create(QiniuService.class);
                service.deleteByUrl(url).execute();
                Log.d(TAG, "Deleted from Qiniu: " + url);
            } catch (Exception e) {
                Log.w(TAG, "Failed to delete from Qiniu: " + url + " — " + e.getMessage());
            }
        });
    }
}
