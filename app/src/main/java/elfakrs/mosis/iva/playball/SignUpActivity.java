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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import elfakrs.mosis.iva.playball.Model.User;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Uri image;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageRef;
    private ProgressDialog mProgressDialog;
    private boolean imageSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mProgressDialog = new ProgressDialog(SignUpActivity.this);
    }


    public void Save(View v) {
        EditText editTxtEmail = (EditText) findViewById(R.id.editTxtEmail);
        EditText editTxtPassword = (EditText) findViewById(R.id.editTxtPassword);
        EditText editTxtPassword2 = (EditText) findViewById(R.id.editTxtPassword2);
        EditText editTxtName = (EditText) findViewById(R.id.editTxtName);

        final String email = editTxtEmail.getText().toString();
        final String password = editTxtPassword.getText().toString();
        String password2 = editTxtPassword2.getText().toString();
        final String name = editTxtName.getText().toString();

        if (email.equals("") || password.equals("") || password2.equals("") || name.equals("")) {
            Toast.makeText(SignUpActivity.this, "Enter your informations first.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(password2)) {
            Toast.makeText(SignUpActivity.this, "Passwords are not the same", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6)
        {
            Toast.makeText(SignUpActivity.this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser fbUser = mAuth.getCurrentUser();
                            User user = new User();
                            user.setId(fbUser.getUid());
                            user.setEmail(email);
                            user.setPassword(password);
                            user.setName(name);
                            user.setRadius(20);
                            user.setNotifications(true);
                            user.setLocation(true);
                            user.setFootball(true);
                            user.setBasketball(true);
                            user.setVolleyball(true);
                            user.setOthers(true);
                            user.setNumOfRatings(0);
                            user.setScore(0);
                            user.setSex("");
                            user.setAge(0);
                            user.setCity("");
                            user.setFavoriteSport("");
                            user.setHasImg(imageSet);

                            myRef.child("users").child(user.getId()).setValue(user);

                            if(imageSet) {
                                mProgressDialog.setMessage("Creating a new profile...");
                                mProgressDialog.show();
                                uploadPhoto(user);
                            }
                            else
                                startNextActivity(user.getId());

                        } else {
                            Toast.makeText(SignUpActivity.this, "Creating a new profile failed.", Toast.LENGTH_SHORT).show();
                            Log.w("Fail", "createUserWithEmail:failure", task.getException());
                        }
                    }
                });
    }

    public void Discard(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Discard");
        builder.setMessage("Are you sure you don't want to make a new profile?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
                startActivity(intent);
            }
        });

        builder.setNegativeButton("No", null);

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void ChoosePicture(View v)
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
            int permissionCheck = SignUpActivity.this.checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");
            permissionCheck += SignUpActivity.this.checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");
            permissionCheck += SignUpActivity.this.checkSelfPermission("Manifest.permission.CAMERA");
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

        ImageView imageView = (ImageView)findViewById(R.id.imgProfilePicture);

        switch(requestCode) {
            case 0:
                if(resultCode == RESULT_OK){
                    imageSet = true;
                    Bitmap bitmap = (Bitmap) imageReturnedIntent.getExtras().get("data");
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    imageView.setImageBitmap(bitmap);

                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    Date date = new Date();
                    String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), bitmap, "PlayBall" + formatter.format(date) , null);
                    image = Uri.parse(path);

                }
                break;

            case 1:
                if(resultCode == RESULT_OK){
                    imageSet = true;
                    Uri imageUri = imageReturnedIntent.getData();
                    imageView.setImageURI(imageUri);

                    image = imageUri;
                }
                break;
        }
    }

    private void uploadPhoto(final User user)
    {
        StorageReference storageRef = mStorageRef.child("images/users/" + user.getId());

        storageRef.putFile(image)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mProgressDialog.dismiss();
                        startNextActivity(user.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        mProgressDialog.dismiss();
                        Toast.makeText(SignUpActivity.this, "Uploading a photo failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startNextActivity(String userID){
        Toast.makeText(SignUpActivity.this, "New profile created!", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(SignUpActivity.this, HomeActivity.class);
        Bundle idBundle = new Bundle();
        idBundle.putString("userid", userID);
        i.putExtras(idBundle);
        startActivity(i);
    }
}

