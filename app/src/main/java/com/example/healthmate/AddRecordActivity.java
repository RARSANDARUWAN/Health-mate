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

    private EditText waterInput, stepsInput, caloriesInput;
    private Button saveRecordBtn;
    private TextView selectedDateLabel;
    private LinearLayout datePickerLayout, logsContainer;
    private ImageView backButton;
    private DatabaseHelper databaseHelper;

    private String targetDateFormatted = "";
    private Calendar calendarInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addrec);

        // initialize db and bind layout components
        databaseHelper = new DatabaseHelper(this);
        waterInput = findViewById(R.id.etWater);
        stepsInput = findViewById(R.id.etSteps);
        caloriesInput = findViewById(R.id.etCalories);
        saveRecordBtn = findViewById(R.id.btnSaveRecord);
        selectedDateLabel = findViewById(R.id.tvSelectedDate);
        datePickerLayout = findViewById(R.id.layoutDate);
        logsContainer = findViewById(R.id.recentLogsContainer);
        backButton = findViewById(R.id.btnBack);

        calendarInstance = Calendar.getInstance();
        setInitialDateToday();

        // open popup date selector
        datePickerLayout.setOnClickListener(v -> openDatePicker());

        // handle record submission
        saveRecordBtn.setOnClickListener(v -> validateAndStoreRecord());

        // return on back click
        backButton.setOnClickListener(v -> finish());

        // populate recent history list on load
        fetchRecentLogs();

        // setup navigation modules
        NavigationHelper.setupBottomNavigation(this);
        NavigationHelper.setupTopBarActions(this);
    }

    // set current date by default on startup
    private void setInitialDateToday() {
        SimpleDateFormat dbFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        targetDateFormatted = dbFormatter.format(calendarInstance.getTime());

        SimpleDateFormat displayFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        selectedDateLabel.setText("Today, " + displayFormatter.format(calendarInstance.getTime()));
    }

    // display native date picker dialog
    private void openDatePicker() {
        DatePickerDialog pickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendarInstance.set(year, month, dayOfMonth);
                    SimpleDateFormat dbFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    targetDateFormatted = dbFormatter.format(calendarInstance.getTime());

                    SimpleDateFormat displayFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    selectedDateLabel.setText(displayFormatter.format(calendarInstance.getTime()));
                },
                calendarInstance.get(Calendar.YEAR),
                calendarInstance.get(Calendar.MONTH),
                calendarInstance.get(Calendar.DAY_OF_MONTH)
        );
        pickerDialog.show();
    }

    // validate user inputs and perform database insertion
    private void validateAndStoreRecord() {
        String waterString = waterInput.getText().toString().trim();
        String stepsString = stepsInput.getText().toString().trim();
        String caloriesString = caloriesInput.getText().toString().trim();

        // ensure all fields have values
        if (waterString.isEmpty() || stepsString.isEmpty() || caloriesString.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        int waterVal = Integer.parseInt(waterString);
        int stepsVal = Integer.parseInt(stepsString);
        int caloriesVal = Integer.parseInt(caloriesString);

        // reject zero or negative numbers
        if (waterVal <= 0 || stepsVal <= 0 || caloriesVal <= 0) {
            Toast.makeText(this, "Values cannot be zero or negative!", Toast.LENGTH_SHORT).show();
            return;
        }

        // block future dates entry
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            java.util.Date chosenDate = sdf.parse(targetDateFormatted);
            java.util.Date currentSystemDate = sdf.parse(sdf.format(Calendar.getInstance().getTime()));

            if (chosenDate != null && chosenDate.after(currentSystemDate)) {
                Toast.makeText(this, "Cannot add records for future dates!", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // check if entry for this date already exists
        if (databaseHelper.checkRecordExistsByDate(targetDateFormatted)) {
            Toast.makeText(this, "A record for this date already exists!", Toast.LENGTH_LONG).show();
            return;
        }

        // insert metrics into db for user id 1
        boolean isSaved = databaseHelper.insertHealthRecord(1, targetDateFormatted, stepsVal, caloriesVal, waterVal);

        if (isSaved) {
            Toast.makeText(this, "Successfully data inserted!", Toast.LENGTH_SHORT).show();
            waterInput.setText("");
            stepsInput.setText("");
            caloriesInput.setText("");
            fetchRecentLogs(); // reload UI list dynamically
        } else {
            Toast.makeText(this, "Failed to insert record. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    // fetch recent logs from sqlite and dynamically inflate card views
    private void fetchRecentLogs() {
        logsContainer.removeAllViews(); // clear previous views
        Cursor cursor = databaseHelper.getRecentHealthRecords();

        if (cursor != null && cursor.moveToFirst()) {
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            do {
                String recordDate = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                int recordSteps = cursor.getInt(cursor.getColumnIndexOrThrow("steps"));
                int recordCalories = cursor.getInt(cursor.getColumnIndexOrThrow("calories"));
                int recordWater = cursor.getInt(cursor.getColumnIndexOrThrow("water"));

                // inflate layout card item
                View cardItemView = layoutInflater.inflate(R.layout.item_recent_log, logsContainer, false);

                TextView dateTv = cardItemView.findViewById(R.id.tvLogDate);
                TextView waterTv = cardItemView.findViewById(R.id.tvLogWater);
                TextView stepsTv = cardItemView.findViewById(R.id.tvLogSteps);
                TextView caloriesTv = cardItemView.findViewById(R.id.tvLogCalories);

                dateTv.setText(recordDate);
                waterTv.setText(recordWater + " ml");
                stepsTv.setText(recordSteps + " steps");
                caloriesTv.setText(recordCalories + " kcal");

                logsContainer.addView(cardItemView);
            } while (cursor.moveToNext());
            cursor.close();
        }
    }
}