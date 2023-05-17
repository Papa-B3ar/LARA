package com.example.attendancerecordingapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class UnitsAdapter extends RecyclerView.Adapter<UnitsAdapter.UnitViewHolder> {

    private List<Map<String, String>> mUnitsList;
    private FirebaseAuth fAuth;
    ImageView deleteUnit;
    String documentID;

    public UnitsAdapter(List<Map<String, String>> unitsList, Context context) {
        mUnitsList = unitsList != null ? unitsList : new ArrayList<>();
    }
    public Map<String, String> getUnit(int position) {
        return mUnitsList.get(position);
    }

    public interface OnUnitClickListener {
        void onUnitClick(int position);
    }
    @NonNull
    @Override
    public UnitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);

        fAuth = FirebaseAuth.getInstance();
        documentID = fAuth.getCurrentUser().getEmail();

        deleteUnit = itemView.findViewById(R.id.ivDeleteUnit);
        return new UnitViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(@NonNull UnitViewHolder holder, int position) {
        Map<String, String> unitData = mUnitsList.get(position);

        String unitName = unitData.get("Unit Name");
        String unitCode = unitData.get("Unit Code");

        holder.mUnitNameTextView.setText(unitName);
        holder.mUnitCodeTextView.setText(unitCode);

        deleteUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Delete Unit");
                builder.setMessage("Are you sure you want to delete " + unitName + " (" + unitCode + ")?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        DocumentReference docRef = db.collection("lecturer").document(documentID);
                        docRef.update("units", FieldValue.arrayRemove(unitData))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(v.getContext(), "Unit deleted successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(v.getContext(), "Failed to delete unit", Toast.LENGTH_SHORT).show();
                                    }});
                    }});
                builder.setNegativeButton("No", null);
                builder.show();
            }
        });
    }
    public Map<String, String> get(int position) {
        return mUnitsList.get(position);
    }

    public static class UnitViewHolder extends RecyclerView.ViewHolder {
        public TextView mUnitNameTextView;
        public TextView mUnitCodeTextView;

        public UnitViewHolder(View itemView) {
            super(itemView);
            mUnitNameTextView = itemView.findViewById(R.id.tvUnitName);
            mUnitCodeTextView = itemView.findViewById(R.id.tvUnitCode);
        }
    }
    @Override
    public int getItemCount() {
        return mUnitsList != null ? mUnitsList.size() : 0;
    }
}
