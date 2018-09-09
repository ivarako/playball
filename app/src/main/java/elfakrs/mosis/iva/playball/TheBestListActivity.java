package elfakrs.mosis.iva.playball;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import elfakrs.mosis.iva.playball.Model.User;


public class TheBestListActivity extends AppCompatActivity {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private ArrayList<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_the_best_list);

        users = new ArrayList<>();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        DatabaseReference refUsers = myRef.child("users");
        refUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    User u = new User();
                    u.setId(ds.getValue(User.class).getId());
                    u.setName(ds.getValue(User.class).getName());
                    u.setScore(ds.getValue(User.class).getScore());
                    u.setNumOfRatings(ds.getValue(User.class).getNumOfRatings());

                    if(!users.contains(u))
                        users.add(u);
                }

                   Collections.sort(users, new Comparator<User>() {
                       @Override
                       public int compare(User u1, User u2) {

                           if(u1.getNumOfRatings() == 0)
                               return 1;
                           if(u2.getNumOfRatings() == 0)
                               return -1;

                           if(u1.getScore()/u1.getNumOfRatings() != u2.getScore()/u2.getNumOfRatings())
                            return Float.compare(u2.getScore()/u2.getNumOfRatings(), u1.getScore()/u1.getNumOfRatings());
                           else
                            return u2.getNumOfRatings() - u1.getNumOfRatings(); }});

                List<User> sortedUsers = new ArrayList<>();
                if (users.size() > 10)
                    sortedUsers = users.subList(0, 10);
                else
                    sortedUsers = users.subList(0, users.size());
                ArrayList<User> tmp = new ArrayList<>(sortedUsers);
                putData(tmp); }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void putData(ArrayList<User> users)
    {
        TableLayout tl = findViewById(R.id.tblBest);
        for(int i = 0; i<users.size(); i++)
        {
            TableRow tr = new TableRow(TheBestListActivity.this);
            tr.setTag(users.get(i).getId());
            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            if(i%2 == 0)
                tr.setBackgroundColor(getResources().getColor(R.color.lightOrange));

            TextView txtRank = new TextView(TheBestListActivity.this);
            txtRank.setText(String.valueOf(i + 1) + ".");
            txtRank.setTextSize(17);
            txtRank.setGravity(Gravity.CENTER);
            txtRank.setTextColor(getResources().getColor(R.color.black));
            tr.addView(txtRank);

            TextView txtName = new TextView(TheBestListActivity.this);
            txtName.setText(users.get(i).getName());
            txtName.setTextSize(17);
            txtName.setTextColor(getResources().getColor(R.color.black));
            txtName.setGravity(Gravity.CENTER);
            tr.addView(txtName);

            TextView txtScore = new TextView(TheBestListActivity.this);
            if(users.get(i).getNumOfRatings() == 0)
                txtScore.setText("0.00");
            else
                 txtScore.setText(String.format("%.2f", users.get(i).getScore() / users.get(i).getNumOfRatings()));
            txtScore.setTextSize(17);
            txtScore.setTextColor(getResources().getColor(R.color.black));
            txtScore.setGravity(Gravity.CENTER);
            tr.addView(txtScore);

            TextView txtNum = new TextView(TheBestListActivity.this);
            txtNum.setText(String.valueOf(users.get(i).getNumOfRatings()));
            txtNum.setTextColor(getResources().getColor(R.color.black));
            txtNum.setTextSize(17);
            txtNum.setGravity(Gravity.CENTER);
            tr.addView(txtNum);

            tl.addView(tr, new TableLayout.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        }
    }

    public void BestBack(View v)
    {
        finish();
    }
}
