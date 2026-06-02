package com.niit.memory.util;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import coil.Coil;
import coil.request.ImageRequest;
import com.niit.memory.R;
import java.util.ArrayList;
import java.util.List;

public class ImageViewer {

    public static void show(Context context, String imageUrl) {
        List<String> list = new ArrayList<>();
        list.add(imageUrl);
        show(context, list, 0);
    }

    public static void show(Context context, List<String> imageUrls, int position) {
        if (imageUrls == null || imageUrls.isEmpty()) return;
        int safePosition = Math.max(0, Math.min(position, imageUrls.size() - 1));

        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);

        FrameLayout root = new FrameLayout(context);
        root.setBackgroundColor(0xFF000000);

        ViewPager2 pager = new ViewPager2(context);
        pager.setOffscreenPageLimit(1);
        pager.setAdapter(new ImagePageAdapter(context, imageUrls, () -> dialog.dismiss()));
        pager.setCurrentItem(safePosition, false);
        root.addView(pager, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Page indicator
        TextView indicator = new TextView(context);
        indicator.setTextColor(0xCCFFFFFF);
        indicator.setTextSize(14);
        FrameLayout.LayoutParams indicatorParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        indicatorParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        indicatorParams.topMargin = 48;
        root.addView(indicator, indicatorParams);

        // Close button
        TextView closeBtn = new TextView(context);
        closeBtn.setText("✕");
        closeBtn.setTextColor(0xCCFFFFFF);
        closeBtn.setTextSize(20);
        closeBtn.setPadding(32, 32, 32, 32);
        closeBtn.setOnClickListener(v -> dialog.dismiss());
        FrameLayout.LayoutParams closeParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        closeParams.gravity = Gravity.TOP | Gravity.END;
        root.addView(closeBtn, closeParams);

        // Update indicator on page change
        if (imageUrls.size() > 1) {
            indicator.setText((safePosition + 1) + " / " + imageUrls.size());
            pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int pos) {
                    indicator.setText((pos + 1) + " / " + imageUrls.size());
                }
            });
        } else {
            indicator.setVisibility(View.GONE);
        }

        dialog.setContentView(root);
        dialog.show();
    }

    private static class ImagePageAdapter extends RecyclerView.Adapter<ImagePageAdapter.ViewHolder> {

        private final Context context;
        private final List<String> urls;
        private final Runnable onDismiss;
        private final int maxWidth;
        private final int maxHeight;

        ImagePageAdapter(Context context, List<String> urls, Runnable onDismiss) {
            this.context = context;
            this.urls = urls;
            this.onDismiss = onDismiss;
            android.graphics.Point size = new android.graphics.Point();
            android.view.WindowManager wm = (android.view.WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getSize(size);
            this.maxWidth = size.x / 2;
            this.maxHeight = size.y / 2;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView iv = new ImageView(parent.getContext());
            iv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            return new ViewHolder(iv);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
            Coil.imageLoader(context).enqueue(
                new ImageRequest.Builder(context)
                    .data(urls.get(pos))
                    .target(holder.imageView)
                    .size(maxWidth, maxHeight)
                    .crossfade(true)
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_placeholder)
                    .build()
            );
            holder.imageView.setOnClickListener(v -> onDismiss.run());
        }

        @Override
        public int getItemCount() { return urls.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView imageView;
            ViewHolder(ImageView iv) {
                super(iv);
                imageView = iv;
            }
        }
    }
}
