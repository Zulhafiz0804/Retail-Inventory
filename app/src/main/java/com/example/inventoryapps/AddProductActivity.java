package com.example.inventoryapps;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.util.*;

public class AddProductActivity extends BaseActivity {

    private EditText editItemName, editItemDescription, editItemPrice, editItemCode;
    private LinearLayout colorContainer;
    private Button btnAddColor, btnSaveProduct;
    private Spinner spinnerCategory;

    private FirebaseFirestore firestore;
    private List<String> categoryNames = new ArrayList<>();
    private Map<String, String> categoryMap = new HashMap<>();

    private static final int PICK_IMAGE_REQUEST = 101;
    private View currentColorEntryForImage;

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
        spinnerCategory = findViewById(R.id.spinnerCategory);

        btnAddColor.setOnClickListener(v -> addColorEntry());
        btnSaveProduct.setOnClickListener(v -> saveProductToFirestore());

        addColorEntry();
        loadCategories();
    }

    private void loadCategories() {
        firestore.collection("PRODUCT_CATEGORY")
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

                    spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String selectedCategoryName = categoryNames.get(position);
                            String selectedCategoryId = categoryMap.get(selectedCategoryName);

                            firestore.collection("PRODUCT_CATEGORY")
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
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load categories", Toast.LENGTH_SHORT).show());
    }

    private void addColorEntry() {
        View colorEntryView = LayoutInflater.from(this).inflate(R.layout.color_entry, colorContainer, false);

        Button btnSelectImage = colorEntryView.findViewById(R.id.btnSelectImage);
        ImageView imgPreview = colorEntryView.findViewById(R.id.imgPreview);

        btnSelectImage.setOnClickListener(v -> {
            currentColorEntryForImage = colorEntryView;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        colorContainer.addView(colorEntryView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            if (currentColorEntryForImage != null) {
                ImageView imgPreview = currentColorEntryForImage.findViewById(R.id.imgPreview);
                Glide.with(this).load(imageUri).into(imgPreview);
                currentColorEntryForImage.setTag(imageUri);
            }
        }
    }

    private void saveProductToFirestore() {
        String name = editItemName.getText().toString().trim();
        String desc = editItemDescription.getText().toString().trim();
        String code = editItemCode.getText().toString().trim();
        String priceStr = editItemPrice.getText().toString().trim();
        String selectedCategory = (String) spinnerCategory.getSelectedItem();
        String categoryId = categoryMap.get(selectedCategory);

        if (name.isEmpty() || desc.isEmpty() || code.isEmpty() || priceStr.isEmpty() || categoryId == null) {
            Toast.makeText(this, "Fill all product fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);

        SharedPreferences prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE);
        String managerId = prefs.getString("user_id", null);
        String role = prefs.getString("user_role", null);

        if (managerId == null || !"manager".equals(role)) {
            Toast.makeText(this, "Not authorized", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Map<String, Integer>> quantitiesMap = new HashMap<>();
        Map<String, String> imageMap = new HashMap<>();
        int total = colorContainer.getChildCount();
        int[] uploadedCount = {0};

        for (int i = 0; i < total; i++) {
            View colorView = colorContainer.getChildAt(i);
            String color = ((EditText) colorView.findViewById(R.id.editColorName)).getText().toString().trim();
            Uri imageUri = (Uri) colorView.getTag();

            if (color.isEmpty() || imageUri == null) {
                Toast.makeText(this, "Fill color & image for entry " + (i + 1), Toast.LENGTH_SHORT).show();
                return;
            }

            int s = parseQty(colorView.findViewById(R.id.editSizeS));
            int m = parseQty(colorView.findViewById(R.id.editSizeM));
            int l = parseQty(colorView.findViewById(R.id.editSizeL));
            int xl = parseQty(colorView.findViewById(R.id.editSizeXL));
            int xxl = parseQty(colorView.findViewById(R.id.editSizeXXL));

            Map<String, Integer> sizeMap = new HashMap<>();
            sizeMap.put("S", s); sizeMap.put("M", m); sizeMap.put("L", l);
            sizeMap.put("XL", xl); sizeMap.put("XXL", xxl);
            quantitiesMap.put(color, sizeMap);

            String filename = code + "_" + color + ".jpg";
            StorageReference ref = FirebaseStorage.getInstance().getReference("product_images/" + filename);
            ref.putFile(imageUri).addOnSuccessListener(task -> {
                ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageMap.put(color, uri.toString());
                    uploadedCount[0]++;
                    if (uploadedCount[0] == total) {
                        saveFinalProduct(name, desc, code, price, categoryId, managerId, imageMap, quantitiesMap);
                    }
                });
            }).addOnFailureListener(e -> Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void saveFinalProduct(String name, String desc, String code, double price,
                                  String categoryId, String managerId,
                                  Map<String, String> imageMap,
                                  Map<String, Map<String, Integer>> quantitiesMap) {

        Map<String, Object> productData = new HashMap<>();
        productData.put("item_name", name);
        productData.put("item_description", desc);
        productData.put("item_code", code);
        productData.put("price", price);
        productData.put("image", imageMap);
        productData.put("quantities", quantitiesMap);
        productData.put("category_id", categoryId);
        productData.put("manager_id", managerId);

        Bitmap qrBitmap = generateQRCodeBitmap(code);
        if (qrBitmap == null) {
            Toast.makeText(this, "QR code generation failed", Toast.LENGTH_SHORT).show();
            return;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] qrData = baos.toByteArray();

        StorageReference qrRef = FirebaseStorage.getInstance().getReference("qr_codes/" + code + ".png");
        qrRef.putBytes(qrData).addOnSuccessListener(taskSnapshot -> {
            qrRef.getDownloadUrl().addOnSuccessListener(qrUri -> {
                productData.put("qr_url", qrUri.toString());

                firestore.collection("INVENTORY_ITEM").document(code)
                        .set(productData)
                        .addOnSuccessListener(unused -> Toast.makeText(this, "Product saved with QR!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            });
        }).addOnFailureListener(e -> Toast.makeText(this, "QR upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private Bitmap generateQRCodeBitmap(String text) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512);
            Bitmap bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565);
            for (int x = 0; x < 512; x++) {
                for (int y = 0; y < 512; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private int parseQty(EditText editText) {
        String text = editText.getText().toString().trim();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }
}
