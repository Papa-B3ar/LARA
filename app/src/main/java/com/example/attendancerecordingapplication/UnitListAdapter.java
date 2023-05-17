package com.example.attendancerecordingapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class UnitListAdapter extends RecyclerView.Adapter<UnitListAdapter.UnitViewHolder> {

    private List<Map<String, String>> mUnitsList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Map<String, String> unitData);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public UnitListAdapter(List<Map<String, String>> unitsList, Context context) {
        mUnitsList = unitsList != null ? unitsList : new ArrayList<>();
    }

    public Map<String, String> getUnit(int position) {
        return mUnitsList.get(position);
    }

    @NonNull
    @Override
    public UnitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.unit, parent, false);

        return new UnitViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UnitViewHolder holder, int position) {
        Map<String, String> unitData = mUnitsList.get(position);

        String unitName = unitData.get("Unit Name");
        String unitCode = unitData.get("Unit Code");

        holder.mUnitNameTextView.setText(unitName);
        holder.mUnitCodeTextView.setText(unitCode);
    }

    public Map<String, String> get(int position) {
        return mUnitsList.get(position);
    }

    public class UnitViewHolder extends RecyclerView.ViewHolder {
        public TextView mUnitNameTextView;
        public TextView mUnitCodeTextView;

        public UnitViewHolder(View itemView) {
            super(itemView);
            mUnitNameTextView = itemView.findViewById(R.id.tvUnitName);
            mUnitCodeTextView = itemView.findViewById(R.id.tvUnitCode);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(mUnitsList.get(position));
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mUnitsList != null ? mUnitsList.size() : 0;
    }
}