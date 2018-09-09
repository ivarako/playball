package elfakrs.mosis.iva.playball;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import elfakrs.mosis.iva.playball.Model.MyGame;
import elfakrs.mosis.iva.playball.Model.User;

//men - [(Age x 0.2017) - (Weight x 0.09036) + (Heart Rate x 0.6309) - 55.0969] x Time / 4.184
//women - [(Age x 0.074) - (Weight x 0.05741) + (Heart Rate x 0.4472) — 20.4022] x Time / 4.184
//Age in years. Weight in pounds. HR in beats per minute. Time in minutes.

public class MyGamesActivity extends AppCompatActivity {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private String userID;
    private ArrayList<String> gamesID;
    private LinearLayout lly;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_games);

        lly = findViewById(R.id.llyMyGames);

        gamesID = new ArrayList<>();

        Intent listIntent = getIntent();
        Bundle idBundle = listIntent.getExtras();
        userID = idBundle.getString("userid");

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        final DatabaseReference refUser = myRef.child("users").child(userID);
        refUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);

                if(user.getMass() == 0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(MyGamesActivity.this);
                    builder.setTitle("Your mass");
                    builder.setMessage("Enter your mass in kilograms for calculation of burned calories");
                    final EditText input = new EditText(MyGamesActivity.this);
                    input.setGravity(Gravity.LEFT);
                    input.setImeOptions(EditorInfo.IME_ACTION_DONE);
                    builder.setView(input);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                           user.setMass(Integer.valueOf(input.getText().toString()));
                           refUser.child("mass").setValue(user.getMass());
                           setData();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else
                {
                    setData();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        Button btnNewGame = findViewById(R.id.btnMyNewGame);
        btnNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MyGamesActivity.this, NewMyGameActivity.class);
                startActivityForResult(i, 1);
            }
        });

    }

    private void setData(){
        DatabaseReference refGames = myRef.child("myGames");
        refGames.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(ds.getValue(MyGame.class).getUserID().equals(userID) && !gamesID.contains(ds.getKey())){

                        gamesID.add(ds.getKey());
                        MyGame game = new MyGame();
                        game.setDuration(ds.getValue(MyGame.class).getDuration());
                        game.setSteps(ds.getValue(MyGame.class).getSteps());
                        game.setName(ds.getValue(MyGame.class).getName());
                        game.setHeartBeats(ds.getValue(MyGame.class).getHeartBeats());
                        game.setDate(ds.getValue(MyGame.class).getDate());

                        LinearLayout llyGame = new LinearLayout(MyGamesActivity.this);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(900, LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins(90, 10, 10, 10);
                        llyGame.setPadding(15, 5, 5, 5);
                        llyGame.setGravity(Gravity.CENTER);
                        llyGame.setLayoutParams(params);
                        llyGame.setOrientation(LinearLayout.VERTICAL);
                        llyGame.setBackgroundColor(getResources().getColor(R.color.lightYellow));

                        TextView txtName = new TextView(MyGamesActivity.this);
                        txtName.setText("Name: " + game.getName());
                        txtName.setTextColor(getResources().getColor(R.color.black));
                        llyGame.addView(txtName);

                        TextView txtDateAndDuration = new TextView(MyGamesActivity.this);
                        txtDateAndDuration.setTextColor(getResources().getColor(R.color.black));
                        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                        String reportDate = df.format(game.getDate());
                        int minutes = game.getDuration() / 60;
                        int seconds = game.getDuration() - minutes*60;
                        if(minutes>60){
                            int hours = minutes / 60;
                            minutes = minutes - hours * 60;
                            txtDateAndDuration.setText(reportDate + "for " + hours + " hour(s), " + minutes + " min(s) and " + seconds + " secs");
                        }
                        else
                            txtDateAndDuration.setText(reportDate + " for  " + minutes + " min(s) and " + seconds + " secs");
                        llyGame.addView(txtDateAndDuration);

                        TextView txtSteps = new TextView(MyGamesActivity.this);
                        txtSteps.setTextColor(getResources().getColor(R.color.black));
                        txtSteps.setText("Steps: " + game.getSteps());
                        llyGame.addView(txtSteps);

                        int sumHeartBeats = 0;
                        int minHeatBeat = 1000;
                        int maxHeartBeat = 0;
                        int beat, averageBeat;

                        for(int i = 0; i<game.getHeartBeats().size(); i++){
                            beat = game.getHeartBeats().get(i);

                            if(beat > maxHeartBeat)
                                maxHeartBeat = beat;

                            if(beat< minHeatBeat)
                                minHeatBeat = beat;

                            sumHeartBeats += beat;
                        }
                        averageBeat = sumHeartBeats / game.getHeartBeats().size();

                        double calories = 0;
                        if(user.getSex().equals("Male")){
                            //[(Age x 0.2017) - (Weight x 0.09036) + (Heart Rate x 0.6309) - 55.0969] x Time / 4.184
                            //1 kg = 2.204 lb
                            calories = ((user.getAge() * 0.2017) - (user.getMass() * 2.204 * 0.09036) + (averageBeat * 0.6309) - 55.0969) * game.getDuration() / 4.184;
                        }
                        else{
                            //[(Age x 0.074) - (Weight x 0.05741) + (Heart Rate x 0.4472) — 20.4022] x Time / 4.184
                            calories = ((user.getAge() * 0.074) - (user.getMass() * 2.204 * 0.05741) + (averageBeat * 0.4472) - 20.4022) * (game.getDuration()/60.0)  / 4.184;
                        }

                        if(calories<0) calories = 0;
                        DecimalFormat decimalFormat = new DecimalFormat("#.###");

                        TextView txtCalories = new TextView(MyGamesActivity.this);
                        txtCalories.setTextColor(getResources().getColor(R.color.black));
                        txtCalories.setText("Burned calories: " +  decimalFormat.format(calories));
                        llyGame.addView(txtCalories);

                        TextView txtBeats = new TextView(MyGamesActivity.this);
                        txtBeats.setTextColor(getResources().getColor(R.color.black));
                        txtBeats.setText("Average BPM: " + averageBeat + " max BPM: " + maxHeartBeat + " min BPM: " + minHeatBeat);
                        llyGame.addView(txtBeats);

                        lly.addView(llyGame);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if(resultCode == android.app.Activity.RESULT_OK){
            setData();
        }
    }
}
