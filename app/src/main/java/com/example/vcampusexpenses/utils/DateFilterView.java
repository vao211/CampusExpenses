package com.example.vcampusexpenses.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;

import com.example.vcampusexpenses.R;

public class DateFilterView extends ConstraintLayout {
    private DateFilterUtil dateFilterUtil;
    private ImageButton btnCalendar, btnPreviousPeriod, btnNextPeriod;
    private TextView txtDateFilterDisplay;

    public DateFilterView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public DateFilterView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DateFilterView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.date_filter_view, this, true);

        btnCalendar = findViewById(R.id.btn_calendar);
        btnPreviousPeriod = findViewById(R.id.btnPreviousPeriod);
        btnNextPeriod = findViewById(R.id.btnNextPeriod);
        txtDateFilterDisplay = findViewById(R.id.txt_date_filter_display);
    }

    public void setDateFilterUtil(DateFilterUtil dateFilterUtil, FragmentManager fragmentManager) {
        this.dateFilterUtil = dateFilterUtil;
        this.dateFilterUtil.setDateFilterDisplay(txtDateFilterDisplay);

        btnCalendar.setOnClickListener(v -> dateFilterUtil.showPeriodSelectionDialog());
        txtDateFilterDisplay.setOnClickListener(v -> dateFilterUtil.showPeriodSelectionDialog());
        btnPreviousPeriod.setOnClickListener(v -> dateFilterUtil.navigatePeriod(-1));
        btnNextPeriod.setOnClickListener(v -> dateFilterUtil.navigatePeriod(1));
    }
}