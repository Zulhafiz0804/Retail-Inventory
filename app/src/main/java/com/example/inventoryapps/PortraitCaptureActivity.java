package com.example.inventoryapps;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import com.journeyapps.barcodescanner.CaptureActivity;

public class PortraitCaptureActivity extends CaptureActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Force portrait orientation for scanner activity
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}

