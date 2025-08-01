package com.example.vcampusexpenses.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.activity.TransactionActivity;
import com.example.vcampusexpenses.activity.SettingActivity;
import com.example.vcampusexpenses.utils.DateFilterUtil;
import com.example.vcampusexpenses.utils.DateFilterView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private ImageButton btnSetting, btnAdd;
    private DateFilterView dateFilterView;
    private DateFilterUtil dateFilterUtil;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeViews(view);
        initializeDateFilter(savedInstanceState);
        setupClickListeners();

        return view;
    }

    private void initializeViews(View view) {
        btnAdd = view.findViewById(R.id.btn_add);
        btnSetting = view.findViewById(R.id.btnSetting);
        dateFilterView = view.findViewById(R.id.date_filter_view);
    }

    private void initializeDateFilter(Bundle savedInstanceState) {
        dateFilterUtil = new DateFilterUtil(requireContext(), getParentFragmentManager());
        dateFilterView.setDateFilterUtil(dateFilterUtil, getParentFragmentManager());
        dateFilterUtil.setOnDateFilterChangedListener((startDate, endDate, filterType) -> loadDataForCurrentPeriod(startDate, endDate, filterType));

        if (savedInstanceState != null) {
            dateFilterUtil.restoreInstanceState(savedInstanceState);
        }
    }

    private void setupClickListeners() {
        btnSetting.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingActivity.class);
            startActivity(intent);
        });
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TransactionActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        dateFilterUtil.saveInstanceState(outState);
    }

    private void loadDataForCurrentPeriod(Calendar startDate, Calendar endDate, DateFilterUtil.FilterType filterType) {
        if (startDate == null || endDate == null) {
            Log.e(TAG, "Cannot load data: startDate or endDate is null.");
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Log.i(TAG, "Loading data for period: " +
                dateFormat.format(startDate.getTime()) + " to " +
                dateFormat.format(endDate.getTime()));
    }
}