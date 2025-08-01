package com.example.vcampusexpenses.utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentManager;

import com.example.vcampusexpenses.R;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateFilterUtil {
    private static final String TAG = "DateFilterUtil";

    public enum FilterType {
        THIS_MONTH, LAST_MONTH, LAST_7_DAYS, LAST_30_DAYS, CUSTOM_RANGE
    }

    private Calendar startDate;
    private Calendar endDate;
    private FilterType currentFilterType = FilterType.THIS_MONTH;
    private SimpleDateFormat dateFormat;
    private final Context context;
    private final FragmentManager fragmentManager;
    private TextView txtDateFilterDisplay;
    private OnDateFilterChangedListener listener;

    // Keys for saving instance state
    public static final String START_DATE_KEY = "startDate";
    public static final String END_DATE_KEY = "endDate";
    public static final String FILTER_TYPE_KEY = "filterType";
    String txtThisMonth, txtLastMonth, txtLast7Days, txtLast30Days, txtCustomRange, txtCustomRangeError;

    public interface OnDateFilterChangedListener {
        void onDateFilterChanged(Calendar startDate, Calendar endDate, FilterType filterType);
    }

    public DateFilterUtil(Context context, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        initializeDateFormats();
        txtThisMonth = context.getString(R.string.this_month);
        txtLastMonth = context.getString(R.string.last_month);
        txtLast7Days = context.getString(R.string.last_7_days);
        txtLast30Days = context.getString(R.string.last_30_days);
        txtCustomRange = context.getString(R.string.custom_range);
        txtCustomRangeError = context.getString(R.string.date_range_error);
        applyFilter(FilterType.THIS_MONTH, false);
    }

    private void initializeDateFormats() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    public void setDateFilterDisplay(TextView textView) {
        this.txtDateFilterDisplay = textView;
        updateDateFilterDisplay();
    }

    public void setOnDateFilterChangedListener(OnDateFilterChangedListener listener) {
        this.listener = listener;
    }

    public void showPeriodSelectionDialog() {
        final CharSequence[] items = {txtThisMonth, txtLastMonth, txtLast7Days, txtLast30Days, txtCustomRange};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(txtCustomRange);
        builder.setItems(items, (dialog, item) -> {
            FilterType selectedType = null;
            if (items[item].equals(txtThisMonth)) selectedType = FilterType.THIS_MONTH;
            else if (items[item].equals(txtLastMonth)) selectedType = FilterType.LAST_MONTH;
            else if (items[item].equals(txtLast7Days)) selectedType = FilterType.LAST_7_DAYS;
            else if (items[item].equals(txtLast30Days)) selectedType = FilterType.LAST_30_DAYS;
            else if (items[item].equals(txtCustomRange)) {
                showCustomDateRangePicker();
                return;
            }

            if (selectedType != null) {
                applyFilter(selectedType, true);
            }
        });
        builder.show();
    }

    public void applyFilter(FilterType filterType, boolean loadData) {
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

        if (loadData && listener != null) {
            listener.onDateFilterChanged(startDate, endDate, currentFilterType);
        }
    }

    private void updateDateFilterDisplay() {
        if (startDate == null || endDate == null || txtDateFilterDisplay == null) {
            Log.w(TAG, "Cannot update display, dates or TextView is null.");
            if (txtDateFilterDisplay != null) txtDateFilterDisplay.setText(txtCustomRange);
            return;
        }

        String displayText;
        if (currentFilterType == FilterType.THIS_MONTH || currentFilterType == FilterType.LAST_MONTH) {
            String monthYear = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(startDate.getTime());
            displayText = monthYear;
        } else if (isSameDay(startDate, endDate)) {
            displayText = dateFormat.format(startDate.getTime());
        } else {
            displayText = String.format(Locale.getDefault(), "%s - %s",
                    dateFormat.format(startDate.getTime()),
                    dateFormat.format(endDate.getTime()));
        }
        txtDateFilterDisplay.setText(displayText);
    }

    private void showCustomDateRangePicker() {
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText(txtCustomRange);

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
                if (listener != null) {
                    listener.onDateFilterChanged(startDate, endDate, currentFilterType);
                }
            } else {
                Log.w(TAG, "Date range picker returned null selection.");
                DisplayToast.Display(context, txtCustomRangeError);
            }
        });
        //MATERIAL_DATE_RANGE_PICKER: dialogFragment
        datePicker.show(fragmentManager, "MATERIAL_DATE_RANGE_PICKER");
    }

    public void navigatePeriod(int direction) {
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
        if (listener != null) {
            listener.onDateFilterChanged(startDate, endDate, currentFilterType);
        }
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

    public void restoreInstanceState(@NonNull Bundle savedInstanceState) {
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
        updateDateFilterDisplay();
    }

    public void saveInstanceState(@NonNull Bundle outState) {
        if (startDate != null) {
            outState.putLong(START_DATE_KEY, startDate.getTimeInMillis());
        }
        if (endDate != null) {
            outState.putLong(END_DATE_KEY, endDate.getTimeInMillis());
        }
        outState.putSerializable(FILTER_TYPE_KEY, currentFilterType);
    }

    public Calendar getStartDate() {
        return startDate;
    }

    public Calendar getEndDate() {
        return endDate;
    }

    public FilterType getCurrentFilterType() {
        return currentFilterType;
    }
}