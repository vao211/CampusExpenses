package com.example.vcampusexpenses.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.activity.LoginActivity;
import com.example.vcampusexpenses.activity.MainActivity;
import com.example.vcampusexpenses.activity.SettingActivity;
import com.example.vcampusexpenses.session.SessionManager;
import com.example.vcampusexpenses.utils.DisplayToast;

public class HomeFragment extends Fragment {
    ImageButton btnSetting, btnAdd;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 1. Inflate layout cho fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 2. Ánh xạ view từ layout đã inflate
        btnAdd = view.findViewById(R.id.btn_add); // Sử dụng view.findViewById
        btnSetting = view.findViewById(R.id.btnSetting);

        // 3. Thiết lập sự kiện click cho nút
        goToSettingActivity();

        // 4. Trả về view đã inflate
        return view;
    }

    private void goToSettingActivity() {
        btnSetting.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingActivity.class);
            startActivity(intent);
        });
    }
}