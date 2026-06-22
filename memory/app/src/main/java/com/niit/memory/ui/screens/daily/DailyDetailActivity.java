package com.niit.memory.ui.screens.daily;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import coil.Coil;
import coil.request.ImageRequest;
import com.niit.memory.R;
import com.niit.memory.data.api.ApiClient;
import com.niit.memory.data.api.DailyService;
import com.niit.memory.data.model.ApiResponse;
import com.niit.memory.data.model.DailyRecord;
import com.niit.memory.databinding.ActivityDailyDetailBinding;
import com.niit.memory.util.ImageViewer;
import com.niit.memory.util.TaskExecutor;
import java.util.ArrayList;
import java.util.List;

public class DailyDetailActivity extends AppCompatActivity {

    private ActivityDailyDetailBinding binding;
    private List<String> photoList = new ArrayList<>();
    private PhotoAdapter photoAdapter;
    private long recordId;
    private DailyRecord currentRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDailyDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        DailyRecord record = (DailyRecord) getIntent().getSerializableExtra("record");
        if (record == null) {
            Toast.makeText(this, "无法加载记录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentRecord = record;
        recordId = record.getId() != null ? record.getId() : 0;
        displayRecord(record);
    }

    private void displayRecord(DailyRecord r) {
        if (r.getMoodIcon() != null && !r.getMoodIcon().isEmpty()) {
            binding.dailyMoodIcon.setText(r.getMoodIcon());
        } else {
            binding.dailyMoodIcon.setText("📝");
        }
        binding.dailyMoodText.setText(r.getMood() != null ? r.getMood() : "");

        binding.dailyTitle.setText(r.getTitle() != null ? r.getTitle() : "");
        binding.dailyAuthor.setText(r.getAuthor() != null ? r.getAuthor() : "");
        binding.dailyDate.setText(r.getRecordDate() != null ? r.getRecordDate() : "");
        binding.dailyLocation.setText(r.getLocation() != null ? r.getLocation() : "");

        String content = r.getContent();
        if (content != null && !content.isEmpty()) {
            binding.dailyContent.setText(content);
            binding.dailyContent.setVisibility(View.VISIBLE);
        } else {
            binding.dailyContent.setVisibility(View.GONE);
        }

        String photos = r.getImageUrls();
        photoList.clear();
        if (photos != null && !photos.isEmpty()) {
            for (String url : photos.split(",")) {
                String trimmed = url.trim();
                if (!trimmed.isEmpty()) photoList.add(trimmed);
            }
        }
        if (photoAdapter == null) {
            photoAdapter = new PhotoAdapter();
            binding.dailyPhotosGrid.setAdapter(photoAdapter);
        }
        photoAdapter.notifyDataSetChanged();
        int count = photoList.size();
        binding.dailyPhotoCount.setText(count + " 张");
        binding.dailyPhotosGrid.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        binding.dailyPhotosSpacer.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        binding.dailyNoPhotos.setVisibility(count > 0 ? View.GONE : View.VISIBLE);
    }

    private void removePhoto(int index) {
        if (recordId == 0) {
            Toast.makeText(this, "记录ID无效", Toast.LENGTH_SHORT).show();
            return;
        }
        // Build new imageUrls string without the removed URL
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < photoList.size(); i++) {
            if (i == index) continue;
            if (sb.length() > 0) sb.append(",");
            sb.append(photoList.get(i));
        }
        String newUrls = sb.toString();

        // Update backend first (send full record to avoid NOT NULL constraint violations)
        TaskExecutor.execute(() -> {
            try {
                DailyService service = ApiClient.getInstance(DailyDetailActivity.this)
                    .create(DailyService.class);
                currentRecord.setImageUrls(newUrls);
                retrofit2.Response<ApiResponse<DailyRecord>> resp =
                    service.updateRecord(recordId, currentRecord).execute();
                if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                    runOnUiThread(() -> {
                        photoList.remove(index);
                        photoAdapter.notifyDataSetChanged();
                        int count = photoList.size();
                        binding.dailyPhotoCount.setText(count + " 张");
                        boolean hasPhotos = count > 0;
                        binding.dailyPhotosGrid.setVisibility(hasPhotos ? View.VISIBLE : View.GONE);
                        binding.dailyPhotosSpacer.setVisibility(hasPhotos ? View.VISIBLE : View.GONE);
                        binding.dailyNoPhotos.setVisibility(hasPhotos ? View.GONE : View.VISIBLE);
                    });
                } else {
                    runOnUiThread(() ->
                        Toast.makeText(DailyDetailActivity.this, "删除失败，请重试", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() ->
                    Toast.makeText(DailyDetailActivity.this, "删除失败，请重试", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private class PhotoAdapter extends BaseAdapter {
        @Override public int getCount() { return photoList.size(); }
        @Override public Object getItem(int pos) { return photoList.get(pos); }
        @Override public long getItemId(int pos) { return pos; }
        @Override
        public View getView(int pos, View convert, ViewGroup parent) {
            ImageView iv;
            if (convert == null) {
                iv = new ImageView(parent.getContext());
                iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                int height = (int) (200 * parent.getContext().getResources().getDisplayMetrics().density);
                iv.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, height));
                iv.setPadding(2, 2, 2, 2);
                iv.setBackgroundColor(0xFFFAF5F0);
            } else {
                iv = (ImageView) convert;
            }
            Coil.imageLoader(parent.getContext()).enqueue(
                new ImageRequest.Builder(parent.getContext())
                    .data(photoList.get(pos))
                    .target(iv)
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_placeholder)
                    .build()
            );
            iv.setOnClickListener(v -> ImageViewer.show(DailyDetailActivity.this, photoList, pos));
            iv.setOnLongClickListener(v -> {
                new AlertDialog.Builder(DailyDetailActivity.this)
                    .setTitle("删除照片")
                    .setMessage("确定要删除这张照片吗？")
                    .setPositiveButton("删除", (d, w) -> removePhoto(pos))
                    .setNegativeButton("取消", null)
                    .show();
                return true;
            });
            return iv;
        }
    }

}
