package elfak.mosis.zeljko.citzens_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;

public class HomePage extends AppCompatActivity {

    ImageButton btnMaps, btnFriends, btnCoins, btnProfile;
    Button btnLog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);


        btnMaps = findViewById(R.id.btnMaps);
        btnMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, MapsTrackerActivity.class);
                startActivityForResult(intent,1);
            }
        });

        btnFriends = findViewById(R.id.btnAddFriend);
        btnFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, FindFriends.class);
                startActivity(intent);
            }
        });

        btnCoins = findViewById(R.id.btnCoins);
        btnCoins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, Coins.class);
                startActivity(intent);
            }
        });

        btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, Profile.class);
                startActivity(intent);
            }
        });

        btnLog=findViewById(R.id.button_logout);

        btnLog.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            }
        });


    }
}
