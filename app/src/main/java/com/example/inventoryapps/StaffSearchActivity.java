package com.example.inventoryapps;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import android.content.SharedPreferences;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class StaffSearchActivity extends BaseActivity {

    private EditText etSearch;
    private RecyclerView recentSearchesList, popularItemsList;
    private ImageView ivSearchIcon;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.staff_search, findViewById(R.id.content_frame));

        setTitle("Search Products");

        // Initialize views
        etSearch = findViewById(R.id.etSearch);
        recentSearchesList = findViewById(R.id.recentSearchesList);
        ivSearchIcon = findViewById(R.id.ivSearchIcon);

        // Firestore init
        firestore = FirebaseFirestore.getInstance();

        // Load recent searches
        loadRecentSearches();

        // Search events
        ivSearchIcon.setOnClickListener(v -> handleSearch());
    }

    private void handleSearch() {
        String code = etSearch.getText().toString().trim().toLowerCase(); // Lowercase for consistent matching
        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, "Please enter product code", Toast.LENGTH_SHORT).show();
            return;
        }
        searchProductByCode(code);
    }

    private void searchProductByCode(String code) {
        firestore.collection("INVENTORY_ITEM")
                .whereEqualTo("item_code", code)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot snapshot = task.getResult();
                        if (!snapshot.isEmpty()) {
                            DocumentSnapshot doc = snapshot.getDocuments().get(0); // ✅ CORRECT

                            String productId = doc.getId();

                            // ✅ Save search
                            saveRecentSearch(code);

                            Intent intent = new Intent(StaffSearchActivity.this, ProductDetailsActivity.class);
                            intent.putExtra("productId", productId);
                            startActivity(intent);
                            etSearch.setText("");
                        } else {
                            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error: " + (task.getException() != null ?
                                task.getException().getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void saveRecentSearch(String code) {
        SharedPreferences prefs = getSharedPreferences("search_prefs", MODE_PRIVATE);
        Set<String> set = prefs.getStringSet("recent_searches", new LinkedHashSet<>());
        Set<String> newSet = new LinkedHashSet<>(set);

        // Avoid duplicates
        newSet.remove(code);
        newSet.add(code);

        // Limit recent items to 10
        while (newSet.size() > 10) {
            String first = newSet.iterator().next();
            newSet.remove(first);
        }

        prefs.edit().putStringSet("recent_searches", newSet).apply();
        loadRecentSearches(); // Refresh list
    }

    // ✅ Add this method below
    private void loadRecentSearches() {
        SharedPreferences prefs = getSharedPreferences("search_prefs", MODE_PRIVATE);
        Set<String> set = prefs.getStringSet("recent_searches", new LinkedHashSet<>());
        List<String> list = new ArrayList<>(set);

        RecentSearchAdapter adapter = new RecentSearchAdapter(this, list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recentSearchesList.setLayoutManager(layoutManager);
        recentSearchesList.setAdapter(adapter);
    }
}
