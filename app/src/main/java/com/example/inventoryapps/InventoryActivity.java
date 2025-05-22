package com.example.inventoryapps;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryActivity extends BaseActivity {

    private EditText productPriceEditText;
    private TextView itemCodeTextView, productNameEditText, productDescriptionEditText ;
    private Button saveButton;
    private LinearLayout quantityContainer;
    private Spinner spinnerColor, spinnerSize;
    private EditText editQuantity;
    private ImageView inventoryProductImage;
    private ImageButton btnToggleDescription;

    private Map<String, Object> quantitiesData;
    private Map<String, String> imageMap;

    private FirebaseFirestore firestore;
    private String item_code = null;
    private String item_name = "";
    private Map<String, Map<String, EditText>> quantityInputs = new HashMap<>();

    private boolean isDescriptionVisible = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_inventory, findViewById(R.id.content_frame), true);

        productNameEditText = findViewById(R.id.editProductName);
        itemCodeTextView = findViewById(R.id.itemCodeTextView);
        productDescriptionEditText = findViewById(R.id.editProductDescription);
        productPriceEditText = findViewById(R.id.editProductPrice);
        saveButton = findViewById(R.id.btnSave);
        quantityContainer = findViewById(R.id.quantityContainer);
        spinnerColor = findViewById(R.id.spinnerColor);
        spinnerSize = findViewById(R.id.spinnerSize);
        editQuantity = findViewById(R.id.editQuantity);
        inventoryProductImage = findViewById(R.id.productImageView);
        btnToggleDescription = findViewById(R.id.btnToggleDescription);

        productNameEditText.setEnabled(false);
        productDescriptionEditText.setEnabled(false);
        productPriceEditText.setEnabled(false);

        productDescriptionEditText.setVisibility(View.GONE);
        btnToggleDescription.setImageResource(R.drawable.drop1);

        btnToggleDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDescriptionVisible) {
                    productDescriptionEditText.setVisibility(View.GONE);
                    btnToggleDescription.setImageResource(R.drawable.drop1);
                } else {
                    productDescriptionEditText.setVisibility(View.VISIBLE);
                    btnToggleDescription.setImageResource(R.drawable.drop2);
                }
                isDescriptionVisible = !isDescriptionVisible;
            }
        });

        firestore = FirebaseFirestore.getInstance();

        if (getIntent() != null && getIntent().hasExtra("productCode")) {
            item_code = getIntent().getStringExtra("productCode");
            loadProductFromFirestore(item_code);
        }

        saveButton.setOnClickListener(v -> {
            String selectedColor = spinnerColor.getSelectedItem().toString();
            String selectedSize = spinnerSize.getSelectedItem().toString();
            String qtyStr = editQuantity.getText().toString().trim();

            if (qtyStr.isEmpty()) {
                Toast.makeText(this, "Quantity cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            int newQty = Integer.parseInt(qtyStr);

            Map<String, Object> sizes = (Map<String, Object>) quantitiesData.get(selectedColor);
            sizes.put(selectedSize, newQty);
            quantitiesData.put(selectedColor, sizes);

            firestore.collection("INVENTORY_ITEM").document(item_code)
                    .update("quantities", quantitiesData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Quantity updated successfully!", Toast.LENGTH_SHORT).show();
                        logInventoryUpdate(item_code, item_name, selectedColor, selectedSize, newQty);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
    }

    private void loadProductFromFirestore(String code) {
        DocumentReference docRef = firestore.collection("INVENTORY_ITEM").document(code);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                item_name = documentSnapshot.getString("item_name");
                productNameEditText.setText(item_name);
                productDescriptionEditText.setText(documentSnapshot.getString("item_description"));
                Double price = documentSnapshot.getDouble("price");
                productPriceEditText.setText(price != null ? "RM " + String.format("%.2f", price) : "");

                String fieldCode = documentSnapshot.getString("item_code");
                itemCodeTextView.setText("Item Code: " + (fieldCode != null ? fieldCode : "N/A"));

                imageMap = (Map<String, String>) documentSnapshot.get("image");
                quantitiesData = (Map<String, Object>) documentSnapshot.get("quantities");

                if (quantitiesData != null) {
                    List<String> colorList = new ArrayList<>(quantitiesData.keySet());
                    ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, colorList);
                    colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerColor.setAdapter(colorAdapter);

                    spinnerColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String selectedColor = colorList.get(position);
                            Map<String, Object> sizes = (Map<String, Object>) quantitiesData.get(selectedColor);
                            List<String> sizeList = Arrays.asList("S", "M", "L", "XL", "XXL");

                            ArrayAdapter<String> sizeAdapter = new ArrayAdapter<>(InventoryActivity.this, android.R.layout.simple_spinner_item, sizeList);
                            sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerSize.setAdapter(sizeAdapter);

                            if (imageMap != null && imageMap.containsKey(selectedColor)) {
                                Glide.with(InventoryActivity.this)
                                        .load(imageMap.get(selectedColor))
                                        .into(inventoryProductImage);
                            } else {
                                inventoryProductImage.setImageDrawable(null);
                            }

                            if (!sizeList.isEmpty()) {
                                String firstSize = sizeList.get(0);
                                Object quantity = sizes.get(firstSize);
                                editQuantity.setText(String.valueOf(((Long) quantity).intValue()));
                            }

                            spinnerSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    String selectedSize = sizeList.get(position);
                                    Object quantity = sizes.get(selectedSize);
                                    editQuantity.setText(String.valueOf(((Long) quantity).intValue()));
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {}
                            });
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                }

            } else {
                Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to load product: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void logInventoryUpdate(String code, String name, String color, String size, int quantity) {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String updatedBy = prefs.getString("user_id", "unknown");

        Map<String, Object> log = new HashMap<>();
        log.put("timestamp", Timestamp.now());
        log.put("action", "update_quantity");  // or "add_item" depending on context
        log.put("item_id", code);
        log.put("manager_id", updatedBy);

        // Create a custom doc ID using current time (or use UUID.randomUUID().toString() if preferred)
        String docId = "log_" + System.currentTimeMillis();

        firestore.collection("ACTIVITY_LOG")
                .document(docId)
                .set(log)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Activity logged", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to log activity: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void generateQuantityInputs(Map<String, Object> quantities) {
        quantityContainer.removeAllViews();
        quantityInputs.clear();

        for (String color : quantities.keySet()) {
            Map<String, Object> sizes = (Map<String, Object>) quantities.get(color);

            TextView colorLabel = new TextView(this);
            colorLabel.setText("Color: " + color);
            quantityContainer.addView(colorLabel);

            Map<String, EditText> sizeInputMap = new HashMap<>();
            for (String size : sizes.keySet()) {
                int qty = ((Long) sizes.get(size)).intValue();

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);

                TextView sizeLabel = new TextView(this);
                sizeLabel.setText("Size: " + size);
                row.addView(sizeLabel);

                EditText qtyInput = new EditText(this);
                qtyInput.setText(String.valueOf(qty));
                qtyInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                row.addView(qtyInput);

                quantityContainer.addView(row);
                sizeInputMap.put(size, qtyInput);
            }

            quantityInputs.put(color, sizeInputMap);
        }
    }

    private void saveQuantitiesToFirestore() {
        Map<String, Object> newQuantities = new HashMap<>();
        for (String color : quantityInputs.keySet()) {
            Map<String, Object> sizeMap = new HashMap<>();
            for (String size : quantityInputs.get(color).keySet()) {
                EditText input = quantityInputs.get(color).get(size);
                String qtyStr = input.getText().toString().trim();
                int qty = qtyStr.isEmpty() ? 0 : Integer.parseInt(qtyStr);
                sizeMap.put(size, qty);
            }
            newQuantities.put(color, sizeMap);
        }

        firestore.collection("INVENTORY_ITEM").document(item_code)
                .update("quantities", newQuantities)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Quantities updated!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error updating quantities: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
