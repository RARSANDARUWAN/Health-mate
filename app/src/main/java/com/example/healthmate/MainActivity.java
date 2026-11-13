package com.example.healthmate;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ImageView profileImgBtn;
    private Button addRecordBtn, historyBtn;
    private DatabaseHelper databaseHelper;

    // chart view arrays for 7-day metrics visualization
    private View[] chartBars;
    private TextView[] dateLabels;
    private TextView[] valueLabels;
    private TextView avgCaloriesLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // initialize database connection
        databaseHelper = new DatabaseHelper(this);
        databaseHelper.getWritableDatabase();

        // bind views from xml layout
        profileImgBtn = findViewById(R.id.btnTopProfile);
        addRecordBtn = findViewById(R.id.btnAddRecord);
        historyBtn = findViewById(R.id.btnViewHistory);
        avgCaloriesLabel = findViewById(R.id.tvAvgCalories);

        // setup array structures for chart bars and labels
        setupChartViews();

        // load weekly data from database to render dynamic chart
        loadWeeklyChartData();

        // handle profile button click
        profileImgBtn.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // handle add record button click
        addRecordBtn.setOnClickListener(view -> {
            Intent intentMove = new Intent(MainActivity.this, AddRecordActivity.class);
            startActivity(intentMove);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // handle history button click
        historyBtn.setOnClickListener(view -> {
            Intent histIntent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(histIntent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // initialize bottom navigation bar
        NavigationHelper.setupBottomNavigation(this);
    }

    // bind layout elements into respective arrays for easy iteration
    private void setupChartViews() {
        chartBars = new View[] {
                findViewById(R.id.bar1), findViewById(R.id.bar2),
                findViewById(R.id.bar3), findViewById(R.id.bar4),
                findViewById(R.id.bar5), findViewById(R.id.bar6),
                findViewById(R.id.bar7)
        };

        dateLabels = new TextView[] {
                findViewById(R.id.tvDay1), findViewById(R.id.tvDay2),
                findViewById(R.id.tvDay3), findViewById(R.id.tvDay4),
                findViewById(R.id.tvDay5), findViewById(R.id.tvDay6),
                findViewById(R.id.tvDay7)
        };

        valueLabels = new TextView[] {
                findViewById(R.id.tvVal1), findViewById(R.id.tvVal2),
                findViewById(R.id.tvVal3), findViewById(R.id.tvVal4),
                findViewById(R.id.tvVal5), findViewById(R.id.tvVal6),
                findViewById(R.id.tvVal7)
        };
    }

    // fetch records from db and update bar heights and labels dynamically
    private void loadWeeklyChartData() {
        Cursor cursor = databaseHelper.getWeeklyChartData();

        if (cursor != null && cursor.moveToFirst()) {
            int index = 0;
            int totalCalories = 0;
            int recordCount = 0;

            do {
                if (index >= 7) break; // limit to maximum 7 days

                String fullDate = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                int calories = cursor.getInt(cursor.getColumnIndexOrThrow("calories"));

                totalCalories += calories;
                recordCount++;

                // format date string from YYYY-MM-DD to MMM DD
                String formattedDate = formatDateString(fullDate);

                // set text values to UI labels
                if (dateLabels[index] != null) {
                    dateLabels[index].setText(formattedDate);
                }
                if (valueLabels[index] != null) {
                    valueLabels[index].setText(String.valueOf(calories));
                }

                // calculate proportional bar height based on calorie count (max 500 kcal = 120dp height)
                int calculatedHeightPx = (calories * 120) / 500;
                if (calculatedHeightPx < 20) {
                    calculatedHeightPx = 20; // enforce minimum visible height
                }

                if (chartBars[index] != null) {
                    android.view.ViewGroup.LayoutParams params = chartBars[index].getLayoutParams();
                    params.height = dpToPx(calculatedHeightPx);
                    chartBars[index].setLayoutParams(params);
                }

                index++;
            } while (cursor.moveToNext());

            cursor.close();

            // compute and display average calorie intake
            if (recordCount > 0) {
                int averageCalories = totalCalories / recordCount;
                avgCaloriesLabel.setText("Avg: " + averageCalories + " kcal");
            }
        }
    }

    // convert date format from YYYY-MM-DD into a readable short label (e.g., Jun 01)
    private String formatDateString(String dateStr) {
        try {
            String[] parts = dateStr.split("-");
            if (parts.length == 3) {
                String monthNum = parts[1];
                String dayNum = parts[2];
                String monthName = "";

                switch (monthNum) {
                    case "01": monthName = "Jan"; break;
                    case "02": monthName = "Feb"; break;
                    case "03": monthName = "Mar"; break;
                    case "04": monthName = "Apr"; break;
                    case "05": monthName = "May"; break;
                    case "06": monthName = "Jun"; break;
                    case "07": monthName = "Jul"; break;
                    case "08": monthName = "Aug"; break;
                    case "09": monthName = "Sep"; break;
                    case "10": monthName = "Oct"; break;
                    case "11": monthName = "Nov"; break;
                    case "12": monthName = "Dec"; break;
                    default: monthName = monthNum;
                }
                return monthName + " " + dayNum;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateStr;
    }

    // helper utility to convert DP units into screen pixels
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}