package com.example.healthmate;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameField, ageField, emailField, passField;
    private Button registerButton;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        databaseHelper = new DatabaseHelper(this);

        // link xml views
        nameField = findViewById(R.id.etFullName);
        ageField = findViewById(R.id.etAge);
        emailField = findViewById(R.id.etEmail);
        passField = findViewById(R.id.etPassword);
        registerButton = findViewById(R.id.btnRegister);

        // handle register button click
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
                finish();
            } else {
                Toast.makeText(this, "Registration Failed (Email may already exist)", Toast.LENGTH_SHORT).show();
            }
        });
    }
}