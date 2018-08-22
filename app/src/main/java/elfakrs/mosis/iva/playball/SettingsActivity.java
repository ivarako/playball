package elfakrs.mosis.iva.playball;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {

    private String userID;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private Switch switchLocation;
    private Switch switchNotifications;
    private Switch switchFootball;
    private Switch switchBasketball;
    private Switch switchVolleyball;
    private Switch switchOthers;
    private EditText txtRadius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Intent listIntent = getIntent();
        Bundle idBundle = listIntent.getExtras();
        userID = idBundle.getString("userid");

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        switchLocation = (Switch) findViewById(R.id.settingsLocation);
        switchNotifications = (Switch) findViewById(R.id.settingsNotifications);
        switchFootball = (Switch) findViewById(R.id.settingsFootball);
        switchBasketball = (Switch) findViewById(R.id.settingsBasketball);
        switchVolleyball = (Switch) findViewById(R.id.settingsVolleyball);
        switchOthers = (Switch) findViewById(R.id.settingsOthers);
        txtRadius = (EditText) findViewById(R.id.settingsRadius);

        setUpdateData(true);
    }

    public void SettingsSave(View v)
    {
        setUpdateData(false);
        Toast.makeText(SettingsActivity.this, "Changes saved!", Toast.LENGTH_SHORT).show();
        setResult(android.app.Activity.RESULT_OK);
        finish();
    }

    public void SettingsDiscard(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Discard changes");
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

    //true - set, false - update
    private void setUpdateData(final boolean tmp)
    {
        final DatabaseReference ref2 = myRef.child("users").child(userID);
        ref2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot item: dataSnapshot.getChildren()) {

                    User user = dataSnapshot.getValue(User.class);
                    if(tmp) {
                        setData(user);
                    }
                    else{
                        boolean changes = checkChanges(user);
                        if(changes)
                            ref2.setValue(user);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void setData(User user)
    {
        switchNotifications.setChecked(user.isNotifications());
        switchLocation.setChecked(user.isLocation());
        switchFootball.setChecked(user.isFootball());
        switchBasketball.setChecked(user.isBasketball());
        switchVolleyball.setChecked(user.isVolleyball());
        switchOthers.setChecked(user.isOthers());
        txtRadius.setText(String.valueOf(user.getRadius()));
    }

    private boolean checkChanges(User user)
    {
        boolean changes = false;

        if(user.isNotifications() != switchNotifications.isChecked())
        {
            user.setNotifications(switchNotifications.isChecked());

            if(user.isNotifications() && !isServiceRunning(MyService.class)){
                Intent i = new Intent(SettingsActivity.this, MyService.class);
                startService(i);
            }

            if((!user.isNotifications()) && isServiceRunning(MyService.class)){
                Intent i = new Intent(SettingsActivity.this, MyService.class);
                stopService(i);
            }

            changes = true;
        }

        if(user.isLocation() != switchLocation.isChecked())
        {
            user.setLocation(switchLocation.isChecked());
            changes = true;
        }

        if(user.isFootball() != switchFootball.isChecked())
        {
            user.setFootball(switchFootball.isChecked());
            changes = true;
        }

        if(user.isBasketball() != switchBasketball.isChecked())
        {
            user.setBasketball(switchBasketball.isChecked());
            changes = true;
        }

        if(user.isVolleyball() != switchVolleyball.isChecked())
        {
            user.setVolleyball(switchVolleyball.isChecked());
            changes = true;
        }

        if(user.isOthers() != switchOthers.isChecked())
        {
            user.setOthers(switchOthers.isChecked());
            changes = true;
        }

        if(user.getRadius() != Integer.parseInt(txtRadius.getText().toString()))
        {
            user.setRadius(Integer.parseInt(txtRadius.getText().toString()));
            changes = true;
        }

        return changes;
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

}
