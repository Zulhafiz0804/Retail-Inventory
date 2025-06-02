package com.example.inventoryapps;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class QRScanActivity extends BaseActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();

        new IntentIntegrator(this)
                .setCaptureActivity(PortraitCaptureActivity.class)
                .setPrompt("Scan QR Code")
                .setOrientationLocked(true)
                .setBeepEnabled(true)
                .setBarcodeImageEnabled(true)
                .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                .initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() != null) {
                String scannedCode = result.getContents();

                db.collection("INVENTORY_ITEM")
                        .whereEqualTo("item_code", scannedCode)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (!task.getResult().isEmpty()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        String productId = document.getId();
                                        String staffId = AuthHelper.getCurrentStaffId(getApplicationContext());
                                        Timestamp timestamp = Timestamp.now();

                                        db.collection("QR_CODE").get().addOnSuccessListener(snapshot -> {
                                            int count = snapshot.size() + 1;
                                            String qrId = String.format("qr%02d", count);

                                            Map<String, Object> qrEntry = new HashMap<>();
                                            qrEntry.put("qr_id", qrId);
                                            qrEntry.put("item_code", scannedCode);
                                            qrEntry.put("staff_id", staffId);
                                            qrEntry.put("timestamp", timestamp);

                                            db.collection("QR_CODE").document(qrId).set(qrEntry);

                                            // Create log_id with date suffix
                                            db.collection("ACTIVITY_LOG").get().addOnSuccessListener(logSnapshot -> {
                                                int logCount = logSnapshot.size() + 1;
                                                String dateSuffix = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
                                                String logId = String.format("log%02d_%s", logCount, dateSuffix);

                                                Map<String, Object> logEntry = new HashMap<>();
                                                logEntry.put("log_id", logId);
                                                logEntry.put("staff_id", staffId);
                                                logEntry.put("action", "Scanned QR");
                                                logEntry.put("item_code", scannedCode);
                                                logEntry.put("timestamp", timestamp);

                                                db.collection("ACTIVITY_LOG").document(logId).set(logEntry);

                                                // Navigate to ProductDetailsActivity
                                                Intent intent = new Intent(QRScanActivity.this, ProductDetailsActivity.class);
                                                intent.putExtra("productId", productId);
                                                startActivity(intent);
                                                finish();
                                            });
                                        });
                                        return;
                                    }
                                } else {
                                    Intent notFoundIntent = new Intent(QRScanActivity.this, ProductNotFoundActivity.class);
                                    notFoundIntent.putExtra("scannedCode", scannedCode);
                                    startActivity(notFoundIntent);
                                    finish();
                                }
                            } else {
                                Toast.makeText(QRScanActivity.this, "Firestore error", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
            } else {
                Toast.makeText(this, "Scan Cancelled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
