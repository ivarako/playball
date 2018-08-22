package elfakrs.mosis.iva.playball;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Date;

import static elfakrs.mosis.iva.playball.MapsActivity.PERMISSION_ACCESS_FINE_LOCATION;


public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private String userID;
    private User user;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageRef;
    private ArrayList<Game> games;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent listIntent = getIntent();
        Bundle idBundle = listIntent.getExtras();
        userID = idBundle.getString("userid");

        games = new ArrayList<>();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        navigationView = (NavigationView)findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        toggle= new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        DatabaseReference refUser = myRef.child("users").child(userID);
        refUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot item: dataSnapshot.getChildren()) {
                    user = dataSnapshot.getValue(User.class);
                }
                setGames();

                if(user.isNotifications() && !isServiceRunning(MyService.class)){

                    if (ContextCompat.checkSelfPermission(HomeActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            &&ContextCompat.checkSelfPermission(HomeActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
                    }
                    else {
                        Intent i = new Intent(HomeActivity.this, MyService.class);
                        startService(i);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

    }

    private void setGames() {
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

                       if(tmp /* && u radiusu*/) {
                           Game game = new Game();
                           game.setId(ds.getValue(Game.class).getId());
                           game.setTittle(ds.getValue(Game.class).getTittle());
                           game.setHasImg(ds.getValue(Game.class).isHasImg());
                           game.setCreatorID(ds.getValue(Game.class).getCreatorID());

                           if(!games.contains(game)) {
                               games.add(game);
                               setGamesData(game);
                           }
                       }
                }}

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void setGamesData(Game game)
    {
        LinearLayout ly = (LinearLayout) findViewById(R.id.content);

        TextView txt = new TextView(HomeActivity.this);
        txt.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        txt.setTextSize(15);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(30, 0, 0, 50);
        txt.setLayoutParams(params);
        txt.setTag(game.getId() + " " + game.getCreatorID());
        txt.setOnClickListener(this);

        if(user.getGoingGamesID().contains(game.getId()))
            txt.setText(game.getTittle() + " - GOING");
        else
            txt.setText(game.getTittle());

        final ImageView img = new ImageView(HomeActivity.this);
        img.setTag(game.getId() + " " + game.getCreatorID());
        img.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 350));
        img.setBackground(getResources().getDrawable(R.drawable.balls1));
        img.setOnClickListener(this);

        if(game.isHasImg()) {
            StorageReference storageRef = mStorageRef.child("images/games/" + game.getId());
            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(HomeActivity.this).load(uri.toString()).resize(773, 250).centerCrop().into(img);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(HomeActivity.this, "Failed to load game photo", Toast.LENGTH_SHORT).show();
                }
            });
        }

        ly.addView(img);
        ly.addView(txt);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return (true);
        }
        return(super.onOptionsItemSelected(item));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        drawerLayout.closeDrawers();

        switch (item.getItemId()){

            case R.id.itemMyProfile: {
                Intent i = new Intent(HomeActivity.this, ProfileActivity.class);
                Bundle idBundle = new Bundle();
                idBundle.putString("userid", userID);
                idBundle.putString("currentUserid", userID);
                i.putExtras(idBundle);
                startActivity(i);
                break;
            }

            case R.id.itemNewGame:{
                Intent i = new Intent(HomeActivity.this, NewGameActivity.class);
                Bundle idBundle = new Bundle();
                idBundle.putString("userid", userID);
                i.putExtras(idBundle);
                startActivity(i);
                break;
            }

            case R.id.itemViewMap:{
                Intent i = new Intent(HomeActivity.this, MapsActivity.class);
                i.putExtra("state", MapsActivity.SHOW_MAP);
                i.putExtra("userid", userID);
                startActivity(i);
                break;
            }

            case R.id.itemTheBest:{
                Intent i = new Intent(HomeActivity.this, TheBestListActivity.class);
                startActivity(i);
                break;
            }

            case R.id.itemSettings:{
                Intent i = new Intent(HomeActivity.this, SettingsActivity.class);
                Bundle idBundle = new Bundle();
                idBundle.putString("userid", userID);
                i.putExtras(idBundle);
                startActivityForResult(i, 1);
                break;
            }

            case R.id.itemLogOut:{
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Log out");
                builder.setMessage("Are you sure you want to log out?");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       mAuth.signOut();
                        Intent i = new Intent(HomeActivity.this, LogInActivity.class);
                        startActivity(i);
                        finish();
                    }
                });

                builder.setNegativeButton("No", null);

                AlertDialog dialog = builder.create();
                dialog.show();

                break;
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        String id= v.getTag().toString();
        String[] ids= id.split("\\s");
        Intent i = new Intent(HomeActivity.this, ViewGameActivity.class);
        Bundle idBundle = new Bundle();
        idBundle.putString("userid", userID);
        idBundle.putString("usercreatorID", ids[1]);
        idBundle.putString("gameid", ids[0]);
        i.putExtras(idBundle);
        startActivity(i);
    }

    @Override
      public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        if(requestCode == 1 && resultCode == android.app.Activity.RESULT_OK) {
            LinearLayout ly = (LinearLayout) findViewById(R.id.content);
            ly.removeAllViews();

            DatabaseReference refUser = myRef.child("users").child(userID);
            refUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot item: dataSnapshot.getChildren()) {
                    user= dataSnapshot.getValue(User.class); }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) { } });

            setGames();
            }
        }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Intent i = new Intent(HomeActivity.this, MyService.class);
                    startService(i);
                }
            }
        }
    }
}
