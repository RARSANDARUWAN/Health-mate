package com.example.healthmate;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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

public class HistoryActivity extends AppCompatActivity {

    private EditText etSearchDate;
    private Button btnAllRecords, btnActiveDays;
    private LinearLayout historyCardsContainer;
    private ImageView btnCalendarPicker;
    private TextView tvAvgWater, tvAvgSteps, tvAvgBurned;
    private DatabaseHelper databaseHelper;

    private int activeUserId = -1;
    private boolean showActiveOnly = false;
    private String currentSearchKeyword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // retrieve active logged-in user id from session preferences
        SharedPreferences sessionPrefs = getSharedPreferences("HealthMateSession", MODE_PRIVATE);
        activeUserId = sessionPrefs.getInt("logged_in_user_id", -1);

        // initialize database helper instance
        databaseHelper = new DatabaseHelper(this);

        // bind xml layout components
        etSearchDate = findViewById(R.id.etSearchDate);
        btnAllRecords = findViewById(R.id.btnAllRecords);
        btnActiveDays = findViewById(R.id.btnActiveDays);
        historyCardsContainer = findViewById(R.id.historyCardsContainer);
        btnCalendarPicker = findViewById(R.id.btnCalendarPicker);
        tvAvgWater = findViewById(R.id.tvAvgWater);
        tvAvgSteps = findViewById(R.id.tvAvgSteps);
        tvAvgBurned = findViewById(R.id.tvAvgBurned);

        // handle all records filter click event
        btnAllRecords.setOnClickListener(v -> {
            showActiveOnly = false;
            switchFilterStyle();
            refreshHistoryList();
        });

        // handle active days filter click event
        btnActiveDays.setOnClickListener(v -> {
            showActiveOnly = true;
            switchFilterStyle();
            refreshHistoryList();
        });

        // monitor live search input text changes
        etSearchDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchKeyword = s.toString().trim();
                refreshHistoryList();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // open calendar date picker popup
        btnCalendarPicker.setOnClickListener(v -> openCalendarSelector());

        // setup navigation modules
        NavigationHelper.setupBottomNavigation(this);
        NavigationHelper.setupTopBarActions(this);

