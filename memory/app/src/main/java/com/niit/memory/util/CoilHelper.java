package com.niit.memory.util;

import android.content.Context;
import android.widget.ImageView;
import coil.Coil;
import coil.request.ImageRequest;

public class CoilHelper {

    private CoilHelper() {}

    public static void loadImage(Context context, String url, ImageView imageView,
                                 int placeholder, int error) {
        if (url == null || url.isEmpty()) return;
        ImageRequest req = new ImageRequest.Builder(context)
            .data(url)
            .target(imageView)
            .placeholder(placeholder)
            .error(error)
            .build();
        Coil.imageLoader(context).enqueue(req);
    }
}
