package com.example.sameer.geofencingapp;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private GeofencingClient mGeofencingClient;
    ArrayList<Geofence> mGeofenceList;
    PendingIntent mGeofencePendingIntent;
    Button mAddGeofenceButton;

    Spinner spinner;
    List<Offer> offers;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String id = getIntent().getStringExtra("ID");
        if (null != id) {
            Intent intent = new Intent(MainActivity.this, DealsActivity.class);
            intent.putExtra("ID", id);
            startActivity(intent);
            finish();
        }
        setContentView(R.layout.activity_main);

        mAddGeofenceButton = (Button) findViewById(R.id.add_geofence_btn);

        offers = new ArrayList<>();
        offers.add(new Offer(13.353014, 74.793609, 300, "AB5"));
        offers.add(new Offer(13.353014, 74.793609, 300, "MAC D"));

        final TextInputEditText xcord = findViewById(R.id.xcord);
        final TextInputEditText ycord = findViewById(R.id.ycord);
        final TextInputEditText radius = findViewById(R.id.radius);

        spinner = findViewById(R.id.spinner);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);

        for (Offer o : offers) {
            adapter.add(o.name);
        }

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                xcord.setText(String.valueOf(offers.get(position).x));
                ycord.setText(String.valueOf(offers.get(position).y));
                radius.setText(String.valueOf(offers.get(position).r));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mAddGeofenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (spinner.getSelectedItem() == null) {
                    Toast.makeText(MainActivity.this, "Select some item first", Toast.LENGTH_SHORT).show();
                    return;
                }
                addGeofencing();
            }
        });
    }

    class Offer {
        double x;
        double y;
        double r;
        String name;

        public Offer(double x, double y, double r, String name) {
            this.x = x;
            this.y = y;
            this.r = r;
            this.name = name;
        }
    }

    private void addGeofencing() {
        Offer offer = offers.get(spinner.getSelectedItemPosition());
        mGeofencingClient = LocationServices.getGeofencingClient(this);
        mGeofenceList = new ArrayList<Geofence>();
        mGeofenceList.add(new Geofence.Builder().setRequestId(offer.name).setCircularRegion(offer.x, offer.y, (float) offer.r).setExpirationDuration(3600000).setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT).build());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences added
                        Toast.makeText(getApplicationContext(), "Geofence added succesfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add geofences
                        Toast.makeText(getApplicationContext(), "Geofence could not be added", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

}
