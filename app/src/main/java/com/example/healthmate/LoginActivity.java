package com.example.healthmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private Button loginBtn;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        databaseHelper = new DatabaseHelper(this);

        // initialize database and insert dummy data if needed
        databaseHelper.getWritableDatabase();

        // bind xml views
        inputEmail = findViewById(R.id.etEmail);
        inputPassword = findViewById(R.id.etPassword);
        loginBtn = findViewById(R.id.btnLogin);

        // handle login button action
        loginBtn.setOnClickListener(v -> {
            String userEmail = inputEmail.getText().toString().trim();
            String userPass = inputPassword.getText().toString().trim();

            if (databaseHelper.checkUserLogin(userEmail, userPass)) {
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
            }
        });
    }
}