package com.example.inventoryapps;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.StaffViewHolder> {

    private List<AddStaffActivity.Staff> staffList;
    private Context context;

    public StaffAdapter(List<AddStaffActivity.Staff> staffList, Context context) {
        this.staffList = staffList;
        this.context = context;
    }

    @NonNull
    @Override
    public StaffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staff, parent, false);
        return new StaffViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffViewHolder holder, int position) {
        AddStaffActivity.Staff staff = staffList.get(position);

        // Display data
        holder.tvUsername.setText(staff.username);
        holder.tvEmail.setText(staff.email);
        holder.tvRole.setText(staff.role);

        // Edit button action
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditStaffActivity.class);
            intent.putExtra("staffID", staff.staffID); // document ID
            intent.putExtra("username", staff.username);
            intent.putExtra("email", staff.email);
            intent.putExtra("role", staff.role);
            intent.putExtra("password", staff.password);
            intent.putExtra("phone_no", staff.phone);
            context.startActivity(intent);
        });

        // Delete button action
        holder.btnDelete.setOnClickListener(v -> {
            String collection = staff.role.equalsIgnoreCase("manager") ? "INVENTORY_MANAGER" : "STAFF";

            FirebaseFirestore.getInstance().collection(collection)
                    .document(staff.staffID) // Use document ID
                    .delete()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(context, "Deleted " + staff.username, Toast.LENGTH_SHORT).show();
                        staffList.remove(holder.getAdapterPosition());
                        notifyItemRemoved(holder.getAdapterPosition());
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Error deleting: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
    }

    @Override
    public int getItemCount() {
        return staffList.size();
    }

    public static class StaffViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvEmail, tvRole;
        Button btnEdit, btnDelete;

        public StaffViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvRole = itemView.findViewById(R.id.tvRole);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
