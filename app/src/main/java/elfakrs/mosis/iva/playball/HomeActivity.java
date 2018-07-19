package elfakrs.mosis.iva.playball;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

        DatabaseReference refUser = myRef.child("users").child(userID);
        refUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot item: dataSnapshot.getChildren()) {
                    user= dataSnapshot.getValue(User.class); }}
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        navigationView = (NavigationView)findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        toggle= new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        setGames();
    }

    private void setGames()
    {
        DatabaseReference refUsers = myRef.child("games");
        refUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                   boolean tmp = false;
                   switch (ds.getValue(Game.class).getSport()){
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
                       }}

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
        txt.setTextColor(R.color.colorPrimaryDark);
        txt.setTextSize(15);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(30, 0, 0, 50);
        txt.setLayoutParams(params);
        txt.setText(game.getTittle());
        txt.setTag(game.getId() + " " + game.getCreatorID());
        txt.setOnClickListener(this);

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
                startActivity(i);
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
}
