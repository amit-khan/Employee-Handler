package com.example.a15101083.lab4task3;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String name;
    private double lati,longi;
    private EditText edTime;
    private int time;
    private Button timerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        name=intent.getStringExtra("name");
        lati= Double.parseDouble(intent.getStringExtra("latitude"));
        longi= Double.parseDouble(intent.getStringExtra("longitude"));
        edTime = findViewById(R.id.ed);
        timerBtn=findViewById(R.id.btn);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(lati,longi);
        Marker marker=mMap.addMarker(new MarkerOptions().position(sydney).title(name));
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(sydney,4);
        mMap.moveCamera(update);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                timer();

                return false;
            }
        });
    }

    private void timer() {
        edTime.setVisibility(View.VISIBLE);
        timerBtn.setVisibility(View.VISIBLE);
    }

    public void timerStart(View view) {
        time = Integer.parseInt((edTime.getText().toString()));
        edTime.setVisibility(View.GONE);
        timerBtn.setVisibility(View.GONE);
        Intent intent = new Intent(this,AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this,0,intent,0);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+time*1000,pi);
    }
}
