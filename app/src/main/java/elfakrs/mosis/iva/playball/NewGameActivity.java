package elfakrs.mosis.iva.playball;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TimePicker;
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

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import elfakrs.mosis.iva.playball.Model.Game;
import elfakrs.mosis.iva.playball.Model.User;

public class NewGameActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, AdapterView.OnItemSelectedListener {

    private String userID;
    private Uri image;
    private boolean imageSet = false;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageRef;
    private ProgressDialog mProgressDialog;
    private String sport;
    private Spinner dropdown;
    private boolean tmp = false;
    private String latitude;
    private String longitude;
    private int myear;
    private int mmonth;
    private int mday;
    private int mhour;
    private int mmin;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);

        Intent listIntent = getIntent();
        Bundle idBundle = listIntent.getExtras();
        userID = idBundle.getString("userid");

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mProgressDialog = new ProgressDialog(NewGameActivity.this);

        dropdown = (Spinner) findViewById(R.id.gameSpinner);
        String[] items = new String[]{"Football", "Basketball", "Volleyball", "Add new"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(this);

        DatabaseReference refUser = myRef.child("users").child(userID);
        refUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot item: dataSnapshot.getChildren()) {
                    user= dataSnapshot.getValue(User.class); }}
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    public void GamePicture(View v)
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

    public void GameDateTime(View v)
    {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

        DatePickerDialog dialog = new DatePickerDialog(NewGameActivity.this, this,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    public void GameLocation(View v)
    {
        Intent i = new Intent(NewGameActivity.this, MapsActivity.class);
        i.putExtra("state", MapsActivity.SELECT_COORDINATES);
        startActivityForResult(i, 2);
    }

    public void GameSave(View v)
    {
        EditText txtTittle = (EditText) findViewById(R.id.gameTxtTittle);
        EditText txtLocation = (EditText) findViewById(R.id.gameTxLocation);
        EditText txtDate = (EditText) findViewById(R.id.gameTxtDateTime);

        String tittle = txtTittle.getText().toString();
        String location = txtLocation.getText().toString();
        String date = txtDate.getText().toString();

        if(tittle.isEmpty() || location.isEmpty() || date.isEmpty() || sport.isEmpty())
        {
            Toast.makeText(NewGameActivity.this, "Enter required informations first!", Toast.LENGTH_SHORT).show();
            return;
        }

        final Game game = new Game();
        game.setCreatorID(userID);
        game.setTittle(tittle);
        game.setLatitude(latitude);
        game.setLongitude(longitude);
        game.setSport(sport);
        game.setDateTime(new Date(myear, mmonth, mday, mhour, mmin));
        game.setHasImg(imageSet);
        game.setId(myRef.push().getKey());

        myRef.child("games").child(game.getId()).setValue(game);
       user.addCreatedGame(game.getId());
       myRef.child("users").child(userID).child("gamesID").setValue(user.getGamesID());

        if(imageSet) {
            mProgressDialog.setMessage("Creating a new game...");
            mProgressDialog.show();
            uploadPhoto(game);
        }
        else {
            Toast.makeText(NewGameActivity.this, "New game created!", Toast.LENGTH_SHORT).show();

            Intent i = new Intent(NewGameActivity.this, ViewGameActivity.class);
            Bundle idBundle = new Bundle();
            idBundle.putString("userid", userID);
            idBundle.putString("gameid", game.getId());
            idBundle.putString("usercreatorID", userID);
            i.putExtras(idBundle);
            startActivity(i);
            setResult(android.app.Activity.RESULT_OK);
            finish();
        }
    }

    public void GameDiscard(View v)
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

    private void checkFilePermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = NewGameActivity.this.checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");
            permissionCheck += NewGameActivity.this.checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");
            permissionCheck += NewGameActivity.this.checkSelfPermission("Manifest.permission.CAMERA");
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

        ImageView imageView = (ImageView)findViewById(R.id.gameImageView);

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

            case 2:
                try {
                    if (resultCode == Activity.RESULT_OK) {
                        longitude = imageReturnedIntent.getExtras().getString("lon");
                        latitude = imageReturnedIntent.getExtras().getString("lat");

                        Geocoder geocoder;
                        List<Address> addresses;
                        geocoder = new Geocoder(this, Locale.getDefault());

                        addresses = geocoder.getFromLocation(Double.parseDouble(latitude), Double.parseDouble(longitude), 1);
                        String address = addresses.get(0).getAddressLine(0);

                        EditText txtLocation = (EditText) findViewById(R.id.gameTxLocation);
                        txtLocation.setVisibility(View.VISIBLE);
                        txtLocation.setText(address);
                    }
                }
                catch(Exception e) { }
        }
    }

    private void uploadPhoto(final Game game) {
        StorageReference storageRef = mStorageRef.child("images/games/" + game.getId());

        storageRef.putFile(image)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mProgressDialog.dismiss();
                        Toast.makeText(NewGameActivity.this, "New game created!", Toast.LENGTH_SHORT).show();

                        Intent i = new Intent(NewGameActivity.this, ViewGameActivity.class);
                        Bundle idBundle = new Bundle();
                        idBundle.putString("userid", userID);
                        idBundle.putString("gameid", game.getId());
                        idBundle.putString("usercreatorID", userID);
                        i.putExtras(idBundle);
                        startActivity(i);

                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        mProgressDialog.dismiss();
                        Toast.makeText(NewGameActivity.this, "Uploading a photo failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth) {

        myear = year; mmonth = month + 1; mday = dayOfMonth;

        final EditText txtDate = (EditText) findViewById(R.id.gameTxtDateTime);

        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(NewGameActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                mhour = selectedHour; mmin = selectedMinute;

                txtDate.setVisibility(View.VISIBLE);
                txtDate.setText(selectedHour + ":" + selectedMinute + " " + dayOfMonth + "/" + mmonth + "/" + year);
            }
        }, hour, minute, true);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (position){

            case 0:{
                sport = "football";
                break;
            }

            case 1:{
                sport = "basketball";
                break;
            }

            case 2:{
                sport = "volleyball";
                break;
            }

            case 3:{

                if(tmp) {
                    sport = dropdown.getItemAtPosition(3).toString();
                }

                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("New sport");
                    final EditText input = new EditText(NewGameActivity.this);
                    input.setGravity(Gravity.LEFT);
                    input.setImeOptions(EditorInfo.IME_ACTION_DONE);
                    builder.setView(input);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            String[] items = new String[]{"Football", "Basketball", "Volleyball", input.getText().toString()};
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(NewGameActivity.this, android.R.layout.simple_spinner_dropdown_item, items);
                            dropdown.setAdapter(adapter);
                            dropdown.setSelection(3);
                            sport = input.getText().toString();
                            tmp = true;
                        }
                    });

                    builder.setNegativeButton("Discard", null);

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onDestroy() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        super.onDestroy();
    }

}
