package com.example.inventoryapps;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
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
        setContentView(R.layout.activity_base);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);

        setNavigationMenuBasedOnUserRole();
        setupNavigationHeader();
    }

    private void setNavigationMenuBasedOnUserRole() {
        SharedPreferences prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE);
        role = prefs.getString("user_role", "staff");

        navigationView.getMenu().clear();

        if ("manager".equalsIgnoreCase(role)) {
            navigationView.inflateMenu(R.menu.manager_menu);
        } else {
            navigationView.inflateMenu(R.menu.staff_menu);
        }
    }

    private void setupNavigationHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView textUserName = headerView.findViewById(R.id.text_user_name);
        TextView textUserRole = headerView.findViewById(R.id.text_user_role);
        ImageView imageProfile = headerView.findViewById(R.id.image_profile);

        SharedPreferences prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE);
        String userName = prefs.getString("user_name", "Staff");
        String userRole = prefs.getString("user_role", "staff");

        textUserName.setText(userName);
        textUserRole.setText(capitalize(userRole));

        imageProfile.setImageResource(R.drawable.username3);
    }

    private String capitalize(String input) {
        if (input == null || input.isEmpty()) return "";
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (role.equalsIgnoreCase("manager")) {
            if (id == R.id.nav_home) {
                if (this.getClass().equals(ManagerActivity.class)) {
                    Toast.makeText(this, "You are already in Home page", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(this, ManagerActivity.class));
                }
            } else if (id == R.id.nav_add) {
                if (this.getClass().equals(AddProductActivity.class)) {
                    Toast.makeText(this, "You are already in Add Item page", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(this, AddProductActivity.class));
                }
            } else if (id == R.id.nav_staff) {
                if (this.getClass().equals(StaffManagementActivity.class)) {
                    Toast.makeText(this, "You are already in Staff Management page", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(this, StaffManagementActivity.class));
                }
            } else if (id == R.id.nav_about) {
                if (this.getClass().equals(AboutActivity.class)) {
                    Toast.makeText(this, "You are already in About page", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(this, AboutActivity.class));
                }
            } else if (id == R.id.nav_search) {
                if (this.getClass().equals(ManagerSearchActivity.class)) {
                    Toast.makeText(this, "You are already in Search page", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(this, ManagerSearchActivity.class));
                }
            } else if (id == R.id.nav_status) {
                if (this.getClass().equals(InventoryStatusActivity.class)) {
                    Toast.makeText(this, "You are already in Inventory Status page", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(this, InventoryStatusActivity.class));
                }
            } else if (id == R.id.nav_logout) {
                performLogout();
            } else {
                Toast.makeText(this, "Manager menu: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        } else {
            if (id == R.id.navi_home) {
                if (this.getClass().equals(StaffActivity.class)) {
                    Toast.makeText(this, "You are already in Home page", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(this, StaffActivity.class));
                }
            } else if (id == R.id.nav_scan_qr) {
                if (this.getClass().equals(QRScanActivity.class)) {
                    Toast.makeText(this, "You are already in QR Scanner page", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(this, QRScanActivity.class));
                }
            } else if (id == R.id.nav_search_code) {
                if (this.getClass().equals(StaffSearchActivity.class)) {
                    Toast.makeText(this, "You are already in Search page", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(this, StaffSearchActivity.class));
                }
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
                    SharedPreferences prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE);
                    prefs.edit().clear().apply();

                    Toast.makeText(this, "You have been logged out", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
