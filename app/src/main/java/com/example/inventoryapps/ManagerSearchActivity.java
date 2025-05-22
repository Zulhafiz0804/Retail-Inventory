package com.example.inventoryapps;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ManagerSearchActivity extends BaseActivity {

    private ListView listView;
    private EditText searchBar;
    private Button btnSearch;
    private ArrayList<String> productList = new ArrayList<>();
    private ArrayList<String> productCodes = new ArrayList<>();
    private ArrayList<String> filteredList = new ArrayList<>();
    private ArrayList<String> filteredCodes = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.manager_search, findViewById(R.id.content_frame), true);

        setTitle("Search Products");

        listView = findViewById(R.id.listViewProducts);
        searchBar = findViewById(R.id.searchBar);
        btnSearch = findViewById(R.id.btnSearch);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filteredList);
        listView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();

        // Fetch data from firestore
        firestore.collection("INVENTORY_ITEM").get().addOnSuccessListener(querySnapshot -> {
            productList.clear();
            productCodes.clear();
            filteredList.clear();
            filteredCodes.clear();

            for (QueryDocumentSnapshot document : querySnapshot) {
                String code = document.getId();
                String name = document.getString("item_name");
                String entry = name + " (" + code + ")";
                productList.add(entry);
                productCodes.add(code);
            }

            filteredList.addAll(productList);
            filteredCodes.addAll(productCodes);
            adapter.notifyDataSetChanged();
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filteredList.clear();
                filteredCodes.clear();

                String keyword = s.toString().toLowerCase();
                for (int i = 0; i < productList.size(); i++) {
                    if (productList.get(i).toLowerCase().contains(keyword)) {
                        filteredList.add(productList.get(i));
                        filteredCodes.add(productCodes.get(i));
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });

        listView.setOnItemClickListener((adapterView, view, position, id) -> {
            String selectedCode = filteredCodes.get(position);
            Intent intent = new Intent(ManagerSearchActivity.this, InventoryActivity.class);
            intent.putExtra("productCode", selectedCode);
            startActivity(intent);
        });
        btnSearch.setOnClickListener(v -> {
            String query = searchBar.getText().toString().trim();
            if (!query.isEmpty()) {
                firestore.collection("INVENTORY_ITEM").document(query).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                // Product exists, open InventoryActivity
                                Intent intent = new Intent(ManagerSearchActivity.this, InventoryActivity.class);
                                intent.putExtra("productCode", query);
                                startActivity(intent);
                            } else {
                                // Product does not exist, show a toast
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
    }}
