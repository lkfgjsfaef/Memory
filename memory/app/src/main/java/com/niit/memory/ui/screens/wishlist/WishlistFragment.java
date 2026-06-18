package com.niit.memory.ui.screens.wishlist;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.niit.memory.R;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.niit.memory.databinding.FragmentWishlistBinding;
import com.niit.memory.ui.adapters.WishAdapter;
import com.niit.memory.util.SessionManager;
import com.niit.memory.util.FileUtils;
import com.niit.memory.util.TaskExecutor;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WishlistFragment extends Fragment {

    private static final String TAG = "WishlistFragment";

    private FragmentWishlistBinding binding;
    private WishlistViewModel viewModel;
    private WishAdapter adapter;

    private String[] statusOptions = {"全部", "pending", "in_progress", "completed"};
    private String[] statusLabels = {"全部状态", "待实现", "进行中", "已完成"};
    private String[] categoryOptions = {"全部", "未来规划", "旅行计划", "生活目标"};
    private String[] categoryLabels = {"全部分类", "未来规划", "旅行计划", "生活目标"};
    private String[] ownerOptions = {"all", "mine", "partner"};
    private String[] ownerLabels = {"全部归属", "我的", "TA的"};

    private List<Uri> pendingImageUris = new ArrayList<>();
    private String pendingImageUrls = "";
    private Runnable pendingImagePreviewUpdater;
    private volatile boolean isWishUploading = false;
    private android.widget.Button currentUploadBtn;

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
        binding = FragmentWishlistBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(WishlistViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new WishAdapter(
            id -> confirmDeleteWish(id),
            (id, status) -> viewModel.updateWishStatus(id, status),
            wish -> showEditDialog(wish));
        binding.wishList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.wishList.setAdapter(adapter);

        setupFilters();
        setupSwipeRefresh();
        setupFab();
        observeViewModel();
        viewModel.loadWishes(null, null);
    }

    private void setupFilters() {
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_item, statusLabels);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.wishStatusFilter.setAdapter(statusAdapter);

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_item, categoryLabels);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.wishCategoryFilter.setAdapter(catAdapter);

        ArrayAdapter<String> ownerAdapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_item, ownerLabels);
        ownerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.wishOwnerFilter.setAdapter(ownerAdapter);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                int statusPos = binding.wishStatusFilter.getSelectedItemPosition();
                int catPos = binding.wishCategoryFilter.getSelectedItemPosition();
                int ownerPos = binding.wishOwnerFilter.getSelectedItemPosition();
                if (statusPos < 0 || catPos < 0 || ownerPos < 0) return;
                String status = statusOptions[statusPos];
                String cat = categoryOptions[catPos];
                String owner = ownerOptions[ownerPos];
                viewModel.setOwnerFilter(owner);
                viewModel.loadWishes("全部".equals(status) ? null : status,
                    "全部".equals(cat) ? null : cat);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
        binding.wishStatusFilter.setOnItemSelectedListener(listener);
        binding.wishCategoryFilter.setOnItemSelectedListener(listener);
        binding.wishOwnerFilter.setOnItemSelectedListener(listener);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadWishes(null, null));
    }

    private void setupFab() {
        binding.fabAddWish.setOnClickListener(v -> showAddDialog());
    }

    private void showAddDialog() {
        pendingImageUrls = "";
        showWishFormDialog(null);
    }

    private void showEditDialog(com.niit.memory.data.model.Wish existing) {
        pendingImageUrls = existing.getImageUrls() != null ? existing.getImageUrls() : "";
        showWishFormDialog(existing);
    }

    private void showWishFormDialog(@Nullable com.niit.memory.data.model.Wish existing) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(existing != null ? "编辑心愿" : "添加心愿");

        LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(48, 24, 48, 24);

        final EditText titleInput = new EditText(getContext());
        titleInput.setHint("心愿标题");
        if (existing != null) titleInput.setText(existing.getTitle());

        final EditText descInput = new EditText(getContext());
        descInput.setHint("描述");
        descInput.setMinLines(3);
        descInput.setGravity(android.view.Gravity.TOP);
        if (existing != null) descInput.setText(existing.getDescription());

        // Category spinner
        String[] cats = {"未来规划", "旅行计划", "生活目标"};
        final android.widget.Spinner catSpinner = new android.widget.Spinner(getContext());
        catSpinner.setAdapter(new ArrayAdapter<>(getContext(),
            android.R.layout.simple_spinner_item, cats));
        if (existing != null && existing.getCategory() != null) {
            for (int i = 0; i < cats.length; i++) {
                if (cats[i].equals(existing.getCategory())) { catSpinner.setSelection(i); break; }
            }
        }

        // Status spinner
        String[] statuses = {"待实现", "进行中", "已完成"};
        final android.widget.Spinner statusSpinner = new android.widget.Spinner(getContext());
        statusSpinner.setAdapter(new ArrayAdapter<>(getContext(),
            android.R.layout.simple_spinner_item, statuses));
        if (existing != null) {
            if ("completed".equals(existing.getStatus())) {
                statusSpinner.setSelection(2);
            } else if ("in_progress".equals(existing.getStatus())) {
                statusSpinner.setSelection(1);
            }
        }

        // Author spinner
        SessionManager sm = SessionManager.getInstance(requireContext());
        String myName = sm.getNickname() != null ? sm.getNickname() : "我";
        String[] authors = {myName, "对方"};
        final android.widget.Spinner authorSpinner = new android.widget.Spinner(getContext());
        authorSpinner.setAdapter(new ArrayAdapter<>(getContext(),
            android.R.layout.simple_spinner_item, authors));
        if (existing != null && existing.getAuthor() != null) {
            for (int i = 0; i < authors.length; i++) {
                if (authors[i].equals(existing.getAuthor())) { authorSpinner.setSelection(i); break; }
            }
        } else {
            String nickname = sm.getNickname();
            if (nickname != null) {
                for (int i = 0; i < authors.length; i++) {
                    if (authors[i].equals(nickname)) { authorSpinner.setSelection(i); break; }
                }
            }
        }

        // Date
        final EditText dateInput = new EditText(getContext());
        dateInput.setHint("发起日期 (yyyy-MM-dd)");
        if (existing != null && existing.getStartDate() != null) {
            dateInput.setText(existing.getStartDate());
        } else {
            dateInput.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        }

        // Upload button
        currentUploadBtn = new android.widget.Button(getContext());
        currentUploadBtn.setText("📷 上传配图 (可多选)");
        currentUploadBtn.setOnClickListener(v -> {
            if (isWishUploading) {
                Toast.makeText(getContext(), "正在上传中，请稍候...", Toast.LENGTH_SHORT).show();
                return;
            }
            imagePickerLauncher.launch("image/*");
        });

        // Image preview area label + container
        addLabel(ll, "配图预览");

        final LinearLayout imagePreviewRow = new LinearLayout(getContext());
        imagePreviewRow.setOrientation(LinearLayout.HORIZONTAL);
        imagePreviewRow.setPadding(0, 4, 0, 0);
        imagePreviewRow.setMinimumHeight((int) (80 * getResources().getDisplayMetrics().density));

        // Hint when no images
        final TextView imageHint = new TextView(getContext());
        imageHint.setText("  点击上方按钮上传配图");
        imageHint.setTextColor(0xFFBBB0A5);
        imageHint.setTextSize(13);

        // Rebuild preview helper
        pendingImagePreviewUpdater = () -> {
            imagePreviewRow.removeAllViews();
            String[] urls = pendingImageUrls.split(",");
            boolean hasImages = false;
            for (int i = 0; i < urls.length; i++) {
                String url = urls[i].trim();
                if (url.isEmpty()) continue;
                hasImages = true;
                final int index = i;
                ImageView iv = new ImageView(getContext());
                int sz = (int) (80 * getResources().getDisplayMetrics().density);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sz, sz);
                lp.setMargins(0, 0, 8, 0);
                iv.setLayoutParams(lp);
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                coil.Coil.imageLoader(requireContext()).enqueue(
                    new coil.request.ImageRequest.Builder(requireContext())
                        .data(url)
                        .target(iv)
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_placeholder)
                        .build());
                iv.setOnLongClickListener(v -> {
                    new AlertDialog.Builder(requireContext())
                        .setTitle("删除照片")
                        .setMessage("确定要删除这张照片吗？")
                        .setPositiveButton("删除", (dd, ww) -> {
                            String[] currUrls = pendingImageUrls.split(",");
                            StringBuilder sb = new StringBuilder();
                            for (int j = 0; j < currUrls.length; j++) {
                                String trimmed = currUrls[j].trim();
                                if (!trimmed.isEmpty() && j != index) {
                                    if (sb.length() > 0) sb.append(",");
                                    sb.append(trimmed);
                                }
                            }
                            // Delete from Qiniu
                            String removedUrl = index < currUrls.length ? currUrls[index].trim() : "";
                            if (!removedUrl.isEmpty()) {
                                com.niit.memory.util.QiniuHelper.deleteImageSilently(getContext(), removedUrl);
                            }
                            pendingImageUrls = sb.toString();
                            pendingImagePreviewUpdater.run();
                        })
                        .setNegativeButton("取消", null)
                        .show();
                    return true;
                });
                imagePreviewRow.addView(iv);
            }
            if (!hasImages) {
                imagePreviewRow.addView(imageHint);
            }
        };

        // Run preview to show existing images or hint
        pendingImagePreviewUpdater.run();

        ll.addView(titleInput);
        ll.addView(descInput);
        addLabel(ll, "分类"); ll.addView(catSpinner);
        addLabel(ll, "状态"); ll.addView(statusSpinner);
        addLabel(ll, "发起人"); ll.addView(authorSpinner);
        addLabel(ll, "发起日期"); ll.addView(dateInput);
        ll.addView(currentUploadBtn, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) {{
            setMargins(0, 12, 0, 0);
        }});
        ll.addView(imagePreviewRow);

        ScrollView scrollView = new ScrollView(getContext());
        scrollView.addView(ll);
        builder.setView(scrollView);

        final long editId = existing != null && existing.getId() != null ? existing.getId() : 0;
        builder.setPositiveButton("保存", (d, w) -> {});
        builder.setNegativeButton("取消", null);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (isWishUploading) {
                Toast.makeText(getContext(), "图片正在上传中，请稍候再保存...", Toast.LENGTH_SHORT).show();
                return;
            }
            String title = titleInput.getText().toString();
            String desc = descInput.getText().toString();
            if (title.isEmpty()) { Toast.makeText(getContext(), "请输入标题", Toast.LENGTH_SHORT).show(); return; }

            String cat = cats[catSpinner.getSelectedItemPosition()];
            String status;
            int statusPos = statusSpinner.getSelectedItemPosition();
            if (statusPos == 2) status = "completed";
            else if (statusPos == 1) status = "in_progress";
            else status = "pending";
            String author = authors[authorSpinner.getSelectedItemPosition()];
            String date = dateInput.getText().toString();
            String imgUrls = pendingImageUrls.isEmpty() ? "" : pendingImageUrls;
            Log.d(TAG, "saveWish: editId=" + editId + " title=" + title + " imageUrls='" + imgUrls
                + "' status=" + status + " cat=" + cat);

            if (editId > 0) {
                viewModel.updateWish(editId, title, desc, cat, status, author, date, imgUrls);
            } else {
                viewModel.createWish(title, desc, cat, status, author, date, imgUrls);
            }
            dialog.dismiss();
        });
    }

    private void addLabel(LinearLayout parent, String text) {
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setTextColor(0xFF9B8B80);
        tv.setTextSize(13);
        tv.setPadding(0, 12, 0, 4);
        parent.addView(tv);
    }

    private void confirmDeleteWish(long id) {
        new AlertDialog.Builder(requireContext())
            .setTitle("确定删除这个心愿吗？")
            .setPositiveButton("删除", (d, w) -> viewModel.deleteWish(id))
            .setNegativeButton("取消", null).show();
    }

    private void uploadPendingImages() {
        if (pendingImageUris == null || pendingImageUris.isEmpty()) {
            Log.w(TAG, "uploadPendingImages: pendingImageUris is null/empty, skipping");
            return;
        }
        android.content.Context ctx = getContext();
        if (ctx == null) return;
        final int total = pendingImageUris.size();
        Log.d(TAG, "uploadPendingImages: START count=" + total);
        isWishUploading = true;
        android.app.Activity act = getActivity();
        if (act != null) act.runOnUiThread(() -> {
            if (currentUploadBtn != null) {
                currentUploadBtn.setText("⏳ 上传中 0/" + total + "...");
                currentUploadBtn.setEnabled(false);
            }
        });

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
                        Log.e(TAG, "uploadPendingImages: read failed for index " + idx, e);
                        if (act != null) act.runOnUiThread(() -> {
                            if (currentUploadBtn != null) currentUploadBtn.setText("⏳ 上传中 " + idx + "/" + total + "...");
                            Toast.makeText(ctx, "第" + idx + "张无法读取", Toast.LENGTH_SHORT).show();
                        });
                        continue;
                    }
                    Log.d(TAG, "uploadPendingImages: index=" + idx + " tempFile size=" + tempFile.length());

                    String url = viewModel.uploadImage(tempFile);
                    Log.d(TAG, "uploadPendingImages: ViewModel returned url=" + url);
                    tempFile.delete();
                    if (url != null && !url.isEmpty()) {
                        successCount++;
                        synchronized (WishlistFragment.this) {
                            pendingImageUrls = (pendingImageUrls.isEmpty() ? "" : pendingImageUrls + ",") + url;
                        }
                        Log.d(TAG, "uploadPendingImages: SUCCESS, pendingImageUrls now=" + pendingImageUrls);
                        if (act != null) act.runOnUiThread(() -> {
                            if (currentUploadBtn != null) currentUploadBtn.setText("⏳ 上传中 " + idx + "/" + total + "...");
                            if (pendingImagePreviewUpdater != null) pendingImagePreviewUpdater.run();
                        });
                    } else {
                        Log.e(TAG, "uploadPendingImages: url null/empty for index " + idx);
                        if (act != null) act.runOnUiThread(() -> {
                            if (currentUploadBtn != null) currentUploadBtn.setText("⏳ 上传中 " + idx + "/" + total + "...");
                            Toast.makeText(ctx, "第" + idx + "张上传失败", Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "uploadPendingImages: EXCEPTION for index " + idx, e);
                    if (act != null) act.runOnUiThread(() -> {
                        if (currentUploadBtn != null) currentUploadBtn.setText("⏳ 上传中 " + idx + "/" + total + "...");
                        Toast.makeText(ctx, "第" + idx + "张上传失败", Toast.LENGTH_SHORT).show();
                    });
                }
            }
            // All done
            isWishUploading = false;
            final int uploaded = successCount;
            if (act != null) act.runOnUiThread(() -> {
                if (currentUploadBtn != null) {
                    currentUploadBtn.setText("📷 上传配图 (可多选)");
                    currentUploadBtn.setEnabled(true);
                }
                Toast.makeText(ctx, "上传完成 (" + uploaded + "/" + total + "张)", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void updateEmptyState(java.util.List<com.niit.memory.data.model.Wish> list) {
        boolean empty = list == null || list.isEmpty();
        binding.wishEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.wishList.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void observeViewModel() {
        viewModel.wishes.observe(getViewLifecycleOwner(), list -> {
            if (list != null) {
                // Filter by owner if needed
                String owner = viewModel.getCurrentOwner();
                if ("mine".equals(owner)) {
                    SessionManager sm = SessionManager.getInstance(requireContext());
                    int myId;
                    try {
                        myId = Integer.parseInt(sm.getUserId() != null ? sm.getUserId() : "0");
                    } catch (NumberFormatException e) {
                        myId = 0;
                    }
                    java.util.List<com.niit.memory.data.model.Wish> filtered = new java.util.ArrayList<>();
                    for (com.niit.memory.data.model.Wish w : list) {
                        if (w.getUserId() != null && w.getUserId() == myId) filtered.add(w);
                    }
                    adapter.submitList(filtered);
                    updateEmptyState(filtered);
                } else if ("partner".equals(owner)) {
                    SessionManager sm = SessionManager.getInstance(requireContext());
                    int partnerId = (sm.getUserId() != null && sm.getUserId().equals("1")) ? 2 : 1;
                    java.util.List<com.niit.memory.data.model.Wish> filtered = new java.util.ArrayList<>();
                    for (com.niit.memory.data.model.Wish w : list) {
                        if (w.getUserId() != null && w.getUserId() == partnerId) filtered.add(w);
                    }
                    adapter.submitList(filtered);
                    updateEmptyState(filtered);
                } else {
                    adapter.submitList(list);
                    updateEmptyState(list);
                }
            }
        });
        viewModel.total.observe(getViewLifecycleOwner(), t ->
            binding.wishTotal.setText(String.valueOf(t != null ? t : 0)));
        viewModel.completed.observe(getViewLifecycleOwner(), c ->
            binding.wishCompleted.setText(String.valueOf(c != null ? c : 0)));
        viewModel.pending.observe(getViewLifecycleOwner(), p ->
            binding.wishPending.setText(String.valueOf(p != null ? p : 0)));
        viewModel.loading.observe(getViewLifecycleOwner(), loading -> {
            if (loading != null && !loading) binding.swipeRefresh.setRefreshing(false);
        });
        viewModel.errorMessage.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }
}
