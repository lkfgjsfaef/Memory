package com.niit.memory.ui.screens.albumdetail;

import androidx.appcompat.app.AlertDialog;
import android.net.Uri;
import com.niit.memory.util.FileUtils;
import com.niit.memory.util.ImageViewer;
import com.niit.memory.util.TaskExecutor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import coil.Coil;
import coil.request.ImageRequest;
import com.niit.memory.R;
import com.niit.memory.data.model.MemoryAlbum;
import com.niit.memory.databinding.ActivityAlbumDetailBinding;
import com.niit.memory.ui.components.MusicBarHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AlbumDetailActivity extends AppCompatActivity {

    private ActivityAlbumDetailBinding binding;
    private AlbumDetailViewModel viewModel;
    private MusicBarHelper musicBarHelper;
    private long albumId;
    private List<String> photoList = new ArrayList<>();
    private PhotoAdapter photoAdapter;

    private volatile String pendingCoverUrl;

    private static final String[] ALBUM_EMOJIS = {"🏛️", "🌊", "🗼", "🧸", "🍲", "🌹", "🍦", "🌸", "🦋"};

    private final ActivityResultLauncher<String> coverPicker = registerForActivityResult(
        new ActivityResultContracts.GetContent(), this::onCoverPicked);
    private final ActivityResultLauncher<String> photoPicker = registerForActivityResult(
        new ActivityResultContracts.GetMultipleContents(), this::onPhotosPicked);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAlbumDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        albumId = getIntent().getLongExtra("id", 0);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(AlbumDetailViewModel.class);
        observeViewModel();
        viewModel.loadAlbum(albumId);

        binding.albumEditBtn.setOnClickListener(v -> showEditDialog());
        binding.albumAddPhotoBtn.setOnClickListener(v -> photoPicker.launch("image/*"));

        musicBarHelper = new MusicBarHelper(this);
        musicBarHelper.setup();
    }

    private void observeViewModel() {
        viewModel.album.observe(this, a -> {
            if (a != null) {
                binding.albumLocation.setText(a.getLocation());
                binding.albumDate.setText(a.getAlbumDate());

                // Cover image — fallback to first photo if no explicit cover
                String coverUrl = a.getCoverUrl();
                if ((coverUrl == null || coverUrl.isEmpty()) && a.getPhotoUrls() != null && !a.getPhotoUrls().isEmpty()) {
                    String firstPhoto = a.getPhotoUrls().split(",")[0].trim();
                    if (!firstPhoto.isEmpty()) coverUrl = firstPhoto;
                }
                if (coverUrl != null && !coverUrl.isEmpty()) {
                    binding.albumCover.setVisibility(View.VISIBLE);
                    binding.albumEmojiPlaceholder.setVisibility(View.GONE);
                    Coil.imageLoader(this).enqueue(
                        new ImageRequest.Builder(this)
                            .data(coverUrl)
                            .target(binding.albumCover)
                            .placeholder(R.drawable.image_placeholder)
                            .error(R.drawable.image_placeholder)
                            .build()
                    );
                } else if (a.getEmoji() != null && !a.getEmoji().isEmpty()) {
                    binding.albumCover.setVisibility(View.GONE);
                    binding.albumEmojiPlaceholder.setVisibility(View.VISIBLE);
                    binding.albumEmojiPlaceholder.setText(a.getEmoji());
                } else {
                    binding.albumCover.setImageResource(R.drawable.image_placeholder);
                    binding.albumEmojiPlaceholder.setVisibility(View.GONE);
                }

                // Photos grid
                String photos = a.getPhotoUrls();
                photoList.clear();
                if (photos != null && !photos.isEmpty()) {
                    for (String url : photos.split(",")) {
                        String trimmed = url.trim();
                        if (!trimmed.isEmpty()) photoList.add(trimmed);
                    }
                }
                if (photoAdapter == null) {
                    photoAdapter = new PhotoAdapter();
                    binding.albumPhotosGrid.setAdapter(photoAdapter);
                }
                photoAdapter.notifyDataSetChanged();
                binding.albumPhotoCount.setText(photoList.size() + " 张");
            }
        });
        viewModel.loading.observe(this, loading -> {
            // Could show progress
        });
        viewModel.errorMessage.observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void showEditDialog() {
        MemoryAlbum a = viewModel.album.getValue();
        if (a == null) return;

        pendingCoverUrl = a.getCoverUrl();

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        EditText locationInput = new EditText(this);
        locationInput.setHint("地点/主题");
        locationInput.setText(a.getLocation());
        layout.addView(locationInput);

        EditText dateInput = new EditText(this);
        dateInput.setHint("日期 (yyyy-MM-dd)");
        dateInput.setText(a.getAlbumDate());
        layout.addView(dateInput);

        // Emoji spinner wrapper
        LinearLayout emojiRow = new LinearLayout(this);
        emojiRow.setOrientation(LinearLayout.HORIZONTAL);
        emojiRow.setPadding(0, 12, 0, 8);
        for (String em : ALBUM_EMOJIS) {
            TextView tv = new TextView(this);
            tv.setText(em);
            tv.setTextSize(28);
            tv.setPadding(8, 4, 8, 4);
            tv.setOnClickListener(v -> {
                for (int i = 0; i < emojiRow.getChildCount(); i++) {
                    emojiRow.getChildAt(i).setBackgroundColor(0x00000000);
                }
                tv.setBackgroundColor(0x20FF9A9E);
                tv.setTag("selected");
            });
            if (em.equals(a.getEmoji())) {
                tv.setBackgroundColor(0x20FF9A9E);
                tv.setTag("selected");
            }
            emojiRow.addView(tv);
        }
        layout.addView(emojiRow);

        TextView coverBtn = new TextView(this);
        coverBtn.setText(pendingCoverUrl != null && !pendingCoverUrl.isEmpty()
            ? "📷 封面已选择 (点击更换)" : "📷 选择封面图片");
        coverBtn.setTextSize(14);
        coverBtn.setPadding(0, 12, 0, 8);
        coverBtn.setTextColor(0xFF9B8B80);
        coverBtn.setOnClickListener(v -> coverPicker.launch("image/*"));
        layout.addView(coverBtn);

        AlertDialog editDialog = new AlertDialog.Builder(this)
            .setTitle("编辑相册")
            .setView(layout)
            .setPositiveButton("保存", (d, w) -> {})
            .setNegativeButton("取消", null)
            .create();
        editDialog.show();
        editDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String loc = locationInput.getText().toString().trim();
            if (loc.isEmpty()) { Toast.makeText(this, "请输入地点", Toast.LENGTH_SHORT).show(); return; }
            String selectedEmoji = a.getEmoji();
            for (int i = 0; i < emojiRow.getChildCount(); i++) {
                View child = emojiRow.getChildAt(i);
                if (child instanceof TextView && "selected".equals(child.getTag())) {
                    selectedEmoji = ((TextView) child).getText().toString();
                    break;
                }
            }
            String finalCoverUrl = pendingCoverUrl;
            String photoUrls = a.getPhotoUrls();
            if ((finalCoverUrl == null || finalCoverUrl.isEmpty()) && photoUrls != null && !photoUrls.isEmpty()) {
                String first = photoUrls.split(",")[0].trim();
                if (!first.isEmpty()) finalCoverUrl = first;
            }
            viewModel.updateAlbum(albumId, loc, dateInput.getText().toString().trim(),
                selectedEmoji, finalCoverUrl, photoUrls, a.getIsPrivate() != null ? a.getIsPrivate() : 0);
            editDialog.dismiss();
        });
    }

    private void onCoverPicked(Uri uri) {
        if (uri == null) return;
        TaskExecutor.execute(() -> {
            try {
                File file = copyUriToTempFile(uri);
                String url = viewModel.uploadImage(file);
                file.delete();
                pendingCoverUrl = url;
                runOnUiThread(() -> Toast.makeText(this, "封面上传成功", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "上传失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void onPhotosPicked(List<Uri> uris) {
        if (uris == null || uris.isEmpty()) return;
        TaskExecutor.execute(() -> {
            int success = 0;
            for (Uri uri : uris) {
                try {
                    File file = copyUriToTempFile(uri);
                    String url = viewModel.uploadImage(file);
                    file.delete();
                    MemoryAlbum a = viewModel.album.getValue();
                    if (a != null) {
                        String existing = a.getPhotoUrls();
                        String newUrls = (existing != null && !existing.isEmpty())
                            ? existing + "," + url : url;
                        viewModel.updateAlbum(albumId, a.getLocation(), a.getAlbumDate(),
                            a.getEmoji(), a.getCoverUrl(), newUrls, a.getIsPrivate() != null ? a.getIsPrivate() : 0);
                    }
                    success++;
                } catch (Exception e) {
                    final int idx = success;
                    runOnUiThread(() -> Toast.makeText(AlbumDetailActivity.this, "第" + (idx + 1) + "张上传失败", Toast.LENGTH_SHORT).show());
                }
            }
            final int uploaded = success;
            runOnUiThread(() -> Toast.makeText(AlbumDetailActivity.this, "上传完成 (" + uploaded + "/" + uris.size() + ")", Toast.LENGTH_SHORT).show());
        });
    }

    private void removePhoto(int index) {
        MemoryAlbum a = viewModel.album.getValue();
        if (a == null || a.getPhotoUrls() == null) return;
        String[] parts = a.getPhotoUrls().split(",");
        String removedUrl = "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String trimmed = parts[i].trim();
            if (!trimmed.isEmpty()) {
                if (i == index) {
                    removedUrl = trimmed;
                } else {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(trimmed);
                }
            }
        }
        if (!removedUrl.isEmpty()) {
            com.niit.memory.util.QiniuHelper.deleteImageSilently(this, removedUrl);
        }
        viewModel.updateAlbum(albumId, a.getLocation(), a.getAlbumDate(),
            a.getEmoji(), a.getCoverUrl(), sb.toString(), a.getIsPrivate() != null ? a.getIsPrivate() : 0);
    }

    private File copyUriToTempFile(Uri uri) throws Exception {
        return FileUtils.copyUriToTempFile(this, uri);
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
            iv.setOnLongClickListener(v -> {
                new AlertDialog.Builder(AlbumDetailActivity.this)
                    .setTitle("删除照片")
                    .setMessage("确定要删除这张照片吗？")
                    .setPositiveButton("删除", (d, w) -> removePhoto(pos))
                    .setNegativeButton("取消", null)
                    .show();
                return true;
            });
            iv.setOnClickListener(v -> ImageViewer.show(AlbumDetailActivity.this, photoList, pos));
            return iv;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (musicBarHelper != null) musicBarHelper.onDestroy();
    }
}
