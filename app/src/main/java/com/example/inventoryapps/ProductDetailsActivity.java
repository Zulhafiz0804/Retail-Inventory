package com.example.inventoryapps;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductDetailsActivity extends BaseActivity {

    TextView productName, productCode, description, price;
    ImageView productImage;
    Spinner colorSpinner;
    LinearLayout sizeContainer;
    ImageButton btnToggleDescription;

    private FirebaseFirestore firestore;
    private Map<String, Map<String, Long>> quantities = new HashMap<>();
    private Map<String, String> image;
    private boolean isDescriptionVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_product_details, findViewById(R.id.content_frame));

        setTitle("Product Details");

        productName = findViewById(R.id.tvProductName);
        productCode = findViewById(R.id.tvProductCode);
        description = findViewById(R.id.tvDescription);
        price = findViewById(R.id.tvPrice);
        productImage = findViewById(R.id.productImage);
        colorSpinner = findViewById(R.id.colorSpinner);
        sizeContainer = findViewById(R.id.sizeContainer);
        btnToggleDescription = findViewById(R.id.btnToggleDescription);

        // Initially hide description and set toggle icon
        description.setVisibility(View.GONE);
        btnToggleDescription.setImageResource(R.drawable.drop1);

        btnToggleDescription.setOnClickListener(v -> {
            if (isDescriptionVisible) {
                description.setVisibility(View.GONE);
                btnToggleDescription.setImageResource(R.drawable.drop1);
            } else {
                description.setVisibility(View.VISIBLE);
                btnToggleDescription.setImageResource(R.drawable.drop2);
            }
            isDescriptionVisible = !isDescriptionVisible;
        });

        String productId = getIntent().getStringExtra("productId");

        if (productId == null) {
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firestore = FirebaseFirestore.getInstance();

        firestore.collection("INVENTORY_ITEM").document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String code = documentSnapshot.getString("item_code");
                        String name = documentSnapshot.getString("item_name");
                        String desc = documentSnapshot.getString("item_description");
                        Double p = documentSnapshot.getDouble("price");

                        image = (Map<String, String>) documentSnapshot.get("image");

                        Map<String, Object> rawQuantities = (Map<String, Object>) documentSnapshot.get("quantities");
                        if (rawQuantities != null) {
                            for (Map.Entry<String, Object> colorEntry : rawQuantities.entrySet()) {
                                String color = colorEntry.getKey();
                                Map<String, Long> sizeMap = new HashMap<>();

                                Object sizeData = colorEntry.getValue();
                                if (sizeData instanceof Map) {
                                    Map<String, Object> sizeObjMap = (Map<String, Object>) sizeData;
                                    for (Map.Entry<String, Object> sizeEntry : sizeObjMap.entrySet()) {
                                        String size = sizeEntry.getKey();
                                        Object qtyObj = sizeEntry.getValue();
                                        if (qtyObj instanceof Number) {
                                            sizeMap.put(size, ((Number) qtyObj).longValue());
                                        }
                                    }
                                }

                                quantities.put(color, sizeMap);
                            }
                        }

                        productName.setText(name != null ? name : "N/A");
                        productCode.setText("Code: " + (code != null ? code : "-"));
                        description.setText(desc != null ? desc : "No description");
                        price.setText("RM " + (p != null ? String.format("%.2f", p) : "0.00"));

                        List<String> colorList = new ArrayList<>();
                        if (image != null && !image.isEmpty()) {
                            colorList.addAll(image.keySet());
                        } else if (!quantities.isEmpty()) {
                            colorList.addAll(quantities.keySet());
                        }

                        if (!colorList.isEmpty()) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                    android.R.layout.simple_spinner_item, colorList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            colorSpinner.setAdapter(adapter);

                            colorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    String selectedColor = colorList.get(position);
                                    updateProductImageAndQuantity(selectedColor);
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                    // Do nothing
                                }
                            });

                            colorSpinner.setSelection(0);
                            updateProductImageAndQuantity(colorList.get(0));
                        } else {
                            Toast.makeText(this, "No color options available", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void updateProductImageAndQuantity(String color) {
        Log.d("DEBUG", "Selected color: " + color);
        if (image != null && image.containsKey(color)) {
            String imageUrl = image.get(color);
            Log.d("DEBUG", "Image URL: " + imageUrl);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder)
                        .into(productImage);
            } else {
                productImage.setImageResource(R.drawable.placeholder);
            }
        } else {
            productImage.setImageResource(R.drawable.placeholder);
        }

        // Clear previous size views
        sizeContainer.removeAllViews();
        if (quantities != null && quantities.containsKey(color)) {
            Map<String, Long> sizes = quantities.get(color);
            // Define the size order
            List<String> sizeOrder = Arrays.asList("S", "M", "L", "XL", "XXL");
            for (String size : sizeOrder) {
                if (sizes.containsKey(size)) {
                    Long quantity = sizes.get(size);
                    TextView sizeText = new TextView(this);
                    sizeText.setText("Size " + size + ": " + quantity);
                    sizeText.setTextSize(16);
                    sizeText.setTextColor(getResources().getColor(android.R.color.black));

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 0, 0, 12);
                    sizeText.setLayoutParams(params);

                    sizeContainer.addView(sizeText);
                }}
        } else {
            TextView noStock = new TextView(this);
            noStock.setText("No stock info available.");
            noStock.setTextColor(getResources().getColor(android.R.color.darker_gray));
            noStock.setTextSize(16);
            sizeContainer.addView(noStock);
        }}}
