package com.niit.memory.ui.screens.daily;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.niit.memory.R;
import com.niit.memory.databinding.DialogDailyFormBinding;
import com.niit.memory.databinding.FragmentDailyBinding;
import com.niit.memory.ui.adapters.DailyRecordAdapter;
import com.niit.memory.util.FileUtils;
import com.niit.memory.util.TaskExecutor;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DailyFragment extends Fragment {

    private static final String TAG = "DailyFragment";

    private FragmentDailyBinding binding;
    private DailyViewModel viewModel;
    private DailyRecordAdapter adapter;
    private String[] months = {"全部", "01", "02", "03", "04", "05", "06",
        "07", "08", "09", "10", "11", "12"};

    private List<Uri> pendingImageUris = new ArrayList<>();
    private String uploadedImageUrls = "";
    private TextView currentUploadStatus;
    private Runnable currentPreviewUpdater;
    private volatile boolean isUploading = false;

    private final ActivityResultLauncher<String> imagePickerLauncher =
        registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), uris -> {
            if (uris != null && !uris.isEmpty()) {
                pendingImageUris = uris;
                uploadPendingImages();
            }
        });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDailyBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(DailyViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new DailyRecordAdapter(
            id -> confirmDeleteRecord(id),
            record -> showEditDialog(record),
            record -> {
                Intent intent = new Intent(getContext(), DailyDetailActivity.class);
                intent.putExtra("record", record);
                startActivity(intent);
            });
        binding.dailyList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.dailyList.setAdapter(adapter);

        setupFilters();
        setupSwipeRefresh();
        setupFab();

        observeViewModel();

        Calendar cal = Calendar.getInstance();
        viewModel.loadRecords(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
    }

    private void setupFilters() {
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        java.util.List<String> yearList = new java.util.ArrayList<>();
        yearList.add("全部");
        for (int y = 2004; y <= currentYear; y++) yearList.add(String.valueOf(y));
        String[] years = yearList.toArray(new String[0]);

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.yearFilter.setAdapter(yearAdapter);

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.monthFilter.setAdapter(monthAdapter);

        int currYearIdx = 0;
        String currYearStr = String.valueOf(cal.get(Calendar.YEAR));
        for (int i = 0; i < years.length; i++) {
            if (years[i].equals(currYearStr)) { currYearIdx = i; break; }
        }
        binding.yearFilter.setSelection(currYearIdx);
        binding.monthFilter.setSelection(cal.get(Calendar.MONTH) + 1);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String y = binding.yearFilter.getSelectedItem().toString();
                String m = binding.monthFilter.getSelectedItem().toString();
                Integer year = "全部".equals(y) ? null : Integer.parseInt(y);
                Integer month = "全部".equals(m) ? null : Integer.parseInt(m);
                viewModel.loadRecords(year, month);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
        binding.yearFilter.setOnItemSelectedListener(listener);
        binding.monthFilter.setOnItemSelectedListener(listener);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            Integer year = viewModel.currentYear.getValue();
            Integer month = viewModel.currentMonth.getValue();
            viewModel.loadRecords(year, month);
        });
    }

    private void setupFab() {
        binding.fabAddRecord.setOnClickListener(v -> showAddDialog());
    }

    private void showAddDialog() {
        uploadedImageUrls = "";
        showRecordFormDialog(null);
    }

    private void showEditDialog(com.niit.memory.data.model.DailyRecord existing) {
        uploadedImageUrls = existing.getImageUrls() != null ? existing.getImageUrls() : "";
        showRecordFormDialog(existing);
    }

    private void showRecordFormDialog(@Nullable com.niit.memory.data.model.DailyRecord existing) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        DialogDailyFormBinding formBinding = DialogDailyFormBinding.inflate(
            LayoutInflater.from(getContext()), null, false);
        dialog.setContentView(formBinding.getRoot());

        String[] moods = {"开心 😊", "幸福 🥰", "平淡 😌", "难过 😢", "生气 😠", "兴奋 🎉", "感动 💕", "想他 🥺"};
        ArrayAdapter<String> moodAdapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_item, moods);
        moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        formBinding.inputMood.setAdapter(moodAdapter);

        String[] authors = {"我", "他", "她"};
        ArrayAdapter<String> authorAdapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_item, authors);
        authorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        formBinding.inputAuthor.setAdapter(authorAdapter);

        // Image preview container (added programmatically below upload row)
        LinearLayout rootLayout = (LinearLayout) formBinding.getRoot();
        LinearLayout previewRow = new LinearLayout(getContext());
        previewRow.setOrientation(LinearLayout.HORIZONTAL);
        previewRow.setPadding(0, 8, 0, 8);
        Runnable[] rebuildPreviewHolder = {null};
        Runnable rebuildPreview = () -> {
            previewRow.removeAllViews();
            String[] urls = uploadedImageUrls.split(",");
                for (int i = 0; i < urls.length; i++) {
                    String url = urls[i].trim();
                    if (url.isEmpty()) continue;
                    final int index = i;
                    ImageView iv = new ImageView(getContext());
                    int sz = (int) (72 * getResources().getDisplayMetrics().density);
                    iv.setLayoutParams(new LinearLayout.LayoutParams(sz, sz) {{
                        setMargins(0, 0, 8, 0);
                    }});
                    iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    coil.Coil.imageLoader(requireContext()).enqueue(
                        new coil.request.ImageRequest.Builder(requireContext())
                            .data(url)
                            .target(iv)
                            .placeholder(R.drawable.image_placeholder)
                            .error(R.drawable.image_placeholder)
                            .build());
                    iv.setOnLongClickListener(v -> {
                        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle("删除照片")
                            .setMessage("确定要删除这张照片吗？")
                            .setPositiveButton("删除", (dd, ww) -> {
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
                                if (!removedUrl.isEmpty()) {
                                    com.niit.memory.util.QiniuHelper.deleteImageSilently(getContext(), removedUrl);
                                }
                                uploadedImageUrls = sb.toString();
                                rebuildPreviewHolder[0].run();
                                if (currentUploadStatus != null) {
                                    int count = uploadedImageUrls.isEmpty() ? 0 : uploadedImageUrls.split(",").length;
                                    currentUploadStatus.setText(count > 0 ? "已上传 " + count + " 张图片" : "");
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                        return true;
                    });
                    previewRow.addView(iv);
                }
            }
        ;
        rebuildPreviewHolder[0] = rebuildPreview;
        int btnSaveIndex = rootLayout.indexOfChild(formBinding.btnSave);
        rootLayout.addView(previewRow, btnSaveIndex);
        currentPreviewUpdater = rebuildPreview;
        // Show existing images on edit
        if (uploadedImageUrls != null && !uploadedImageUrls.isEmpty()) {
            rebuildPreview.run();
        }

        // Pre-fill for edit
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        if (existing != null) {
            formBinding.inputTitle.setText(existing.getTitle());
            formBinding.inputContent.setText(existing.getContent());
            formBinding.inputLocation.setText(existing.getLocation());
            if (existing.getRecordDate() != null) {
                formBinding.inputDate.setText(existing.getRecordDate());
            }

            String moodVal = existing.getMood();
            if (moodVal != null) {
                for (int i = 0; i < moods.length; i++) {
                    if (moods[i].startsWith(moodVal)) {
                        formBinding.inputMood.setSelection(i);
                        break;
                    }
                }
            }

            String authorVal = existing.getAuthor();
            if (authorVal != null) {
                for (int i = 0; i < authors.length; i++) {
                    if (authors[i].equals(authorVal)) {
                        formBinding.inputAuthor.setSelection(i);
                        break;
                    }
                }
            }
        } else {
            formBinding.inputDate.setText(sdf.format(new java.util.Date()));
        }

        currentUploadStatus = formBinding.uploadStatus;
        if (!uploadedImageUrls.isEmpty()) {
            int count = uploadedImageUrls.split(",").length;
            currentUploadStatus.setText("已上传 " + count + " 张图片");
        }
        formBinding.btnUploadImage.setOnClickListener(v ->
            imagePickerLauncher.launch("image/*"));

        final long editId = existing != null && existing.getId() != null ? existing.getId() : 0;
        formBinding.btnSave.setOnClickListener(btn -> {
            if (isUploading) {
                Toast.makeText(getContext(), "图片正在上传中，请稍候...", Toast.LENGTH_SHORT).show();
                return;
            }
            String title = formBinding.inputTitle.getText() != null
                ? formBinding.inputTitle.getText().toString() : "";
            String content = formBinding.inputContent.getText() != null
                ? formBinding.inputContent.getText().toString() : "";
            String author = formBinding.inputAuthor.getSelectedItem().toString();
            String loc = formBinding.inputLocation.getText() != null
                ? formBinding.inputLocation.getText().toString() : "";
            String moodFull = formBinding.inputMood.getSelectedItem().toString();
            String[] moodParts = moodFull.split(" ");
            String moodText = moodParts[0];
            String moodIcon = moodParts.length > 1 ? moodParts[1] : "😊";

            if (title.isEmpty()) {
                Toast.makeText(getContext(), "请输入标题", Toast.LENGTH_SHORT).show();
                return;
            }

            String imgUrls = uploadedImageUrls.isEmpty() ? "" : uploadedImageUrls;
            String recordDate = formBinding.inputDate.getText() != null
                ? formBinding.inputDate.getText().toString() : sdf.format(new java.util.Date());

            if (editId > 0) {
                viewModel.updateRecord(editId, title, content, author, loc, moodText, moodIcon,
                    recordDate, imgUrls);
            } else {
                viewModel.createRecord(title, content, author, loc, moodText, moodIcon, recordDate, imgUrls);
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void confirmDeleteRecord(long id) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("确定删除这条记录吗？")
            .setPositiveButton("删除", (d, w) -> {
                viewModel.deleteRecord(id);
                Toast.makeText(getContext(), "删除成功", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("取消", null).show();
    }

    private void uploadPendingImages() {
        if (pendingImageUris == null || pendingImageUris.isEmpty()) return;
        android.content.Context ctx = getContext();
        if (ctx == null) return;
        isUploading = true;
        final int total = pendingImageUris.size();
        Toast.makeText(ctx, "正在上传 0/" + total + "...", Toast.LENGTH_SHORT).show();

        TaskExecutor.execute(() -> {
            int successCount = 0;
            for (int i = 0; i < pendingImageUris.size(); i++) {
                Uri uri = pendingImageUris.get(i);
                final int idx = i + 1;
                try {
                    File tempFile;
                    try {
                        tempFile = FileUtils.copyUriToTempFile(ctx, uri);
                    } catch (Exception e) {
                        android.app.Activity act = getActivity();
                        if (act != null) act.runOnUiThread(() ->
                            Toast.makeText(ctx, "第" + idx + "张无法读取", Toast.LENGTH_SHORT).show());
                        continue;
                    }

                    String url = viewModel.uploadImage(tempFile);
                    tempFile.delete();
                    if (url != null && !url.isEmpty()) {
                        successCount++;
                        synchronized (DailyFragment.this) {
                            uploadedImageUrls = (uploadedImageUrls.isEmpty() ? "" : uploadedImageUrls + ",") + url;
                        }
                        android.app.Activity act = getActivity();
                        if (act != null) act.runOnUiThread(() -> {
                            if (currentUploadStatus != null) {
                                int count = uploadedImageUrls.isEmpty() ? 0 : uploadedImageUrls.split(",").length;
                                currentUploadStatus.setText("正在上传 " + idx + "/" + total + "...");
                            }
                            if (currentPreviewUpdater != null) currentPreviewUpdater.run();
                        });
                    } else {
                        android.app.Activity act = getActivity();
                        if (act != null) act.runOnUiThread(() ->
                            Toast.makeText(ctx, "第" + idx + "张上传失败", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Image upload error for index " + idx, e);
                    android.app.Activity act = getActivity();
                    if (act != null) act.runOnUiThread(() ->
                        Toast.makeText(ctx, "第" + idx + "张上传失败", Toast.LENGTH_SHORT).show());
                }
            }
            isUploading = false;
            final int uploaded = successCount;
            android.app.Activity act = getActivity();
            if (act != null) act.runOnUiThread(() -> {
                Toast.makeText(ctx, "上传完成 (" + uploaded + "/" + total + "张)", Toast.LENGTH_SHORT).show();
                if (currentUploadStatus != null) {
                    currentUploadStatus.setText(uploaded > 0 ? "已上传 " + uploaded + " 张图片" : "");
                }
                if (currentPreviewUpdater != null) currentPreviewUpdater.run();
            });
        });
    }

    private void observeViewModel() {
        viewModel.records.observe(getViewLifecycleOwner(), list -> {
            if (list != null) {
                adapter.submitList(list);
                boolean empty = list.isEmpty();
                binding.dailyEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                binding.dailyList.setVisibility(empty ? View.GONE : View.VISIBLE);
            }
        });
        viewModel.total.observe(getViewLifecycleOwner(), t ->
            binding.statTotal.setText(String.valueOf(t != null ? t : 0)));
        viewModel.monthCount.observe(getViewLifecycleOwner(), m ->
            binding.statMonth.setText(String.valueOf(m != null ? m : 0)));
        viewModel.streak.observe(getViewLifecycleOwner(), s ->
            binding.statStreak.setText(String.valueOf(s != null ? s : 0)));
        viewModel.loveDays.observe(getViewLifecycleOwner(), ld ->
            binding.statLoveDays.setText(String.valueOf(ld != null ? ld : 0)));
        viewModel.loading.observe(getViewLifecycleOwner(), loading -> {
            if (loading != null && !loading) binding.swipeRefresh.setRefreshing(false);
        });
        viewModel.errorMessage.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }
}
