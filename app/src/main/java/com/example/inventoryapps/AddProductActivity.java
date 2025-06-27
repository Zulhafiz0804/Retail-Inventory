package com.example.inventoryapps;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class AddProductActivity extends BaseActivity {

    private EditText editItemName, editItemDescription, editItemPrice, editItemCode;
    private LinearLayout colorContainer;
    private Button btnAddColor, btnSaveProduct;
    private Spinner spinnerCategory;

    private FirebaseFirestore firestore;

    // For category spinner
    private List<String> categoryNames = new ArrayList<>();
    private Map<String, String> categoryMap = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_add_product, findViewById(R.id.content_frame), true);

        setTitle("Add Item");

        firestore = FirebaseFirestore.getInstance();

        editItemName = findViewById(R.id.editItemName);
        editItemDescription = findViewById(R.id.editItemDescription);
        editItemPrice = findViewById(R.id.editItemPrice);
        editItemCode = findViewById(R.id.editItemCode);
        colorContainer = findViewById(R.id.colorContainer);
        btnAddColor = findViewById(R.id.btnAddColor);
        btnSaveProduct = findViewById(R.id.btnSaveProduct);
        spinnerCategory = findViewById(R.id.spinnerCategory); // Ensure spinner exists in XML

        btnAddColor.setOnClickListener(v -> addColorEntry());

        btnSaveProduct.setOnClickListener(v -> saveProductToFirestore());

        addColorEntry(); // Add default one

        loadCategories(); // <-- Load categories into spinner
    }

    private void loadCategories() {
        FirebaseFirestore.getInstance().collection("PRODUCT_CATEGORY")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getString("category_id");
                        String name = doc.getString("category_name");
                        categoryMap.put(name, id);
                        categoryNames.add(name);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_dropdown_item, categoryNames);
                    spinnerCategory.setAdapter(adapter);

                    // 🟨 ADD SPINNER LISTENER TO SHOW DESCRIPTION
                    spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String selectedCategoryName = categoryNames.get(position);
                            String selectedCategoryId = categoryMap.get(selectedCategoryName);

                            FirebaseFirestore.getInstance().collection("PRODUCT_CATEGORY")
                                    .whereEqualTo("category_id", selectedCategoryId)
                                    .limit(1)
                                    .get()
                                    .addOnSuccessListener(docs -> {
                                        if (!docs.isEmpty()) {
                                            String description = docs.getDocuments().get(0).getString("description");
                                            TextView descriptionText = findViewById(R.id.categoryDescription);
                                            descriptionText.setText(description != null ? description : "No description");
                                        }
                                    });
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load categories", Toast.LENGTH_SHORT).show();
                });
    }

    private void addColorEntry() {
        View colorEntryView = LayoutInflater.from(this).inflate(R.layout.color_entry, colorContainer, false);

        Button btnPaste = colorEntryView.findViewById(R.id.btnPasteImageUrl);
        EditText imageUrlField = colorEntryView.findViewById(R.id.editImageUrl);

        btnPaste.setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                android.content.ClipData clip = clipboard.getPrimaryClip();
                if (clip != null && clip.getItemCount() > 0) {
                    String pastedText = clip.getItemAt(0).coerceToText(this).toString();
                    imageUrlField.setText(pastedText);
                }
            }
        });

        colorContainer.addView(colorEntryView);
    }

    private void saveProductToFirestore() {
        String name = editItemName.getText().toString().trim();
        String desc = editItemDescription.getText().toString().trim();
        String code = editItemCode.getText().toString().trim();
        String priceStr = editItemPrice.getText().toString().trim();
        String selectedCategory = (String) spinnerCategory.getSelectedItem();
        String categoryId = categoryMap.get(selectedCategory);

        if (name.isEmpty() || desc.isEmpty() || code.isEmpty() || priceStr.isEmpty() || categoryId == null) {
            Toast.makeText(this, "Fill all main product fields including category", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);

        Map<String, String> imageMap = new HashMap<>();
        Map<String, Map<String, Integer>> quantitiesMap = new HashMap<>();

        for (int i = 0; i < colorContainer.getChildCount(); i++) {
            View colorView = colorContainer.getChildAt(i);
            String color = ((EditText) colorView.findViewById(R.id.editColorName)).getText().toString().trim();
            String imageUrl = ((EditText) colorView.findViewById(R.id.editImageUrl)).getText().toString().trim();

            if (color.isEmpty() || imageUrl.isEmpty()) {
                Toast.makeText(this, "Fill all fields in color entry " + (i + 1), Toast.LENGTH_SHORT).show();
                return;
            }

            int s = parseQty(colorView.findViewById(R.id.editSizeS));
            int m = parseQty(colorView.findViewById(R.id.editSizeM));
            int l = parseQty(colorView.findViewById(R.id.editSizeL));
            int xl = parseQty(colorView.findViewById(R.id.editSizeXL));
            int xxl = parseQty(colorView.findViewById(R.id.editSizeXXL));

            Map<String, Integer> sizeMap = new HashMap<>();
            sizeMap.put("S", s);
            sizeMap.put("M", m);
            sizeMap.put("L", l);
            sizeMap.put("XL", xl);
            sizeMap.put("XXL", xxl);

            imageMap.put(color, imageUrl);
            quantitiesMap.put(color, sizeMap);
        }

        Map<String, Object> productData = new HashMap<>();
        productData.put("item_name", name);
        productData.put("item_description", desc);
        productData.put("item_code", code);
        productData.put("price", price);
        productData.put("image", imageMap);
        productData.put("quantities", quantitiesMap);
        productData.put("category_id", categoryId); // Add category ID

        firestore.collection("INVENTORY_ITEM").document(code)
                .set(productData)
                .addOnSuccessListener(unused -> Toast.makeText(this, "Product saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private int parseQty(EditText editText) {
        String text = editText.getText().toString().trim();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }
}
