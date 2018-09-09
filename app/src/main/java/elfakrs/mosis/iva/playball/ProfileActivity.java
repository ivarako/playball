package elfakrs.mosis.iva.playball;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private SectionsPageAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private String userID;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSectionsPagerAdapter = new SectionsPageAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        Intent listIntent = getIntent();
        Bundle idBundle = listIntent.getExtras();
        userID = idBundle.getString("userid");
        currentUserID = idBundle.getString("currentUserid");

        setUpViewPager(mViewPager);
    }

    private void setUpViewPager (ViewPager viewPager){
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());

        Bundle idBundle = new Bundle();
        idBundle.putString("userid", userID);
        idBundle.putString("currentUserid", currentUserID);

        BasicInfoFragment basicInfoFragment = new BasicInfoFragment();
        basicInfoFragment.setArguments(idBundle);

        FriendsFragment friendsFragment = new FriendsFragment();
        friendsFragment.setArguments(idBundle);

        CommentsFragment commentsFragment = new CommentsFragment();
        commentsFragment.setArguments(idBundle);

        GamesFragment gamesFragment = new GamesFragment();
        gamesFragment.setArguments(idBundle);

        adapter.addFragment(basicInfoFragment, "Info");
        adapter.addFragment(commentsFragment, "Rates");
        adapter.addFragment(friendsFragment, "Friends");
        adapter.addFragment(gamesFragment, "Games");

        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
       finish();
       return true;
    }

}
