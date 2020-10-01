package elfak.mosis.zeljko.citzens_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    Button buttonLogin;
    Button buttonRegister;
    EditText editTextEmail,editTextPassword;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextPassword=findViewById(R.id.edit_text_password);
        editTextEmail= findViewById(R.id.edit_text_email);
        buttonLogin = findViewById(R.id.button_login);
        buttonRegister = findViewById(R.id.button_register);
        fAuth= FirebaseAuth.getInstance();

        if(fAuth.getCurrentUser()!=null)
        {
            startActivity(new Intent(getApplicationContext(),HomePage.class));          //ako je korisnik vec ulogovan
            finish();
        }

        FirebaseMessaging.getInstance().subscribeToTopic("event"); //in java code

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);      //otvara register activiry na dugme
                startActivity(intent);
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=editTextEmail.getText().toString().trim();
                String password=editTextPassword.getText().toString().trim();

                if(TextUtils.isEmpty(email))
                {   editTextEmail.setError("Email is required");  return; }

                if(TextUtils.isEmpty(password))
                {   editTextPassword.setError("Password is required");    return; }

                if(password.length()<6)
                {  editTextPassword.setError("Minimum 6 characters"); return;}


                fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this,"Logged in successfuly",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),HomePage.class));
                        }
                        else
                        {
                            Toast.makeText(MainActivity.this,"Error"+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            // progressBar.setVisibility(View.GONE);
                        }
                    }
                });

            }
        });

    }
}
