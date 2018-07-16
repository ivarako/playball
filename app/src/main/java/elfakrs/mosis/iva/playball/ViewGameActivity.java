package elfakrs.mosis.iva.playball;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ViewGameActivity extends AppCompatActivity {

    private String userID;
    private String gameID;
    private Game game;
    private User user;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_game);

        Intent listIntent = getIntent();
        Bundle idBundle = listIntent.getExtras();
        userID = idBundle.getString("userid");
        gameID = idBundle.getString("gameid");

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        setUser();
        setGame();
    }

    private void setUser() {
        final DatabaseReference refUser = myRef.child("users").child(userID);
        refUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot item: dataSnapshot.getChildren()) {
                        user = dataSnapshot.getValue(User.class);
                        setUserData();
                    }
                }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void setGame(){
        final DatabaseReference refUser = myRef.child("games").child(gameID);
        refUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot item: dataSnapshot.getChildren()) {
                        game = dataSnapshot.getValue(Game.class);
                        try {
                            setGameData();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void setGameData() throws IOException {
        final ImageView imgGame = (ImageView) findViewById(R.id.viewGameImg);
        TextView txtTittle = (TextView) findViewById(R.id.viewGameTxtTittle);
        TextView txtSport = (TextView) findViewById(R.id.viewGameTxtSport);
        TextView txtTime = (TextView) findViewById(R.id.viewGameTxtTime);
        TextView txtLocation = (TextView) findViewById(R.id.viewGameTxtLocation);
        TextView txtGoing = (TextView) findViewById(R.id.viewGameTxtGoing);

        if(game.isHasImg()) {
            StorageReference storageRef = mStorageRef.child("images/games/" + game.getId());
            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(ViewGameActivity.this).load(uri.toString()).resize(690, 250).centerCrop().into(imgGame);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(ViewGameActivity.this, "Failed to load game photo", Toast.LENGTH_SHORT).show();
                }
            });
        }

        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        String reportDate = df.format(game.getDateTime());

        txtTittle.setText(game.getTittle());
        txtSport.setText(game.getSport());
        txtTime.setText(reportDate);
        txtGoing.setText(String.valueOf(game.getGoingUsersID().size()) + " people going");

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(Double.parseDouble(game.getLatitude()), Double.parseDouble(game.getLongitude()), 1);
            String address = addresses.get(0).getAddressLine(0);
            txtLocation.setText(address);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void setUserData()
    {
        final ImageView imgCreator = (ImageView) findViewById(R.id.viewGameImgCreator);
        TextView txtName = (TextView) findViewById(R.id.viewGameTxtName);
        txtName.setText(user.getName());

        if(user.isHasImg()) {
            StorageReference storageRef = mStorageRef.child("images/users/" + userID);
            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(ViewGameActivity.this).load(uri.toString()).resize(100, 100).centerCrop().into(imgCreator);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(ViewGameActivity.this, "Failed to load user's photo", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void ViewGameMap(View v)
    {
        Intent i = new Intent(this, MapsActivity.class);
        i.putExtra("state", MapsActivity.CENTER_PLACE_ON_MAP);
        i.putExtra("lat", game.getLatitude());
        i.putExtra("lon", game.getLongitude());
        startActivity(i);
    }

    public void ViewGameGoing(View v)
    {
        if(user.getGoingGamesID().contains(gameID)) {
            Toast.makeText(ViewGameActivity.this, "You already marked your arrival to this game", Toast.LENGTH_SHORT).show();
            return;
        }

        user.addGoingGame(gameID);
        myRef.child("users").child(userID).setValue(user);

        game.addGoingUser(userID);
        myRef.child("games").child(gameID).setValue(game);

        Toast.makeText(ViewGameActivity.this, "You are going!", Toast.LENGTH_SHORT).show();
    }
}
