package com.niit.memory.util;

import android.content.Context;
import android.util.Log;
import com.niit.memory.data.api.ApiClient;
import com.niit.memory.data.api.QiniuService;

public class QiniuHelper {

    private static final String TAG = "QiniuHelper";

    /**
     * Silently delete an image from Qiniu cloud storage.
     * Runs on a background thread, does not block or show errors.
     */
    public static void deleteImageSilently(Context context, String url) {
        if (url == null || url.isEmpty()) return;
        new Thread(() -> {
            try {
                QiniuService service = ApiClient.getInstance(context).create(QiniuService.class);
                service.deleteByUrl(url).execute();
                Log.d(TAG, "Deleted from Qiniu: " + url);
            } catch (Exception e) {
                Log.w(TAG, "Failed to delete from Qiniu: " + url + " — " + e.getMessage());
            }
        }).start();
    }
}
