package elfakrs.mosis.iva.playball;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import static android.app.Activity.RESULT_OK;

public class FriendsFragment extends Fragment implements View.OnClickListener{

    private String userID;
    private String currentUserID;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageRef;
    private User user;
    private ArrayList<String> friends;
    private LinearLayout lly;

    public FriendsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_friends, container, false);

        Bundle bundle = getArguments();
        userID = bundle.getString("userid");
        currentUserID = bundle.getString("currentUserid");

        if(userID.equals(currentUserID)){

            Button btnNewFriends = view.findViewById(R.id.findFriendsBtn);
            btnNewFriends.setVisibility(View.VISIBLE);
            btnNewFriends.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getActivity(), FindFriendsActivity.class);
                    Bundle idBundle = new Bundle();
                    idBundle.putString("userid", userID);
                    i.putExtras(idBundle);
                    startActivityForResult(i, 1);
                }
            });
        }

        lly = view.findViewById(R.id.friendsFragmentLLY);
        friends = new ArrayList<>();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        DatabaseReference refUser = myRef.child("users").child(userID);
        refUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                setData();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        return view;
    }

    private void setData(){

        DatabaseReference refFriends = myRef.child("users");
        refFriends.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    if(user.getFriendsID().contains(ds.getKey()) && !friends.contains(ds.getKey())){
                        User friend = ds.getValue(User.class);

                        LinearLayout linearLayout = new LinearLayout(getActivity());
                        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        linearLayout.setTag(friend.getId());
                        linearLayout.setOnClickListener(FriendsFragment.this);

                        if(friend.isHasImg()){
                            final ImageView img = new ImageView(getActivity());
                            img.setLayoutParams(new LinearLayout.LayoutParams(200, 200));

                            StorageReference storageRef = mStorageRef.child("images/users/" + friend.getId());
                            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Picasso.with(getActivity()).load(uri.toString()).resize(300, 300).centerCrop().into(img);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Toast.makeText(getActivity(), "Failed to load user's photo", Toast.LENGTH_SHORT).show();
                                }
                            });

                            linearLayout.addView(img);
                        }

                        TextView txtName = new TextView(getActivity());
                        txtName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        txtName.setText(friend.getName());
                        txtName.setTextSize(20);
                        txtName.setTextColor(getResources().getColor(R.color.black));

                        linearLayout.addView(txtName);
                        lly.addView(linearLayout);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent(getContext(), ProfileActivity.class);
        Bundle idBundle = new Bundle();
        idBundle.putString("userid", v.getTag().toString());
        idBundle.putString("currentUserid", userID);
        i.putExtras(idBundle);
        startActivity(i);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if(resultCode == RESULT_OK) {
            lly.removeAllViews();
            friends.clear();
            setData();
        }
    }
}
