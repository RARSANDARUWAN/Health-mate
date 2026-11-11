package com.example.healthmate;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class AddRecordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addrec);
        NavigationHelper.setupBottomNavigation(this);

        // Bottom Navigation Setup
        NavigationHelper.setupBottomNavigation(this);

        // Top Bar Back & Profile Actions Setup
        NavigationHelper.setupTopBarActions(this);
    }

}