package com.example.vcampusexpenses.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;

import com.example.vcampusexpenses.R;

public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 1. Inflate layout cho fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 2. Ánh xạ view từ layout đã inflate
        ImageButton btnAdd = view.findViewById(R.id.btn_add); // Sử dụng view.findViewById thay vì getActivity()

        // 3. Thiết lập sự kiện click cho nút thêm
//        btnAdd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Tạo intent để mở AddTransactionAndBudgetActivity
//                Intent intent = new Intent(getActivity(), AddTransactionAndBudgetActivity.class);
//                startActivity(intent);
//            }
//        }
//        );

        // 4. Trả về view đã inflate
        return view;
    }
}