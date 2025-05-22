package com.example.inventoryapps;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_settings, findViewById(R.id.content_frame), true);

        setTitle("Settings");
        // Add functionality for settings items
        setupSettingsOptions();
    }

    private void setupSettingsOptions() {
        findViewById(R.id.changePassword).setOnClickListener(v ->
                Toast.makeText(this, "Change Password clicked", Toast.LENGTH_SHORT).show());

        findViewById(R.id.aboutApp).setOnClickListener(v ->
                Toast.makeText(this, "About App clicked", Toast.LENGTH_SHORT).show());

        findViewById(R.id.logout).setOnClickListener(v ->
                Toast.makeText(this, "Log Out clicked", Toast.LENGTH_SHORT).show());
    }
}
