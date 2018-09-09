package elfakrs.mosis.iva.playball;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import elfakrs.mosis.iva.playball.Model.User;

public class BasicInfoFragment extends Fragment {

    private String userID;
    private String currentUserID;
    private ImageView img;
    private TextView txtName;
    private TextView txtInfo;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageRef;
    private User user;

    public BasicInfoFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_basic_info, container, false);

        Bundle bundle = getArguments();
        userID = bundle.getString("userid");
        currentUserID = bundle.getString("currentUserid");

        if(userID.equals(currentUserID))
        {
            Button btnEdit = (Button) view.findViewById(R.id.btnInfoEdit);
            btnEdit.setVisibility(View.VISIBLE);

            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent i = new Intent(getActivity(), EditProfileActivity.class);
                    Bundle idBundle = new Bundle();
                    idBundle.putString("userid", userID);
                    i.putExtras(idBundle);
                    startActivityForResult(i, 1);
                }
            });
        }

        img = view.findViewById(R.id.infoImg);
        txtName = view.findViewById(R.id.infoNameTxt);
        txtInfo = view.findViewById(R.id.infoTxt);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        DatabaseReference refUser = myRef.child("users").child(userID);
        refUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    user = dataSnapshot.getValue(User.class);
                    setData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        return view;
    }

    private void setData() {
        if(user.isHasImg()) {
            StorageReference storageRef = mStorageRef.child("images/users/" + userID);
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
        }

        txtName.setText(user.getName());

        String info = "";
        if(!user.getSex().isEmpty())
            info += "Sex: " + user.getSex() + "\n";
        if(user.getAge()!=0)
            info += "Age: " + String.valueOf(user.getAge()) + "\n";
        if(!user.getCity().isEmpty())
            info += "From: " + user.getCity() + "\n";
        if(!user.getFavoriteSport().isEmpty())
            info += "Favorite sport: " + user.getFavoriteSport() + "\n";

        info += "Average score: " + String.valueOf(user.getScore());

        txtInfo.setText(info);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        if(requestCode == 1 && resultCode == android.app.Activity.RESULT_OK)
            setData();

        }

}
