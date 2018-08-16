package elfakrs.mosis.iva.playball;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class FriendsFragment extends Fragment {

    private String userID;
    private String currentUserID;

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
                    startActivity(i);
                }
            });
        }

        return view;
    }


}
