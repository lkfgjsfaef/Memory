package com.niit.memory.ui.screens.daily;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import coil.Coil;
import coil.request.ImageRequest;
import com.niit.memory.R;
import com.niit.memory.data.model.DailyRecord;
import com.niit.memory.databinding.ActivityDailyPublishBinding;
import com.niit.memory.util.FileUtils;
import com.niit.memory.util.QiniuHelper;
import com.niit.memory.util.TaskExecutor;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyPublishActivity extends AppCompatActivity {

    private static final String TAG = "DailyPublishActivity";
    private static final String DRAFT_KEY = "daily_draft";
    private static final String[] MOODS = {"开心 😊", "幸福 🥰", "平淡 😌", "难过 😢", "生气 😠", "兴奋 🎉", "感动 💕", "想他 🥺"};
    private static final String[] AUTHORS = {"我", "他", "她"};

    private ActivityDailyPublishBinding binding;
    private DailyViewModel viewModel;
    private long editId = 0;
    private String uploadedImageUrls = "";
    private List<Uri> pendingImageUris = new ArrayList<>();
    private boolean isUploading = false;
    private LinearLayout previewRow;
    private final ActivityResultLauncher<String> imagePickerLauncher =
        registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), uris -> {
            if (uris != null && !uris.isEmpty()) {
                pendingImageUris = uris;
                uploadPendingImages();
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDailyPublishBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(DailyViewModel.class);

        setupSpinners();
        setupImagePreview();
        setupToolbar();

        DailyRecord record = (DailyRecord) getIntent().getSerializableExtra("record");
        if (record != null) {
            editId = record.getId() != null ? record.getId() : 0;
            binding.toolbar.setTitle("编辑日常");
            prefillForm(record);
            uploadedImageUrls = record.getImageUrls() != null ? record.getImageUrls() : "";
            rebuildPreviews();
        } else {
            binding.inputDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
            loadDraft();
        }

        observeViewModel();
    }

    private void setupSpinners() {
        ArrayAdapter<String> moodAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, MOODS);
        moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.inputMood.setAdapter(moodAdapter);

        ArrayAdapter<String> authorAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, AUTHORS);
        authorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.inputAuthor.setAdapter(authorAdapter);
    }

    private void setupImagePreview() {
        previewRow = new LinearLayout(this);
        previewRow.setOrientation(LinearLayout.VERTICAL);
        previewRow.setPadding(0, 0, 0, 12);
        // Insert preview row inside the ScrollView's LinearLayout container
        android.widget.ScrollView sv = (android.widget.ScrollView) binding.getRoot().getChildAt(1);
        LinearLayout container = (LinearLayout) sv.getChildAt(0);
        container.addView(previewRow, container.indexOfChild(findUploadRow()) + 1);

        binding.btnUploadImage.setOnClickListener(v -> {
            if (!isUploading) imagePickerLauncher.launch("image/*");
            else Toast.makeText(this, "图片正在上传中，请稍候...", Toast.LENGTH_SHORT).show();
        });
    }

    private View findUploadRow() {
        return (View) binding.btnUploadImage.getParent();
    }

    private void setupToolbar() {
        binding.btnPublish.setOnClickListener(v -> publish());
        binding.btnCancel.setOnClickListener(v -> onCancel());
    }

    private void prefillForm(DailyRecord r) {
        binding.inputTitle.setText(r.getTitle());
        binding.inputContent.setText(r.getContent());
        binding.inputLocation.setText(r.getLocation());
        if (r.getRecordDate() != null) binding.inputDate.setText(r.getRecordDate());
        String moodVal = r.getMood();
        if (moodVal != null) {
            for (int i = 0; i < MOODS.length; i++) {
                if (MOODS[i].startsWith(moodVal)) { binding.inputMood.setSelection(i); break; }
            }
        }
        String authorVal = r.getAuthor();
        if (authorVal != null) {
            for (int i = 0; i < AUTHORS.length; i++) {
                if (AUTHORS[i].equals(authorVal)) { binding.inputAuthor.setSelection(i); break; }
            }
        }
    }

    private void loadDraft() {
        SharedPreferences prefs = getSharedPreferences("daily_drafts", Context.MODE_PRIVATE);
        String draft = prefs.getString(DRAFT_KEY, null);
        if (draft != null && !draft.isEmpty()) {
            new AlertDialog.Builder(this)
                .setTitle("恢复草稿")
                .setMessage("有未发布的草稿，是否恢复？")
                .setPositiveButton("恢复", (d, w) -> {
                    try {
                        String[] parts = draft.split("\\|\\|\\|", 7);
                        if (parts.length >= 7) {
                            binding.inputTitle.setText(parts[0]);
                            binding.inputContent.setText(parts[1]);
                            binding.inputDate.setText(parts[2]);
                            binding.inputLocation.setText(parts[3]);
                            try { binding.inputMood.setSelection(Integer.parseInt(parts[4])); } catch (Exception ignored) {}
                            try { binding.inputAuthor.setSelection(Integer.parseInt(parts[5])); } catch (Exception ignored) {}
                            uploadedImageUrls = parts[6];
                            rebuildPreviews();
                        }
                    } catch (Exception ignored) {}
                    clearDraft();
                })
                .setNegativeButton("丢弃", (d, w) -> clearDraft())
                .show();
        }
    }

    private void saveDraft() {
        try {
            String title = textOf(binding.inputTitle);
            String content = textOf(binding.inputContent);
            String date = textOf(binding.inputDate);
            String location = textOf(binding.inputLocation);
            int moodIdx = binding.inputMood.getSelectedItemPosition();
            int authorIdx = binding.inputAuthor.getSelectedItemPosition();
            String draft = title + "|||" + content + "|||" + date + "|||" + location + "|||"
                + moodIdx + "|||" + authorIdx + "|||" + uploadedImageUrls;
            getSharedPreferences("daily_drafts", Context.MODE_PRIVATE)
                .edit().putString(DRAFT_KEY, draft).apply();
        } catch (Exception ignored) {}
    }

    private void clearDraft() {
        getSharedPreferences("daily_drafts", Context.MODE_PRIVATE)
            .edit().remove(DRAFT_KEY).apply();
    }

    private String textOf(TextView tv) {
        CharSequence cs = tv.getText();
        return cs != null ? cs.toString() : "";
    }

    private void onCancel() {
        String title = textOf(binding.inputTitle);
        String content = textOf(binding.inputContent);
        if (!title.isEmpty() || !content.isEmpty() || !uploadedImageUrls.isEmpty()) {
            new AlertDialog.Builder(this)
                .setTitle("保存草稿？")
                .setMessage("是否将当前内容保存为草稿？")
                .setPositiveButton("保存草稿", (d, w) -> { saveDraft(); finish(); })
                .setNegativeButton("不保存", (d, w) -> {
                    deleteUploadedImages();
                    finish();
                })
                .setNeutralButton("继续编辑", null)
                .show();
        } else {
            finish();
        }
    }

    private void publish() {
        if (isUploading) {
            Toast.makeText(this, "图片正在上传中，请稍候...", Toast.LENGTH_SHORT).show();
            return;
        }
        String title = textOf(binding.inputTitle).trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show();
            return;
        }
        String content = textOf(binding.inputContent).trim();
        String author = AUTHORS[binding.inputAuthor.getSelectedItemPosition()];
        String loc = textOf(binding.inputLocation).trim();
        String moodFull = MOODS[binding.inputMood.getSelectedItemPosition()];
        String[] moodParts = moodFull.split(" ");
        String moodText = moodParts[0];
        String moodIcon = moodParts.length > 1 ? moodParts[1] : "😊";
        String imgUrls = uploadedImageUrls.isEmpty() ? "" : uploadedImageUrls;
        String recordDate = textOf(binding.inputDate).trim();
        if (recordDate.isEmpty()) {
            recordDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        }

        if (editId > 0) {
            viewModel.updateRecord(editId, title, content, author, loc, moodText, moodIcon, recordDate, imgUrls);
        } else {
            viewModel.createRecord(title, content, author, loc, moodText, moodIcon, recordDate, imgUrls);
        }
        clearDraft();
        finish();
    }

    private void uploadPendingImages() {
        if (pendingImageUris.isEmpty()) return;
        isUploading = true;
        final int total = pendingImageUris.size();
        binding.uploadStatus.setText("正在上传 0/" + total + "...");

        TaskExecutor.execute(() -> {
            int successCount = 0;
            for (int i = 0; i < pendingImageUris.size(); i++) {
                Uri uri = pendingImageUris.get(i);
                final int idx = i + 1;
                try {
                    File tempFile = FileUtils.copyUriToTempFile(this, uri);
                    String url = QiniuHelper.uploadImage(this, tempFile);
                    tempFile.delete();
                    if (url != null && !url.isEmpty()) {
                        successCount++;
                        synchronized (DailyPublishActivity.this) {
                            uploadedImageUrls = (uploadedImageUrls.isEmpty() ? "" : uploadedImageUrls + ",") + url;
                        }
                        runOnUiThread(() -> {
                            binding.uploadStatus.setText("正在上传 " + idx + "/" + total + "...");
                            rebuildPreviews();
                        });
                    } else {
                        runOnUiThread(() ->
                            Toast.makeText(this, "第" + idx + "张上传失败", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Image upload error", e);
                    runOnUiThread(() ->
                        Toast.makeText(this, "第" + idx + "张上传失败", Toast.LENGTH_SHORT).show());
                }
            }
            isUploading = false;
            final int uploaded = successCount;
            runOnUiThread(() -> {
                binding.uploadStatus.setText(uploaded > 0 ? "已上传 " + uploaded + " 张图片" : "");
                rebuildPreviews();
            });
        });
    }

    private void deleteUploadedImages() {
        if (uploadedImageUrls.isEmpty()) return;
        String[] urls = uploadedImageUrls.split(",");
        TaskExecutor.execute(() -> {
            for (String url : urls) {
                String trimmed = url.trim();
                if (!trimmed.isEmpty()) QiniuHelper.deleteImageSilently(this, trimmed);
            }
        });
    }

    private void rebuildPreviews() {
        previewRow.removeAllViews();
        if (uploadedImageUrls.isEmpty()) return;
        String[] urls = uploadedImageUrls.split(",");
        // ScrollView has 20dp padding; previewRow uses match_parent inside it
        int scrollPadding = (int) (20 * getResources().getDisplayMetrics().density);
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int spacing = (int) (4 * getResources().getDisplayMetrics().density);
        int availableWidth = screenWidth - scrollPadding * 2;
        int imgSize = (availableWidth - spacing * 2) / 3;

        LinearLayout currentRow = null;
        int imageIndex = 0;
        for (int i = 0; i < urls.length; i++) {
            String url = urls[i].trim();
            if (url.isEmpty()) continue;
            final int index = i;
            int col = imageIndex % 3;
            boolean isLastInRow = col == 2;

            if (col == 0) {
                currentRow = new LinearLayout(this);
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                rowLp.setMargins(0, 0, 0, spacing);
                previewRow.addView(currentRow, rowLp);
            }

            ImageView iv = new ImageView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(imgSize, imgSize);
            lp.setMargins(0, 0, isLastInRow ? 0 : spacing, 0);
            iv.setLayoutParams(lp);
            imageIndex++;
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Coil.imageLoader(this).enqueue(
                new ImageRequest.Builder(this)
                    .data(url)
                    .target(iv)
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_placeholder)
                    .build());
            iv.setOnLongClickListener(v -> {
                new AlertDialog.Builder(this)
                    .setTitle("删除照片")
                    .setMessage("确定要删除这张照片吗？")
                    .setPositiveButton("删除", (d, w) -> {
                        String[] currUrls = uploadedImageUrls.split(",");
                        StringBuilder sb = new StringBuilder();
                        String removedUrl = "";
                        for (int j = 0; j < currUrls.length; j++) {
                            String trimmed = currUrls[j].trim();
                            if (!trimmed.isEmpty()) {
                                if (j == index) removedUrl = trimmed;
                                else {
                                    if (sb.length() > 0) sb.append(",");
                                    sb.append(trimmed);
                                }
                            }
                        }
                        if (!removedUrl.isEmpty()) QiniuHelper.deleteImageSilently(this, removedUrl);
                        uploadedImageUrls = sb.toString();
                        rebuildPreviews();
                        int count = uploadedImageUrls.isEmpty() ? 0 : uploadedImageUrls.split(",").length;
                        binding.uploadStatus.setText(count > 0 ? "已上传 " + count + " 张图片" : "");
                    })
                    .setNegativeButton("取消", null)
                    .show();
                return true;
            });
            currentRow.addView(iv);
        }
    }

    private void observeViewModel() {
        viewModel.errorMessage.observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onBackPressed() {
        onCancel();
    }
}
