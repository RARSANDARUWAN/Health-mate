package com.example.healthmate;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;

public class NavigationHelper {

    public static void setupBottomNavigation(Activity activity) {
        LinearLayout homeTab = activity.findViewById(R.id.navHome);
        LinearLayout historyTab = activity.findViewById(R.id.navHistory);
        LinearLayout addRecordTab = activity.findViewById(R.id.navAddRecord);
        LinearLayout profileTab = activity.findViewById(R.id.navProfile);

        // handle home navigation
        if (homeTab != null) {
            homeTab.setOnClickListener(v -> {
                if (!(activity instanceof MainActivity)) {
                    activity.startActivity(new Intent(activity, MainActivity.class));
                    activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });
        }

        // handle history navigation
        if (historyTab != null) {
            historyTab.setOnClickListener(v -> {
                if (!(activity instanceof HistoryActivity)) {
                    activity.startActivity(new Intent(activity, HistoryActivity.class));
                    activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });
        }

        // handle add record navigation
        if (addRecordTab != null) {
            addRecordTab.setOnClickListener(v -> {
                if (!(activity instanceof AddRecordActivity)) {
                    activity.startActivity(new Intent(activity, AddRecordActivity.class));
                    activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });
        }

        // handle profile navigation
        if (profileTab != null) {
            profileTab.setOnClickListener(v -> {
                if (!(activity instanceof ProfileActivity)) {
                    activity.startActivity(new Intent(activity, ProfileActivity.class));
                    activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });
        }
    }

    // common top bar actions setup
    public static void setupTopBarActions(Activity activity) {
        View backBtn = activity.findViewById(R.id.btnBack);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> activity.onBackPressed());
        }

        View topProfileIcon = activity.findViewById(R.id.btnTopProfile);
        if (topProfileIcon != null) {
            topProfileIcon.setOnClickListener(v -> {
                if (!(activity instanceof ProfileActivity)) {
                    activity.startActivity(new Intent(activity, ProfileActivity.class));
                    activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });
        }
    }
}