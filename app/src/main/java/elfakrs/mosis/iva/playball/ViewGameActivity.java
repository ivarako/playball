package elfakrs.mosis.iva.playball;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import elfakrs.mosis.iva.playball.Model.Game;
import elfakrs.mosis.iva.playball.Model.User;

public class ViewGameActivity extends AppCompatActivity {

    private String userID;
    private String gameID;
    private String currentUserID;
    private Game game;
    private User user;
    private User currentUser;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_game);

        Intent listIntent = getIntent();
        Bundle idBundle = listIntent.getExtras();
        userID = idBundle.getString("usercreatorID");
        currentUserID = idBundle.getString("userid");
        gameID = idBundle.getString("gameid");

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        setUser();
        setGame();

        final DatabaseReference refUser = myRef.child("users").child(currentUserID);
        refUser.addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot item: dataSnapshot.getChildren()) {
                    currentUser = dataSnapshot.getValue(User.class);

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
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

        /*DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String reportDate = df.format(game.getDateTime());*/
        Date date = game.getDateTime();

        String min = String.valueOf(date.getMinutes());
        if(min.length() == 1) min = "0" + min;

        String hour = String.valueOf(date.getHours());
        if(hour.length() == 1) hour = "0" + hour;

        txtTittle.setText(game.getTittle());
        txtSport.setText(game.getSport());
        txtTime.setText(String.valueOf(date.getDate()) + "/" + String.valueOf(date.getMonth()) + "/" + String.valueOf(date.getYear()) + " " + hour + ":" + min);
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
        if(currentUser.getGoingGamesID().contains(gameID)) {
            Toast.makeText(ViewGameActivity.this, "You already marked your arrival to this game", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.addGoingGame(gameID);
        myRef.child("users").child(currentUserID).setValue(currentUser);

        game.addGoingUser(currentUserID);
        myRef.child("games").child(gameID).setValue(game);

        Toast.makeText(ViewGameActivity.this, "You are going!", Toast.LENGTH_SHORT).show();
    }

    public void OpenCreator(View v)
    {
         Intent i = new Intent(ViewGameActivity.this, ProfileActivity.class);
         Bundle idBundle = new Bundle();
         idBundle.putString("userid", userID);
         idBundle.putString("currentUserid", currentUserID);
         i.putExtras(idBundle);
         startActivity(i);

    }
}
