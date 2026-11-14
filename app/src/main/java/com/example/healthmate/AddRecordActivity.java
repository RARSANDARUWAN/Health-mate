package com.example.healthmate;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddRecordActivity extends AppCompatActivity {

    private EditText etWater, etSteps, etCalories;
    private Button btnSaveRecord;
    private TextView tvSelectedDate;
    private LinearLayout layoutDate, recentLogsContainer;
    private ImageView btnBack;
    private DatabaseHelper databaseHelper;

    private String selectedDateFormatted = "";
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addrec);

        // Initialize database helper and UI components
        databaseHelper = new DatabaseHelper(this);
        etWater = findViewById(R.id.etWater);
        etSteps = findViewById(R.id.etSteps);
        etCalories = findViewById(R.id.etCalories);
        btnSaveRecord = findViewById(R.id.btnSaveRecord);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        layoutDate = findViewById(R.id.layoutDate);
        recentLogsContainer = findViewById(R.id.recentLogsContainer);
        btnBack = findViewById(R.id.btnBack);

        calendar = Calendar.getInstance();
        setDefaultDateToday();

        // Open calendar dialog on date card click
        layoutDate.setOnClickListener(v -> showDatePickerDialog());

        // Validate and save record on button click
        btnSaveRecord.setOnClickListener(v -> validateAndSaveRecord());

        // Handle back button click
        btnBack.setOnClickListener(v -> finish());

        // Load recent logs list on startup
        loadRecentLogs();

        NavigationHelper.setupBottomNavigation(this);
        NavigationHelper.setupTopBarActions(this);
    }

    // Set today's date by default
    private void setDefaultDateToday() {
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDateFormatted = dbFormat.format(calendar.getTime());

        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        tvSelectedDate.setText("Today, " + displayFormat.format(calendar.getTime()));
    }

    // Show date picker dialog to select custom date
    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    selectedDateFormatted = dbFormat.format(calendar.getTime());

                    SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    tvSelectedDate.setText(displayFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    // Validate inputs and insert into database
    private void validateAndSaveRecord() {
        String waterStr = etWater.getText().toString().trim();
        String stepsStr = etSteps.getText().toString().trim();
        String caloriesStr = etCalories.getText().toString().trim();

        // Check for empty fields
        if (waterStr.isEmpty() || stepsStr.isEmpty() || caloriesStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        int water = Integer.parseInt(waterStr);
        int steps = Integer.parseInt(stepsStr);
        int calories = Integer.parseInt(caloriesStr);

        // Prevent zero or negative values
        if (water <= 0 || steps <= 0 || calories <= 0) {
            Toast.makeText(this, "Values cannot be zero or negative!", Toast.LENGTH_SHORT).show();
            return;
        }
        // Prevent selecting future dates
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            java.util.Date selectedDate = sdf.parse(selectedDateFormatted);
            java.util.Date currentDate = sdf.parse(sdf.format(Calendar.getInstance().getTime()));

            if (selectedDate != null && selectedDate.after(currentDate)) {
                Toast.makeText(this, "Cannot add records for future dates!", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Check if record for selected date already exists
        if (databaseHelper.checkRecordExistsByDate(selectedDateFormatted)) {
            Toast.makeText(this, "A record for this date already exists!", Toast.LENGTH_LONG).show();
            return;
        }

        // Insert record into database (default user id = 1)
        boolean isInserted = databaseHelper.insertHealthRecord(1, selectedDateFormatted, steps, calories, water);

        if (isInserted) {
            Toast.makeText(this, "Successfully data inserted!", Toast.LENGTH_SHORT).show();
            etWater.setText("");
            etSteps.setText("");
            etCalories.setText("");
            loadRecentLogs(); // Refresh recent logs dynamically
        } else {
            Toast.makeText(this, "Failed to insert record. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    // Load recent 5 records dynamically and populate container
    private void loadRecentLogs() {
        recentLogsContainer.removeAllViews(); // Clear old views before reloading
        Cursor cursor = databaseHelper.getRecentHealthRecords();

        if (cursor != null && cursor.moveToFirst()) {
            LayoutInflater inflater = LayoutInflater.from(this);
            do {
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                int steps = cursor.getInt(cursor.getColumnIndexOrThrow("steps"));
                int calories = cursor.getInt(cursor.getColumnIndexOrThrow("calories"));
                int water = cursor.getInt(cursor.getColumnIndexOrThrow("water"));

                // Inflate a card layout for each log item
                View cardView = inflater.inflate(R.layout.item_recent_log, recentLogsContainer, false);

                TextView tvDate = cardView.findViewById(R.id.tvLogDate);
                TextView tvWater = cardView.findViewById(R.id.tvLogWater);
                TextView tvSteps = cardView.findViewById(R.id.tvLogSteps);
                TextView tvCalories = cardView.findViewById(R.id.tvLogCalories);

                tvDate.setText(date);
                tvWater.setText(water + " ml");
                tvSteps.setText(steps + " steps");
                tvCalories.setText(calories + " kcal");

                recentLogsContainer.addView(cardView);
            } while (cursor.moveToNext());
            cursor.close();
        }
    }
}