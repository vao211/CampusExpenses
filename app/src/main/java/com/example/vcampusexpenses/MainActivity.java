package com.example.vcampusexpenses;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.vcampusexpenses.authentication.LogInLogOut;
import com.example.vcampusexpenses.fragments.AnalysisFragment;
import com.example.vcampusexpenses.fragments.CategoriesFragment;
import com.example.vcampusexpenses.fragments.HomeFragment;
import com.example.vcampusexpenses.fragments.TransactionFragment;
import com.example.vcampusexpenses.session.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    //test hiển thị user
    private Button btnLogout;
    private TextView txtUserEmail2;
    private SessionManager sessionManager;
    private BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        txtUserEmail2 = findViewById(R.id.txtUserEmail2);
        btnLogout = findViewById(R.id.btnLogout);
        sessionManager = new SessionManager(this);

        LoadUserEmail();
        Logout();

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        //Fragment mặc định
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_analysis) {
                selectedFragment = new AnalysisFragment();
            } else if (itemId == R.id.nav_transaction) {
                selectedFragment = new TransactionFragment();
            } else if (itemId == R.id.nav_categories) {
                selectedFragment = new CategoriesFragment();
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

    }

    @SuppressLint("SetTextI18n")
    private void LoadUserEmail(){
        String email = sessionManager.getSavedEmail();
        if (email != null) {
            txtUserEmail2.setText("UserEmail: "+ email);
        }
    }
    private void Logout(){
        btnLogout.setOnClickListener(v -> {
            LogInLogOut.LogOut(this);
            finish();
        });
    }
}
