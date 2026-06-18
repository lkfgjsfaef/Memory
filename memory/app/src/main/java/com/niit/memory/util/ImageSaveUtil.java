package com.niit.memory.util;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.OutputStream;

public class ImageSaveUtil {

    private static final String TAG = "ImageSaveUtil";

    private ImageSaveUtil() {}

    /**
     * Save the currently displayed image from an ImageView to the system gallery.
     */
    public static void saveViewToGallery(Context context, ImageView imageView) {
        if (imageView == null) return;
        Drawable drawable = imageView.getDrawable();
        if (drawable == null) {
            Toast.makeText(context, "图片加载中", Toast.LENGTH_SHORT).show();
            return;
        }

        final Bitmap source = drawableToBitmap(drawable, imageView);
        if (source == null) {
            Toast.makeText(context, "图片保存失败", Toast.LENGTH_SHORT).show();
            return;
        }

        TaskExecutor.execute(() -> {
            try {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME,
                    "memory_" + System.currentTimeMillis() + ".jpg");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/Memory");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.put(MediaStore.Images.Media.IS_PENDING, 1);
                }

                Uri uri = context.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                try (OutputStream os = context.getContentResolver().openOutputStream(uri)) {
                    source.compress(Bitmap.CompressFormat.JPEG, 95, os);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear();
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    context.getContentResolver().update(uri, values, null, null);
                }

                source.recycle();

                TaskExecutor.runOnUiThread(() ->
                    Toast.makeText(context, "已保存到相册", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                Log.e(TAG, "Save failed", e);
                TaskExecutor.runOnUiThread(() ->
                    Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private static Bitmap drawableToBitmap(Drawable drawable, ImageView imageView) {
        if (drawable instanceof BitmapDrawable) {
            Bitmap bm = ((BitmapDrawable) drawable).getBitmap();
            if (bm != null && bm.getWidth() > 0) {
                // Always copy — fixes hardware bitmap crash and ensures thread safety
                return bm.copy(Bitmap.Config.ARGB_8888, false);
            }
        }

        int width = imageView.getWidth();
        int height = imageView.getHeight();
        if (width <= 0) width = imageView.getResources().getDisplayMetrics().widthPixels;
        if (height <= 0) height = imageView.getResources().getDisplayMetrics().heightPixels / 2;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }
}
