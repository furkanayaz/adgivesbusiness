package com.advertise.adgivesbusiness.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.core.location.LocationManagerCompat;
import androidx.fragment.app.Fragment;
import com.advertise.adgivesbusiness.R;
import com.advertise.adgivesbusiness.activities.MainActivity;
import com.advertise.adgivesbusiness.activities.MapViewActivity;
import com.advertise.adgivesbusiness.models.Ad;
import com.advertise.adgivesbusiness.models.Partner;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.ArrayList;
import maes.tech.intentanim.CustomIntent;

public class FragmentAdvertise extends Fragment {
    private TextInputEditText textInputEditTextAdvertiseIdentifier;
    private CardView cardViewChooseLocation,cardViewAdvertiseSelectAdVideo,cardViewAdvertiseSubmit;
    private TextView textViewSelectAdVideo;
    private ProgressDialog progressDialogAdvertise;
    private ProgressDialog progressDialogAdvertiseSuccessfully;
    private ArrayList<String> spinnerBudgetArrayList;
    private ArrayAdapter<String> spinnerBudgetArrayAdapter;
    private ArrayList<String> spinnerDistanceArrayList;
    private ArrayAdapter<String> spinnerDistanceArrayAdapter;
    private Spinner spinnerAdvertiseBudget;
    private Spinner spinnerAdvertiseDistanceRange;

