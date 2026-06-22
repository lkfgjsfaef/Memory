package com.niit.memory.ui.screens.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.niit.memory.R;
import com.niit.memory.data.api.ApiClient;
import com.niit.memory.data.api.CustomEmojiService;
import com.niit.memory.data.model.ApiResponse;
import com.niit.memory.data.model.CalendarMood;
import com.niit.memory.data.model.CalendarNote;
import com.niit.memory.data.model.CustomEmoji;
import com.niit.memory.databinding.FragmentCalendarBinding;
import com.niit.memory.ui.adapters.CalendarNoteAdapter;
import com.niit.memory.ui.adapters.ImportantDateAdapter;
import com.niit.memory.util.TaskExecutor;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private CalendarViewModel viewModel;
    private CalendarNoteAdapter noteAdapter;
    private CalendarNoteAdapter monthNoteAdapter;
    private ImportantDateAdapter importantDateAdapter;
    private NavController navController;
    private int year, month;
    private String currentSelectedDate;

    private static final String[] MOODS = {"😊", "😢", "😐", "😠"};
    private static final String[] MOOD_NAMES = {"开心", "伤心", "普通", "生气"};
    private static final String[] NOTE_ICONS = {"📌", "🌟", "💕", "🎂", "🎉", "✈️", "📅", "🌸", "🍰"};

    private List<CustomEmoji> customEmojis = new ArrayList<>();
    private CustomEmojiService customEmojiService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(CalendarViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        Calendar cal = Calendar.getInstance();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        currentSelectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        noteAdapter = new CalendarNoteAdapter(id -> confirmDeleteNote(id));
        binding.calendarNotesList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.calendarNotesList.setNestedScrollingEnabled(false);
        binding.calendarNotesList.setAdapter(noteAdapter);

        monthNoteAdapter = new CalendarNoteAdapter(id -> confirmDeleteNote(id));
        binding.monthNotesList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.monthNotesList.setNestedScrollingEnabled(false);
        binding.monthNotesList.setAdapter(monthNoteAdapter);

        importantDateAdapter = new ImportantDateAdapter();
        binding.calendarImportantDatesList.setLayoutManager(
            new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.calendarImportantDatesList.setAdapter(importantDateAdapter);
        importantDateAdapter.setOnDateActionListener(new ImportantDateAdapter.OnDateActionListener() {
            @Override
            public void onEdit(com.niit.memory.data.model.ImportantDate date) {
                // Navigate to home page for editing important dates
                if (navController != null) navController.navigate(R.id.nav_home);
            }
            @Override
            public void onDelete(long id) {
                new AlertDialog.Builder(requireContext())
                    .setTitle("确定删除？")
                    .setPositiveButton("删除", (d, w) -> viewModel.deleteImportantDate(id))
                    .setNegativeButton("取消", null).show();
            }
        });

        binding.btnManageDates.setOnClickListener(v -> {
            if (navController != null) navController.navigate(R.id.nav_home);
        });

        customEmojiService = ApiClient.getInstance(requireContext())
            .create(CustomEmojiService.class);
        setupMoodPicker();
        loadCustomEmojis();
        setupNavigation();
        setupAddNote();

        viewModel.loadNotes(year, month + 1);
        observeViewModel();
        renderCalendar();
    }

    private void setupMoodPicker() {
        LinearLayout parent = binding.moodIcons;
        parent.removeAllViews();

        // Built-in mood emojis
        for (int i = 0; i < MOODS.length; i++) {
            TextView tv = new TextView(getContext());
            tv.setText(MOODS[i]);
            tv.setTextSize(28);
            tv.setPadding(16, 8, 16, 8);
            final int idx = i;
            tv.setOnClickListener(v -> {
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                viewModel.setMood(today, MOOD_NAMES[idx], MOODS[idx]);
                Toast.makeText(getContext(), "已记录 " + today + " 心情: " + MOODS[idx], Toast.LENGTH_SHORT).show();
            });
            parent.addView(tv);
        }

        // Custom emojis
        android.util.TypedValue outValue = new android.util.TypedValue();
        android.content.Context ctx = getContext();
        ctx.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        int bgRes = outValue.resourceId;
        for (CustomEmoji ce : customEmojis) {
            TextView tv = new TextView(ctx);
            tv.setText(ce.getEmojiUrl());
            tv.setTextSize(28);
            tv.setPadding(16, 8, 16, 8);
            tv.setClickable(true);
            tv.setFocusable(true);
            tv.setBackgroundResource(bgRes);
            tv.setOnClickListener(v -> {
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String name = ce.getEmojiName() != null && !ce.getEmojiName().isEmpty()
                    ? ce.getEmojiName() : "自定义";
                viewModel.setMood(today, name, ce.getEmojiUrl());
                Toast.makeText(getContext(), "已记录 " + today + " 心情: " + name, Toast.LENGTH_SHORT).show();
            });
            tv.setOnLongClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                    .setTitle("删除自定义表情")
                    .setMessage("确定要删除「" + ce.getEmojiUrl() + "」吗？")
                    .setPositiveButton("删除", (d, w) -> deleteCustomEmoji(ce.getId()))
                    .setNegativeButton("取消", null)
                    .show();
                return true;
            });
            parent.addView(tv);
        }

        // "+" add button
        TextView addBtn = new TextView(getContext());
        addBtn.setText("+");
        addBtn.setTextSize(28);
        addBtn.setPadding(16, 8, 16, 8);
        addBtn.setTextColor(0xFF9B8B80);
        addBtn.setOnClickListener(v -> showAddCustomEmojiDialog());
        parent.addView(addBtn);
    }

    private void loadCustomEmojis() {
        TaskExecutor.execute(() -> {
            try {
                retrofit2.Call<ApiResponse<List<CustomEmoji>>> call = customEmojiService.list();
                retrofit2.Response<ApiResponse<List<CustomEmoji>>> resp = call.execute();
                if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                    List<CustomEmoji> list = resp.body().getData();
                    if (list != null) {
                        customEmojis = list;
                        android.app.Activity act = getActivity();
                        if (act != null) act.runOnUiThread(this::setupMoodPicker);
                    }
                }
            } catch (Exception ignored) {}
        });
    }

    private void showAddCustomEmojiDialog() {
        android.content.Context ctx = getContext();
        if (ctx == null) return;
        LinearLayout ll = new LinearLayout(ctx);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(48, 24, 48, 24);

        final android.widget.EditText emojiInput = new android.widget.EditText(ctx);
        emojiInput.setHint("输入表情符号（如 🤓💙）");
        emojiInput.setTextSize(24);
        emojiInput.setGravity(android.view.Gravity.CENTER);
        ll.addView(emojiInput);

        final android.widget.EditText nameInput = new android.widget.EditText(ctx);
        nameInput.setHint("表情名称（可选）");
        ll.addView(nameInput, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) {{
            setMargins(0, 12, 0, 0);
        }});

        new AlertDialog.Builder(ctx)
            .setTitle("添加自定义表情")
            .setView(ll)
            .setPositiveButton("添加", (d, w) -> {
                String emoji = emojiInput.getText().toString().trim();
                if (emoji.isEmpty()) {
                    Toast.makeText(ctx, "请输入表情符号", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Check duplicates: built-in moods + custom emojis
                for (String m : MOODS) {
                    if (m.equals(emoji)) {
                        Toast.makeText(ctx, "该表情已存在", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                for (CustomEmoji ce : customEmojis) {
                    if (emoji.equals(ce.getEmojiUrl())) {
                        Toast.makeText(ctx, "该表情已存在", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                String name = nameInput.getText().toString().trim();
                TaskExecutor.execute(() -> {
                    try {
                        CustomEmoji ce = new CustomEmoji();
                        ce.setEmojiUrl(emoji);
                        ce.setEmojiName(name);
                        retrofit2.Response<ApiResponse<Void>> resp = customEmojiService.add(ce).execute();
                        android.app.Activity act = getActivity();
                        if (act != null) {
                            act.runOnUiThread(() -> {
                                if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                                    loadCustomEmojis();
                                } else {
                                    String msg = (resp.body() != null) ? resp.body().getMessage() : "添加失败";
                                    Toast.makeText(act, msg, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (Exception e) {
                        android.app.Activity act = getActivity();
                        if (act != null) {
                            act.runOnUiThread(() -> Toast.makeText(act, "网络错误，请重试", Toast.LENGTH_SHORT).show());
                        }
                    }
                });
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void deleteCustomEmoji(long id) {
        TaskExecutor.execute(() -> {
            try {
                customEmojiService.delete(id).execute();
                android.app.Activity act = getActivity();
                if (act != null) act.runOnUiThread(this::loadCustomEmojis);
            } catch (Exception ignored) {}
        });
    }

    private void setupNavigation() {
        binding.prevMonth.setOnClickListener(v -> {
            month--;
            if (month < 0) { month = 11; year--; }
            updateCalendar();
        });
        binding.nextMonth.setOnClickListener(v -> {
            month++;
            if (month > 11) { month = 0; year++; }
            updateCalendar();
        });
        binding.btnToday.setOnClickListener(v -> {
            Calendar today = Calendar.getInstance();
            year = today.get(Calendar.YEAR);
            month = today.get(Calendar.MONTH);
            currentSelectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            updateCalendar();
            binding.selectedDateLabel.setText(currentSelectedDate + " 笔记");
            List<CalendarNote> todayNotes = viewModel.getNotesForDate(currentSelectedDate);
            noteAdapter.submitList(todayNotes);
        });
    }

    private void setupAddNote() {
        binding.addNoteButton.setOnClickListener(v -> {
            if (currentSelectedDate == null) {
                Toast.makeText(getContext(), "请先选择日期", Toast.LENGTH_SHORT).show();
                return;
            }
            showAddNoteDialog();
        });
    }

    private void showDayActionDialog(String dateStr) {
        android.content.Context ctx = getContext();
        if (ctx == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(dateStr);

        LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(48, 24, 48, 24);

        boolean isToday = dateStr.equals(
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        // Mood section — only for today
        final String[] selectedMood = {null};
        final String[] selectedMoodIcon = {null};
        LinearLayout moodRow = null;
        if (isToday) {
            TextView moodLabel = new TextView(getContext());
            moodLabel.setText("记录心情：");
            moodLabel.setTextColor(0xFF3D3028);
            moodLabel.setTextSize(14);
            ll.addView(moodLabel);

            // Wrap in HorizontalScrollView for custom emojis
            android.widget.HorizontalScrollView scrollView = new android.widget.HorizontalScrollView(ctx);
            scrollView.setHorizontalScrollBarEnabled(false);

            moodRow = new LinearLayout(getContext());
            moodRow.setOrientation(LinearLayout.HORIZONTAL);
            moodRow.setPadding(0, 8, 0, 16);
            scrollView.addView(moodRow);

            android.util.TypedValue outValue = new android.util.TypedValue();
            ctx.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            int bgRes = outValue.resourceId;

            // Built-in moods
            for (int i = 0; i < MOODS.length; i++) {
                LinearLayout moodItem = createMoodItem(ctx, MOODS[i], MOOD_NAMES[i], bgRes);
                final int idx = i;
                final LinearLayout row = moodRow;
                moodItem.setOnClickListener(v -> {
                    selectedMood[0] = MOOD_NAMES[idx];
                    selectedMoodIcon[0] = MOODS[idx];
                    highlightSelectedMood(row, v);
                    Toast.makeText(getContext(), "已选: " + MOODS[idx] + " " + MOOD_NAMES[idx], Toast.LENGTH_SHORT).show();
                });
                moodRow.addView(moodItem);
            }

            // Custom emojis
            for (CustomEmoji ce : customEmojis) {
                String name = ce.getEmojiName() != null && !ce.getEmojiName().isEmpty()
                    ? ce.getEmojiName() : "自定义";
                LinearLayout moodItem = createMoodItem(ctx, ce.getEmojiUrl(), name, bgRes);
                final String moodName = name;
                final String moodIcon = ce.getEmojiUrl();
                final LinearLayout row = moodRow;
                moodItem.setOnClickListener(v -> {
                    selectedMood[0] = moodName;
                    selectedMoodIcon[0] = moodIcon;
                    highlightSelectedMood(row, v);
                    Toast.makeText(getContext(), "已选: " + moodIcon + " " + moodName, Toast.LENGTH_SHORT).show();
                });
                moodRow.addView(moodItem);
            }
            ll.addView(scrollView);
        }

        // Note section
        TextView noteLabel = new TextView(getContext());
        noteLabel.setText("添加笔记：");
        noteLabel.setTextColor(0xFF3D3028);
        noteLabel.setTextSize(14);
        ll.addView(noteLabel);

        final android.widget.EditText noteInput = new android.widget.EditText(getContext());
        noteInput.setHint("写点什么...");
        ll.addView(noteInput, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) {{
            setMargins(0, 8, 0, 8);
        }});

        // Icon picker for note
        final android.widget.Spinner iconSpinner = new android.widget.Spinner(getContext());
        android.widget.ArrayAdapter<String> iconAdapter = new android.widget.ArrayAdapter<>(
            getContext(), android.R.layout.simple_spinner_item, NOTE_ICONS);
        iconAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        iconSpinner.setAdapter(iconAdapter);
        ll.addView(iconSpinner);

        builder.setView(ll);

        builder.setPositiveButton("保存", (dialog, which) -> {
            // Save mood if selected
            if (selectedMood[0] != null) {
                viewModel.setMood(dateStr, selectedMood[0], selectedMoodIcon[0]);
            }
            // Save note if text entered
            String text = noteInput.getText().toString().trim();
            if (!text.isEmpty()) {
                int iconIdx = iconSpinner.getSelectedItemPosition();
                String icon = NOTE_ICONS[iconIdx >= 0 ? iconIdx : 0];
                viewModel.addNote(dateStr, text, icon);
            }

            // Update the selected date notes display
            binding.selectedDateLabel.setText(dateStr + " 笔记");
            List<CalendarNote> dayNotes = viewModel.getNotesForDate(dateStr);
            noteAdapter.submitList(dayNotes);
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void showAddNoteDialog() {
        android.content.Context ctx = getContext();
        if (ctx == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("添加笔记 - " + currentSelectedDate);

        LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(48, 24, 48, 24);

        final android.widget.EditText input = new android.widget.EditText(getContext());
        input.setHint("写点什么...");

        final String[] calEmojis = NOTE_ICONS;
        final android.widget.Spinner iconSpinner = new android.widget.Spinner(getContext());
        android.widget.ArrayAdapter<String> iconAdapter = new android.widget.ArrayAdapter<>(
            getContext(), android.R.layout.simple_spinner_item, calEmojis);
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
                viewModel.addNote(currentSelectedDate, text, icon);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void updateCalendar() {
        binding.currentMonthTitle.setText(year + "年" + (month + 1) + "月");
        viewModel.loadNotes(year, month + 1);
        renderCalendar();
    }

    private void renderCalendar() {
        binding.currentMonthTitle.setText(year + "年" + (month + 1) + "月");

        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        binding.calendarGrid.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() { return 42; }
            @Override
            public Object getItem(int pos) { return null; }
            @Override
            public long getItemId(int pos) { return 0; }
            @Override
            public View getView(int pos, View convert, ViewGroup parent) {
                View v;
                if (convert == null) {
                    v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_calendar_day, parent, false);
                } else {
                    v = convert;
                }
                TextView dayNum = v.findViewById(R.id.day_number);
                TextView dot = v.findViewById(R.id.day_dot);
                TextView dayMood = v.findViewById(R.id.day_mood);
                if (dayNum == null || dot == null || dayMood == null) return v;

                int day = pos - firstDayOfWeek + 1;
                if (pos < firstDayOfWeek || day > daysInMonth) {
                    dayNum.setText("");
                    dot.setVisibility(View.GONE);
                    dayMood.setVisibility(View.GONE);
                    dayNum.setBackgroundColor(0x00000000);
                } else {
                    dayNum.setText(String.valueOf(day));
                    String dateStr = String.format(Locale.getDefault(), "%d-%02d-%02d",
                        year, month + 1, day);

                    // Highlight today
                    Calendar today = Calendar.getInstance();
                    boolean isToday = year == today.get(Calendar.YEAR)
                        && month == today.get(Calendar.MONTH)
                        && day == today.get(Calendar.DAY_OF_MONTH);
                    boolean isWeekend = pos % 7 == 0 || pos % 7 == 6;

                    dayNum.setTextColor(isToday ? 0xFFFFFFFF : (isWeekend ? 0xFFE8C0C0 : 0xFF3D3028));
                    dayNum.setBackgroundColor(isToday ? 0xFFFF9A9E : (isWeekend ? 0x10FFFDFA : 0x00000000));

                    // Show dot if has notes
                    List<CalendarNote> notes = viewModel.getNotesForDate(dateStr);
                    boolean hasNote = notes != null && !notes.isEmpty();
                    dot.setVisibility(hasNote ? View.VISIBLE : View.GONE);

                    // Show mood emojis
                    List<CalendarMood> moods = viewModel.getMoodsForDate(dateStr);
                    if (moods != null && !moods.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        for (CalendarMood m : moods) {
                            if (m.getMoodIcon() != null) sb.append(m.getMoodIcon());
                        }
                        dayMood.setText(sb.toString());
                        dayMood.setVisibility(View.VISIBLE);
                    } else {
                        dayMood.setVisibility(View.GONE);
                    }

                    // Note indicator - orange bg for days with notes
                    if (hasNote && !isToday) {
                        dayNum.setBackgroundColor(0x20FFE0B0);
                    }

                    v.setOnClickListener(v2 -> {
                        currentSelectedDate = dateStr;
                        showDayActionDialog(dateStr);
                    });
                }
                return v;
            }
        });

        // Show today's notes by default
        binding.selectedDateLabel.setText(currentSelectedDate + " 笔记");
        List<CalendarNote> todayNotes = viewModel.getNotesForDate(currentSelectedDate);
        noteAdapter.submitList(todayNotes);
    }

    private void confirmDeleteNote(long id) {
        new AlertDialog.Builder(requireContext())
            .setTitle("确定删除这条标记？")
            .setPositiveButton("删除", (d, w) -> viewModel.deleteNote(id))
            .setNegativeButton("取消", null).show();
    }

    private void observeViewModel() {
        viewModel.notes.observe(getViewLifecycleOwner(), notes -> {
            renderCalendar();
            if (currentSelectedDate != null) {
                List<CalendarNote> dayNotes = viewModel.getNotesForDate(currentSelectedDate);
                noteAdapter.submitList(dayNotes);
            }
            // Update month notes list
            if (notes != null) {
                monthNoteAdapter.submitList(notes);
                boolean empty = notes.isEmpty();
                binding.monthNotesEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                binding.monthNotesList.setVisibility(empty ? View.GONE : View.VISIBLE);
            }
        });
        viewModel.monthMoods.observe(getViewLifecycleOwner(), moods -> {
            renderCalendar();
        });
        viewModel.importantDates.observe(getViewLifecycleOwner(), dates -> {
            if (dates != null) importantDateAdapter.submitList(dates);
        });
        viewModel.loading.observe(getViewLifecycleOwner(), loading -> {
            if (loading != null) {
                binding.monthNotesLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
            }
        });
        viewModel.errorMessage.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    private LinearLayout createMoodItem(android.content.Context ctx, String emoji, String label, int bgRes) {
        LinearLayout item = new LinearLayout(ctx);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(android.view.Gravity.CENTER);
        item.setPadding(12, 8, 12, 8);
        item.setClickable(true);
        item.setFocusable(true);
        item.setBackgroundResource(bgRes);

        TextView emojiTv = new TextView(ctx);
        emojiTv.setText(emoji);
        emojiTv.setTextSize(32);
        emojiTv.setGravity(android.view.Gravity.CENTER);
        item.addView(emojiTv);

        TextView labelTv = new TextView(ctx);
        labelTv.setText(label);
        labelTv.setTextSize(11);
        labelTv.setTextColor(0xFF9B8B80);
        labelTv.setGravity(android.view.Gravity.CENTER);
        item.addView(labelTv);

        return item;
    }

    private void highlightSelectedMood(LinearLayout row, View selected) {
        for (int j = 0; j < row.getChildCount(); j++) {
            row.getChildAt(j).setBackgroundColor(
                row.getChildAt(j) == selected ? 0x30FF9A9E : 0x00000000);
        }
    }
}
