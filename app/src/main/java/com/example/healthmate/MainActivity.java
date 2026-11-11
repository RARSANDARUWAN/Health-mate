package com.example.healthmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ImageView profileImgBtn;
    private Button addRecordBtn, historyBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // find views by id
        profileImgBtn = findViewById(R.id.btnTopProfile);
        addRecordBtn = findViewById(R.id.btnAddRecord);
        historyBtn = findViewById(R.id.btnViewHistory);

        // profile button click listener
        profileImgBtn.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // go to add record screen
        addRecordBtn.setOnClickListener(view -> {
            Intent intentMove = new Intent(MainActivity.this, AddRecordActivity.class);
            startActivity(intentMove);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // go to history screen
        historyBtn.setOnClickListener(view -> {
            Intent histIntent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(histIntent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // setup bottom navigation
        NavigationHelper.setupBottomNavigation(this);
    }
}