    private int budget = 0;
    private Uri videoUri = null;
    private final int ALLOW_MULTIPLE_CODE = 1;
    private Partner partner = null;
    private int frommapview = 0;
    private double latitude = 0;
    private double longitude = 0;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_advertise,container,false);
        textInputEditTextAdvertiseIdentifier = view.findViewById(R.id.textInputEditTextAdvertiseIdentifier);
        cardViewChooseLocation = view.findViewById(R.id.cardViewChooseLocation);
        cardViewAdvertiseSelectAdVideo = view.findViewById(R.id.cardViewAdvertiseSelectAdVideo);
        textViewSelectAdVideo = view.findViewById(R.id.textViewSelectAdVideo);
        spinnerAdvertiseBudget = view.findViewById(R.id.spinnerAdvertiseBudget);
        spinnerAdvertiseDistanceRange = view.findViewById(R.id.spinnerAdvertiseDistanceRange);
        cardViewAdvertiseSubmit = view.findViewById(R.id.cardViewAdvertiseSubmit);

        Intent intent = getActivity().getIntent();
        frommapview = intent.getIntExtra("frommapview",0);
        if (frommapview == 1){
            latitude = intent.getDoubleExtra("latitude",0);
            longitude = intent.getDoubleExtra("longitude",0);
        }

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        progressDialogAdvertise = ProgressDialog.show(getContext(),"ADVERTISE","Please wait while your ads information is loaded",false,false);
        progressDialogAdvertise.dismiss();

        spinnerBudgetArrayList = new ArrayList<>();
        spinnerBudgetArrayList.add("Choose a budget");
        spinnerBudgetArrayList.add("$ 50");
        spinnerBudgetArrayList.add("$ 100");
        spinnerBudgetArrayList.add("$ 500");
        spinnerBudgetArrayList.add("$ 1000");

        spinnerBudgetArrayAdapter = new ArrayAdapter<String >(getContext(), android.R.layout.simple_list_item_1,android.R.id.text1,spinnerBudgetArrayList);

        spinnerAdvertiseBudget.setAdapter(spinnerBudgetArrayAdapter);

        spinnerAdvertiseBudget.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                switch (position) {
                    case 1:
                        budget = 50;
                        break;
                    case 2:
                        budget = 100;
                        break;
                    case 3:
                        budget = 500;
                        break;
                    case 4:
                        budget = 1000;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerDistanceArrayList = new ArrayList<>();

        spinnerDistanceArrayList.add("Choose a distance");
        spinnerDistanceArrayList.add("Local");
        spinnerDistanceArrayList.add("Statewide");
        spinnerDistanceArrayList.add("Nationwide");

        spinnerDistanceArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,android.R.id.text1, spinnerDistanceArrayList);

        spinnerAdvertiseDistanceRange.setAdapter(spinnerDistanceArrayAdapter);

        spinnerAdvertiseDistanceRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        cardViewChooseLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean locationcontroller = isLocationEnabled(getContext());

                if (locationcontroller){
                    Intent intent = new Intent(getActivity(), MapViewActivity.class);
                    startActivity(intent);
                    CustomIntent.customType(getContext(),"fadein-to-fadeout");
                    getActivity().finish();
                }else {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }

            }
        });

        cardViewAdvertiseSelectAdVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textViewSelectAdVideo.getText().toString().trim().equals("Select your ad video")){
                    pickVideoIntent();
                }else {
                    PopupMenu popupMenu = new PopupMenu(getContext(),v);
                    popupMenu.getMenuInflater().inflate(R.menu.selectvideo,popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getItemId() == R.id.action_showad){
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setCancelable(false);
                                builder.setTitle("SHOW AD");
                                View view = LayoutInflater.from(getContext()).inflate(R.layout.alertview_showad,null);
                                TextView showAdName = view.findViewById(R.id.textViewShowAdName);
                                TextView showAdSize = view.findViewById(R.id.textViewShowAdSize);
                                showAdName.setText(getFileName(videoUri));
                                showAdSize.setText(String.valueOf(getVideoSize(getContext(),videoUri))+" MB");
                                VideoView videoViewShowAd = view.findViewById(R.id.videoViewShowAd);
                                videoViewShowAd.setVideoURI(videoUri);
                                videoViewShowAd.start();
                                builder.setView(view);
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                                builder.create().show();

                            }
                            if (item.getItemId() == R.id.action_selectad){
                                pickVideoIntent();
                            }
                            return true;
                        }
                    });
                    popupMenu.show();


                }

            }
        });

        cardViewAdvertiseSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseFirestore.collection("partners").document(firebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()){
                            partner = documentSnapshot.toObject(Partner.class);

                            String identifier = textInputEditTextAdvertiseIdentifier.getText().toString().trim();
                            //String budget = spinnerBudgetArrayList.get(spinnerAdvertiseBudget.getSelectedItemPosition());
                            String distance = spinnerDistanceArrayList.get(spinnerAdvertiseDistanceRange.getSelectedItemPosition());

                            if (!spinnerBudgetArrayList.get(spinnerAdvertiseBudget.getSelectedItemPosition()).equals("Choose a budget") && !spinnerDistanceArrayList.get(spinnerAdvertiseDistanceRange.getSelectedItemPosition()).equals("Choose a distance") && frommapview == 1 && !identifier.isEmpty() && !String.valueOf(budget).isEmpty() && !String.valueOf(videoUri).equals("null")){
                                storageReference.child(getFileName(videoUri)).putFile(videoUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        if (task.isSuccessful()){
                                            storageReference.child(getFileName(videoUri)).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    String videourl = String.valueOf(uri);
                                                    Ad ad = new Ad(budget,partner.getName(),firebaseUser.getUid(),distance,firebaseUser.getUid(),false,latitude,longitude,0,identifier,0,videourl,0);
                                                    firebaseFirestore.collection("ads").document().set(ad).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            progressDialogAdvertise.dismiss();

                                                            if (task.isSuccessful()){
                                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                                builder.setCancelable(false);
                                                                builder.setTitle("ADVERTISE");
                                                                builder.setMessage("Your ad transactions have been successful");
                                                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        Intent intent = new Intent(getActivity(), MainActivity.class);
                                                                        startActivity(intent);
                                                                        CustomIntent.customType(getContext(),"fadein-to-fadeout");
                                                                        getActivity().finish();
                                                                    }
                                                                });
                                                                builder.create().show();
                                                            }else {
                                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                                builder.setCancelable(false);
                                                                builder.setTitle("ADVERTISE");
                                                                builder.setMessage("An error occurred while processing your ads");
                                                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        Intent intent = new Intent(getActivity(), MainActivity.class);
                                                                        startActivity(intent);
                                                                        CustomIntent.customType(getContext(),"fadein-to-fadeout");
                                                                        getActivity().finish();
                                                                    }
                                                                });
                                                                builder.create().show();
                                                            }

                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            progressDialogAdvertise.dismiss();
                                                            Snackbar.make(v,"Error loading ad information",Snackbar.LENGTH_LONG).show();

                                                        }
                                                    });
                                                }
                                            });

                                        }else {
                                            progressDialogAdvertise.dismiss();
                                            Snackbar.make(v,"Error loading ad information",Snackbar.LENGTH_LONG).show();
                                        }
                                    }
                                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                        progressDialogAdvertise.show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialogAdvertise.dismiss();
                                        Snackbar.make(v,"Error loading ad information",Snackbar.LENGTH_LONG).show();
                                    }
                                });

                            }else {
                                Snackbar.make(v,"Please fill in the required fields",Snackbar.LENGTH_LONG).show();
                            }

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(v,"An error occurred while loading information",Snackbar.LENGTH_LONG).show();
                    }
                });

            }
        });

        return view;
    }

    public String getFileName(Uri filepath) {
        String result = null;
        if (filepath.getScheme().equals("content")) {
            Cursor cursor = getContext().getContentResolver().query(filepath, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = filepath.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private float getVideoSize(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            cursor.moveToFirst();
            float videoSize = cursor.getLong(sizeIndex);
            cursor.close();

            return videoSize;
        }
        return 0;
    }

    private void pickVideoIntent() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"SELECT AD VIDEO"),ALLOW_MULTIPLE_CODE);
    }

    private boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return LocationManagerCompat.isLocationEnabled(locationManager);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ALLOW_MULTIPLE_CODE && resultCode == Activity.RESULT_OK && data != null){
            videoUri = data.getData();
            textViewSelectAdVideo.setText("Ad selected successfully");
        }
    }
}
