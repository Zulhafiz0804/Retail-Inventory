package com.example.inventoryapps;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class StaffManagementActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private StaffAdapter staffAdapter;
    private List<AddStaffActivity.Staff> staffList;
    private FirebaseFirestore firestore;
    private String managerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_staff_management, findViewById(R.id.content_frame));

        setTitle("Staff Management");

        managerId = getIntent().getStringExtra("managerId");

        Button btnAddStaff = findViewById(R.id.btnAddStaff);
        btnAddStaff.setOnClickListener(view -> {
            Intent intent = new Intent(StaffManagementActivity.this, AddStaffActivity.class);
            intent.putExtra("manager_id", managerId);
            startActivity(intent);
        });


        recyclerView = findViewById(R.id.recyclerViewStaff);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        staffList = new ArrayList<>();
        staffAdapter = new StaffAdapter(staffList, this);
        recyclerView.setAdapter(staffAdapter);

        firestore = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStaffData();
    }

    private void loadStaffData() {
        staffList.clear();

        // Load staff
        firestore.collection("STAFF").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                AddStaffActivity.Staff staff = doc.toObject(AddStaffActivity.Staff.class);
                staff.staffID = doc.getId(); // Use document ID
                staff.username = doc.getString("staff_name"); // Map to common field
                staff.phone = doc.getString("phone_no");
                staff.role = "staff"; // Explicitly assign role
                staffList.add(staff);
            }

            // Load managers
            firestore.collection("INVENTORY_MANAGER").get().addOnSuccessListener(managerSnapshots -> {
                for (QueryDocumentSnapshot doc : managerSnapshots) {
                    AddStaffActivity.Staff manager = doc.toObject(AddStaffActivity.Staff.class);
                    manager.staffID = doc.getId(); // Use document ID
                    manager.username = doc.getString("manager_name"); // Map to common field
                    manager.role = "manager"; // Explicitly assign role
                    staffList.add(manager);
                }

                staffAdapter.notifyDataSetChanged(); // Update RecyclerView
            })
                    .addOnFailureListener(e ->
                    Toast.makeText(this, "Error loading managers", Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error loading staff", Toast.LENGTH_SHORT).show()
        );
    }
}
