package elfak.mosis.zeljko.citzens_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class HomePage extends AppCompatActivity {

    CardView btnMaps, btnFriends, btnCoins, btnProfile,btnNews,btnReport;
    CardView btnLog;

    public static final String CHANNEL_ID="Citizens_app";
    public static final String CHANNEL_NAME="Citizens_application";
    public static final String CHANNEL_DESC = "Citizens_desc";

    private DatabaseReference mUsersDatabaseRef;
    private StorageReference mImagesStorageRef;
    private FirebaseAuth mAuth;
    public ArrayList<String> friendsIds = new ArrayList<>();
    public static HashMap<String, Bitmap> profileImages = new HashMap<>();
    private ProgressBar mProgressBar;

    int LOCATION_REQUEST_CODE = 10002;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page2);


        createNotificationChannel();
        mProgressBar = (ProgressBar)findViewById(R.id.fetching_progress);


        btnMaps = findViewById(R.id.btnMaps);
        btnMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, MapsActivityZara.class);
                startActivityForResult(intent,1);

            }
        });

        btnFriends = findViewById(R.id.btnAddFriend);
        btnFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, AddFriend.class);
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

        btnNews = findViewById(R.id.btnNews);
        btnNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, FeedActivity.class);
                startActivity(intent);
            }
        });


        btnReport = findViewById(R.id.btnReport);
        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, Maps.class);
                startActivity(intent);
            }
        });

        initLocationService();

        mUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        mImagesStorageRef = FirebaseStorage.getInstance().getReference().child("profileImages");
        mAuth = FirebaseAuth.getInstance();

        readFriends(new FriendsCallback() {
            @Override
            public void onCallback(ArrayList<String> ids) {
                mProgressBar.setVisibility(View.VISIBLE);
                downloadImages(new ImagesCallback() {
                    @Override
                    public void onCallback(String id, Bitmap bmp) {
                        profileImages.put(id, bmp);
                    }
                },ids);
                mProgressBar.setVisibility(View.GONE);
            }

        });


    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            NotificationChannel nc = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            nc.setDescription(CHANNEL_DESC);
            NotificationManager mngr = getSystemService(NotificationManager.class);
            mngr.createNotificationChannel(nc);

        }
    }

    private void initLocationService() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean locked = prefs.getBoolean("locked", false);
        if(locked) {
            Profile.switchFlag=true;
            LocationServiceHelper.startLocationService(getApplicationContext());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.homepagemenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
        return super.onOptionsItemSelected(item);
    }

    private void readFriends(FriendsCallback fbc) {
        mUsersDatabaseRef.child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendsIds.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    friendsIds.add(ds.getKey());
                }
                fbc.onCallback(friendsIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void downloadImages(ImagesCallback icb, ArrayList<String> ids) {
        for(String s : ids) {
            mImagesStorageRef.child(s + ".jpeg").getBytes(5*1024*1024).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                @Override
                public void onComplete(@NonNull Task<byte[]> task) {
                    if(task.isSuccessful()) {
                        byte[] data = task.getResult();
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Bitmap scaledBmp = Bitmap.createScaledBitmap(bmp, 100,100,false);
                        icb.onCallback(s,createBitmap(scaledBmp));


                    }
                }
            });
        }

    }

    private Bitmap createBitmap(Bitmap bitmap) {
        Bitmap result = null;
        try{
            result = Bitmap.createBitmap(70, 110, Bitmap.Config.ARGB_8888);
            result.eraseColor(Color.TRANSPARENT);
            Canvas canvas = new Canvas(result);
            Drawable drawable = getResources().getDrawable(R.drawable.circle);
            drawable.setBounds(0, 0, 70, 110);
            drawable.draw(canvas);

            Paint roundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            RectF bitmapRect = new RectF();
            canvas.save();

            //Bitmap bitmap = BitmapFactory.decodeFile(path.toString()); /*generate bitmap here if your image comes from any url*/
            if (bitmap != null) {
                BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                Matrix matrix = new Matrix();
                float scale = 70 / (float) bitmap.getWidth();
                matrix.postTranslate(5, 5);
                matrix.postScale(scale, scale);
                roundPaint.setShader(shader);
                shader.setLocalMatrix(matrix);
                bitmapRect.set(5, 5, 60 + 5, 60 + 5);
                canvas.drawRoundRect(bitmapRect, 26, 26, roundPaint);

            }
            canvas.restore();
            try {
                canvas.setBitmap(null);
            } catch (Exception e) {}
        }
        catch(Throwable t) {
            t.printStackTrace();
        }

        return result;
    }


    private interface FriendsCallback {
        void onCallback(ArrayList<String> ids);
    }

    private interface ImagesCallback {
        void onCallback(String id, Bitmap bmp);
    }

}
