package com.example.inventoryapps;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

public class ManagerActivity extends BaseActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_manager, findViewById(R.id.content_frame));

        setTitle("Manager Dashboard");

        toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
            String managerId = prefs.getString("user_id", null);

            if (itemId == R.id.nav_home) {
                Toast.makeText(this, "You are already in Home page", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_add) {
                startActivity(new Intent(ManagerActivity.this, AddProductActivity.class)); // Updated flow
            } else if (itemId == R.id.nav_staff) {
                Intent intent = new Intent(ManagerActivity.this, StaffManagementActivity.class);
                intent.putExtra("managerId", managerId);
                startActivity(intent);
            } else if (itemId == R.id.nav_about) {
                startActivity(new Intent(ManagerActivity.this, AboutActivity.class));
            } else if (itemId == R.id.nav_search) {
                startActivity(new Intent(ManagerActivity.this, ManagerSearchActivity.class));
            } else if (itemId == R.id.nav_status) {
                startActivity(new Intent(ManagerActivity.this, InventoryStatusActivity.class));
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    // Dashboard button click handlers
    public void openHome(View view) {
        Toast.makeText(this, "You are already in Home page", Toast.LENGTH_SHORT).show();
    }

    public void openInventory(View view) {
        startActivity(new Intent(this, AddProductActivity.class)); // Updated flow
    }

    public void openStaffManagement(View view) {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String managerId = prefs.getString("user_id", null);

        Intent intent = new Intent(this, StaffManagementActivity.class);
        intent.putExtra("managerId", managerId);
        startActivity(intent);
    }

    public void openSettings(View view) {
        startActivity(new Intent(this, AboutActivity.class));
    }

    public void openSearch(View view) {
        startActivity(new Intent(this, ManagerSearchActivity.class));
    }
    public void openInventoryStatus(View view) {
        startActivity(new Intent(this, InventoryStatusActivity.class));
    }

}
