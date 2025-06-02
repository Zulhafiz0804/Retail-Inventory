package com.example.inventoryapps;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class InventoryStatusActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private InventoryStatusAdapter adapter;
    private FirebaseFirestore firestore;
    private boolean isAscending = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_inventory_status, findViewById(R.id.content_frame), true);

        recyclerView = findViewById(R.id.recyclerViewInventory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new InventoryStatusAdapter();
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        loadInventoryStatus();
    }

    private void loadInventoryStatus() {
        firestore.collection("INVENTORY_ITEM").get().addOnSuccessListener(querySnapshot -> {
            List<ItemStatus> statusList = new ArrayList<>();
            for (DocumentSnapshot doc : querySnapshot) {
                String name = doc.getString("item_name");
                String code = doc.getString("item_code");
                Map<String, Object> quantities = (Map<String, Object>) doc.get("quantities");

                if (quantities != null) {
                    for (String color : quantities.keySet()) {
                        Map<String, Object> sizes = (Map<String, Object>) quantities.get(color);
                        for (String size : sizes.keySet()) {
                            long qty = (long) sizes.get(size);
                            statusList.add(new ItemStatus(name, code, color, size, qty));
                        }
                    }
                }
            }
            adapter.setItems(statusList);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.inventory_status_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_toggle_sort) {
            if (isAscending) {
                adapter.sortByStockLevelDescending();
                item.setIcon(R.drawable.descending); // Optional: change icon
            } else {
                adapter.sortByStockLevel();
                item.setIcon(R.drawable.ascending); // Optional: change icon
            }
            isAscending = !isAscending;
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    static class ItemStatus {
        String name, code, color, size;
        long quantity;

        public ItemStatus(String name, String code, String color, String size, long quantity) {
            this.name = name;
            this.code = code;
            this.color = color;
            this.size = size;
            this.quantity = quantity;
        }
    }

    static class InventoryStatusAdapter extends RecyclerView.Adapter<InventoryStatusAdapter.ViewHolder> {
        private List<ItemStatus> items = new ArrayList<>();
        private Map<String, Long> totalMap = new java.util.HashMap<>();

        public void setItems(List<ItemStatus> items) {
            this.items = items;

            totalMap.clear();
            for (ItemStatus item : items) {
                String key = item.code + "_" + item.color;
                long current = totalMap.getOrDefault(key, 0L);
                totalMap.put(key, current + item.quantity);
            }

            notifyDataSetChanged();
        }

        // Ascending: Understock to Overstock
        public void sortByStockLevel() {
            int MAX_PER_SIZE = 100;
            Collections.sort(items, Comparator.comparingDouble(a -> (a.quantity * 100.0) / MAX_PER_SIZE));
            notifyDataSetChanged();
        }

        // Descending: Overstock to Understock
        public void sortByStockLevelDescending() {
            int MAX_PER_SIZE = 100;
            Collections.sort(items, (a, b) -> Double.compare(
                    (b.quantity * 100.0) / MAX_PER_SIZE,
                    (a.quantity * 100.0) / MAX_PER_SIZE
            ));
            notifyDataSetChanged();
        }


        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textName, textCode, textColorSize, textQuantity, textRestockHint;

            public ViewHolder(View itemView) {
                super(itemView);
                textName = itemView.findViewById(R.id.textName);
                textCode = itemView.findViewById(R.id.textCode);
                textColorSize = itemView.findViewById(R.id.textColorSize);
                textQuantity = itemView.findViewById(R.id.textQuantity);
                textRestockHint = itemView.findViewById(R.id.textRestockHint);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory_status, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ItemStatus item = items.get(position);
            int MAX_PER_SIZE = 100;
            double percent = (item.quantity * 100.0) / MAX_PER_SIZE;

            holder.textName.setText(item.name);
            holder.textCode.setText("Code: " + item.code);
            holder.textColorSize.setText("Color: " + item.color + " | Size: " + item.size);
            holder.textQuantity.setText("Qty: " + item.quantity + " (" + String.format("%.1f", percent) + "%)");

            int color;
            if (percent < 30) {
                color = android.graphics.Color.RED;
            } else if (percent < 70) {
                color = android.graphics.Color.rgb(255, 165, 0); // Orange-Yellow
            } else {
                color = android.graphics.Color.GREEN;
            }
            holder.textQuantity.setTextColor(color);

            // Restock hint
            if (percent < 30) {
                holder.textRestockHint.setVisibility(View.VISIBLE);
            } else {
                holder.textRestockHint.setVisibility(View.GONE);
            }

        }

            @Override
        public int getItemCount() {
            return items.size();
        }
    }
}
