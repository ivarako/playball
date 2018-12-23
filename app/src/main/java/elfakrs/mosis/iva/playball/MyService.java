package elfakrs.mosis.iva.playball;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

import elfakrs.mosis.iva.playball.Model.Game;
import elfakrs.mosis.iva.playball.Model.User;

public class MyService extends Service {

    private String TAG = "MyService";
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private User user = null;
    Location mLastLocation;
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 5000;
    private static final float LOCATION_DISTANCE = 0;
    LocationListener[] mLocationListeners = new LocationListener[]{new LocationListener(LocationManager.GPS_PROVIDER), new LocationListener(LocationManager.NETWORK_PROVIDER)};


    public MyService() { }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        DatabaseReference refUser = myRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        refUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    user = dataSnapshot.getValue(User.class);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private void checkIfNearEvent(final Location location){
        if(user != null && user.isNotifications()){
            DatabaseReference refGames = myRef.child("games");
            refGames.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot item: dataSnapshot.getChildren()) {
                       String gameID = item.getKey();
                       String gameLat = item.getValue(Game.class).getLatitude();
                       String gameLon = item.getValue(Game.class).getLongitude();
                       String creatorID = item.getValue(Game.class).getCreatorID();

                       String sport = item.getValue(Game.class).getSport();
                       Date date = item.getValue(Game.class).getDateTime();
                       Date tmpDate = new Date(date.getYear() - 1900, date.getMonth() - 1, date.getDate(), date.getHours(), date.getMinutes());
                       Date currentDate = new Date();

                        boolean tmp = false;
                        if(tmpDate.after(currentDate))
                        {
                            switch (sport) {
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

                        float[] dist = new float[1];
                        Location.distanceBetween(location.getLatitude(), location.getLongitude(), Double.parseDouble(gameLat), Double.parseDouble(gameLon), dist);

                        if(dist[0] <= 1000 && !creatorID.equals(user.getId()) && !user.getNotifiedGames().contains(gameID) && tmp){

                            user.addNotifiedGame(gameID);
                            myRef.child("users").child(user.getId()).child("notifiedGames").setValue(user.getNotifiedGames());

                            Intent i = new Intent(MyService.this, HomeActivity.class);
                            Bundle idBundle = new Bundle();
                            idBundle.putString("userid", user.getId());
                            i.putExtras(idBundle);
                            PendingIntent pendingIntent = PendingIntent.getActivity(MyService.this, 0, i, 0);
                            Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "CHANNEL_ID")
                                            .setSmallIcon(R.drawable.beachball)
                                            .setContentTitle("Game nearby")
                                            .setSound(uri)
                                            .setContentText("There's a game nearby. Click here to view it.")
                                            .setContentIntent(pendingIntent)
                                            .setAutoCancel(true);

                            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            mNotificationManager.notify(001, mBuilder.build());
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) { }
            });
        }
        else{
            DatabaseReference refUser = myRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            refUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    user = dataSnapshot.getValue(User.class);
                    checkIfNearEvent(location);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) { }
            });
        }
    }



    private class LocationListener implements android.location.LocationListener {

        public LocationListener(String provider) {
            Log.d(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);

        }

        @Override
        public void onLocationChanged(Location location) {
            mLastLocation.set(location);

            String latitude = String.valueOf(location.getLatitude());
            String longitude = String.valueOf(location.getLongitude());

            FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("latitude").setValue(latitude);
            FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("longitude").setValue(longitude);

            checkIfNearEvent(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "onStatusChanged: " + provider);
        }
    }


}
