package com.example.inventoryapps;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import com.google.firebase.firestore.*;
import java.util.HashMap;
import java.util.Map;

public class AddStaffActivity extends BaseActivity {

    private EditText etEmail, etUsername, etPassword, etPhone;
    private RadioGroup rgRole;
    private Button btnSave;

    private FirebaseFirestore db;
    private String currentManagerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_add_staff, findViewById(R.id.content_frame), true);

        setTitle("Add Staff");

        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        rgRole = findViewById(R.id.rgRole);
        btnSave = findViewById(R.id.btnSave);

        db = FirebaseFirestore.getInstance();
        currentManagerId = getIntent().getStringExtra("manager_id");

        if (currentManagerId == null) {
            SharedPreferences prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE);
            currentManagerId = prefs.getString("user_id", null);
        }

        btnSave.setOnClickListener(view -> saveStaff());
    }

    private void saveStaff() {
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        int selectedRoleId = rgRole.getCheckedRadioButtonId();
        if (selectedRoleId == -1) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadio = findViewById(selectedRoleId);
        String role = selectedRadio.getText().toString().toLowerCase().contains("manager") ? "manager" : "staff";

        if (email.isEmpty() || username.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        String collectionName = role.equals("manager") ? "INVENTORY_MANAGER" : "STAFF";
        String idPrefix = role.equals("manager") ? "manager" : "staff";

        // Generate a new sequential ID like staff01, staff02, etc.
        db.collection(collectionName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int maxId = 0;
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String id = doc.getId();
                        if (id.startsWith(idPrefix)) {
                            try {
                                int num = Integer.parseInt(id.substring(idPrefix.length()));
                                if (num > maxId) maxId = num;
                            } catch (NumberFormatException ignored) {}
                        }
                    }

                    String newId = String.format("%s%02d", idPrefix, maxId + 1); // e.g. staff03

                    Map<String, Object> staffData = new HashMap<>();
                    staffData.put("email", email);
                    staffData.put("password", password);
                    staffData.put("role", role);
                    staffData.put("phone_no", phone);

                    if (role.equals("manager")) {
                        staffData.put("managerID", newId);
                        staffData.put("manager_name", username);
                    } else {
                        staffData.put("staffID", newId);
                        staffData.put("staff_name", username);
                        staffData.put("manager_id", currentManagerId);
                    }

                    db.collection(collectionName).document(newId)
                            .set(staffData)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "User added with ID: " + newId, Toast.LENGTH_SHORT).show();
                                finish(); // return to previous screen
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public static class Staff {
        public String email, username, password, role, staffID, phone;

        public Staff() {}

        public Staff(String email, String username, String password, String role, String staffID, String phone_no) {
            this.email = email;
            this.username = username;
            this.password = password;
            this.role = role;
            this.staffID = staffID;
            this.phone = phone_no;
        }
    }
}
