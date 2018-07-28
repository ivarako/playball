package elfakrs.mosis.iva.playball;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        return view;
    }

}
