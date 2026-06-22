package com.niit.memory.ui.screens.daily;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.niit.memory.databinding.FragmentDailyBinding;
import com.niit.memory.ui.adapters.DailyRecordAdapter;
import java.util.Calendar;

public class DailyFragment extends Fragment {

    private FragmentDailyBinding binding;
    private DailyViewModel viewModel;
    private DailyRecordAdapter adapter;
    private String[] months = {"全部", "01", "02", "03", "04", "05", "06",
        "07", "08", "09", "10", "11", "12"};

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
            record -> {
                Intent intent = new Intent(getContext(), DailyPublishActivity.class);
                intent.putExtra("record", record);
                startActivity(intent);
            },
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
        binding.fabAddRecord.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), DailyPublishActivity.class);
            startActivity(intent);
        });
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

    @Override
    public void onResume() {
        super.onResume();
        Integer year = viewModel.currentYear.getValue();
        Integer month = viewModel.currentMonth.getValue();
        viewModel.loadRecords(year, month);
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
