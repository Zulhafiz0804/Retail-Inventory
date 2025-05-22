package com.example.inventoryapps;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;

public class StaffActivity extends BaseActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Button btnSearchCode, btnScanQR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_staff, findViewById(R.id.content_frame));

        setTitle("Staff Dashboard");

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

// Buttons remain optional here if you're using LinearLayout click listeners
        btnSearchCode = findViewById(R.id.btnSearchStaff);
        btnScanQR = findViewById(R.id.btnScanQR);
    }

    // Called from LinearLayout: android:onClick="onScanQRClick"
    public void onScanQRClick(View view) {
        Intent intent = new Intent(StaffActivity.this, QRScanActivity.class);
        startActivity(intent);
    }

    // Called from LinearLayout: android:onClick="onSearchClick"
    public void onSearchClick(View view) {
        Intent intent = new Intent(StaffActivity.this, StaffSearchActivity.class);
        startActivity(intent);
    }
}
