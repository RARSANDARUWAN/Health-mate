package com.example.healthmate;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ImageView profileImgBtn;
    private Button addRecordBtn, historyBtn;
    private DatabaseHelper databaseHelper;

    // chart view references for weekly metrics display
    private View[] chartBars;
    private TextView[] dateLabels;
    private TextView[] valueLabels;
    private TextView avgCaloriesLabel;

    // today dashboard summary card views
    private TextView tvWaterVal, tvStepsVal, tvCaloriesVal, tvDateBadge;

    private ImageView logoutImgBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // initialize sqlite database helper instance
        databaseHelper = new DatabaseHelper(this);
        databaseHelper.getWritableDatabase();

        // bind top bar, action buttons, and summary components from xml
        profileImgBtn = findViewById(R.id.btnTopProfile);
        addRecordBtn = findViewById(R.id.btnAddRecord);
        historyBtn = findViewById(R.id.btnViewHistory);
        avgCaloriesLabel = findViewById(R.id.tvAvgCalories);

        tvWaterVal = findViewById(R.id.tvWaterVal);
        tvStepsVal = findViewById(R.id.tvStepsVal);
        tvCaloriesVal = findViewById(R.id.tvCaloriesVal);
        tvDateBadge = findViewById(R.id.tvDateBadge);

        // setup array structures for chart UI elements
        setupChartViews();

        // populate dashboard widgets on initial launch
        loadWeeklyChartData();
        loadTodaySummary();

        // handle navigation to user profile activity
        profileImgBtn.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // handle navigation to record creation screen
        addRecordBtn.setOnClickListener(view -> {
            Intent intentMove = new Intent(MainActivity.this, AddRecordActivity.class);
            startActivity(intentMove);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // handle navigation to history logs activity
        historyBtn.setOnClickListener(view -> {
            Intent histIntent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(histIntent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // initialize bottom bar navigation controller
        NavigationHelper.setupBottomNavigation(this);

        //linking logout button
        logoutImgBtn = findViewById(R.id.btnLogout);

        // Logout button click event with Yes/No confirmation
        logoutImgBtn.setOnClickListener(view -> {
            new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        android.widget.Toast.makeText(MainActivity.this, "Logged out successfully", android.widget.Toast.LENGTH_SHORT).show();

                        Intent logoutIntent = new Intent(MainActivity.this, LoginActivity.class);
                        // close whole screens that pre written
                        logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(logoutIntent);
                        finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // Nothing happens when press no
                        dialog.dismiss();
                    })
                    .show();
        });
    //Share summery btn
        Button btnShareSummary = findViewById(R.id.btnShareSummary);

        btnShareSummary.setOnClickListener(v -> shareHealthSummary());

    }

    @Override
    protected void onResume() {
        super.onResume();
        // refresh data widgets whenever activity regains focus
        loadWeeklyChartData();
        loadTodaySummary();
    }

    // map chart UI components into searchable arrays for dynamic iteration
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

    // fetch and display today's recorded health metrics on dashboard card
    private void loadTodaySummary() {
        String todayDbDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String todayBadgeText = new SimpleDateFormat("MMM dd", Locale.getDefault()).format(new Date());

        if (tvDateBadge != null) {
            tvDateBadge.setText("• " + todayBadgeText);
        }

        Cursor recordCursor = databaseHelper.getRecordByDate(todayDbDate);
        if (recordCursor != null && recordCursor.moveToFirst()) {
            int waterAmount = recordCursor.getInt(recordCursor.getColumnIndexOrThrow("water"));
            int stepCount = recordCursor.getInt(recordCursor.getColumnIndexOrThrow("steps"));
            int calorieBurn = recordCursor.getInt(recordCursor.getColumnIndexOrThrow("calories"));

            if (tvWaterVal != null) tvWaterVal.setText(waterAmount + " ml");
            if (tvStepsVal != null) tvStepsVal.setText(stepCount + " steps");
            if (tvCaloriesVal != null) tvCaloriesVal.setText(calorieBurn + " kcal");
            recordCursor.close();
        } else {
            // fallback default zeros if no log exists for today
            if (tvWaterVal != null) tvWaterVal.setText("0 ml");
            if (tvStepsVal != null) tvStepsVal.setText("0 steps");
            if (tvCaloriesVal != null) tvCaloriesVal.setText("0 kcal");
        }
    }

    // query database records and render the 7-day visual bar chart
    private void loadWeeklyChartData() {
        // reset all chart bars and text labels to baseline default empty state
        for (int i = 0; i < 7; i++) {
            if (dateLabels[i] != null) dateLabels[i].setText("--");
            if (valueLabels[i] != null) valueLabels[i].setText("0");
            if (chartBars[i] != null) {
                android.view.ViewGroup.LayoutParams layoutParams = chartBars[i].getLayoutParams();
                layoutParams.height = dpToPx(20);
                chartBars[i].setLayoutParams(layoutParams);
            }
        }

        Cursor cursor = databaseHelper.getWeeklyChartData();

        if (cursor != null && cursor.moveToFirst()) {
            int cumulativeCalories = 0;
            int totalRecordsCount = 0;

            int rowTotalCount = cursor.getCount();
            String[] dateArray = new String[rowTotalCount];
            int[] calorieArray = new int[rowTotalCount];

            int indexTracker = 0;
            do {
                dateArray[indexTracker] = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                calorieArray[indexTracker] = cursor.getInt(cursor.getColumnIndexOrThrow("calories"));
                cumulativeCalories += calorieArray[indexTracker];
                totalRecordsCount++;
                indexTracker++;
            } while (cursor.moveToNext() && indexTracker < rowTotalCount);
            cursor.close();

            // map latest records to fill up to 7 UI chart slots from right to left
            int startOffsetIndex = rowTotalCount < 7 ? 0 : rowTotalCount - 7;
            int currentChartIndex = 0;

            for (int j = startOffsetIndex; j < rowTotalCount && currentChartIndex < 7; j++) {
                String formattedDateLabel = formatDateString(dateArray[j]);
                int calorieVal = calorieArray[j];

                if (dateLabels[currentChartIndex] != null) {
                    dateLabels[currentChartIndex].setText(formattedDateLabel);
                }
                if (valueLabels[currentChartIndex] != null) {
                    valueLabels[currentChartIndex].setText(String.valueOf(calorieVal));
                }

                // calculate dynamic bar height relative to calorie value
                int computedBarHeightPx = (calorieVal * 75) / 500;
                if (computedBarHeightPx < 15) {
                    computedBarHeightPx = 15; // enforce minimum display height limit
                }

                if (chartBars[currentChartIndex] != null) {
                    android.view.ViewGroup.LayoutParams barParams = chartBars[currentChartIndex].getLayoutParams();
                    barParams.height = dpToPx(computedBarHeightPx);
                    chartBars[currentChartIndex].setLayoutParams(barParams);
                }

                currentChartIndex++;
            }

            // compute and output overall average calorie intake
            if (totalRecordsCount > 0) {
                int meanCalories = cumulativeCalories / totalRecordsCount;
                avgCaloriesLabel.setText("Avg: " + meanCalories + " kcal");
            }
        }
    }

    // helper utility to parse raw database date string into short readable month format
    private String formatDateString(String rawDateString) {
        try {
            String[] segments = rawDateString.split("-");
            if (segments.length == 3) {
                String monthCode = segments[1];
                String dayCode = segments[2];
                String monthAbbreviation = "";

                switch (monthCode) {
                    case "01": monthAbbreviation = "Jan"; break;
                    case "02": monthAbbreviation = "Feb"; break;
                    case "03": monthAbbreviation = "Mar"; break;
                    case "04": monthAbbreviation = "Apr"; break;
                    case "05": monthAbbreviation = "May"; break;
                    case "06": monthAbbreviation = "Jun"; break;
                    case "07": monthAbbreviation = "Jul"; break;
                    case "08": monthAbbreviation = "Aug"; break;
                    case "09": monthAbbreviation = "Sep"; break;
                    case "10": monthAbbreviation = "Oct"; break;
                    case "11": monthAbbreviation = "Nov"; break;
                    case "12": monthAbbreviation = "Dec"; break;
                    default: monthAbbreviation = monthCode;
                }
                return monthAbbreviation + " " + dayCode;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return rawDateString;
    }

    // utility method to convert density-independent pixels to raw screen pixels
    private int dpToPx(int dpValue) {
        float screenDensity = getResources().getDisplayMetrics().density;
        return Math.round((float) dpValue * screenDensity);
    }
    //share summery button method
    private void shareHealthSummary() {
        // Get today's summary values from TextViews
        TextView tvDate = findViewById(R.id.tvDateBadge);
        TextView tvWater = findViewById(R.id.tvWaterVal);
        TextView tvSteps = findViewById(R.id.tvStepsVal);
        TextView tvCalories = findViewById(R.id.tvCaloriesVal);
        TextView tvAvgCalories = findViewById(R.id.tvAvgCalories);

        String date = tvDate.getText().toString().replace("•", "").trim();
        String water = tvWater.getText().toString().trim();
        String steps = tvSteps.getText().toString().trim();
        String calories = tvCalories.getText().toString().trim();
        String avgCal = tvAvgCalories.getText().toString().trim();

        // Get 7-day bar chart details
        int[] dayIds = {R.id.tvDay1, R.id.tvDay2, R.id.tvDay3, R.id.tvDay4, R.id.tvDay5, R.id.tvDay6, R.id.tvDay7};
        int[] valIds = {R.id.tvVal1, R.id.tvVal2, R.id.tvVal3, R.id.tvVal4, R.id.tvVal5, R.id.tvVal6, R.id.tvVal7};

        StringBuilder chartDetails = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            TextView tvDay = findViewById(dayIds[i]);
            TextView tvVal = findViewById(valIds[i]);
            String dayName = tvDay.getText().toString().trim();
            String calVal = tvVal.getText().toString().trim();

            if (!dayName.equals("--")) {
                chartDetails.append("• ").append(dayName).append(": ").append(calVal).append(" kcal\n");
            }
        }

        // Build formatted share message
        String shareMessage = "🌿 *Health Tracker Summary* (" + date + ")\n\n"
                + "📊 *Today's Metrics:*\n"
                + "💧 Water Intake: " + water + "\n"
                + "👟 Steps Walked: " + steps + "\n"
                + "🔥 Calories Burned: " + calories + "\n\n"
                + "📈 *Weekly Calorie History* (" + avgCal + "):\n"
                + (chartDetails.length() > 0 ? chartDetails.toString() : "No weekly records yet.\n")
                + "\nTracked via Health Tracker App 💚";

        // Open Android share sheet (WhatsApp, Messenger, etc.)
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Health Summary - " + date);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(shareIntent, "Share Health Summary via"));
    }
}