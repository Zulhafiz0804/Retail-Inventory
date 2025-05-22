package com.example.inventoryapps;

import android.os.Bundle;
import android.widget.TextView;

public class ProductNotFoundActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_product_not_found, findViewById(R.id.content_frame));
        setTitle("Product Not Found");

        TextView message = findViewById(R.id.tvNotFoundMessage);
        String scannedCode = getIntent().getStringExtra("scannedCode");
        message.setText("No product found for code: " + scannedCode);
    }
}