        // initialize UI state on start
        switchFilterStyle();
        refreshHistoryList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sessionPrefs = getSharedPreferences("HealthMateSession", MODE_PRIVATE);
        activeUserId = sessionPrefs.getInt("logged_in_user_id", -1);
        refreshHistoryList();
    }

    // switch button theme styling based on active filter mode
    private void switchFilterStyle() {
        if (!showActiveOnly) {
            btnAllRecords.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_dark));
            btnAllRecords.setTextColor(Color.WHITE);

            btnActiveDays.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1A261D")));
            btnActiveDays.setTextColor(Color.parseColor("#8FA693"));
        } else {
            btnActiveDays.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_dark));
            btnActiveDays.setTextColor(Color.WHITE);

            btnAllRecords.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1A261D")));
            btnAllRecords.setTextColor(Color.parseColor("#8FA693"));
        }
    }

    // display calendar picker dialog to filter records by target date
    private void openCalendarSelector() {
        Calendar calInstance = Calendar.getInstance();
        DatePickerDialog pickerDialog = new DatePickerDialog(
                this,
                (view, yr, mth, day) -> {
                    calInstance.set(yr, mth, day);
                    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    etSearchDate.setText(dateFormatter.format(calInstance.getTime()));
                },
                calInstance.get(Calendar.YEAR),
                calInstance.get(Calendar.MONTH),
                calInstance.get(Calendar.DAY_OF_MONTH)
        );
        pickerDialog.show();
    }

    // fetch active user records, apply filters, calculate stats, and inflate cards
    private void refreshHistoryList() {
        historyCardsContainer.removeAllViews();
        Cursor dbCursor = databaseHelper.getAllHealthRecordsByUser(activeUserId);

        int cumulativeWater = 0, cumulativeSteps = 0, cumulativeCalories = 0, rowCounter = 0;
        LayoutInflater layoutInflater = LayoutInflater.from(this);

        if (dbCursor != null && dbCursor.moveToFirst()) {
            do {
                int recordId = dbCursor.getInt(dbCursor.getColumnIndexOrThrow("id"));
                String recordDate = dbCursor.getString(dbCursor.getColumnIndexOrThrow("date"));
                int stepCount = dbCursor.getInt(dbCursor.getColumnIndexOrThrow("steps"));
                int calorieCount = dbCursor.getInt(dbCursor.getColumnIndexOrThrow("calories"));
                int waterVolume = dbCursor.getInt(dbCursor.getColumnIndexOrThrow("water"));

                if (showActiveOnly && stepCount < 6000) {
                    continue;
                }

                if (!currentSearchKeyword.isEmpty()) {
                    String queryTerm = currentSearchKeyword.toLowerCase();
                    boolean dateMatch = recordDate.toLowerCase().contains(queryTerm);
                    boolean stepMatch = String.valueOf(stepCount).contains(queryTerm);
                    boolean calorieMatch = String.valueOf(calorieCount).contains(queryTerm);
                    boolean waterMatch = String.valueOf(waterVolume).contains(queryTerm);

                    if (!dateMatch && !stepMatch && !calorieMatch && !waterMatch) {
                        continue;
                    }
                }

                cumulativeWater += waterVolume;
                cumulativeSteps += stepCount;
                cumulativeCalories += calorieCount;
                rowCounter++;

                View cardInstance = layoutInflater.inflate(R.layout.item_history_card, historyCardsContainer, false);

                TextView tvDateDisplay = cardInstance.findViewById(R.id.tvLogDate);
                TextView tvWaterDisplay = cardInstance.findViewById(R.id.tvLogWater);
                TextView tvStepsDisplay = cardInstance.findViewById(R.id.tvLogSteps);
                TextView tvCaloriesDisplay = cardInstance.findViewById(R.id.tvLogCalories);
                TextView tvStatusIndicator = cardInstance.findViewById(R.id.tvGoalStatus);
                ImageView triggerEditAction = cardInstance.findViewById(R.id.btnEditRecord);
                ImageView triggerDeleteAction = cardInstance.findViewById(R.id.btnDeleteRecord);

                tvDateDisplay.setText(recordDate);
                tvWaterDisplay.setText(waterVolume + " ml");
                tvStepsDisplay.setText(String.format(Locale.getDefault(), "%,d", stepCount));
                tvCaloriesDisplay.setText(calorieCount + " kcal");

                if (stepCount >= 6000) {
                    tvStatusIndicator.setText("✓ Goal Met");
                    tvStatusIndicator.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                } else {
                    tvStatusIndicator.setText("• In Progress");
                    tvStatusIndicator.setTextColor(getResources().getColor(android.R.color.darker_gray));
                }

                triggerDeleteAction.setOnClickListener(clickSource -> {
                    new AlertDialog.Builder(HistoryActivity.this)
                            .setTitle("Delete Record")
                            .setMessage("Are you sure you want to delete this health record?")
                            .setPositiveButton("Delete", (dialogInterface, whichButton) -> {
                                boolean isRemoved = databaseHelper.deleteRecordById(recordId);
                                if (isRemoved) {
                                    Toast.makeText(HistoryActivity.this, "Record deleted successfully!", Toast.LENGTH_SHORT).show();
                                    refreshHistoryList();
                                } else {
                                    Toast.makeText(HistoryActivity.this, "Failed to delete record.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });

                final boolean[] editStateFlag = {false};
                triggerEditAction.setOnClickListener(clickSource -> {
                    if (!editStateFlag[0]) {
                        editStateFlag[0] = true;
                        triggerEditAction.setAlpha(0.5f);

                        EditText editableWaterField = convertTextToInput(tvWaterDisplay, String.valueOf(waterVolume));
                        EditText editableStepsField = convertTextToInput(tvStepsDisplay, String.valueOf(stepCount));
                        EditText editableCaloriesField = convertTextToInput(tvCaloriesDisplay, String.valueOf(calorieCount));

                        LinearLayout headerLayoutParent = (LinearLayout) tvDateDisplay.getParent();
                        Button saveActionButton = new Button(HistoryActivity.this);
                        saveActionButton.setText("Save");
                        saveActionButton.setTextSize(11f);
                        saveActionButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_dark));
                        saveActionButton.setTextColor(Color.WHITE);

                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT, 80);
                        layoutParams.setMargins(8, 0, 8, 0);
                        saveActionButton.setLayoutParams(layoutParams);

                        headerLayoutParent.addView(saveActionButton, headerLayoutParent.indexOfChild(triggerEditAction) + 1);

                        saveActionButton.setOnClickListener(innerClick -> {
                            String updatedWaterText = editableWaterField.getText().toString().trim();
                            String updatedStepsText = editableStepsField.getText().toString().trim();
                            String updatedCaloriesText = editableCaloriesField.getText().toString().trim();

                            if (updatedWaterText.isEmpty() || updatedStepsText.isEmpty() || updatedCaloriesText.isEmpty()) {
                                Toast.makeText(HistoryActivity.this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            int finalWaterValue = Integer.parseInt(updatedWaterText);
                            int finalStepsValue = Integer.parseInt(updatedStepsText);
                            int finalCaloriesValue = Integer.parseInt(updatedCaloriesText);

                            boolean isUpdatedSuccessfully = databaseHelper.updateHealthRecord(recordId, finalStepsValue, finalCaloriesValue, finalWaterValue);
                            if (isUpdatedSuccessfully) {
                                Toast.makeText(HistoryActivity.this, "Record updated successfully!", Toast.LENGTH_SHORT).show();
                                refreshHistoryList();
                            } else {
                                Toast.makeText(HistoryActivity.this, "Failed to update record.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                historyCardsContainer.addView(cardInstance);

            } while (dbCursor.moveToNext());
            dbCursor.close();
        }

        if (rowCounter > 0) {
            tvAvgWater.setText((cumulativeWater / rowCounter) + " ml");
            tvAvgSteps.setText(String.format(Locale.getDefault(), "%,d", (cumulativeSteps / rowCounter)));
            tvAvgBurned.setText((cumulativeCalories / rowCounter) + " kcal");
        } else {
            tvAvgWater.setText("0 ml");
            tvAvgSteps.setText("0");
            tvAvgBurned.setText("0 kcal");
        }
    }

    // dynamically substitute a textview with an editable text input view
    private EditText convertTextToInput(TextView targetView, String initialValueText) {
        LinearLayout containerParent = (LinearLayout) targetView.getParent();
        int targetPosition = containerParent.indexOfChild(targetView);

        EditText replacementInput = new EditText(this);
        replacementInput.setText(initialValueText);
        replacementInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        replacementInput.setTextSize(13f);
        replacementInput.setTextColor(Color.WHITE);
        replacementInput.setBackgroundColor(Color.parseColor("#121C14"));
        replacementInput.setLayoutParams(targetView.getLayoutParams());

        containerParent.removeView(targetView);
        containerParent.addView(replacementInput, targetPosition);
        return replacementInput;
    }
}