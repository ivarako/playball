package elfakrs.mosis.iva.playball;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    public static final int SHOW_MAP = 2;
    public static final int CENTER_PLACE_ON_MAP = 3;
    public static final int SELECET_COORDINATES = 4;

    private LatLng placeLoc;
    private int state = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        try {
            Intent mapIntent = getIntent();
            Bundle mapBundle = mapIntent.getExtras();
            if (mapBundle != null) {
                state = mapBundle.getInt("state");
                if (state == CENTER_PLACE_ON_MAP) {
                    String placeLat = mapBundle.getString("lat");
                    String placeLon = mapBundle.getString("lon");
                    placeLoc = new LatLng(Double.parseDouble(placeLat), Double.parseDouble(placeLon));
                }
            }
        } catch (Exception e) {
            Log.d("Error", "Error reading state");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
        }
        else
        {
            if (state == SHOW_MAP) {
                mMap.setMyLocationEnabled(true);

            } else if (state == SELECET_COORDINATES) {
                setOnMapClickListener();
            } else {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLoc, 15));
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(placeLoc);
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_place_black_24dp));
                mMap.addMarker(markerOptions);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (state == SHOW_MAP) {
                        mMap.setMyLocationEnabled(true);

                    } else if (state == SELECET_COORDINATES) {
                        setOnMapClickListener();
                    } else {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLoc, 15));
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(placeLoc);
                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_place_black_24dp));
                        mMap.addMarker(markerOptions);
                    }
                }
            }
        }
    }

    private void setOnMapClickListener()
    {
        if(mMap != null && state == SELECET_COORDINATES)
        {
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                        String lon = Double.toString(latLng.longitude);
                        String lat = Double.toString(latLng.latitude);
                        Intent locationIntent = new Intent();
                        locationIntent.putExtra("lon", lon);
                        locationIntent.putExtra("lat", lat);
                        setResult(Activity.RESULT_OK, locationIntent);
                        finish();
                    }
            });
        }
    }
}
