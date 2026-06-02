package com.niit.memory.util;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import coil.Coil;
import coil.request.ImageRequest;
import com.niit.memory.R;

public class ImageViewer {

    public static void show(Context context, String imageUrl) {
        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ImageView iv = new ImageView(context);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setBackgroundColor(0xFF000000);
        iv.setOnClickListener(v -> dialog.dismiss());

        ImageRequest request = new ImageRequest.Builder(context)
            .data(imageUrl)
            .target(iv)
            .placeholder(R.drawable.image_placeholder)
            .error(R.drawable.image_placeholder)
            .build();
        Coil.imageLoader(context).enqueue(request);

        dialog.setContentView(iv);
        dialog.show();
    }
}
