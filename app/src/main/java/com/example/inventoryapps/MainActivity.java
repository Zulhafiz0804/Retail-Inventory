package com.example.inventoryapps;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.*;
import com.google.android.material.imageview.ShapeableImageView;

public class MainActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private FirebaseFirestore db;

    private Handler handler = new Handler();
    private Runnable flipRunnable;
    private boolean isFlipping = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already signed in
        SharedPreferences prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE);
        String role = prefs.getString("user_role", null);
        String userId = prefs.getString("user_id", null);

        if (role != null && userId != null) {
            // Redirect to correct activity
            if (role.equals("manager")) {
                Intent intent = new Intent(MainActivity.this, ManagerActivity.class);
                intent.putExtra("managerId", userId);
                startActivity(intent);
                finish();
                return;
            } else if (role.equals("staff")) {
                Intent intent = new Intent(MainActivity.this, StaffActivity.class);
                intent.putExtra("staffId", userId);
                startActivity(intent);
                finish();
                return;
            }
        }

        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        ShapeableImageView imagePlaceholder = findViewById(R.id.imagePlaceholder);
        flipRunnable = new Runnable() {
            @Override
            public void run() {
                if (isFlipping) {
                    imagePlaceholder.animate()
                            .rotationYBy(180f)
                            .setDuration(1000)
                            .withEndAction(this)
                            .start();
                }
            }
        };
        handler.post(flipRunnable);

        btnLogin.setOnClickListener(v -> {
            String email = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(MainActivity.this, "Please enter all details", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isFlipping = false;
        handler.removeCallbacks(flipRunnable);
    }

    private void loginUser(String email, String password) {
        db.collection("INVENTORY_MANAGER")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                        String dbPassword = doc.getString("password");

                        if (dbPassword != null && dbPassword.equals(password)) {
                            SharedPreferences prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE);
                            prefs.edit()
                                    .putString("user_role", "manager")
                                    .putString("user_id", doc.getId())
                                    .apply();

                            Intent intent = new Intent(MainActivity.this, ManagerActivity.class);
                            intent.putExtra("managerId", doc.getId());
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        db.collection("STAFF")
                                .whereEqualTo("email", email)
                                .get()
                                .addOnCompleteListener(staffTask -> {
                                    if (staffTask.isSuccessful() && !staffTask.getResult().isEmpty()) {
                                        DocumentSnapshot doc = staffTask.getResult().getDocuments().get(0);
                                        String dbPassword = doc.getString("password");

                                        if (dbPassword != null && dbPassword.equals(password)) {
                                            SharedPreferences prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE);
                                            prefs.edit()
                                                    .putString("user_role", "staff")
                                                    .putString("user_id", doc.getId())
                                                    .apply();

                                            Intent intent = new Intent(MainActivity.this, StaffActivity.class);
                                            intent.putExtra("staffId", doc.getId());
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(MainActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                                        }

                                    } else {
                                        Toast.makeText(MainActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Login error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("LoginDebug", "Login failed", e);
                });
    }
}
