package elfakrs.mosis.iva.playball;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import elfakrs.mosis.iva.playball.MiBand.MiBand;
import elfakrs.mosis.iva.playball.MiBand.Observer;
import elfakrs.mosis.iva.playball.Model.*;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NewMyGameActivity extends AppCompatActivity implements Observer {

    private Button btnStart, btnStop, btnStopVibration;
    private EditText txtName;
    private TextView txtDuration, txtSteps, txtBPM;
    private int seconds;
    private Timer timer;
    private MiBand miBand;
    private User user;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private boolean isGameStarted, isMiBandConnected, stepsAtBeg;
    private MyGame myGame;
    private int stepsAtBeginning, steps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_my_game);

        btnStart = findViewById(R.id.btnStartGame);
        btnStop = findViewById(R.id.btnStopGame);
        btnStopVibration = findViewById(R.id.btnStopVibration);
        txtName = findViewById(R.id.txtMyGameName);
        txtDuration = findViewById(R.id.txtDuration);
        txtSteps = findViewById(R.id.txtSteps);
        txtBPM = findViewById(R.id.txtBPM);

        isGameStarted = false;
        isMiBandConnected = false;
        stepsAtBeg = true;
        myGame = new MyGame();

        miBand = new MiBand(this);
        miBand.addObserver(this);
        miBand.connect();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        final String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference refUser = myRef.child("users").child(userID);
        refUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        DatabaseReference refGames = myRef.child("games");
        refGames.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {

                    if(ds.getValue(Game.class).getGoingUsersID().contains(userID)){
                        Calendar cal1 = Calendar.getInstance();
                        Calendar cal2 = Calendar.getInstance();
                        cal1.setTime(ds.getValue(Game.class).getDateTime());
                        cal2.setTime(new Date());
                        boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

                        if(sameDay)
                            txtName.setText(ds.getValue(Game.class).getTittle());
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isMiBandConnected) {
                    Toast.makeText(NewMyGameActivity.this, "MiBand is not connected", Toast.LENGTH_LONG).show();
                    return;
                }

                if(txtName.getText().toString().equals("")){
                    Toast.makeText(NewMyGameActivity.this, "Provide a name for the game first", Toast.LENGTH_LONG).show();
                    return;
                }

                isGameStarted = true;
                myGame.setName(txtName.getText().toString());
                miBand.getSteps();
                startTimer();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isGameStarted){
                    Toast.makeText(NewMyGameActivity.this, "There is no game to stop", Toast.LENGTH_LONG).show();
                    return;
                }

                timer.cancel();

                myGame.setId(myRef.push().getKey());
                myGame.setUserID(user.getId());
                myGame.setDuration(seconds);
                myGame.setSteps(steps);
                myRef.child("myGames").child(myGame.getId()).setValue(myGame);

                user.addMyGame(myGame.getId());
                myRef.child("users").child(user.getId()).setValue(user);

                Toast.makeText(NewMyGameActivity.this, "Game is finished and saved!", Toast.LENGTH_LONG).show();
               setResult(android.app.Activity.RESULT_OK);
                finish();
            }
        });

        btnStopVibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                miBand.stopVibrate();
                btnStopVibration.setVisibility(View.INVISIBLE);
            }
        });

    }

    private void startTimer(){
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                seconds++;

                if(seconds % 15 == 0)
                    miBand.getSteps();

                if(seconds % 40 == 0)
                    miBand.startScanHeartRate();

                int minutes =seconds / 60;
                int seconds2 = seconds - minutes*60;

                String minutesS = String.valueOf(minutes);
                if(minutesS.length() == 1) minutesS = "0" + minutesS;
                String seconds2S = String.valueOf(seconds2);
                if(seconds2S.length() == 1) seconds2S = "0" + seconds2S;

                setDuration(minutesS, seconds2S);
            }
        },1000, 1000);
    }

    private void setDuration(final String min, final String sec){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtDuration.setText(min + ":" + sec);
            }
        });
    }

    @Override
    public void updateObserver(int mode, Object object) {
        switch (mode){

            //connected
            case 1:{
                isMiBandConnected = true;
                Toast.makeText(NewMyGameActivity.this, "MiBand is connected", Toast.LENGTH_LONG).show();
                break;
            }

            //bpm
            case 2:{
                final int heartBeat = Integer.valueOf(object.toString());
                if(heartBeat!= 0) {
                    myGame.addHeartBeat(heartBeat);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txtBPM.setText("BPM: " + heartBeat);
                        }
                    });

                    if (heartBeat <= 40 || heartBeat >= 200) {
                        miBand.startVibrate();
                        btnStopVibration.setVisibility(View.VISIBLE);
                    }
                }
                break;
            }

            //steps
            case 3:{
                if(stepsAtBeg){
                    stepsAtBeginning = Integer.valueOf(object.toString());
                    stepsAtBeg = false;
                }
                else{
                    steps = Integer.valueOf(object.toString()) - stepsAtBeginning;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txtSteps.setText("Steps: " + steps);
                        }
                    });
                }
                break;
            }
        }
    }
}



