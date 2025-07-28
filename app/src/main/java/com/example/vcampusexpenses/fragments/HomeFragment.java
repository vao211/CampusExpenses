package com.example.vcampusexpenses.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // For logging
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
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects; // For Objects.requireNonNull

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    // Views
    private ImageButton btnSetting, btnAdd;
    private ImageButton btnPreviousPeriod, btnNextPeriod;
    private TextView tvDateFilterDisplay;

    // Date and Filter State
    private Calendar startDate;
    private Calendar endDate;
    private SimpleDateFormat monthYearFormat; // For "MMMM yyyy"
    private SimpleDateFormat dayMonthFormat;  // For "dd MMM" (short range display)
    private SimpleDateFormat fullDateFormat;  // For "dd MMM yyyy" (custom range display)


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
            applyFilter(FilterType.THIS_MONTH, false); // Don't trigger data load initially, let onResume or similar handle
        }

        setupClickListeners();
        updateDateFilterDisplay(); // Ensure display is correct on create

        return view;
    }

    private void initializeDateFormats() {
        monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        dayMonthFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
        fullDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    }

    private void initializeViews(View view) {
        btnAdd = view.findViewById(R.id.btn_add);
        btnSetting = view.findViewById(R.id.btnSetting);
        btnPreviousPeriod = view.findViewById(R.id.btnPreviousPeriod);
        btnNextPeriod = view.findViewById(R.id.btnNextPeriod);
        tvDateFilterDisplay = view.findViewById(R.id.tv_date_filter_display);
    }

    private void setupClickListeners() {
        btnSetting.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingActivity.class);
            startActivity(intent);
        });

        tvDateFilterDisplay.setOnClickListener(v -> showPeriodSelectionDialog());
        btnPreviousPeriod.setOnClickListener(v -> navigatePeriod(-1));
        btnNextPeriod.setOnClickListener(v -> navigatePeriod(1));

        // btnAdd listener (if any)
        // btnAdd.setOnClickListener(v -> { /* ... */ });
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
            if (currentFilterType == null) currentFilterType = FilterType.THIS_MONTH; // Safety default
        }
        // No need to call applyFilter here, just update display. Data load should be triggered by lifecycle (e.g. onResume)
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
                return; // Picker handles its own applyFilter logic
            }

            if (selectedType != null) {
                applyFilter(selectedType, true);
            }
        });
        builder.show();
    }

    /**
     * Applies the selected filter, calculates start and end dates,
     * updates the display, and optionally triggers data loading.
     * @param filterType The type of filter to apply.
     * @param loadData   True if data should be reloaded, false otherwise.
     */
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
                // endDate is today (or rather, its calculation reference point)
                newStartDate.add(Calendar.DAY_OF_YEAR, -6); // Today - 6 days = 7 days total
                break;

            case LAST_30_DAYS:
                newStartDate.add(Calendar.DAY_OF_YEAR, -29); // Today - 29 days = 30 days total
                break;

            case CUSTOM_RANGE:
                // This case is typically set by the date range picker's callback.
                // If applyFilter is called directly with CUSTOM_RANGE, it means
                // we are restoring state or need to ensure current startDate/endDate are used.
                if (this.startDate == null || this.endDate == null) {
                    // Fallback if custom range is set but dates are missing (should not happen in normal flow)
                    Log.w(TAG, "Custom range applied but startDate or endDate is null. Defaulting to THIS_MONTH.");
                    applyFilter(FilterType.THIS_MONTH, loadData); // Revert to a safe default
                    return;
                }
                // Use existing custom dates
                newStartDate.setTimeInMillis(this.startDate.getTimeInMillis());
                newEndDate.setTimeInMillis(this.endDate.getTimeInMillis());
                break;
        }

        // Ensure time components are set correctly for all non-custom, predefined ranges
        if (filterType != FilterType.CUSTOM_RANGE) {
            setCalendarToBeginningOfDay(newStartDate);
            setCalendarToEndOfDay(newEndDate);
        }
        // For custom range, begin/end of day is handled in the picker's callback

        this.startDate = newStartDate;
        this.endDate = newEndDate;

        updateDateFilterDisplay();

        if (loadData) {
            loadDataForCurrentPeriod();
        }
    }

    private void updateDateFilterDisplay() {
        if (startDate == null || endDate == null || tvDateFilterDisplay == null) {
            Log.w(TAG, "Cannot update display, dates or TextView is null.");
            if (tvDateFilterDisplay != null) tvDateFilterDisplay.setText("Select Period");
            return;
        }

        String displayText;
        switch (currentFilterType) {
            case THIS_MONTH:
            case LAST_MONTH:
                displayText = monthYearFormat.format(startDate.getTime());
                break;
            case LAST_7_DAYS:
            case LAST_30_DAYS:
                displayText = String.format(Locale.getDefault(), "%s - %s",
                        dayMonthFormat.format(startDate.getTime()),
                        dayMonthFormat.format(endDate.getTime()));
                break;
            case CUSTOM_RANGE:
                if (isSameDay(startDate, endDate)) {
                    displayText = fullDateFormat.format(startDate.getTime());
                } else {
                    displayText = String.format(Locale.getDefault(), "%s - %s",
                            fullDateFormat.format(startDate.getTime()),
                            fullDateFormat.format(endDate.getTime()));
                }
                break;
            default:
                displayText = "Select Period";
        }
        tvDateFilterDisplay.setText(displayText);
    }

    private void showCustomDateRangePicker() {
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Select Date Range");

        // Attempt to pre-fill with current custom range, or a sensible default
        long initialStart = startDate != null && currentFilterType == FilterType.CUSTOM_RANGE ?
                startDate.getTimeInMillis() : MaterialDatePicker.todayInUtcMilliseconds();
        long initialEnd = endDate != null && currentFilterType == FilterType.CUSTOM_RANGE ?
                endDate.getTimeInMillis() : MaterialDatePicker.todayInUtcMilliseconds();

        // Ensure start is not after end for initial selection
        if (initialStart > initialEnd) initialStart = initialEnd;

        builder.setSelection(new Pair<>(initialStart, initialEnd));

        // Optional: Set constraints (e.g., prevent future dates)
        // CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        // constraintsBuilder.setValidator(DateValidatorPointBackward.now());
        // builder.setCalendarConstraints(constraintsBuilder.build());

        final MaterialDatePicker<Pair<Long, Long>> datePicker = builder.build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            if (selection.first != null && selection.second != null) {
                currentFilterType = FilterType.CUSTOM_RANGE;

                // Picker gives UTC ms. Set to our Calendar instances.
                this.startDate = Calendar.getInstance();
                this.startDate.setTimeInMillis(selection.first);
                setCalendarToBeginningOfDay(this.startDate); // Ensure it's start of the selected day

                this.endDate = Calendar.getInstance();
                this.endDate.setTimeInMillis(selection.second);
                setCalendarToEndOfDay(this.endDate); // Ensure it's end of the selected day

                updateDateFilterDisplay();
                loadDataForCurrentPeriod();
            } else {
                Log.w(TAG, "Date range picker returned null selection.");
                Toast.makeText(getContext(), "Invalid date range selected.", Toast.LENGTH_SHORT).show();
            }
        });

        datePicker.show(getParentFragmentManager(), "MATERIAL_DATE_RANGE_PICKER");
    }

    private void navigatePeriod(int direction) { // direction: -1 for previous, 1 for next
        if (startDate == null || endDate == null) {
            Log.w(TAG, "Cannot navigate period, dates are null.");
            return; // Or apply a default filter
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
                if (duration <= 0) { // If single day or invalid range, shift by 1 day
                    duration = 24 * 60 * 60 * 1000L;
                }
                navStartDate.add(Calendar.MILLISECOND, (int) (duration * direction));
                navEndDate.add(Calendar.MILLISECOND, (int) (duration * direction));
                break;
            default:
                return; // Should not happen
        }

        // Apply calculated navigated dates
        this.startDate = navStartDate;
        this.endDate = navEndDate;
        // Ensure time parts are correct after navigation, especially for predefined periods
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

        // --- YOUR DATA LOADING LOGIC GOES HERE ---
        Log.i(TAG, "Loading data for period: " +
                fullDateFormat.format(startDate.getTime()) + " to " +
                fullDateFormat.format(endDate.getTime()));
        Toast.makeText(getContext(), "Filtering: " + tvDateFilterDisplay.getText(), Toast.LENGTH_SHORT).show();

        // Example:
        // viewModel.loadExpenses(startDate.getTimeInMillis(), endDate.getTimeInMillis());
        // Or directly call a method in your fragment/activity to fetch and update UI
    }

    // --- Utility Methods ---
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
