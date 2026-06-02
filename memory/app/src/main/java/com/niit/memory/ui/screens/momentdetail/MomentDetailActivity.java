package com.niit.memory.ui.screens.momentdetail;

import androidx.appcompat.app.AlertDialog;
import android.net.Uri;
import com.niit.memory.util.ImageViewer;
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
import com.niit.memory.data.model.MemoryMoment;
import com.niit.memory.databinding.ActivityMomentDetailBinding;
import com.niit.memory.ui.components.MusicBarHelper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MomentDetailActivity extends AppCompatActivity {

    private ActivityMomentDetailBinding binding;
    private MomentDetailViewModel viewModel;
    private MusicBarHelper musicBarHelper;
    private long momentId;
    private List<String> photoList = new ArrayList<>();
    private PhotoAdapter photoAdapter;

    private static final String[] MOMENT_EMOJIS = {"🏞️", "🗼", "🌊", "💕", "🎉", "🍲", "✈️", "🏔️", "🌅"};

    private final ActivityResultLauncher<String> photoPicker = registerForActivityResult(
        new ActivityResultContracts.GetContent(), this::onPhotoPicked);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMomentDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        momentId = getIntent().getLongExtra("id", 0);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(MomentDetailViewModel.class);
        observeViewModel();
        viewModel.loadMoment(momentId);

        binding.momentEditBtn.setOnClickListener(v -> showEditDialog());
        binding.momentAddPhotoBtn.setOnClickListener(v -> photoPicker.launch("image/*"));

        musicBarHelper = new MusicBarHelper(this);
        musicBarHelper.setup();
    }

    private void observeViewModel() {
        viewModel.moment.observe(this, m -> {
            if (m != null) {
                binding.momentTitle.setText(m.getTitle());
                binding.momentDate.setText(m.getMomentDate());
                binding.momentLocation.setText(m.getLocation());

                // Emoji / Cover header
                String photos = m.getPhotoUrls();
                if (photos != null && !photos.isEmpty()) {
                    String[] photoArr = photos.split(",");
                    String firstPhoto = photoArr[0].trim();
                    if (!firstPhoto.isEmpty()) {
                        binding.momentCover.setVisibility(View.VISIBLE);
                        binding.momentEmoji.setVisibility(View.GONE);
                        Coil.imageLoader(this).enqueue(
                            new ImageRequest.Builder(this)
                                .data(firstPhoto)
                                .target(binding.momentCover)
                                .placeholder(R.drawable.image_placeholder)
                                .error(R.drawable.image_placeholder)
                                .build()
                        );
                    } else {
                        binding.momentCover.setVisibility(View.GONE);
                        binding.momentEmoji.setVisibility(View.VISIBLE);
                        if (m.getEmoji() != null && !m.getEmoji().isEmpty()) {
                            binding.momentEmoji.setText(m.getEmoji());
                        } else {
                            binding.momentEmoji.setText("💕");
                        }
                    }
                } else {
                    binding.momentCover.setVisibility(View.GONE);
                    binding.momentEmoji.setVisibility(View.VISIBLE);
                    if (m.getEmoji() != null && !m.getEmoji().isEmpty()) {
                        binding.momentEmoji.setText(m.getEmoji());
                    } else {
                        binding.momentEmoji.setText("💕");
                    }
                }

                // Photos grid
                photoList.clear();
                if (photos != null && !photos.isEmpty()) {
                    for (String url : photos.split(",")) {
                        String trimmed = url.trim();
                        if (!trimmed.isEmpty()) photoList.add(trimmed);
                    }
                }
                if (photoAdapter == null) {
                    photoAdapter = new PhotoAdapter();
                    binding.momentPhotosGrid.setAdapter(photoAdapter);
                }
                photoAdapter.notifyDataSetChanged();
                int count = photoList.size();
                binding.momentPhotoCount.setText(count + " 张");
                binding.momentPhotosGrid.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
                binding.momentNoPhotos.setVisibility(count > 0 ? View.GONE : View.VISIBLE);
            }
        });
        viewModel.errorMessage.observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void showEditDialog() {
        MemoryMoment m = viewModel.moment.getValue();
        if (m == null) return;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        EditText titleInput = new EditText(this);
        titleInput.setHint("瞬间标题");
        titleInput.setText(m.getTitle());
        layout.addView(titleInput);

        EditText dateInput = new EditText(this);
        dateInput.setHint("日期 (yyyy-MM-dd)");
        dateInput.setText(m.getMomentDate());
        layout.addView(dateInput);

        EditText locationInput = new EditText(this);
        locationInput.setHint("地点");
        locationInput.setText(m.getLocation());
        layout.addView(locationInput);

        // Emoji picker
        LinearLayout emojiRow = new LinearLayout(this);
        emojiRow.setOrientation(LinearLayout.HORIZONTAL);
        emojiRow.setPadding(0, 12, 0, 8);
        for (String em : MOMENT_EMOJIS) {
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
            if (em.equals(m.getEmoji())) {
                tv.setBackgroundColor(0x20FF9A9E);
                tv.setTag("selected");
            }
            emojiRow.addView(tv);
        }
        layout.addView(emojiRow);

        new AlertDialog.Builder(this)
            .setTitle("编辑瞬间")
            .setView(layout)
            .setPositiveButton("保存", (d, w) -> {
                String title = titleInput.getText().toString().trim();
                if (title.isEmpty()) { Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show(); return; }
                String selectedEmoji = m.getEmoji();
                for (int i = 0; i < emojiRow.getChildCount(); i++) {
                    View child = emojiRow.getChildAt(i);
                    if (child instanceof TextView && "selected".equals(child.getTag())) {
                        selectedEmoji = ((TextView) child).getText().toString();
                        break;
                    }
                }
                viewModel.updateMoment(momentId, title,
                    dateInput.getText().toString().trim(),
                    locationInput.getText().toString().trim(),
                    selectedEmoji, m.getPhotoUrls());
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void onPhotoPicked(Uri uri) {
        if (uri == null) return;
        new Thread(() -> {
            try {
                File file = copyUriToTempFile(uri);
                String url = viewModel.uploadImage(file);
                file.delete();
                MemoryMoment m = viewModel.moment.getValue();
                if (m != null) {
                    String existing = m.getPhotoUrls();
                    String newUrls = (existing != null && !existing.isEmpty())
                        ? existing + "," + url : url;
                    viewModel.updateMoment(momentId, m.getTitle(), m.getMomentDate(),
                        m.getLocation(), m.getEmoji(), newUrls);
                }
                runOnUiThread(() -> Toast.makeText(this, "照片添加成功", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "上传失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void removePhoto(int index) {
        MemoryMoment m = viewModel.moment.getValue();
        if (m == null || m.getPhotoUrls() == null) return;
        String[] parts = m.getPhotoUrls().split(",");
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
        viewModel.updateMoment(momentId, m.getTitle(), m.getMomentDate(),
            m.getLocation(), m.getEmoji(), sb.toString());
    }

    private File copyUriToTempFile(Uri uri) throws Exception {
        File file = File.createTempFile("upload_", ".jpg", getCacheDir());
        InputStream is = getContentResolver().openInputStream(uri);
        if (is == null) {
            file.delete();
            throw new Exception("无法读取图片文件");
        }
        try (is; FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) != -1) fos.write(buf, 0, n);
        }
        return file;
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
                new AlertDialog.Builder(MomentDetailActivity.this)
                    .setTitle("删除照片")
                    .setMessage("确定要删除这张照片吗？")
                    .setPositiveButton("删除", (d, w) -> removePhoto(pos))
                    .setNegativeButton("取消", null)
                    .show();
                return true;
            });
            iv.setOnClickListener(v -> ImageViewer.show(MomentDetailActivity.this, photoList, pos));
            return iv;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (musicBarHelper != null) musicBarHelper.onDestroy();
    }
}
