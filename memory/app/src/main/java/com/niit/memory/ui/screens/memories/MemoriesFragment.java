package com.niit.memory.ui.screens.memories;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.tabs.TabLayout;
import com.niit.memory.databinding.FragmentMemoriesBinding;
import com.niit.memory.ui.adapters.MemoryAlbumAdapter;
import com.niit.memory.ui.adapters.MomentAdapter;
import com.niit.memory.ui.adapters.VisitedLocationAdapter;
import com.niit.memory.ui.screens.albumdetail.AlbumDetailActivity;
import com.niit.memory.ui.screens.momentdetail.MomentDetailActivity;
import com.niit.memory.util.FileUtils;
import com.niit.memory.util.TaskExecutor;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

public class MemoriesFragment extends Fragment {

    private static final String TAG = "MemoriesFragment";

    private FragmentMemoriesBinding binding;
    private MemoriesViewModel viewModel;
    private MapView mapView;
    private VisitedLocationAdapter locationAdapter;
    private MemoryAlbumAdapter albumAdapter;
    private MomentAdapter momentAdapter;

    // Map click state
    private Double pendingLat, pendingLng;
    private Marker clickMarker;

    // Image upload state
    private List<Uri> pendingImageUris = new ArrayList<>();
    private String pendingImageUrls = "";
    private String pendingCoverUrl;
    private String pendingLocationImageUrl;
    private volatile boolean isUploading = false;
    private Runnable imagePreviewUpdater;
    private Runnable coverPreviewUpdater;
    private Runnable locationImagePreviewUpdater;
    // Which form is currently using the image picker
    private String imageTarget = "moment"; // "moment", "album_cover", "album_photo", "location"

