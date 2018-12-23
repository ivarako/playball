package elfakrs.mosis.iva.playball;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import elfakrs.mosis.iva.playball.Model.User;

public class EditProfileActivity extends AppCompatActivity {

    private String userID;
    private User user;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageRef;
    private ImageView img;
    private EditText txtName;
    private EditText txtSex;
    private EditText txtAge;
    private EditText txtCity;
    private EditText txtSport;
    private boolean imgChanged = false;
    private Uri image;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Intent listIntent = getIntent();
        Bundle idBundle = listIntent.getExtras();
        userID = idBundle.getString("userid");

        img = (ImageView) findViewById(R.id.editProfileImg);
        txtName = (EditText) findViewById(R.id.editProfileName);
        txtSex = (EditText) findViewById(R.id.editProfileSex);
        txtAge = (EditText) findViewById(R.id.editProfileAge);
        txtCity = (EditText) findViewById(R.id.editProfileCity);
        txtSport = (EditText) findViewById(R.id.editProfileSport);
        mProgressDialog = new ProgressDialog(EditProfileActivity.this);

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
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void setData()
    {
        if(user.isHasImg()) {
            StorageReference storageRef = mStorageRef.child("images/users/" + userID);
            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(EditProfileActivity.this).load(uri.toString()).resize(300, 300).centerCrop().into(img);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(EditProfileActivity.this, "Failed to load user's photo", Toast.LENGTH_SHORT).show();
                }
            });
        }

        txtName.setText(user.getName());

        if(!user.getSex().isEmpty())
            txtSex.setText(user.getSex());
        if(user.getAge()!=0)
            txtAge.setText(String.valueOf(user.getAge()));
        if(!user.getCity().isEmpty())
            txtCity.setText(user.getCity());
        if(!user.getFavoriteSport().isEmpty())
            txtSport.setText(user.getFavoriteSport());
    }

    public void UpdatePicture(View v)
    {
        checkFilePermissions();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Profile picture");
        builder.setMessage("Do you want to take a new photo or choose one from the gallery?");

        builder.setPositiveButton("Take a photo", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, 0);
            }
        });

        builder.setNegativeButton("Choose from the gallery", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , 1);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void checkFilePermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = EditProfileActivity.this.checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");
            permissionCheck += EditProfileActivity.this.checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");
            permissionCheck += EditProfileActivity.this.checkSelfPermission("Manifest.permission.CAMERA");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.CAMERA}, 1001); //Any number
            }
        }else{
            Log.d("Permission", "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case 0:
                if(resultCode == RESULT_OK){
                    imgChanged = true;
                    Bitmap bitmap = (Bitmap) imageReturnedIntent.getExtras().get("data");
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    img.setImageBitmap(bitmap);

                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    Date date = new Date();
                    String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), bitmap, "PlayBall" + formatter.format(date) , null);
                    image = Uri.parse(path);

                }
                break;

            case 1:
                if(resultCode == RESULT_OK){
                    imgChanged = true;
                    Uri imageUri = imageReturnedIntent.getData();
                    img.setImageURI(imageUri);
                    image = imageUri;
                }
                break;
        }
    }

    public void EditProfileSave(View v)
    {
        user.setName(txtName.getText().toString());
        user.setSex(txtSex.getText().toString());
        user.setAge(Integer.valueOf(txtAge.getText().toString()));
        user.setCity(txtCity.getText().toString());
        user.setFavoriteSport(txtSport.getText().toString());

        DatabaseReference refUser = myRef.child("users").child(userID);
        refUser.setValue(user);

        if(imgChanged) {
            mProgressDialog.setMessage("Updating...");
            mProgressDialog.show();

            final StorageReference storageRef = mStorageRef.child("images/users/" + user.getId());
            storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Success", "onSuccess: deleted file");

                    storageRef.putFile(image)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(EditProfileActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
                                    finish();
                                    setResult(android.app.Activity.RESULT_OK);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(EditProfileActivity.this, "Uploading a photo failed.", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    mProgressDialog.dismiss();
                    Toast.makeText(EditProfileActivity.this, "Deleting a photo failed.", Toast.LENGTH_SHORT).show();
                    Log.d("Fail", "onFailure: did not delete file");
                }
            });

        }
        else{
            Toast.makeText(EditProfileActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
            finish();
            setResult(android.app.Activity.RESULT_OK);}

    }

    public void EditProfileDiscard(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Discard");
        builder.setMessage("Are you sure?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        builder.setNegativeButton("No", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        super.onDestroy();
    }
}
