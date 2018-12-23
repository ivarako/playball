package elfakrs.mosis.iva.playball;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

import elfakrs.mosis.iva.playball.Model.Comment;
import elfakrs.mosis.iva.playball.Model.User;

public class CommentsFragment extends Fragment {

    private String userID;
    private String currentUserID;
    private Button btnScore;
    private LinearLayout lyComments;
    private TextView txtAverageScore;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private User user;
    private ArrayList<String> comments;
    private boolean bck;
    private String authorName;

    public CommentsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_comments, container, false);

        Bundle bundle = getArguments();
        userID = bundle.getString("userid");
        currentUserID = bundle.getString("currentUserid");

        btnScore = view.findViewById(R.id.btnScoreComm);
        lyComments = view.findViewById(R.id.lyComments);
        txtAverageScore = view.findViewById(R.id.txtScoreComments);

        comments = new ArrayList<>();
        bck = true;

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        DatabaseReference refCurrentUser = myRef.child("users").child(currentUserID);
        refCurrentUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                authorName = dataSnapshot.getValue(User.class).getName();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        DatabaseReference refUser = myRef.child("users").child(userID);
        refUser.addListenerForSingleValueEvent(new ValueEventListener() {
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

    private void setData(){
        if(user.getNumOfRatings() != 0)
            txtAverageScore.setText(String.format("%.2f",user.getScore() / user.getNumOfRatings()));
        else
            txtAverageScore.setText("0.00");

        if(user.getFriendsID().contains(currentUserID)){
            btnScore.setVisibility(View.VISIBLE);
            btnScore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final Dialog dialog = new Dialog(getActivity());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.alert_dialog_score);
                    Button submitBtn = (Button)dialog.findViewById(R.id.submitRateBtn);
                    Button cancelBtn = (Button)dialog.findViewById(R.id.cancelRateBtn);
                    final RatingBar ratingBar = (RatingBar)dialog.findViewById(R.id.ratingsBar);
                    final EditText reviewED = (EditText)dialog.findViewById(R.id.reviewED);

                    submitBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            String comment = reviewED.getText().toString();

                            if(!comment.equals("")){
                                Comment comm = new Comment();
                                comm.setText(comment);
                                comm.setDate(new Date());
                                comm.setUserID(userID);
                                comm.setAuthorID(currentUserID);
                                comm.setAuthorName(authorName);
                                comm.setId(myRef.push().getKey());
                                myRef.child("comments").child(comm.getId()).setValue(comm);

                                if(!user.getCommentsID().contains(comm.getId()))
                                    user.addComment(comm.getId());
                            }

                            float rating =  ratingBar.getRating();
                            user.setScore(user.getScore() + rating);
                            user.setNumOfRatings(user.getNumOfRatings()+1);

                            myRef.child("users").child(userID).setValue(user);
                            //setData();

                            dialog.cancel();
                            Toast.makeText(getActivity(), "Saved!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.cancel();
                        }
                    });

                    dialog.show();
                }
            });
        }

        DatabaseReference refComments = myRef.child("comments");
        refComments.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(user.getCommentsID().contains(ds.getValue(Comment.class).getId()) && !comments.contains(ds.getValue(Comment.class).getId())){

                        comments.add(ds.getValue(Comment.class).getId());
                        Comment comm = new Comment();
                        comm.setId(ds.getValue(Comment.class).getId());
                        comm.setAuthorID(ds.getValue(Comment.class).getAuthorID());
                        comm.setAuthorName(ds.getValue(Comment.class).getAuthorName());
                        comm.setDate(ds.getValue(Comment.class).getDate());
                        comm.setText(ds.getValue(Comment.class).getText());

                        TextView txt = new TextView(getActivity());
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins(30, 0, 30, 10);
                        txt.setLayoutParams(params);
                        txt.setTextSize(15);
                        txt.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        if(bck)
                            txt.setBackgroundColor(getResources().getColor(R.color.lightYellow));
                        else
                            txt.setBackgroundColor(getResources().getColor(R.color.lightOrange));
                        bck = !bck;

                        Date date = comm.getDate();
                        txt.setText(comm.getAuthorName() + ": " + comm.getText() + "\n" + String.valueOf(date.getDate()) + "/" + String.valueOf(date.getMonth()) + "/" + String.valueOf(date.getYear() + 1900));

                        lyComments.addView(txt);
                        txtAverageScore.setText(String.format("%.2f",user.getScore() / user.getNumOfRatings()));
                    }
                }
               }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

    }

}
