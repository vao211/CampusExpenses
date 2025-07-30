package com.example.vcampusexpenses.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.adapters.CategoryRadioAdapter;
import com.example.vcampusexpenses.model.Category;
import com.example.vcampusexpenses.services.CategoryService;
import com.example.vcampusexpenses.session.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {
    SessionManager sessionManager;
    ImageButton btnClose;
    LinearLayout llIncome, llOutcome, llTransfer, llDatePicker, llSelectCategory, llSelectAccount, llDescription;
    TextView txtDate, txtSelectedCategory, txtSelectedAccount, txtSelectedBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addtransaction);

        btnClose = findViewById(R.id.btn_close);
        llIncome = findViewById(R.id.ll_income);
        llOutcome = findViewById(R.id.ll_outcome);
        llTransfer = findViewById(R.id.ll_transfer);
        llDatePicker = findViewById(R.id.ll_datePicker);
        llSelectCategory = findViewById(R.id.ll_select_category);
        llSelectAccount = findViewById(R.id.ll_select_account);
        llDescription = findViewById(R.id.ll_description);
        txtDate = findViewById(R.id.txt_date);
        txtSelectedCategory = findViewById(R.id.txt_selected_category);
        txtSelectedAccount = findViewById(R.id.txt_selected_account);
        txtSelectedBudget = findViewById(R.id.txt_selected_budget);

        sessionManager = new SessionManager(this);

        loadDate();
        chooseDate();
        setCategorySelection();
        showCategoryDialog();
        close();
    }
    private void showCategoryDialog() {
        llSelectCategory.setOnClickListener(v -> {
            CategoryService categoryService = new CategoryService(this, sessionManager.getUserId());
            List<Category> categories = categoryService.getListCategories();

            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_category, null);
            RecyclerView recyclerView = dialogView.findViewById(R.id.rv_category_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            AlertDialog alertDialog = new AlertDialog.Builder(this).setView(dialogView).create();

            CategoryRadioAdapter adapter = new CategoryRadioAdapter(categories, selectedCategory -> {
                txtSelectedCategory.setText(selectedCategory.getName());
                alertDialog.dismiss();
            });
            recyclerView.setAdapter(adapter);
            alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
            alertDialog.show();
        });
    }



    private void setCategorySelection() {
        setCategorySelection(null);
        llIncome.setOnClickListener(v -> setCategorySelection("income"));
        llOutcome.setOnClickListener(v -> setCategorySelection("outcome"));
        llTransfer.setOnClickListener(v -> setCategorySelection("transfer"));
    }
    private void setCategorySelection(String selected) {
        llIncome.setBackgroundResource(R.drawable.border);
        llOutcome.setBackgroundResource(R.drawable.border);
        llTransfer.setBackgroundResource(R.drawable.border);

        if ("income".equals(selected)) {
            llIncome.setBackgroundResource(R.drawable.bg_selected_income);
        } else if ("outcome".equals(selected)) {
            llOutcome.setBackgroundResource(R.drawable.bg_selected_outcome);
        } else if ("transfer".equals(selected)) {
            llTransfer.setBackgroundResource(R.drawable.bg_selected_transfer);
        }
    }
    private void chooseDate(){
        llDatePicker.setOnClickListener(v -> {
            Calendar calendar1 = Calendar.getInstance();
            int year = calendar1.get(Calendar.YEAR);
            int month = calendar1.get(Calendar.MONTH);
            int day = calendar1.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    AddTransactionActivity.this,
                    (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                        String selectedDate = String.format(Locale.getDefault(), "%04d/%02d/%02d",
                                selectedYear, selectedMonth + 1, selectedDayOfMonth);
                        txtDate.setText(selectedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        });
    }

    private void close (){
        btnClose.setOnClickListener(v -> {
            finish();
        });
    }
    private void loadDate(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        String currentDate = sdf.format(calendar.getTime());

        txtDate.setText(currentDate);
    }
}
