package com.example.healthmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST_CODE = 100;

    private ImageView profileAvatarImg, backActionBtn;
    private TextView profileNameTv, profileEmailTv, userAgeTv, joinedDateTv, totalLogsTv;
    private EditText editNameInput, editAgeInput;
    private Button editProfileBtn, logoutBtn;
    private DatabaseHelper databaseManager;

    private int currentUserId = 1;
    private boolean isEditStateActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // initialize database helper
        databaseManager = new DatabaseHelper(this);

        // retrieve session user id from shared preferences
        SharedPreferences sessionPrefs = getSharedPreferences("HealthMateSession", MODE_PRIVATE);
        currentUserId = sessionPrefs.getInt("logged_in_user_id", 1);

        // bind view elements from xml layout
        backActionBtn = findViewById(R.id.btnBack);
        profileAvatarImg = findViewById(R.id.ivProfilePic);
        profileNameTv = findViewById(R.id.tvProfileName);
        profileEmailTv = findViewById(R.id.tvProfileEmail);
        userAgeTv = findViewById(R.id.tvUserAge);
        joinedDateTv = findViewById(R.id.tvJoinedDate);
        totalLogsTv = findViewById(R.id.tvTotalLogs);
        editNameInput = findViewById(R.id.etEditName);
        editAgeInput = findViewById(R.id.etEditAge);
        editProfileBtn = findViewById(R.id.btnEditProfile);
        logoutBtn = findViewById(R.id.btnLogout);

        // handle top back navigation action
        if (backActionBtn != null) {
            backActionBtn.setOnClickListener(v -> finish());
        }

        // fetch user details from sqlite
        fetchUserProfileDetails();

        // launch gallery picker to update profile image
        profileAvatarImg.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
        });

        // handle profile edit and update save action
        editProfileBtn.setOnClickListener(v -> {
            if (!isEditStateActive) {
                // activate edit mode and switch component visibilities
                isEditStateActive = true;
                editProfileBtn.setText("Save Profile");

                String currentNameText = profileNameTv.getText().toString();
                String currentAgeText = userAgeTv.getText().toString().replace(" years", "").trim();

                editNameInput.setText(currentNameText);
                editAgeInput.setText(currentAgeText);

                profileNameTv.setVisibility(View.GONE);
                editNameInput.setVisibility(View.VISIBLE);

                userAgeTv.setVisibility(View.GONE);
                editAgeInput.setVisibility(View.VISIBLE);

            } else {
                // read edited inputs and validate
                String updatedName = editNameInput.getText().toString().trim();
                String updatedAgeStr = editAgeInput.getText().toString().trim();

                if (updatedName.isEmpty() || updatedAgeStr.isEmpty()) {
                    Toast.makeText(this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                int updatedAgeVal = Integer.parseInt(updatedAgeStr);

                // persist changes to database
                boolean isUpdateSuccessful = databaseManager.updateUserProfile(currentUserId, updatedName, updatedAgeVal);

                if (isUpdateSuccessful) {
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    isEditStateActive = false;
                    editProfileBtn.setText("Edit Profile");

                    editNameInput.setVisibility(View.GONE);
                    profileNameTv.setVisibility(View.VISIBLE);

                    editAgeInput.setVisibility(View.GONE);
                    userAgeTv.setVisibility(View.VISIBLE);

                    fetchUserProfileDetails();
                } else {
                    Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // handle user session logout and clear preferences
        logoutBtn.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sessionPrefs.edit();
            editor.clear();
            editor.apply();

            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(ProfileActivity.this, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
        });

        // initialize bottom navigation bar setup
        NavigationHelper.setupBottomNavigation(this);
    }

    // load user profile info and total log stats from database
    private void fetchUserProfileDetails() {
        Cursor profileCursor = databaseManager.getUserProfileById(currentUserId);
        if (profileCursor != null) {
            if (profileCursor.moveToFirst()) {
                String userName = profileCursor.getString(profileCursor.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME));
                String userEmail = profileCursor.getString(profileCursor.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL));
                int userAge = profileCursor.getInt(profileCursor.getColumnIndexOrThrow(DatabaseHelper.COL_AGE));
                String joinDateStr = profileCursor.getString(profileCursor.getColumnIndexOrThrow(DatabaseHelper.COL_JOINED_DATE));

                profileNameTv.setText(userName);
                profileEmailTv.setText(userEmail);
                userAgeTv.setText(userAge + " years");
                joinedDateTv.setText(joinDateStr != null ? joinDateStr : "October 2026");
            }
            profileCursor.close();
        }

        // fetch total health records count created by user
        int totalRecordsCount = databaseManager.getTotalLogsCountByUserId(currentUserId);
        totalLogsTv.setText(totalRecordsCount + " Health Records Saved");
    }

    // handle image selection result from device storage gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUriResult = data.getData();
            if (imageUriResult != null) {
                profileAvatarImg.setImageURI(imageUriResult);
                Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show();
            }
        }
    }
}