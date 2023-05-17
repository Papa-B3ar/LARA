package com.example.attendancerecordingapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class SignSheetAdapter extends RecyclerView.Adapter<SignSheetAdapter.MyViewHolder> {
    private List<DocumentSnapshot> items;
    private OnItemClickListener listener;
    private FirebaseFirestore firestore;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvUnitName;
        public TextView tvRecordDate;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvUnitName = itemView.findViewById(R.id.tvUnitName);
            tvRecordDate = itemView.findViewById(R.id.tvRecordDate);
        }

        public void bind(final DocumentSnapshot item, final OnItemClickListener listener) {
            String name = item.getString("Name"); //get the name field from the document
            String date = item.getString("date"); //get the date field from the document
            String[] nameParts = name.split("_"); //split the name field using underscore as separator
            tvUnitName.setText(nameParts[0]); //set the first part as the unit name
            tvRecordDate.setText(date + " - " + nameParts[1]); //set the date and second part as the record date
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot item);
    }

    public SignSheetAdapter(ArrayList<DocumentSnapshot> documents, OnItemClickListener listener) {
        this.items = new ArrayList<>();
        this.listener = listener;
        this.firestore = FirebaseFirestore.getInstance();
        loadItems();
    }

    private void loadItems() {
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        FirebaseFirestore.getInstance().collection("lecturer")
                .document(currentUserEmail)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String lecturerName = documentSnapshot.getString("Name");
                            firestore.collection("attendance sheets")
                                    .whereEqualTo("LecturerName", lecturerName)
                                    .get()
                                    .addOnSuccessListener(querySnapshot -> {
                                        items.addAll(querySnapshot.getDocuments());
                                        notifyDataSetChanged();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle failure
                                    });
                        }
                    }
                });
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sign_sheet, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}