    private final ActivityResultLauncher<String> imagePickerLauncher =
        registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), uris -> {
            if (uris != null && !uris.isEmpty()) {
                pendingImageUris = uris;
                uploadPendingImages();
            }
        });

    private final ActivityResultLauncher<String> singleImagePickerLauncher =
        registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                pendingImageUris = new ArrayList<>();
                pendingImageUris.add(uri);
                uploadPendingImages();
            }
        });


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMemoriesBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(MemoriesViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        setupTabs();
        setupMap();
        setupRecyclerViews();
        setupFabs();
        observeViewModel();

        viewModel.loadLocations();
        viewModel.loadMoments();
        viewModel.loadAlbums();
    }

    private void setupTabs() {
        binding.memoriesTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showTab(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void showTab(int index) {
        binding.tabMap.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        binding.tabRiver.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        binding.tabAlbums.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
    }

    private void setupMap() {
        mapView = binding.mapView;
        mapView.setTilesScaledToDpi(false);
        mapView.setTileSource(new org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase(
            "Amap", 0, 18, 256, ".png",
            new String[]{"https://webrd01.is.autonavi.com"}) {
            @Override
            public String getTileURLString(long pMapTileIndex) {
                return getBaseUrl() + "/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x="
                    + MapTileIndex.getX(pMapTileIndex) + "&y="
                    + MapTileIndex.getY(pMapTileIndex) + "&z="
                    + MapTileIndex.getZoom(pMapTileIndex);
            }
        });
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(5.0);
        mapView.getController().setCenter(new GeoPoint(35.0, 105.0));

        // Map click to add location
        MapEventsReceiver receiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                pendingLat = p.getLatitude();
                pendingLng = p.getLongitude();
                android.app.Activity act = getActivity();
                if (act != null) act.runOnUiThread(() -> {
                    if (clickMarker != null) mapView.getOverlays().remove(clickMarker);
                    clickMarker = new Marker(mapView);
                    clickMarker.setPosition(p);
                    clickMarker.setTitle("点击这里添加足迹");
                    clickMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    mapView.getOverlays().add(clickMarker);
                    mapView.invalidate();
                    showLocationDialog();
                });
                return true;
            }
            @Override public boolean longPressHelper(GeoPoint p) { return false; }
        };
        mapView.getOverlays().add(new MapEventsOverlay(receiver));
    }

    private void setupRecyclerViews() {
        binding.locationsList.setLayoutManager(
            new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        locationAdapter = new VisitedLocationAdapter();
        locationAdapter.setOnDeleteListener(id -> confirmDeleteLocation(id));
        locationAdapter.setOnEditListener(loc -> showEditLocationDialog(loc));
        locationAdapter.setOnNavigateListener(loc -> {
            Double lat = loc.getLat();
            Double lng = loc.getLng();
            Log.d(TAG, "onNavigate: name=" + loc.getName() + " lat=" + lat + " lng=" + lng
                + " mapX=" + loc.getMapX() + " mapY=" + loc.getMapY());
            if (lat == null && loc.getMapX() != null) lat = loc.getMapX().doubleValue();
            if (lng == null && loc.getMapY() != null) lng = loc.getMapY().doubleValue();
            if (lat != null && lng != null) {
                final double navLat = lat;
                final double navLng = lng;
                Log.d(TAG, "onNavigate: animating to " + navLat + ", " + navLng);
                showTab(0);
                binding.memoriesTabs.selectTab(binding.memoriesTabs.getTabAt(0));
                mapView.post(() -> {
                    Log.d(TAG, "onNavigate: mapView.post running, map visible=" + (mapView.getVisibility() == View.VISIBLE));
                    mapView.post(() -> {
                        Log.d(TAG, "onNavigate: double-post animating now");
                        mapView.getController().animateTo(new GeoPoint(navLat, navLng));
                        mapView.getController().setZoom(12.0);
                    });
                });
            } else {
                Log.w(TAG, "onNavigate: skipping, lat/lng are null for " + loc.getName());
            }
        });
        binding.locationsList.setAdapter(locationAdapter);

        binding.albumsList.setLayoutManager(
            new GridLayoutManager(getContext(), 2));
        albumAdapter = new MemoryAlbumAdapter(
            id -> {
                Intent intent = new Intent(getContext(), AlbumDetailActivity.class);
                intent.putExtra("id", id);
                startActivity(intent);
            },
            album -> showAlbumFormDialog(album),
            id -> confirmDeleteAlbum(id));
        binding.albumsList.setAdapter(albumAdapter);

        binding.momentsList.setLayoutManager(new LinearLayoutManager(getContext()));
        momentAdapter = new MomentAdapter(
            id -> {
                Intent intent = new Intent(getContext(), MomentDetailActivity.class);
                intent.putExtra("id", id);
                startActivity(intent);
            },
            moment -> showMomentFormDialog(moment),
            id -> confirmDeleteMoment(id));
        binding.momentsList.setAdapter(momentAdapter);
    }

    private void setupFabs() {
        binding.fabAddMoment.setOnClickListener(v -> showMomentFormDialog(null));
        binding.fabAddAlbum.setOnClickListener(v -> showAlbumFormDialog(null));
        binding.fabAddLocation.setOnClickListener(v -> showLocationDialog());
    }

    // ================== Location Dialog ==================

    private void showLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("添加足迹");

        LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(48, 24, 48, 24);

        // Location status
        TextView statusTv = new TextView(getContext());
        if (pendingLat != null) {
            statusTv.setText("已选择位置 (" + String.format("%.4f", pendingLat) + ", " + String.format("%.4f", pendingLng) + ")");
            statusTv.setTextColor(0xFF81C784);
        } else {
            statusTv.setText("请关闭弹窗，在地图上点击位置");
            statusTv.setTextColor(0xFFD4A853);
        }
        statusTv.setPadding(0, 0, 0, 12);

        final EditText nameInput = new EditText(getContext());
        nameInput.setHint("城市名");

        final EditText titleInput = new EditText(getContext());
        titleInput.setHint("标题描述 (如: 第一次一起旅行)");

        final EditText provinceInput = new EditText(getContext());
        provinceInput.setHint("省份");

        final EditText dateInput = new EditText(getContext());
        dateInput.setHint("到访日期 (yyyy-MM-dd)");
        dateInput.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        android.widget.Button uploadBtn = new android.widget.Button(getContext());
        uploadBtn.setText("📷 上传照片");
        uploadBtn.setOnClickListener(v -> {
            imageTarget = "location";
            singleImagePickerLauncher.launch("image/*");
        });

        // Image preview
        final ImageView locationPreview = new ImageView(getContext());
        locationPreview.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 180));
        locationPreview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        locationPreview.setBackgroundColor(0xFFFAF7F3);
        locationPreview.setImageResource(R.drawable.image_placeholder);
        locationPreview.setVisibility(pendingLocationImageUrl != null && !pendingLocationImageUrl.isEmpty()
            ? View.VISIBLE : View.GONE);
        locationImagePreviewUpdater = () -> {
            if (pendingLocationImageUrl != null && !pendingLocationImageUrl.isEmpty()) {
                locationPreview.setVisibility(View.VISIBLE);
                coil.Coil.imageLoader(requireContext()).enqueue(
                    new coil.request.ImageRequest.Builder(requireContext())
                        .data(pendingLocationImageUrl)
                        .target(locationPreview)
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_placeholder)
                        .build());
            }
        };

        ll.addView(statusTv);
        ll.addView(nameInput);
        ll.addView(titleInput);
        ll.addView(provinceInput);
        ll.addView(dateInput);
        ll.addView(uploadBtn, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) {{
            setMargins(0, 12, 0, 0);
        }});
        ll.addView(locationPreview, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) {{
            setMargins(0, 8, 0, 0);
        }});
        ScrollView scrollView = new ScrollView(getContext());
        scrollView.addView(ll);
        builder.setView(scrollView);

        builder.setPositiveButton("保存", (d, w) -> {});
        builder.setNegativeButton("取消", (d, w) -> {
            pendingLat = null; pendingLng = null;
            pendingLocationImageUrl = null;
            if (clickMarker != null) { mapView.getOverlays().remove(clickMarker); mapView.invalidate(); clickMarker = null; }
        });
        AlertDialog locationDialog = builder.create();
        locationDialog.show();
        locationDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = nameInput.getText().toString();
            if (name.isEmpty()) { Toast.makeText(getContext(), "请输入城市名", Toast.LENGTH_SHORT).show(); return; }
            double lat = pendingLat != null ? pendingLat : 32.5;
            double lng = pendingLng != null ? pendingLng : 110.0;
            viewModel.createLocation(name, provinceInput.getText().toString(),
                dateInput.getText().toString(), titleInput.getText().toString(),
                pendingLocationImageUrl, lat, lng);
            pendingLocationImageUrl = null;
            pendingLat = null; pendingLng = null;
            if (clickMarker != null) { mapView.getOverlays().remove(clickMarker); mapView.invalidate(); clickMarker = null; }
            locationDialog.dismiss();
        });
    }

    private void showEditLocationDialog(com.niit.memory.data.model.VisitedLocation existing) {
        pendingLocationImageUrl = existing.getImageUrl();

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("编辑足迹");

        LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(48, 24, 48, 24);

        final EditText nameInput = new EditText(getContext());
        nameInput.setHint("城市名");
        nameInput.setText(existing.getName());

        final EditText provinceInput = new EditText(getContext());
        provinceInput.setHint("省份");
        provinceInput.setText(existing.getProvince());

        final EditText titleInput = new EditText(getContext());
        titleInput.setHint("标题描述");
        titleInput.setText(existing.getTitle());

        final EditText dateInput = new EditText(getContext());
        dateInput.setHint("到访日期 (yyyy-MM-dd)");
        dateInput.setText(existing.getVisitDate());

        android.widget.Button uploadBtn = new android.widget.Button(getContext());
        uploadBtn.setText("📷 更换照片");
        uploadBtn.setOnClickListener(v -> {
            imageTarget = "location";
            singleImagePickerLauncher.launch("image/*");
        });

        // Image preview
        final ImageView editLocPreview = new ImageView(getContext());
        editLocPreview.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 180));
        editLocPreview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        editLocPreview.setBackgroundColor(0xFFFAF7F3);
        if (pendingLocationImageUrl != null && !pendingLocationImageUrl.isEmpty()) {
            editLocPreview.setVisibility(View.VISIBLE);
            coil.Coil.imageLoader(requireContext()).enqueue(
                new coil.request.ImageRequest.Builder(requireContext())
                    .data(pendingLocationImageUrl)
                    .target(editLocPreview)
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_placeholder)
                    .build());
        } else {
            editLocPreview.setVisibility(View.GONE);
        }
        locationImagePreviewUpdater = () -> {
            if (pendingLocationImageUrl != null && !pendingLocationImageUrl.isEmpty()) {
                editLocPreview.setVisibility(View.VISIBLE);
                coil.Coil.imageLoader(requireContext()).enqueue(
                    new coil.request.ImageRequest.Builder(requireContext())
                        .data(pendingLocationImageUrl)
                        .target(editLocPreview)
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_placeholder)
                        .build());
            }
        };

        ll.addView(nameInput);
        ll.addView(provinceInput);
        ll.addView(titleInput);
        ll.addView(dateInput);
        ll.addView(uploadBtn, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) {{
            setMargins(0, 12, 0, 0);
        }});
        ll.addView(editLocPreview, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) {{
            setMargins(0, 8, 0, 0);
        }});
        ScrollView scrollView = new ScrollView(getContext());
        scrollView.addView(ll);
        builder.setView(scrollView);

        builder.setPositiveButton("保存", (d, w) -> {});
        builder.setNegativeButton("取消", (d, w) -> { pendingLocationImageUrl = null; });
        AlertDialog editLocDialog = builder.create();
        editLocDialog.show();
        editLocDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = nameInput.getText().toString();
            if (name.isEmpty()) { Toast.makeText(getContext(), "请输入城市名", Toast.LENGTH_SHORT).show(); return; }
            viewModel.updateLocation(existing.getId(), name, provinceInput.getText().toString(),
                dateInput.getText().toString(), titleInput.getText().toString(),
                pendingLocationImageUrl, existing.getLat(), existing.getLng());
            pendingLocationImageUrl = null;
            editLocDialog.dismiss();
        });
    }

    // ================== Moment Form ==================

    private void showMomentFormDialog(@Nullable com.niit.memory.data.model.MemoryMoment existing) {
        pendingImageUrls = (existing != null && existing.getPhotoUrls() != null) ? existing.getPhotoUrls() : "";
        imageTarget = "moment";

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(existing != null ? "编辑记忆瞬间" : "添加记忆瞬间");

        LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(48, 24, 48, 24);

        final EditText titleInput = new EditText(getContext());
        titleInput.setHint("瞬间标题");
        if (existing != null) titleInput.setText(existing.getTitle());

        final EditText dateInput = new EditText(getContext());
        dateInput.setHint("日期 (yyyy-MM-dd)");
        if (existing != null && existing.getMomentDate() != null) dateInput.setText(existing.getMomentDate());
        else dateInput.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        final EditText locInput = new EditText(getContext());
        locInput.setHint("地点");
        if (existing != null) locInput.setText(existing.getLocation());

        String[] emojis = {"🏞️", "🗼", "🌊", "💕", "🎉", "🍲", "✈️", "🏔️", "🌅"};
        final android.widget.Spinner emojiSpinner = new android.widget.Spinner(getContext());
        emojiSpinner.setAdapter(new android.widget.ArrayAdapter<>(getContext(),
            android.R.layout.simple_spinner_item, emojis));
        if (existing != null && existing.getEmoji() != null) {
            for (int i = 0; i < emojis.length; i++) {
                if (emojis[i].equals(existing.getEmoji())) { emojiSpinner.setSelection(i); break; }
            }
        }

        android.widget.Button uploadBtn = new android.widget.Button(getContext());
        uploadBtn.setText("📷 上传照片");
        uploadBtn.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // Image preview row
        final LinearLayout momentPreviewRow = new LinearLayout(getContext());
        momentPreviewRow.setOrientation(LinearLayout.HORIZONTAL);
        momentPreviewRow.setPadding(0, 8, 0, 0);
        final Runnable[] rebuildMomentPreviewHolder = {null};
        final Runnable rebuildMomentPreview = () -> {
            momentPreviewRow.removeAllViews();
            if (pendingImageUrls.isEmpty()) return;
            String[] urls = pendingImageUrls.split(",");
            for (int i = 0; i < urls.length; i++) {
                String url = urls[i].trim();
                if (url.isEmpty()) continue;
                final int index = i;
                ImageView iv = new ImageView(getContext());
                int sz = (int) (80 * getResources().getDisplayMetrics().density);
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
                    new AlertDialog.Builder(requireContext())
                        .setTitle("删除照片")
                        .setMessage("确定要删除这张照片吗？")
                        .setPositiveButton("删除", (dd, ww) -> {
                            String[] currUrls = pendingImageUrls.split(",");
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
                            pendingImageUrls = sb.toString();
                            rebuildMomentPreviewHolder[0].run();
                        })
                        .setNegativeButton("取消", null)
                        .show();
                    return true;
                });
                momentPreviewRow.addView(iv);
            }
        };
        rebuildMomentPreviewHolder[0] = rebuildMomentPreview;
        imagePreviewUpdater = rebuildMomentPreview;
        if (existing != null && existing.getPhotoUrls() != null && !existing.getPhotoUrls().isEmpty()) {
            rebuildMomentPreview.run();
        }

        ll.addView(titleInput);
        ll.addView(dateInput);
        ll.addView(locInput);
        addFormLabel(ll, "图标"); ll.addView(emojiSpinner);
        ll.addView(uploadBtn, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) {{
            setMargins(0, 12, 0, 0);
        }});
        ll.addView(momentPreviewRow);
        ScrollView scrollView = new ScrollView(getContext());
        scrollView.addView(ll);
        builder.setView(scrollView);

        final long editId = existing != null && existing.getId() != null ? existing.getId() : 0;
        builder.setPositiveButton("保存", (d, w) -> {});
        builder.setNegativeButton("取消", null);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (isUploading) {
                Toast.makeText(getContext(), "图片正在上传中，请稍候...", Toast.LENGTH_SHORT).show();
                return;
            }
            String title = titleInput.getText().toString();
            if (title.isEmpty()) { Toast.makeText(getContext(), "请输入标题", Toast.LENGTH_SHORT).show(); return; }
            String emoji = emojis[emojiSpinner.getSelectedItemPosition()];
            String imgUrls = pendingImageUrls.isEmpty() ? "" : pendingImageUrls;
            if (editId > 0) {
                viewModel.updateMoment(editId, title, dateInput.getText().toString(),
                    locInput.getText().toString(), emoji, imgUrls);
            } else {
                viewModel.createMoment(title, dateInput.getText().toString(),
                    locInput.getText().toString(), emoji, imgUrls);
            }
            dialog.dismiss();
        });
    }

    // ================== Album Form ==================

    private void showAlbumFormDialog(@Nullable com.niit.memory.data.model.MemoryAlbum existing) {
        pendingCoverUrl = existing != null ? existing.getCoverUrl() : null;
        pendingImageUrls = (existing != null && existing.getPhotoUrls() != null) ? existing.getPhotoUrls() : "";

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(existing != null ? "编辑相册" : "创建新相册");

        LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(48, 24, 48, 24);

        final EditText locInput = new EditText(getContext());
        locInput.setHint("地点/主题");
        if (existing != null) locInput.setText(existing.getLocation());

        final EditText dateInput = new EditText(getContext());
        dateInput.setHint("日期 (yyyy-MM-dd)");
        if (existing != null && existing.getAlbumDate() != null) dateInput.setText(existing.getAlbumDate());
        else dateInput.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        String[] emojis = {"🏛️", "🌊", "🗼", "🧸", "🍲", "🌹", "🍦", "🌸", "🦋"};
        final android.widget.Spinner emojiSpinner = new android.widget.Spinner(getContext());
        emojiSpinner.setAdapter(new android.widget.ArrayAdapter<>(getContext(),
            android.R.layout.simple_spinner_item, emojis));
        if (existing != null && existing.getEmoji() != null) {
            for (int i = 0; i < emojis.length; i++) {
                if (emojis[i].equals(existing.getEmoji())) { emojiSpinner.setSelection(i); break; }
            }
        }

        android.widget.Button coverBtn = new android.widget.Button(getContext());
        coverBtn.setText("📷 上传封面");
        coverBtn.setOnClickListener(v -> {
            imageTarget = "album_cover";
            singleImagePickerLauncher.launch("image/*");
        });

        // Cover preview
        final ImageView coverPreview = new ImageView(getContext());
        coverPreview.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 160));
        coverPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        coverPreview.setBackgroundColor(0xFFFAF5F0);
        coverPreview.setImageResource(R.drawable.image_placeholder);
        final Runnable rebuildCoverPreview = () -> {
            if (pendingCoverUrl != null && !pendingCoverUrl.isEmpty()) {
                coil.Coil.imageLoader(requireContext()).enqueue(
                    new coil.request.ImageRequest.Builder(requireContext())
                        .data(pendingCoverUrl)
                        .target(coverPreview)
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_placeholder)
                        .build());
            }
        };
        coverPreviewUpdater = rebuildCoverPreview;
        if (existing != null && existing.getCoverUrl() != null && !existing.getCoverUrl().isEmpty()) {
            rebuildCoverPreview.run();
        }

        android.widget.Button photoBtn = new android.widget.Button(getContext());
        photoBtn.setText("📷 上传相册图片");
        photoBtn.setOnClickListener(v -> {
            imageTarget = "album_photo";
            imagePickerLauncher.launch("image/*");
        });

        // Album photo preview row
        final LinearLayout albumPhotoPreviewRow = new LinearLayout(getContext());
        albumPhotoPreviewRow.setOrientation(LinearLayout.HORIZONTAL);
        albumPhotoPreviewRow.setPadding(0, 4, 0, 0);
        final Runnable[] rebuildAlbumPhotoPreviewHolder = {null};
        final Runnable rebuildAlbumPhotoPreview = () -> {
            albumPhotoPreviewRow.removeAllViews();
            if (pendingImageUrls.isEmpty()) return;
            String[] urls = pendingImageUrls.split(",");
            for (int i = 0; i < urls.length; i++) {
                String url = urls[i].trim();
                if (url.isEmpty()) continue;
                final int index = i;
                ImageView iv = new ImageView(getContext());
                int sz = (int) (72 * getResources().getDisplayMetrics().density);
                iv.setLayoutParams(new LinearLayout.LayoutParams(sz, sz) {{
                    setMargins(0, 0, 6, 0);
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
                    new AlertDialog.Builder(requireContext())
                        .setTitle("删除照片")
                        .setMessage("确定要删除这张照片吗？")
                        .setPositiveButton("删除", (dd, ww) -> {
                            String[] currUrls = pendingImageUrls.split(",");
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
                            pendingImageUrls = sb.toString();
                            rebuildAlbumPhotoPreviewHolder[0].run();
                        })
                        .setNegativeButton("取消", null)
                        .show();
                    return true;
                });
                albumPhotoPreviewRow.addView(iv);
            }
        };
        rebuildAlbumPhotoPreviewHolder[0] = rebuildAlbumPhotoPreview;
        if (existing != null && existing.getPhotoUrls() != null && !existing.getPhotoUrls().isEmpty()) {
            rebuildAlbumPhotoPreview.run();
        }
        imagePreviewUpdater = rebuildAlbumPhotoPreview;

        // Private toggle
        final android.widget.CheckBox privateCheck = new android.widget.CheckBox(getContext());
        privateCheck.setText("设为私密相册（仅自己可见）");
        privateCheck.setTextColor(0xFF6B5D50);
        if (existing != null && existing.getIsPrivate() != null) {
            privateCheck.setChecked(existing.getIsPrivate() == 1);
        }

        ll.addView(locInput);
        ll.addView(dateInput);
        addFormLabel(ll, "图标"); ll.addView(emojiSpinner);
        ll.addView(coverBtn, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) {{
            setMargins(0, 12, 0, 0);
        }});
        ll.addView(coverPreview, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) {{
            setMargins(0, 8, 0, 0);
        }});
        ll.addView(photoBtn, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) {{
            setMargins(0, 8, 0, 0);
        }});
        ll.addView(albumPhotoPreviewRow);
        ll.addView(privateCheck, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) {{
            setMargins(0, 8, 0, 0);
        }});
        ScrollView scrollView = new ScrollView(getContext());
        scrollView.addView(ll);
        builder.setView(scrollView);

        final long editId = existing != null && existing.getId() != null ? existing.getId() : 0;
        builder.setPositiveButton("保存", (d, w) -> {});
        builder.setNegativeButton("取消", null);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (isUploading) {
                Toast.makeText(getContext(), "图片正在上传中，请稍候...", Toast.LENGTH_SHORT).show();
                return;
            }
            String loc = locInput.getText().toString();
            if (loc.isEmpty()) { Toast.makeText(getContext(), "请输入地点/主题", Toast.LENGTH_SHORT).show(); return; }
            String emoji = emojis[emojiSpinner.getSelectedItemPosition()];
            String imgUrls = pendingImageUrls.isEmpty() ? "" : pendingImageUrls;
            // Auto-set cover from first photo if no cover was uploaded
            String finalCoverUrl = pendingCoverUrl;
            if ((finalCoverUrl == null || finalCoverUrl.isEmpty()) && !imgUrls.isEmpty()) {
                String first = imgUrls.split(",")[0].trim();
                if (!first.isEmpty()) finalCoverUrl = first;
            }
            int isPrivate = privateCheck.isChecked() ? 1 : 0;
            Log.d(TAG, "saveAlbum: editId=" + editId + " location=" + loc
                + " coverUrl=" + finalCoverUrl + " photoUrls=" + imgUrls
                + " isPrivate=" + isPrivate);
            if (editId > 0) {
                viewModel.updateAlbum(editId, loc, dateInput.getText().toString(), emoji,
                    finalCoverUrl, imgUrls, isPrivate);
            } else {
                viewModel.createAlbum(loc, dateInput.getText().toString(), emoji,
                    finalCoverUrl, imgUrls, isPrivate);
            }
            pendingCoverUrl = null;
            dialog.dismiss();
        });
    }

    // ================== Delete Confirmations ==================

    private void confirmDeleteAlbum(long id) {
        new AlertDialog.Builder(requireContext())
            .setTitle("确定删除？")
            .setPositiveButton("删除", (d, w) -> viewModel.deleteAlbum(id))
            .setNegativeButton("取消", null).show();
    }

    private void confirmDeleteLocation(long id) {
        new AlertDialog.Builder(requireContext())
            .setTitle("确定删除这个足迹吗？")
            .setPositiveButton("删除", (d, w) -> viewModel.deleteLocation(id))
            .setNegativeButton("取消", null).show();
    }

    private void confirmDeleteMoment(long id) {
        new AlertDialog.Builder(requireContext())
            .setTitle("确定删除？")
            .setPositiveButton("删除", (d, w) -> viewModel.deleteMoment(id))
            .setNegativeButton("取消", null).show();
    }

    // ================== Image Upload ==================

    private void uploadPendingImages() {
        if (pendingImageUris == null || pendingImageUris.isEmpty()) {
            Log.w(TAG, "uploadPendingImages: pendingImageUris is null/empty, skipping");
            return;
        }
        android.content.Context ctx = getContext();
        if (ctx == null) return;
        isUploading = true;
        final int total = pendingImageUris.size();
        Log.d(TAG, "uploadPendingImages: START count=" + total + " target=" + imageTarget);
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
                        Log.e(TAG, "uploadPendingImages: read failed for index " + idx, e);
                        android.app.Activity act = getActivity();
                        if (act != null) act.runOnUiThread(() ->
                            Toast.makeText(ctx, "第" + idx + "张无法读取", Toast.LENGTH_SHORT).show());
                        continue;
                    }
                    Log.d(TAG, "uploadPendingImages: index=" + idx + " tempFile size=" + tempFile.length());

                    String url = viewModel.uploadImage(tempFile);
                    Log.d(TAG, "uploadPendingImages: ViewModel returned url=" + url);
                    tempFile.delete();
                    if (url != null && !url.isEmpty()) {
                        successCount++;
                        android.app.Activity act = getActivity();
                        if (act != null) act.runOnUiThread(() -> {
                            switch (imageTarget) {
                                case "album_cover":
                                    pendingCoverUrl = url;
                                    if (coverPreviewUpdater != null) coverPreviewUpdater.run();
                                    break;
                                case "location":
                                    pendingLocationImageUrl = url;
                                    if (locationImagePreviewUpdater != null) locationImagePreviewUpdater.run();
                                    break;
                                case "album_photo":
                                case "moment":
                                default:
                                    pendingImageUrls = (pendingImageUrls.isEmpty() ? "" : pendingImageUrls + ",") + url;
                                    if (imagePreviewUpdater != null) imagePreviewUpdater.run();
                                    break;
                            }
                            Toast.makeText(ctx, "已上传 " + idx + "/" + total, Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        Log.e(TAG, "uploadPendingImages: url null/empty for index " + idx);
                        android.app.Activity act = getActivity();
                        if (act != null) act.runOnUiThread(() ->
                            Toast.makeText(ctx, "第" + idx + "张上传失败", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "uploadPendingImages: EXCEPTION for index " + idx, e);
                    android.app.Activity act = getActivity();
                    if (act != null) act.runOnUiThread(() ->
                        Toast.makeText(ctx, "第" + idx + "张上传失败", Toast.LENGTH_SHORT).show());
                }
            }
            // All done
            isUploading = false;
            final int uploaded = successCount;
            android.app.Activity act = getActivity();
            if (act != null) act.runOnUiThread(() -> {
                Toast.makeText(ctx, "上传完成 (" + uploaded + "/" + total + "张)", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void addFormLabel(LinearLayout parent, String text) {
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setTextColor(0xFF9B8B80);
        tv.setTextSize(13);
        tv.setPadding(0, 12, 0, 4);
        parent.addView(tv);
    }

    // ================== Observe ==================

    private void observeViewModel() {
        viewModel.locations.observe(getViewLifecycleOwner(), locs -> {
            if (locs != null) {
                updateMapMarkers(locs);
                locationAdapter.submitList(locs);
                binding.locationCount.setText("目前去过了 " + locs.size() + " 个地方");
            }
        });

        viewModel.albums.observe(getViewLifecycleOwner(), albums -> {
            if (albums != null) {
                albumAdapter.submitList(albums);
                boolean empty = albums.isEmpty();
                binding.albumsEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                binding.albumsList.setVisibility(empty ? View.GONE : View.VISIBLE);
            }
        });

        viewModel.moments.observe(getViewLifecycleOwner(), moments -> {
            if (moments != null) {
                momentAdapter.submitGroups(moments);
                boolean empty = moments.isEmpty();
                binding.momentsEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                binding.momentsList.setVisibility(empty ? View.GONE : View.VISIBLE);
            }
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateMapMarkers(List<com.niit.memory.data.model.VisitedLocation> locs) {
        // Remove all markers except clickMarker
        java.util.List<org.osmdroid.views.overlay.Overlay> toRemove = new java.util.ArrayList<>();
        for (org.osmdroid.views.overlay.Overlay overlay : mapView.getOverlays()) {
            if (overlay instanceof Marker && overlay != clickMarker) {
                toRemove.add(overlay);
            }
        }
        for (org.osmdroid.views.overlay.Overlay overlay : toRemove) {
            mapView.getOverlays().remove(overlay);
        }

        for (com.niit.memory.data.model.VisitedLocation loc : locs) {
            if (loc.getLat() != null && loc.getLng() != null) {
                Marker marker = new Marker(mapView);
                marker.setPosition(new GeoPoint(loc.getLat(), loc.getLng()));
                marker.setTitle(loc.getTitle() != null ? loc.getTitle() : loc.getName());
                marker.setSnippet((loc.getProvince() != null ? loc.getProvince() + " " : "")
                    + (loc.getVisitDate() != null ? loc.getVisitDate() : ""));
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                mapView.getOverlays().add(marker);
            }
        }
        mapView.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mapView != null) mapView.onDetach();
    }
}
