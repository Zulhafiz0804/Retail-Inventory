package com.example.inventoryapps;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ManagerSearchActivity extends BaseActivity {

    private ListView listView;
    private EditText searchBar;
    private ImageView btnSearchIcon;
    private ImageView btnFilterIcon;

    private ArrayList<String> productList = new ArrayList<>();
    private ArrayList<String> productCodes = new ArrayList<>();
    private ArrayList<String> productCategoryIds = new ArrayList<>();
    private ArrayList<String> filteredList = new ArrayList<>();
    private ArrayList<String> filteredCodes = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private FirebaseFirestore firestore;
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.manager_search, findViewById(R.id.content_frame), true);

        setTitle("Search Products");

        listView = findViewById(R.id.listViewProducts);
        searchBar = findViewById(R.id.searchBar);
        btnSearchIcon = findViewById(R.id.btnSearchIcon);
        btnFilterIcon = findViewById(R.id.btnFilterIcon);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filteredList);
        listView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();

        firestore.collection("INVENTORY_ITEM").get().addOnSuccessListener(querySnapshot -> {
            productList.clear();
            productCodes.clear();
            productCategoryIds.clear();

            for (QueryDocumentSnapshot document : querySnapshot) {
                String code = document.getId();
                String name = document.getString("item_name");
                String categoryId = document.getString("category_id");

                String entry = name + " (" + code + ")";
                productList.add(entry);
                productCodes.add(code);
                productCategoryIds.add(categoryId != null ? categoryId : "unknown");
            }

            applyFilter(currentFilter);
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().toLowerCase();
                filterByText(keyword);
            }
        });

        btnSearchIcon.setOnClickListener(v -> {
            String query = searchBar.getText().toString().trim();
            if (!query.isEmpty()) {
                firestore.collection("INVENTORY_ITEM").document(query).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                Intent intent = new Intent(ManagerSearchActivity.this, InventoryActivity.class);
                                intent.putExtra("productCode", query);
                                startActivity(intent);
                            } else {
                                Toast.makeText(ManagerSearchActivity.this, "Item not found", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ManagerSearchActivity.this, "Error checking item", Toast.LENGTH_SHORT).show();
                        });
            } else {
                searchBar.setError("Please enter a product code");
            }
        });

        btnFilterIcon.setOnClickListener(this::showFilterMenu);

        listView.setOnItemClickListener((adapterView, view, position, id) -> {
            String selectedCode = filteredCodes.get(position);
            Intent intent = new Intent(ManagerSearchActivity.this, InventoryActivity.class);
            intent.putExtra("productCode", selectedCode);
            startActivity(intent);
        });
    }

    private void showFilterMenu(View v) {
        PopupMenu popup = new PopupMenu(ManagerSearchActivity.this, v);
        popup.getMenu().add("All");
        popup.getMenu().add("Male");
        popup.getMenu().add("Female");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString().toLowerCase();
            applyFilter(title);
            return true;
        });

        popup.show();
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        filteredList.clear();
        filteredCodes.clear();

        String categoryFilter = null;
        if (filter.equals("male")) categoryFilter = "category01";
        else if (filter.equals("female")) categoryFilter = "category02";

        for (int i = 0; i < productList.size(); i++) {
            String catId = productCategoryIds.get(i);
            boolean match = filter.equals("all") || catId.equals(categoryFilter);
            if (match) {
                filteredList.add(productList.get(i));
                filteredCodes.add(productCodes.get(i));
            }
        }

        filterByText(searchBar.getText().toString().toLowerCase());
    }

    private void filterByText(String keyword) {
        ArrayList<String> tempList = new ArrayList<>();
        ArrayList<String> tempCodes = new ArrayList<>();

        for (int i = 0; i < filteredList.size(); i++) {
            if (filteredList.get(i).toLowerCase().contains(keyword)) {
                tempList.add(filteredList.get(i));
                tempCodes.add(filteredCodes.get(i));
            }
        }

        adapter.clear();
        adapter.addAll(tempList);
        adapter.notifyDataSetChanged();

        filteredCodes = tempCodes;
    }
}
