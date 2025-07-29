package com.example.vcampusexpenses.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vcampusexpenses.R;

public class AddTransactionActivity extends AppCompatActivity {
    ImageButton btnClose;
    LinearLayout btnIncome, btnOutcome, btnTransfer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addtransaction);

        btnClose = findViewById(R.id.btn_close);
        btnIncome = findViewById(R.id.btn_income);
        btnOutcome = findViewById(R.id.btn_outcome);
        btnTransfer = findViewById(R.id.btn_transfer);

        close();
    }

    private void close (){
        btnClose.setOnClickListener(v -> {
            finish();
        });
    }

}
