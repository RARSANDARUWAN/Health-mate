package com.example.healthmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ImageView profileImgBtn;
    private Button addRecordBtn, historyBtn;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // initialize database and ensure tables/dummy data are created
        databaseHelper = new DatabaseHelper(this);
        databaseHelper.getWritableDatabase();

        // bind xml components
        profileImgBtn = findViewById(R.id.btnTopProfile);
        addRecordBtn = findViewById(R.id.btnAddRecord);
        historyBtn = findViewById(R.id.btnViewHistory);

        // handle profile click
        profileImgBtn.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // handle add record click
        addRecordBtn.setOnClickListener(view -> {
            Intent intentMove = new Intent(MainActivity.this, AddRecordActivity.class);
            startActivity(intentMove);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // handle history click
        historyBtn.setOnClickListener(view -> {
            Intent histIntent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(histIntent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // initialize bottom navigation
        NavigationHelper.setupBottomNavigation(this);
    }
}