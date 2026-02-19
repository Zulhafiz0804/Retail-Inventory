package com.example.inventoryapps;

import android.os.Bundle;
import android.text.InputType;
import android.widget.*;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class EditStaffActivity extends BaseActivity {

    private EditText etUsername, etEmail, etPassword, etRole, etPhone;
    private Button btnSave;
    private FirebaseFirestore db;
    private String staffID, username, role, phone, managerId;
    private ImageView ivTogglePassword;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_edit_staff, findViewById(R.id.content_frame), true);

        setTitle("Edit Staff");

        // Initialize views
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etRole = findViewById(R.id.etRole);
        etPhone = findViewById(R.id.etPhone);
        btnSave = findViewById(R.id.btnSave);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);

        ivTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                // Hide password
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.hidden);
            } else {
                // Show password
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.visibility);
            }
            isPasswordVisible = !isPasswordVisible;
            etPassword.setSelection(etPassword.getText().length()); // Move cursor to end
        });


        db = FirebaseFirestore.getInstance();

        // Retrieve intent data
        staffID = getIntent().getStringExtra("staffID");
        username = getIntent().getStringExtra("username");
        String email = getIntent().getStringExtra("email");
        String password = getIntent().getStringExtra("password");
        phone = getIntent().getStringExtra("phone_no");
        role = getIntent().getStringExtra("role");

        // Initially try to get managerId from Intent
        managerId = getIntent().getStringExtra("managerId");

        // Load manager_id from Firestore if role is staff
        if (!"manager".equalsIgnoreCase(role)) {
            db.collection("STAFF").document(staffID)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists() && document.contains("manager_id")) {
                            managerId = document.getString("manager_id");
                        }
                    });
        }

        // Populate fields
        etUsername.setText(username);
        etEmail.setText(email);
        etPassword.setText(password);
        etPhone.setText(phone);
        etRole.setText(role);

        // Disable editing username and role
        etUsername.setEnabled(false);
        etRole.setEnabled(false);

        btnSave.setOnClickListener(v -> updateStaff());
    }

    private void updateStaff() {
        String updatedEmail = etEmail.getText().toString().trim();
        String updatedPassword = etPassword.getText().toString().trim();
        String updatedPhone = etPhone.getText().toString().trim();

        if (updatedEmail.isEmpty() || updatedPassword.isEmpty() || updatedPhone.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String collection = role.equalsIgnoreCase("manager") ? "INVENTORY_MANAGER" : "STAFF";
        String nameField = role.equalsIgnoreCase("manager") ? "manager_name" : "staff_name";
        String idField = role.equalsIgnoreCase("manager") ? "managerID" : "staffID";

        Map<String, Object> updatedStaff = new HashMap<>();
        updatedStaff.put("email", updatedEmail);
        updatedStaff.put("password", updatedPassword);
        updatedStaff.put("phone_no", updatedPhone);
        updatedStaff.put("role", role);
        updatedStaff.put(nameField, username);
        updatedStaff.put(idField, staffID);

        // Preserve manager_id if role is staff
        if (!"manager".equalsIgnoreCase(role) && managerId != null) {
            updatedStaff.put("manager_id", managerId);
        }

        db.collection(collection).document(staffID)
                .set(updatedStaff)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
