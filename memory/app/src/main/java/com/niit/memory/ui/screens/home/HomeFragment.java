package com.niit.memory.ui.screens.home;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import coil.Coil;
import coil.request.ImageRequest;
import com.niit.memory.R;
import com.niit.memory.data.model.CalendarNote;
import com.niit.memory.data.model.Couple;
import com.niit.memory.data.model.ImportantDate;
import com.niit.memory.databinding.FragmentHomeBinding;
import com.niit.memory.util.SessionManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private NavController navController;

    private int calYear, calMonth;
    private String selectedCalDate;
    private int avatarTargetUserId; // 1=his, 2=her
    private Uri avatarFileUri;

    private SessionManager session;

    private final ActivityResultLauncher<String> imagePickerLauncher =
        registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                avatarFileUri = uri;
                uploadAvatar();
            }
        });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        session = SessionManager.getInstance(requireContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        Calendar cal = Calendar.getInstance();
        calYear = cal.get(Calendar.YEAR);
        calMonth = cal.get(Calendar.MONTH);

        setupMiniCalendar();
        setupClickListeners();
        setupAvatarClicks();
        observeViewModel();
        viewModel.loadData();
    }

    private void setupMiniCalendar() {
        viewModel.loadCalendarNotes(calYear, calMonth + 1);
        renderCalendar();
    }

    private void renderCalendar() {
        binding.miniCalendarTitle.setText(calYear + "年 " + (calMonth + 1) + "月");

        Calendar cal = Calendar.getInstance();
        cal.set(calYear, calMonth, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int todayYear = Calendar.getInstance().get(Calendar.YEAR);
        int todayMonth = Calendar.getInstance().get(Calendar.MONTH);
        int todayDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        LinearLayout grid = binding.miniCalendarGrid;
        grid.removeAllViews();

        // Build 6 rows (7 cols each) for the full month grid
        for (int row = 0; row < 6; row++) {
            LinearLayout rowLayout = new LinearLayout(getContext());
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            for (int col = 0; col < 7; col++) {
                int pos = row * 7 + col;
                int day = pos - firstDayOfWeek + 1;

                TextView tv = new TextView(getContext());
                tv.setGravity(android.view.Gravity.CENTER);
                tv.setPadding(4, 10, 4, 10);
                tv.setTextSize(14);
                tv.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                if (pos < firstDayOfWeek || day > daysInMonth) {
                    tv.setText("");
                    tv.setTextColor(0xFFD8CFC5);
                    tv.setBackgroundResource(0);
                    rowLayout.addView(tv);
                } else {
                    // Create a vertical layout for day number + dot indicator
                    LinearLayout cellLayout = new LinearLayout(getContext());
                    cellLayout.setOrientation(LinearLayout.VERTICAL);
                    cellLayout.setGravity(android.view.Gravity.CENTER);
                    cellLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    cellLayout.setPadding(4, 6, 4, 6);

                    // Fix: tv was created with width=0 for horizontal layout, but it's now inside a VERTICAL cellLayout.
                    // In vertical layout, layout_weight distributes height, so width=0 would make text invisible.
                    tv.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    tv.setText(String.valueOf(day));
                    String dateStr = String.format(Locale.getDefault(), "%d-%02d-%02d", calYear, calMonth + 1, day);

                    boolean isToday = calYear == todayYear && calMonth == todayMonth && day == todayDay;
                    boolean isSelected = dateStr.equals(selectedCalDate);
                    boolean isWeekend = col == 0 || col == 6;

                    if (isToday) {
                        tv.setTextColor(0xFFE88D2E);
                        tv.setBackgroundColor(0x20FFE0A0);
                    } else if (isWeekend) {
                        tv.setTextColor(0xFFE8C0C0);
                        tv.setBackgroundColor(0x10FFFDFA);
                    } else {
                        tv.setTextColor(0xFF7B6B5E);
                        tv.setBackgroundColor(0x00000000);
                    }

                    if (isSelected && !isToday) {
                        tv.setBackgroundColor(0x30FFE8D0);
                    }

                    cellLayout.addView(tv);

                    // Event dot indicator
                    TextView dot = new TextView(getContext());
                    dot.setTextSize(8);
                    dot.setGravity(android.view.Gravity.CENTER);

                    List<ImportantDate> impDates = viewModel.importantDates.getValue();
                    List<CalendarNote> calNotes = viewModel.calendarNotes.getValue();
                    boolean hasEvent = false;
                    if (impDates != null) {
                        for (ImportantDate d : impDates) {
                            if (dateStr.equals(d.getEventDate())) { hasEvent = true; break; }
                        }
                    }
                    if (!hasEvent && calNotes != null) {
                        for (CalendarNote n : calNotes) {
                            if (dateStr.equals(n.getNoteDate())) { hasEvent = true; break; }
                        }
                    }
                    if (hasEvent) {
                        dot.setText("•");
                        dot.setTextColor(0xFFFF9A9E);
                    } else {
                        dot.setText("");
                    }
                    cellLayout.addView(dot);

                    final String ds = dateStr;
                    cellLayout.setClickable(true);
                    cellLayout.setOnClickListener(v -> {
                        selectedCalDate = ds;
                        renderCalendar();
                        showCalDatePopup(ds);
                    });

                    rowLayout.addView(cellLayout);
                }
            }
            grid.addView(rowLayout);
        }
    }

    private void showCalDatePopup(String dateStr) {
        // Collect events for this date
        StringBuilder eventsText = new StringBuilder();
        List<ImportantDate> dates = viewModel.importantDates.getValue();
        if (dates != null) {
            for (ImportantDate d : dates) {
                if (dateStr.equals(d.getEventDate())) {
                    String icon = d.getIcon() != null ? d.getIcon() : "📅";
                    long dl = d.getDaysLeft() != null ? d.getDaysLeft() : 0;
                    String countdown = dl > 0 ? "还有" + dl + "天" : (dl == 0 ? "就是今天!" : "已过");
                    eventsText.append(icon).append(" ").append(d.getTitle())
                        .append(" (").append(countdown).append(")\n");
                }
            }
        }
        List<CalendarNote> notes = viewModel.calendarNotes.getValue();
        if (notes != null) {
            for (CalendarNote n : notes) {
                if (dateStr.equals(n.getNoteDate())) {
                    String icon = n.getIcon() != null ? n.getIcon() : "📝";
                    eventsText.append(icon).append(" ").append(n.getText()).append("\n");
                }
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(dateStr);
        if (eventsText.length() > 0) {
            builder.setMessage(eventsText.toString().trim());
        } else {
            builder.setMessage("这一天还没有标记");
        }
        builder.setPositiveButton("添加标记", (d, w) -> showCalNoteDialog(dateStr));
        builder.setNegativeButton("关闭", null);
        builder.show();
    }

    private void showCalNoteDialog(String dateStr) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("添加日历标记 - " + dateStr);

        LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(48, 24, 48, 24);

        final EditText input = new EditText(getContext());
        input.setHint("标记内容");

        final String[] calEmojis = {"📌", "🌟", "💕", "🎉", "🍰", "🎂", "💐", "🌸", "🎵", "📝"};
        final String[] calEmojiLabels = new String[calEmojis.length];
        for (int i = 0; i < calEmojis.length; i++) {
            calEmojiLabels[i] = calEmojis[i] + " " + getEmojiName(calEmojis[i]);
        }
        final android.widget.Spinner iconSpinner = new android.widget.Spinner(getContext());
        android.widget.ArrayAdapter<String> iconAdapter = new android.widget.ArrayAdapter<>(
            getContext(), android.R.layout.simple_spinner_item, calEmojiLabels);
        iconAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        iconSpinner.setAdapter(iconAdapter);

        ll.addView(input);
        ll.addView(iconSpinner, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) {{
            setMargins(0, 16, 0, 0);
        }});
        builder.setView(ll);

        builder.setPositiveButton("保存", (dialog, which) -> {
            String text = input.getText().toString();
            if (!text.isEmpty()) {
                int idx = iconSpinner.getSelectedItemPosition();
                String icon = calEmojis[idx >= 0 ? idx : 0];
                viewModel.addCalendarNote(dateStr, text, icon, calYear, calMonth + 1);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private String getEmojiName(String emoji) {
        switch (emoji) {
            case "📌": return "图钉";
            case "🌟": return "星星";
            case "💕": return "爱心";
            case "🎉": return "庆祝";
            case "🍰": return "蛋糕";
            case "🎂": return "生日";
            case "💐": return "鲜花";
            case "🌸": return "樱花";
            case "🎵": return "音乐";
            case "📝": return "笔记";
            default: return "";
        }
    }

    private void setupClickListeners() {
        NavOptions navOptions = new NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setPopUpTo(R.id.nav_home, true)
            .build();

        binding.actionDaily.setOnClickListener(v ->
            navController.navigate(R.id.nav_daily, null, navOptions));
        binding.actionWishlist.setOnClickListener(v ->
            navController.navigate(R.id.nav_wishlist, null, navOptions));
        binding.actionCalendar.setOnClickListener(v ->
            navController.navigate(R.id.nav_calendar, null, navOptions));
        binding.actionMemories.setOnClickListener(v ->
            navController.navigate(R.id.nav_memories, null, navOptions));

        binding.calPrevMonth.setOnClickListener(v -> {
            calMonth--;
            if (calMonth < 0) { calMonth = 11; calYear--; }
            selectedCalDate = null;
            viewModel.loadCalendarNotes(calYear, calMonth + 1);
            renderCalendar();
        });
        binding.calNextMonth.setOnClickListener(v -> {
            calMonth++;
            if (calMonth > 11) { calMonth = 0; calYear++; }
            selectedCalDate = null;
            viewModel.loadCalendarNotes(calYear, calMonth + 1);
            renderCalendar();
        });

        binding.btnAddImportantDate.setOnClickListener(v -> showImportantDateDialog(null));
    }

    private void setupAvatarClicks() {
        binding.avatarHis.setOnClickListener(v -> {
            int myUserId = parseUserIdSafe();
            if (myUserId != 1) {
                Toast.makeText(getContext(), "只能更换自己的头像哦~", Toast.LENGTH_SHORT).show();
                return;
            }
            avatarTargetUserId = 1;
            imagePickerLauncher.launch("image/*");
        });
        binding.avatarHer.setOnClickListener(v -> {
            int myUserId = parseUserIdSafe();
            if (myUserId != 2) {
                Toast.makeText(getContext(), "只能更换自己的头像哦~", Toast.LENGTH_SHORT).show();
                return;
            }
            avatarTargetUserId = 2;
            imagePickerLauncher.launch("image/*");
        });
    }

    private int parseUserIdSafe() {
        try {
            String uid = session.getUserId();
            return uid != null ? Integer.parseInt(uid) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void uploadAvatar() {
        if (avatarFileUri == null) return;
        android.content.Context ctx = getContext();
        if (ctx == null) return;
        Toast.makeText(ctx, "上传中...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                InputStream rawIs = ctx.getContentResolver().openInputStream(avatarFileUri);
                if (rawIs == null) {
                    android.app.Activity activity = getActivity();
                if (activity != null) activity.runOnUiThread(() ->
                        Toast.makeText(ctx, "无法读取图片文件", Toast.LENGTH_SHORT).show());
                    return;
                }
                File tempFile = new File(ctx.getCacheDir(), "avatar_upload.jpg");
                try (InputStream is = rawIs;
                     FileOutputStream fos = new FileOutputStream(tempFile)) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = is.read(buf)) != -1) fos.write(buf, 0, n);
                }

                String url = viewModel.uploadImage(tempFile);
                tempFile.delete();
                if (url != null && !url.isEmpty()) {
                    viewModel.updateAvatar(url);
                    android.app.Activity activity = getActivity();
                if (activity != null) activity.runOnUiThread(() -> {
                        session.saveAvatarUrl(url);
                        loadAvatarImages();
                        Toast.makeText(ctx, "头像更新成功", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    android.app.Activity activity = getActivity();
                if (activity != null) activity.runOnUiThread(() ->
                        Toast.makeText(ctx, "上传失败", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e(TAG, "Avatar upload error", e);
                android.app.Activity activity = getActivity();
                if (activity != null) activity.runOnUiThread(() ->
                    Toast.makeText(ctx, "上传失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showImportantDateDialog(@Nullable ImportantDate existing) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(existing != null ? "编辑重要日子" : "添加重要日子");

        LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(48, 24, 48, 24);

        final EditText titleInput = new EditText(getContext());
        titleInput.setHint("标题");
        if (existing != null) titleInput.setText(existing.getTitle());

        final String[] emojis = {"💚", "🎂", "🎋", "🌕", "🇨🇳", "💕", "🎉", "📅", "🌟", "🎁", "💍", "🎊"};
        final android.widget.Spinner iconSpinner = new android.widget.Spinner(getContext());
        android.widget.ArrayAdapter<String> iconAdapter = new android.widget.ArrayAdapter<>(
            getContext(), android.R.layout.simple_spinner_item, emojis);
        iconAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        iconSpinner.setAdapter(iconAdapter);

        final EditText dateInput = new EditText(getContext());
        dateInput.setHint("日期 (yyyy-MM-dd)");
        if (existing != null) dateInput.setText(existing.getEventDate());

        final EditText lunarInput = new EditText(getContext());
        lunarInput.setHint("农历日期 (如: 农历五月初五)");
        if (existing != null && existing.getLunarDate() != null) lunarInput.setText(existing.getLunarDate());

        final EditText noteInput = new EditText(getContext());
        noteInput.setHint("备注");
        if (existing != null) noteInput.setText(existing.getNote());

        final android.widget.CheckBox recurringCb = new android.widget.CheckBox(getContext());
        recurringCb.setText("每年重复");
        if (existing != null && existing.getRecurring() != null) {
            recurringCb.setChecked(existing.getRecurring() == 1);
        } else {
            recurringCb.setChecked(true);
        }

        ll.addView(titleInput);
        ll.addView(iconSpinner, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) {{
            setMargins(0, 12, 0, 0);
        }});
        ll.addView(dateInput);
        ll.addView(lunarInput);
        ll.addView(noteInput);
        ll.addView(recurringCb);
        builder.setView(ll);

        final long editId = existing != null && existing.getId() != null ? existing.getId() : 0;
        builder.setPositiveButton("保存", (d, w) -> {
            String title = titleInput.getText().toString();
            String date = dateInput.getText().toString();
            String lunar = lunarInput.getText().toString();
            String note = noteInput.getText().toString();
            int recurring = recurringCb.isChecked() ? 1 : 0;
            if (title.isEmpty() || date.isEmpty()) {
                Toast.makeText(getContext(), "标题和日期不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            String icon = emojis[iconSpinner.getSelectedItemPosition()];
            Integer recMonth = existing != null ? existing.getRecurringMonth() : null;
            Integer recDay = existing != null ? existing.getRecurringDay() : null;
            if (editId > 0) {
                viewModel.updateImportantDate(editId, title, icon, date, lunar, note, recurring, recMonth, recDay);
            } else {
                viewModel.createImportantDate(title, icon, date, lunar, note, recurring, recMonth, recDay);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void observeViewModel() {
        viewModel.couple.observe(getViewLifecycleOwner(), cr -> {
            if (cr != null && cr.getCouple() != null) {
                Couple c = cr.getCouple();
                updateDaysCounter(c, cr.getLoveDays());
                binding.avatarHisLabel.setText(c.getHisName() != null ? c.getHisName() : "他");
                binding.avatarHerLabel.setText(c.getHerName() != null ? c.getHerName() : "她");
            }
        });

        viewModel.importantDates.observe(getViewLifecycleOwner(), dates -> {
            if (dates != null) renderImportantDates(dates);
        });

        viewModel.calendarNotes.observe(getViewLifecycleOwner(), notes -> {
            renderCalendar();
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.loading.observe(getViewLifecycleOwner(), loading -> {
            if (loading != null) {
                binding.datesLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void updateDaysCounter(Couple c, Long loveDays) {
        if (loveDays != null) {
            binding.daysCount.setText(String.valueOf(loveDays));
        } else {
            // Fallback: use computed days from start date or a default value
            if (c != null && c.getLoveStartDate() != null) {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                    java.util.Date startDate = sdf.parse(c.getLoveStartDate());
                    long diffMs = System.currentTimeMillis() - startDate.getTime();
                    long days = diffMs / (1000 * 60 * 60 * 24);
                    binding.daysCount.setText(String.valueOf(days));
                } catch (Exception e) {
                    binding.daysCount.setText("2770");
                }
            } else {
                binding.daysCount.setText("2770");
            }
        }
        if (c != null && c.getLoveStartDate() != null) {
            binding.loveStartDate.setText("从 " + c.getLoveStartDate() + " 起…");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAvatarImages();
    }

    private void loadAvatarImages() {
        android.content.Context ctx = getContext();
        if (ctx == null) return;
        SessionManager sm = SessionManager.getInstance(ctx);
        new Thread(() -> {
            try {
                com.niit.memory.data.repository.AuthRepository repo =
                    new com.niit.memory.data.repository.AuthRepository(ctx);
                com.niit.memory.data.model.User me = repo.getMe();
                com.niit.memory.data.model.User partner = repo.getUserById(
                    (sm.getUserId() != null && sm.getUserId().equals("1")) ? 2 : 1);

                // Cache both avatar URLs for login page
                if (me != null && partner != null) {
                    if (me.getId() == 1) {
                        sm.saveHisAvatarUrl(me.getAvatarUrl());
                        sm.saveHerAvatarUrl(partner.getAvatarUrl());
                    } else {
                        sm.saveHisAvatarUrl(partner.getAvatarUrl());
                        sm.saveHerAvatarUrl(me.getAvatarUrl());
                    }
                }

                android.app.Activity activity = getActivity();
                if (activity != null) activity.runOnUiThread(() -> {
                    if (me != null) loadAvatarImage(me.getId() == 1 ? binding.avatarHis : binding.avatarHer, me.getAvatarUrl());
                    if (partner != null) loadAvatarImage(partner.getId() == 1 ? binding.avatarHis : binding.avatarHer, partner.getAvatarUrl());
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading avatars", e);
            }
        }).start();
    }

    private void loadAvatarImage(android.widget.ImageView imageView, String url) {
        if (url != null && !url.isEmpty()) {
            ImageRequest req = new ImageRequest.Builder(requireContext())
                .data(url)
                .target(imageView)
                .placeholder(R.drawable.ic_avatar_default)
                .error(R.drawable.ic_avatar_default)
                .build();
            Coil.imageLoader(requireContext()).enqueue(req);
        }
    }

    private void renderImportantDates(List<ImportantDate> dates) {
        LinearLayout container = binding.importantDatesContainer;
        container.removeAllViews();
        android.content.Context ctx = getContext();
        if (ctx == null) return;

        for (ImportantDate date : dates) {
            View itemView = LayoutInflater.from(ctx)
                .inflate(R.layout.item_important_date, container, false);

            TextView icon = itemView.findViewById(R.id.date_icon);
            TextView title = itemView.findViewById(R.id.date_title);
            TextView eventDate = itemView.findViewById(R.id.date_event_date);
            TextView daysLeft = itemView.findViewById(R.id.date_days_left);
            ProgressBar progress = itemView.findViewById(R.id.date_progress);
            TextView editBtn = itemView.findViewById(R.id.date_edit);
            TextView deleteBtn = itemView.findViewById(R.id.date_delete);

            icon.setText(date.getIcon() != null ? date.getIcon() : "📅");
            title.setText(date.getTitle());
            eventDate.setText(date.getEventDate());

            long dl = date.getDaysLeft() != null ? date.getDaysLeft() : 0;
            if (dl > 0) {
                daysLeft.setText("还有 " + dl + " 天");
                daysLeft.setTextColor(0xFFE88D2E);
            } else if (dl == 0) {
                daysLeft.setText("就是今天!");
                daysLeft.setTextColor(0xFF81C784);
            } else {
                daysLeft.setText("已过");
                daysLeft.setTextColor(0xFFC8BFB5);
            }

            long prog = 200 - dl;
            if (prog < 0) prog = 0;
            if (prog > 200) prog = 200;
            progress.setProgress((int) prog);

            editBtn.setOnClickListener(v -> showImportantDateDialog(date));
            deleteBtn.setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                    .setTitle("确定删除？")
                    .setPositiveButton("删除", (d, w) -> viewModel.deleteImportantDate(date.getId()))
                    .setNegativeButton("取消", null)
                    .show());

            container.addView(itemView);
        }
    }
}
