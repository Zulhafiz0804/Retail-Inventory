package com.example.inventoryapps;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class AddProductActivity extends BaseActivity {

    private EditText editItemName, editItemDescription, editItemPrice, editItemCode;
    private LinearLayout colorContainer;
    private Button btnAddColor, btnSaveProduct;

    private FirebaseFirestore firestore;

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

        btnAddColor.setOnClickListener(v -> addColorEntry());

        btnSaveProduct.setOnClickListener(v -> saveProductToFirestore());

        // Add default one
        addColorEntry();
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

        if (name.isEmpty() || desc.isEmpty() || code.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Fill all main product fields", Toast.LENGTH_SHORT).show();
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
