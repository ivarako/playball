package elfakrs.mosis.iva.playball;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.ArrayList;


public class GamesFragment extends Fragment implements  View.OnClickListener{

    private String userID;
    private String currentUserID;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageRef;
    private ArrayList<Game> games;
    private LinearLayout ly;
    private User user;

    public GamesFragment() { }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_games, container, false);

        Bundle bundle = getArguments();
        userID = bundle.getString("userid");
        currentUserID = bundle.getString("currentUserid");

        ly = view.findViewById(R.id.gamesFragmentLY);
        games = new ArrayList<>();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        DatabaseReference refUser = myRef.child("users").child(userID);
        refUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot item: dataSnapshot.getChildren()) {
                    user= dataSnapshot.getValue(User.class); }
                setGames();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        return view;
    }

    private void setGames(){
        DatabaseReference refUsers = myRef.child("games");
        refUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(user.getGoingGamesID().contains(ds.getValue(Game.class).getId()) ||
                            user.getGamesID().contains(ds.getValue(Game.class).getId())) {
                        Game game = new Game();
                        game.setId(ds.getValue(Game.class).getId());
                        game.setTittle(ds.getValue(Game.class).getTittle());
                        game.setHasImg(ds.getValue(Game.class).isHasImg());
                        game.setCreatorID(ds.getValue(Game.class).getCreatorID());

                        if (!games.contains(game)) {
                            games.add(game);
                            setGamesData(game);
                        }
                    }
                }}

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

    }

    private void setGamesData(Game game) {
        TextView txt = new TextView(getActivity());
        txt.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        txt.setTextSize(15);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(30, 0, 0, 50);
        txt.setLayoutParams(params);
        txt.setTag(game.getId() + " " + game.getCreatorID());
        txt.setOnClickListener(this);

        if(user.getGamesID().contains(game.getId()))
            txt.setText(game.getTittle() + " - created by " + user.getName());
        else
            txt.setText(game.getTittle());

        final ImageView img = new ImageView(getActivity());
        img.setTag(game.getId() + " " + game.getCreatorID());
        img.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 350));
        img.setBackground(getResources().getDrawable(R.drawable.balls1));
        img.setOnClickListener(this);

        if(game.isHasImg()) {
            StorageReference storageRef = mStorageRef.child("images/games/" + game.getId());
            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(getActivity()).load(uri.toString()).resize(773, 250).centerCrop().into(img);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getActivity(), "Failed to load game photo", Toast.LENGTH_SHORT).show();
                }
            });
        }

        ly.addView(img);
        ly.addView(txt);
    }

    @Override
    public void onClick(View v) {
        String id= v.getTag().toString();
        String[] ids= id.split("\\s");
        Intent i = new Intent(getActivity(), ViewGameActivity.class);
        Bundle idBundle = new Bundle();
        idBundle.putString("userid", currentUserID);
        idBundle.putString("usercreatorID", ids[1]);
        idBundle.putString("gameid", ids[0]);
        i.putExtras(idBundle);
        startActivity(i);
    }
}
