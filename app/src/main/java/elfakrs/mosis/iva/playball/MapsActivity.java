package elfakrs.mosis.iva.playball;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    public static final int SHOW_MAP = 2;
    public static final int CENTER_PLACE_ON_MAP = 3;
    public static final int SELECT_COORDINATES = 4;

    private LatLng placeLoc;
    private int state = 0;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private String userID;
    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
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
                else if(state == SHOW_MAP){
                    userID = mapBundle.getString("userid");
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
            mMapReady();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    mMapReady();
            }
        }
    }

    private void mMapReady() {
        if (state == SHOW_MAP || state == SELECT_COORDINATES) {
            mMap.setMyLocationEnabled(true);
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mZoom(latLng, false);
            }
        }

        switch (state) {
            case SELECT_COORDINATES: {
                setOnMapClickListener();
                break;
            }
            case SHOW_MAP: {
                setMapData();
                break;
            }
            case CENTER_PLACE_ON_MAP: {
                mZoom(placeLoc, true);
                break;
            }
        }
    }

    private void setOnMapClickListener()
    {
        if(mMap != null && state == SELECT_COORDINATES) {
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

    private void mZoom(LatLng location, boolean marker) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14));

        if(marker) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(location);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_place_black_24dp));
             mMap.addMarker(markerOptions);
        }
    }

    private void setMapData(){
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        setOnMarkerClickListener();

        DatabaseReference refUser = myRef.child("users").child(userID);
        refUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot item: dataSnapshot.getChildren()) {
                    user= dataSnapshot.getValue(User.class);
                }
                setGames();
                setFriends();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void setGames(){
        DatabaseReference refUsers = myRef.child("games");
        refUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    boolean tmp = false;
                    Date date = ds.getValue(Game.class).getDateTime();
                    Log.i("year", String.valueOf(date.getYear()));
                    Date currentDate = new Date();
                    //year 3918???? u bazi 2018.
                    Date dateTmp = new Date(date.getYear() - 1900, date.getMonth(), date.getDate(), date.getHours(), date.getMinutes());

                    if(dateTmp.after(currentDate))
                    {
                        switch (ds.getValue(Game.class).getSport()) {
                            case "football": {
                                tmp = user.isFootball();
                                break;
                            }
                            case "basketball": {
                                tmp = user.isBasketball();
                                break;
                            }
                            case "volleyball": {
                                tmp = user.isVolleyball();
                                break;
                            }
                            default: {
                                tmp = user.isOthers();
                                break;
                            }
                        }
                    }
                    if(tmp) {
                        Game game = new Game();
                        game.setId(ds.getValue(Game.class).getId());
                        game.setTittle(ds.getValue(Game.class).getTittle());
                        game.setLongitude(ds.getValue(Game.class).getLongitude());
                        game.setLatitude(ds.getValue(Game.class).getLatitude());
                        game.setCreatorID(ds.getValue(Game.class).getCreatorID());

                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(new LatLng(Double.parseDouble(game.getLatitude()), Double.parseDouble(game.getLongitude())));
                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_place_black_24dp));
                        markerOptions.title(game.getTittle());
                        Marker marker = mMap.addMarker(markerOptions);
                        marker.setTag(game);
                        marker.setZIndex(1);
                    }
                }}

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

    }

    private void setFriends(){

    }

    private void setOnMarkerClickListener(){
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                //zindex 1 - game, 0 - friend
                if(marker.getZIndex() == 1){
                    Game game = (Game) marker.getTag();
                    Intent i = new Intent(MapsActivity.this, ViewGameActivity.class);
                    Bundle idBundle = new Bundle();
                    idBundle.putString("userid", userID);
                    idBundle.putString("gameid", game.getId());
                    idBundle.putString("usercreatorID", game.getCreatorID());
                    i.putExtras(idBundle);
                    startActivity(i);
                }
                else{


                }
                return false;
            }
        });
    }
}
