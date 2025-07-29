package com.example.vcampusexpenses.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.activity.SettingActivity;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    // Views
    private ImageButton btnSetting, btnAdd, btnCalendar;
    private ImageButton btnPreviousPeriod, btnNextPeriod;
    private TextView txtDateFilterDisplay;

    // Date and Filter State
    private Calendar startDate;
    private Calendar endDate;
    private SimpleDateFormat dateFormat; // For "yyyy-MM-dd"

    private enum FilterType {
        THIS_MONTH, LAST_MONTH, LAST_7_DAYS, LAST_30_DAYS, CUSTOM_RANGE
    }
    private FilterType currentFilterType = FilterType.THIS_MONTH; // Default

    // Keys for saving instance state
    private static final String START_DATE_KEY = "startDate";
    private static final String END_DATE_KEY = "endDate";
    private static final String FILTER_TYPE_KEY = "filterType";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeDateFormats();
        initializeViews(view);

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        } else {
            // Set initial default filter if no saved state
            applyFilter(FilterType.THIS_MONTH, false);
        }

        setupClickListeners();
        updateDateFilterDisplay();

        return view;
    }

    private void initializeDateFormats() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    private void initializeViews(View view) {
        btnCalendar = view.findViewById(R.id.btn_calendar);
        btnAdd = view.findViewById(R.id.btn_add);
        btnSetting = view.findViewById(R.id.btnSetting);
        btnPreviousPeriod = view.findViewById(R.id.btnPreviousPeriod);
        btnNextPeriod = view.findViewById(R.id.btnNextPeriod);
        txtDateFilterDisplay = view.findViewById(R.id.txt_date_filter_display);
    }

    private void setupClickListeners() {
        btnSetting.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingActivity.class);
            startActivity(intent);
        });

        btnCalendar.setOnClickListener(v-> showPeriodSelectionDialog());
        txtDateFilterDisplay.setOnClickListener(v -> showPeriodSelectionDialog());
        btnPreviousPeriod.setOnClickListener(v -> navigatePeriod(-1));
        btnNextPeriod.setOnClickListener(v -> navigatePeriod(1));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (startDate != null) {
            outState.putLong(START_DATE_KEY, startDate.getTimeInMillis());
        }
        if (endDate != null) {
            outState.putLong(END_DATE_KEY, endDate.getTimeInMillis());
        }
        outState.putSerializable(FILTER_TYPE_KEY, currentFilterType);
    }

    private void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(START_DATE_KEY)) {
            startDate = Calendar.getInstance();
            startDate.setTimeInMillis(savedInstanceState.getLong(START_DATE_KEY));
        }
        if (savedInstanceState.containsKey(END_DATE_KEY)) {
            endDate = Calendar.getInstance();
            endDate.setTimeInMillis(savedInstanceState.getLong(END_DATE_KEY));
        }
        if (savedInstanceState.containsKey(FILTER_TYPE_KEY)) {
            currentFilterType = (FilterType) savedInstanceState.getSerializable(FILTER_TYPE_KEY);
            if (currentFilterType == null) currentFilterType = FilterType.THIS_MONTH;
        }
    }

    private void showPeriodSelectionDialog() {
        final CharSequence[] items = {"This Month", "Last Month", "Last 7 Days", "Last 30 Days", "Custom Range..."};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Time Period");
        builder.setItems(items, (dialog, item) -> {
            FilterType selectedType = null;
            if (items[item].equals("This Month")) selectedType = FilterType.THIS_MONTH;
            else if (items[item].equals("Last Month")) selectedType = FilterType.LAST_MONTH;
            else if (items[item].equals("Last 7 Days")) selectedType = FilterType.LAST_7_DAYS;
            else if (items[item].equals("Last 30 Days")) selectedType = FilterType.LAST_30_DAYS;
            else if (items[item].equals("Custom Range...")) {
                showCustomDateRangePicker();
                return;
            }

            if (selectedType != null) {
                applyFilter(selectedType, true);
            }
        });
        builder.show();
    }

    private void applyFilter(FilterType filterType, boolean loadData) {
        currentFilterType = filterType;
        Calendar newStartDate = Calendar.getInstance();
        Calendar newEndDate = Calendar.getInstance();

        switch (filterType) {
            case THIS_MONTH:
                newStartDate.set(Calendar.DAY_OF_MONTH, 1);
                newEndDate.set(Calendar.DAY_OF_MONTH, newEndDate.getActualMaximum(Calendar.DAY_OF_MONTH));
                break;

            case LAST_MONTH:
                newStartDate.add(Calendar.MONTH, -1);
                newStartDate.set(Calendar.DAY_OF_MONTH, 1);
                newEndDate.add(Calendar.MONTH, -1);
                newEndDate.set(Calendar.DAY_OF_MONTH, newEndDate.getActualMaximum(Calendar.DAY_OF_MONTH));
                break;

            case LAST_7_DAYS:
                newStartDate.add(Calendar.DAY_OF_YEAR, -6);
                break;

            case LAST_30_DAYS:
                newStartDate.add(Calendar.DAY_OF_YEAR, -29);
                break;

            case CUSTOM_RANGE:
                if (this.startDate == null || this.endDate == null) {
                    Log.w(TAG, "Custom range applied but startDate or endDate is null. Defaulting to THIS_MONTH.");
                    applyFilter(FilterType.THIS_MONTH, loadData);
                    return;
                }
                newStartDate.setTimeInMillis(this.startDate.getTimeInMillis());
                newEndDate.setTimeInMillis(this.endDate.getTimeInMillis());
                break;
        }

        if (filterType != FilterType.CUSTOM_RANGE) {
            setCalendarToBeginningOfDay(newStartDate);
            setCalendarToEndOfDay(newEndDate);
        }

        this.startDate = newStartDate;
        this.endDate = newEndDate;

        updateDateFilterDisplay();

        if (loadData) {
            loadDataForCurrentPeriod();
        }
    }

    private void updateDateFilterDisplay() {
        if (startDate == null || endDate == null || txtDateFilterDisplay == null) {
            Log.w(TAG, "Cannot update display, dates or TextView is null.");
            if (txtDateFilterDisplay != null) txtDateFilterDisplay.setText("Select Period");
            return;
        }

        String displayText;
        if (currentFilterType == FilterType.THIS_MONTH || currentFilterType == FilterType.LAST_MONTH) {
            // For THIS_MONTH and LAST_MONTH, show only year and month (yyyy-MM)
            String monthYear = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(startDate.getTime());
            displayText = monthYear;
        } else if (isSameDay(startDate, endDate)) {
            // For single-day custom range, show only one date
            displayText = dateFormat.format(startDate.getTime());
        } else {
            // For other cases (LAST_7_DAYS, LAST_30_DAYS, or multi-day CUSTOM_RANGE)
            displayText = String.format(Locale.getDefault(), "%s - %s",
                    dateFormat.format(startDate.getTime()),
                    dateFormat.format(endDate.getTime()));
        }
        txtDateFilterDisplay.setText(displayText);
    }

    private void showCustomDateRangePicker() {
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Select Date Range");

        long initialStart = startDate != null && currentFilterType == FilterType.CUSTOM_RANGE ?
                startDate.getTimeInMillis() : MaterialDatePicker.todayInUtcMilliseconds();
        long initialEnd = endDate != null && currentFilterType == FilterType.CUSTOM_RANGE ?
                endDate.getTimeInMillis() : MaterialDatePicker.todayInUtcMilliseconds();

        if (initialStart > initialEnd) initialStart = initialEnd;

        builder.setSelection(new Pair<>(initialStart, initialEnd));

        final MaterialDatePicker<Pair<Long, Long>> datePicker = builder.build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            if (selection.first != null && selection.second != null) {
                currentFilterType = FilterType.CUSTOM_RANGE;

                this.startDate = Calendar.getInstance();
                this.startDate.setTimeInMillis(selection.first);
                setCalendarToBeginningOfDay(this.startDate);

                this.endDate = Calendar.getInstance();
                this.endDate.setTimeInMillis(selection.second);
                setCalendarToEndOfDay(this.endDate);

                updateDateFilterDisplay();
                loadDataForCurrentPeriod();
            } else {
                Log.w(TAG, "Date range picker returned null selection.");
                Toast.makeText(getContext(), "Invalid date range selected.", Toast.LENGTH_SHORT).show();
            }
        });

        datePicker.show(getParentFragmentManager(), "MATERIAL_DATE_RANGE_PICKER");
    }

    private void navigatePeriod(int direction) {
        if (startDate == null || endDate == null) {
            Log.w(TAG, "Cannot navigate period, dates are null.");
            return;
        }

        Calendar navStartDate = (Calendar) startDate.clone();
        Calendar navEndDate = (Calendar) endDate.clone();

        switch (currentFilterType) {
            case THIS_MONTH:
            case LAST_MONTH:
                navStartDate.add(Calendar.MONTH, direction);
                navStartDate.set(Calendar.DAY_OF_MONTH, 1);
                navEndDate.setTime(navStartDate.getTime());
                navEndDate.set(Calendar.DAY_OF_MONTH, navEndDate.getActualMaximum(Calendar.DAY_OF_MONTH));
                break;
            case LAST_7_DAYS:
                navStartDate.add(Calendar.DAY_OF_YEAR, 7 * direction);
                navEndDate.add(Calendar.DAY_OF_YEAR, 7 * direction);
                break;
            case LAST_30_DAYS:
                navStartDate.add(Calendar.DAY_OF_YEAR, 30 * direction);
                navEndDate.add(Calendar.DAY_OF_YEAR, 30 * direction);
                break;
            case CUSTOM_RANGE:
                long duration = endDate.getTimeInMillis() - startDate.getTimeInMillis();
                if (duration <= 0) {
                    duration = 24 * 60 * 60 * 1000L;
                }
                navStartDate.add(Calendar.MILLISECOND, (int) (duration * direction));
                navEndDate.add(Calendar.MILLISECOND, (int) (duration * direction));
                break;
        }

        this.startDate = navStartDate;
        this.endDate = navEndDate;
        setCalendarToBeginningOfDay(this.startDate);
        setCalendarToEndOfDay(this.endDate);

        updateDateFilterDisplay();
        loadDataForCurrentPeriod();
    }

    private void loadDataForCurrentPeriod() {
        if (startDate == null || endDate == null) {
            Log.e(TAG, "Cannot load data: startDate or endDate is null.");
            Toast.makeText(getContext(), "Error: Date range not set.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.i(TAG, "Loading data for period: " +
                dateFormat.format(startDate.getTime()) + " to " +
                dateFormat.format(endDate.getTime()));
        Toast.makeText(getContext(), "Filtering: " + txtDateFilterDisplay.getText(), Toast.LENGTH_SHORT).show();
    }

    private void setCalendarToBeginningOfDay(Calendar cal) {
        if (cal == null) return;
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private void setCalendarToEndOfDay(Calendar cal) {
        if (cal == null) return;
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) return false;
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }
}