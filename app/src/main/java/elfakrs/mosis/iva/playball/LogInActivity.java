package elfakrs.mosis.iva.playball;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LogInActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();

       FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null)
        {
            Intent i = new Intent(LogInActivity.this, HomeActivity.class);
            Bundle idBundle = new Bundle();
            idBundle.putString("userid", currentUser.getUid());
            i.putExtras(idBundle);
            startActivity(i);
        }
    }

    public void LogIn(View v)
    {
        EditText editTextEmail = (EditText) findViewById(R.id.editTxtEmail);
        EditText editTextPassword = (EditText) findViewById(R.id.editTxtPassword);

        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        if(!email.equals("") && !password.equals(""))
        {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                Intent i = new Intent(LogInActivity.this, HomeActivity.class);
                                Bundle idBundle = new Bundle();
                                idBundle.putString("userid", user.getUid());
                                i.putExtras(idBundle);
                                startActivity(i);
                            }
                            else {
                                Toast.makeText(LogInActivity.this, "Authentication failed.",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        else
            Toast.makeText(LogInActivity.this, "Enter your email and password first.", Toast.LENGTH_SHORT).show();
    }

    public void SignUp(View v)
    {
        Intent i = new Intent(LogInActivity.this, SignUpActivity.class);
        startActivity(i);
    }

}
