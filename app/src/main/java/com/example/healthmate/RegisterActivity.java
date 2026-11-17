package com.example.healthmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameField, ageField, emailField, passField;
    private Button registerButton;
    private TextView loginLinkText;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        databaseHelper = new DatabaseHelper(this);

        // Link XML views properly
        nameField = findViewById(R.id.etFullName);
        ageField = findViewById(R.id.etAge);
        emailField = findViewById(R.id.etEmail);
        passField = findViewById(R.id.etPassword);
        registerButton = findViewById(R.id.btnRegister);
        loginLinkText = findViewById(R.id.tvLoginLink);

        // Handle Register button click
        registerButton.setOnClickListener(view -> {
            String fullName = nameField.getText().toString().trim();
            String ageText = ageField.getText().toString().trim();
            String userEmail = emailField.getText().toString().trim();
            String userPass = passField.getText().toString().trim();

            if (fullName.isEmpty() || ageText.isEmpty() || userEmail.isEmpty() || userPass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int parsedAge = Integer.parseInt(ageText);
            boolean success = databaseHelper.registerUser(fullName, parsedAge, userEmail, userPass);

            if (success) {
                Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                // Redirect to Login screen after successful registration
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Registration Failed! Email may already exist.", Toast.LENGTH_LONG).show();
            }
        });

        // Handle Login Here text click to navigate to Login screen
        loginLinkText.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}