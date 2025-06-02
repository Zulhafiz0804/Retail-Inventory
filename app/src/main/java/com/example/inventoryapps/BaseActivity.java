package com.example.inventoryapps;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;

import com.google.android.material.navigation.NavigationView;

public class BaseActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);  // Must have drawer & toolbar

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);

        // 🚀 Automatically set menu based on user role
        setNavigationMenuBasedOnUserRole();
    }

    private void setNavigationMenuBasedOnUserRole() {
        SharedPreferences prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE);
        role = prefs.getString("user_role", "staff"); // ✅ FIXED: assign to the class field

        navigationView.getMenu().clear(); // Prevent duplicates

        if ("manager".equalsIgnoreCase(role)) {
            navigationView.inflateMenu(R.menu.manager_menu);
        } else {
            navigationView.inflateMenu(R.menu.staff_menu);
        }
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (role.equalsIgnoreCase("manager")) {
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, ManagerActivity.class));
            } else if (id == R.id.nav_add) {
                startActivity(new Intent(this, AddProductActivity.class));
            } else if (id == R.id.nav_staff) {
                startActivity(new Intent(this, StaffManagementActivity.class));
            } else if (id == R.id.nav_about) {
                startActivity(new Intent(this, AboutActivity.class));
            } else if (id == R.id.nav_search) {
                startActivity(new Intent(this, ManagerSearchActivity.class));
            } else if (id == R.id.nav_status) {
                startActivity(new Intent(this, InventoryStatusActivity.class));
            } else if (id == R.id.nav_logout) {
                performLogout();
            } else {
                Toast.makeText(this, "Manager menu: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        } else {
            if (id == R.id.navi_home) {
                startActivity(new Intent(this, StaffActivity.class));
            } else if (id == R.id.nav_scan_qr) {
                startActivity(new Intent(this, QRScanActivity.class));
            } else if (id == R.id.nav_search_code) {
                startActivity(new Intent(this, StaffSearchActivity.class));
            } else if (id == R.id.nav_logout) {
                performLogout();
            } else {
                Toast.makeText(this, "Staff menu: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    private void performLogout() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Clear saved session
                    SharedPreferences prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE);
                    prefs.edit().clear().apply();

                    // Show toast
                    Toast.makeText(this, "You have been logged out", Toast.LENGTH_SHORT).show();

                    // Go back to login screen
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss(); // Just close the dialog
                })
                .show();
    }
}
