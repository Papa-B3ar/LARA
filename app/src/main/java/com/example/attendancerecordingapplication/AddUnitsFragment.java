package com.example.attendancerecordingapplication;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AddUnitsFragment extends Fragment{

    private EditText etUnitName, etUnitCode;
    private Button btnAddUnit;
    private ProgressBar progressBar;
    private String documentID;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_units, container, false);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        documentID = fAuth.getCurrentUser().getEmail();

        etUnitName = view.findViewById(R.id.etUnitName);
        etUnitCode = view.findViewById(R.id.etUnitCode);
        btnAddUnit = view.findViewById(R.id.btnAddUnit);
        progressBar = view.findViewById(R.id.progressBar);

        btnAddUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddUnit();
            }

            private void AddUnit() {
                String unitName = etUnitName.getText().toString().trim();
                String unitCode = etUnitCode.getText().toString().trim();

                if (unitName.isEmpty()){
                    etUnitName.setError("Unit name is required!");
                    etUnitName.requestFocus();
                    return;
                }
                if (unitCode.isEmpty()){
                        etUnitCode.setError("Unit code is required!");
                        etUnitCode.requestFocus();
                        return;
                }else{
                    addUnitsToDocument(documentID, unitName, unitCode);
                }
                progressBar.setVisibility(View.GONE);
            }
        });
        return view;
    }
    private void addUnitsToDocument(String documentID, String unitName, String unitCode) {
        unitName = capitalizeFirstLetter(unitName);
        unitCode = capitalizeFirstLetter(unitCode);

        Map<String, Object> unitMap = new HashMap<>();
        unitMap.put("Unit Name", unitName);
        unitMap.put("Unit Code", unitCode);

        fStore.collection("lecturer").document(documentID)
                .update("units", FieldValue.arrayUnion(unitMap))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Unit added successfully", Toast.LENGTH_SHORT).show();
                        etUnitName.setText("");
                        etUnitCode.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Unit addition failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private String capitalizeFirstLetter(String string) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] words = string.split("\\s");
        for (String word : words) {
            if (!word.isEmpty()) {
                stringBuilder.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    stringBuilder.append(word.substring(1).toLowerCase());
                }
                stringBuilder.append(" ");
            }
        }
        return stringBuilder.toString().trim();
    }
}
