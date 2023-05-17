package com.example.attendancerecordingapplication;

import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecordAttendanceFragment extends Fragment implements UnitListAdapter.OnItemClickListener {

    private RecyclerView mRecyclerView;
    private UnitListAdapter mAdapter;
    private FirebaseFirestore fStore;
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record_attendance, container, false);

        fStore = FirebaseFirestore.getInstance();
        FirebaseAuth fAuth = FirebaseAuth.getInstance();

        mRecyclerView = view.findViewById(R.id.recyclerView2);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);

        String documentID = fAuth.getCurrentUser().getEmail();
        fStore.collection("lecturer").document(documentID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        List<Map<String, String>> unitsList = (List<Map<String, String>>) documentSnapshot.get("units");
                        mAdapter = new UnitListAdapter(unitsList, getContext());
                        mAdapter.setOnItemClickListener(RecordAttendanceFragment.this);
                        mRecyclerView.setAdapter(mAdapter);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to retrieve units", Toast.LENGTH_SHORT).show();
                    }
                });
        return view;
    }
        @Override
        public void onItemClick(Map<String, String> unitData) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Capture Attendance");
        builder.setMessage("Do you want to start capturing attendance for " + unitData.get("Unit Name") + " (" + unitData.get("Unit Code") + ")? \n\n Note that if you had done so earlier today that data will be overwritten!");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null) {
                                        LatLng center;
                                        float radius = 0.003f;
                                        center = new LatLng(location.getLatitude(), location.getLongitude());
                                        GeoPoint geoPoint = new GeoPoint(center.latitude, center.longitude);

                                        Geofence geofence = new Geofence.Builder()
                                                .setRequestId("geofence_id")
                                                .setCircularRegion(center.latitude, center.longitude, radius)
                                                .setExpirationDuration(15 * 60 * 1000)
                                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                                                .build();

                                        String unitCode = unitData.get("Unit Code");
                                        String unitName = unitData.get("Unit Name");

                                        fStore = FirebaseFirestore.getInstance();
                                        FirebaseAuth fAuth = FirebaseAuth.getInstance();

                                        String documentID = fAuth.getCurrentUser().getEmail();
                                        fStore.collection("lecturer").document(documentID)
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        String lecturerName = documentSnapshot.getString("Name");
                                                        String docName = lecturerName + " - " + unitData.get("Unit Name") + " - " + getCurrentDate();
                                                        Map<String, Object> attendanceData = new HashMap<>();

                                                        attendanceData.put("Unit Name", unitData.get("Unit Name"));
                                                        attendanceData.put("Unit Code", unitData.get("Unit Code"));
                                                        attendanceData.put("Lecturer Name", lecturerName);
                                                        attendanceData.put("Date", new Date());
                                                        attendanceData.put("Time", new Date());
                                                        attendanceData.put("Location", location.getLatitude() + "," + location.getLongitude());
                                                        attendanceData.put("Geofence", geofence.toString());
                                                        attendanceData.put("Center", geoPoint);
                                                        attendanceData.put("Radius", radius);

                                                        fStore.collection("attendance sheets").document(docName)
                                                                .set(attendanceData)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Toast.makeText(getContext(),"Sign Sheet created!",Toast.LENGTH_LONG).show();
                                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                                        builder.setTitle("Sign Sheet created");
                                                                        builder.setMessage("Give these details to your students: \n\n Lecturer name: " + lecturerName + "\n Unit name: " + unitData.get("Unit Name") + "\n\n Attendance recording will be available for 15 minutes only.\n\n Proceed to view attendance tab and select the above unit to view attendance.");
                                                                        builder.setPositiveButton("OK", null);
                                                                        builder.show();
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(getContext(),"Failed to create Sign Sheet!",Toast.LENGTH_LONG).show();
                                                                    }
                                                                });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getContext(), "Failed to retrieve your details!", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                        } else {
                                        Toast.makeText(getContext(),"Failed to get location!",Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(getContext(), "Location permission is required to capture attendance", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                }
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
