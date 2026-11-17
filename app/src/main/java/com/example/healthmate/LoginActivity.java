package com.example.healthmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private Button loginBtn;
    private DatabaseHelper databaseHelper;
    private TextView registerText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check if user is already logged in and redirect straight to dashboard
        SharedPreferences sessionPrefs = getSharedPreferences("HealthMateSession", MODE_PRIVATE);
        int savedUserId = sessionPrefs.getInt("logged_in_user_id", -1);
        if (savedUserId != -1) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        databaseHelper = new DatabaseHelper(this);
        databaseHelper.getWritableDatabase();

        // bind xml views
        inputEmail = findViewById(R.id.etEmail);
        inputPassword = findViewById(R.id.etPassword);
        loginBtn = findViewById(R.id.btnLogin);
        registerText = findViewById(R.id.tvRegister);

        // handle login button action
        loginBtn.setOnClickListener(v -> {
            String userEmail = inputEmail.getText().toString().trim();
            String userPass = inputPassword.getText().toString().trim();

            if (userEmail.isEmpty() || userPass.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // verify credentials and save active user id to session preferences
            if (databaseHelper.checkUserLogin(userEmail, userPass)) {
                int userId = databaseHelper.getUserIdByEmail(userEmail);

                SharedPreferences.Editor editor = sessionPrefs.edit();
                editor.putInt("logged_in_user_id", userId);
                editor.apply();

                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
            }
        });

        // navigate to registration screen
        registerText.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }
}