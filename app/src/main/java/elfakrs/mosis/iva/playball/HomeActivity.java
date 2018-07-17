package elfakrs.mosis.iva.playball;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private String userID;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent listIntent = getIntent();
        Bundle idBundle = listIntent.getExtras();
        userID = idBundle.getString("userid");

        mAuth = FirebaseAuth.getInstance();

        navigationView = (NavigationView)findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        toggle= new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return (true);
        }
        return(super.onOptionsItemSelected(item));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        drawerLayout.closeDrawers();

        switch (item.getItemId()){

            case R.id.itemMyProfile: {

                break;
            }

            case R.id.itemNewGame:{

                Intent i = new Intent(HomeActivity.this, NewGameActivity.class);
                Bundle idBundle = new Bundle();
                idBundle.putString("userid", userID);
                i.putExtras(idBundle);
                startActivity(i);
                break;
            }

            case R.id.itemViewMap:{

                Intent i = new Intent(HomeActivity.this, MapsActivity.class);
                i.putExtra("state", MapsActivity.SHOW_MAP);
                startActivity(i);
                break;
            }

            case R.id.itemTheBest:{

                Intent i = new Intent(HomeActivity.this, TheBestListActivity.class);
                startActivity(i);
                break;
            }

            case R.id.itemSettings:{
                Intent i = new Intent(HomeActivity.this, SettingsActivity.class);
                Bundle idBundle = new Bundle();
                idBundle.putString("userid", userID);
                i.putExtras(idBundle);
                startActivity(i);
                break;
            }

            case R.id.itemLogOut:{

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Log out");
                builder.setMessage("Are you sure you want to log out?");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.signOut();
                        Intent i = new Intent(HomeActivity.this, LogInActivity.class);
                        startActivity(i);
                        finish();
                    }
                });

                builder.setNegativeButton("No", null);

                AlertDialog dialog = builder.create();
                dialog.show();

                break;
            }
        }
        return true;
    }
}
