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
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    Button buttonCreate;
    EditText editTextEmail, editTextFullname, editTextPhone, editTextPassword1, editTextPassword2;

    FirebaseAuth fAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextPassword1=findViewById(R.id.edit_text_password1);
        editTextPassword2=findViewById(R.id.edit_text_password2);
        editTextFullname=findViewById(R.id.edit_text_fullname);
        editTextPhone=findViewById(R.id.edit_text_phone);
        editTextEmail= findViewById(R.id.edit_text_email);
        buttonCreate = findViewById(R.id.button_create);
        fAuth=FirebaseAuth.getInstance();
        progressBar=findViewById(R.id.progressBar);


        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email=editTextEmail.getText().toString().trim();
                final String fullName=editTextFullname.getText().toString().trim();
                final String phoneNumber=editTextPhone.getText().toString().trim();
                String password1=editTextPassword1.getText().toString().trim();
                String password2=editTextPassword2.getText().toString().trim();

                if(TextUtils.isEmpty(email))
                {   editTextEmail.setError("Email is required");  return; }

                if(TextUtils.isEmpty(password1))
                {   editTextPassword1.setError("Password is required");    return; }
                if(TextUtils.isEmpty(password2))
                {   editTextPassword2.setError("Repeat password");    return; }

                if(password1.length()<6)
                {  editTextPassword1.setError("Minimum 6 characters"); return;}
                if(password2.length()<6)
                {  editTextPassword2.setError("Minimum 6 characters"); return;}

                if(TextUtils.isEmpty(fullName))
                {   editTextFullname.setError("FullName is required");  return; }

                if(TextUtils.isEmpty(phoneNumber))
                {   editTextPhone.setError("PhoneNumber is required");  return; }

                if(!password1.equals(password2))
                {
                    editTextPassword1.setError("Password don't match");    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                fAuth.createUserWithEmailAndPassword(email,password1).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //Toast.makeText(Register.this,"User created",Toast.LENGTH_SHORT).show();
                            // startActivity(new Intent(getApplicationContext(),MainActivity.class));
                            User user = new User(
                                    fullName,
                                    email,
                                    phoneNumber

                            );

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this,"User created",Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                    } else {
                                        Toast.makeText(RegisterActivity.this,"Error"+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(RegisterActivity.this,"Error"+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });



            }
        });
    }
}
