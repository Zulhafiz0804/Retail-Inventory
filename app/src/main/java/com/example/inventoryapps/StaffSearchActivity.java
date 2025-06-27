package com.example.inventoryapps;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class StaffSearchActivity extends BaseActivity {

    private EditText etSearch;
    private ImageView ivSearchIcon, ivFilterIcon;
    private RecyclerView recentSearchesList;
    private FirebaseFirestore firestore;

    private List<String> allSearchItems = new ArrayList<>();
    private List<String> allCategoryIds = new ArrayList<>();
    private List<String> filteredSearchItems = new ArrayList<>();
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.staff_search, findViewById(R.id.content_frame));

        setTitle("Search Products");

        etSearch = findViewById(R.id.etSearch);
        ivSearchIcon = findViewById(R.id.ivSearchIcon);
        ivFilterIcon = findViewById(R.id.ivFilterIcon);
        recentSearchesList = findViewById(R.id.recentSearchesList);

        firestore = FirebaseFirestore.getInstance();

        ivSearchIcon.setOnClickListener(v -> handleSearch());
        ivFilterIcon.setOnClickListener(this::showFilterMenu);

        preloadInventoryItems();
    }

    private void handleSearch() {
        String code = etSearch.getText().toString().trim().toLowerCase();
        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, "Please enter product code", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("INVENTORY_ITEM")
                .whereEqualTo("item_code", code)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot snapshot = task.getResult();
                        if (!snapshot.isEmpty()) {
                            DocumentSnapshot doc = snapshot.getDocuments().get(0);
                            String productId = doc.getId();

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

    private void preloadInventoryItems() {
        firestore.collection("INVENTORY_ITEM").get().addOnSuccessListener(snapshot -> {
            allSearchItems.clear();
            allCategoryIds.clear();

            for (QueryDocumentSnapshot doc : snapshot) {
                String itemCode = doc.getString("item_code");
                String categoryId = doc.getString("category_id");

                allSearchItems.add(itemCode != null ? itemCode.toLowerCase() : "");
                allCategoryIds.add(categoryId != null ? categoryId : "unknown");
            }

            applyFilter(currentFilter);
        });
    }

    private void applyFilter(String filter) {
        currentFilter = filter.toLowerCase();
        filteredSearchItems.clear();

        String filterCat = null;
        if (currentFilter.equals("men")) filterCat = "category01";
        else if (currentFilter.equals("women")) filterCat = "category02";

        for (int i = 0; i < allSearchItems.size(); i++) {
            String cat = allCategoryIds.get(i);
            boolean match = currentFilter.equals("all") || cat.equals(filterCat);
            if (match) {
                filteredSearchItems.add(allSearchItems.get(i));
            }
        }

        loadRecentSearches();
    }

    private void showFilterMenu(View view) {
        PopupMenu popup = new PopupMenu(StaffSearchActivity.this, view);
        popup.getMenu().add("All");
        popup.getMenu().add("Men");
        popup.getMenu().add("Women");

        popup.setOnMenuItemClickListener(item -> {
            applyFilter(item.getTitle().toString().toLowerCase());
            Toast.makeText(this, "Filter: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            return true;
        });

        popup.show();
    }

    private void saveRecentSearch(String code) {
        SharedPreferences prefs = getSharedPreferences("search_prefs", MODE_PRIVATE);
        Set<String> set = prefs.getStringSet("recent_searches", new LinkedHashSet<>());
        Set<String> newSet = new LinkedHashSet<>(set);

        newSet.remove(code);
        newSet.add(code);

        while (newSet.size() > 10) {
            String first = newSet.iterator().next();
            newSet.remove(first);
        }

        prefs.edit().putStringSet("recent_searches", newSet).apply();
        loadRecentSearches();
    }

    private void loadRecentSearches() {
        SharedPreferences prefs = getSharedPreferences("search_prefs", MODE_PRIVATE);
        Set<String> set = prefs.getStringSet("recent_searches", new LinkedHashSet<>());
        List<String> rawList = new ArrayList<>(set);

        List<String> finalList = new ArrayList<>();

        if (currentFilter.equals("all")) {
            finalList.addAll(rawList);
        } else {
            String filterCat = currentFilter.equals("men") ? "category01" : "category02";

            for (String code : rawList) {
                for (int i = 0; i < allSearchItems.size(); i++) {
                    if (allSearchItems.get(i).equalsIgnoreCase(code)
                            && allCategoryIds.get(i).equals(filterCat)) {
                        finalList.add(code);
                        break;
                    }
                }
            }
        }

        RecentSearchAdapter adapter = new RecentSearchAdapter(this, finalList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recentSearchesList.setLayoutManager(layoutManager);
        recentSearchesList.setAdapter(adapter);
    }
}
