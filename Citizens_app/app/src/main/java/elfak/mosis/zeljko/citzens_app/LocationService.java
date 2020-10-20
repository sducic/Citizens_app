package elfak.mosis.zeljko.citzens_app;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationService extends Service {

    private static final String TAG = "1003";
    private FusedLocationProviderClient mFusedLocationClient;
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersDatabaseRef;
    private LocationCallback locationCallback;

    private final static long UPDATE_INTERVAL = 4 * 1000;
    private final static long FASTEST_INTERVAL = 1000;
    private final static double earthRadius = 6371000;
    private static final int CHECK_NEARBY_OBJECTS_INTERVAL = 60000;
    private static final int DISTANCE_USERS = 2000;

    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    LatLng currUserPosition;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("UsersLocation");
        mAuth = FirebaseAuth.getInstance();

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "My Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        }

        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {

                Log.d(TAG, "onLocationResult: got location result.");

                Location location = locationResult.getLastLocation();

                if (location != null) {
                    saveUserLocation(location);
                }
            }

        };

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getLocation();
        checkNearbyObjectsRunnable();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void getLocation() {

        LocationRequest mLocationRequestHighAccuracy = new LocationRequest();
        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequestHighAccuracy.setInterval(UPDATE_INTERVAL);
        mLocationRequestHighAccuracy.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Please allow your location for using Location Service", Toast.LENGTH_LONG).show();
            Log.d(TAG, "getLocation: stopping the location service.");
            stopSelf();
            return;
        }

        mFusedLocationClient.requestLocationUpdates(mLocationRequestHighAccuracy, locationCallback, Looper.myLooper());
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }


    private void saveUserLocation(Location location) {

        if(!Profile.switchFlag) {
            stopLocationUpdates();
            return;
        }
        Map locationMap = new HashMap();
        locationMap.put(mAuth.getUid() + "/" + "longitude", location.getLongitude());
        locationMap.put(mAuth.getUid() + "/" + "latitude", location.getLatitude());
        mUsersDatabaseRef.updateChildren(locationMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

            }
        });

    }

    private void checkNearbyObjectsRunnable() {
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                if(Profile.switchFlag) {
                    Log.d(TAG, String.valueOf(Thread.currentThread().getId()));
                    checkNearbyObjects();
                    mHandler.postDelayed(mRunnable, CHECK_NEARBY_OBJECTS_INTERVAL);
                }
                else {
                    mHandler.removeCallbacks(mRunnable, null);
                }
            }
        }, CHECK_NEARBY_OBJECTS_INTERVAL);

    }

    private void checkNearbyObjects() {
        List<LatLng> usersPositions = new ArrayList<LatLng>();
        mUsersDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    if(ds.getKey().equals(mAuth.getUid()))
                        currUserPosition = new LatLng(ds.child("latitude").getValue(double.class), ds.child("longitude").getValue(double.class));
                    else
                        usersPositions.add(new LatLng(ds.child("latitude").getValue(double.class), ds.child("longitude").getValue(double.class)));
                }

                for(LatLng latLng : usersPositions) {
                    Log.e(TAG, String.valueOf(calculateDistance(currUserPosition, latLng)));
                    if (calculateDistance(currUserPosition, latLng) <= DISTANCE_USERS) {
                        NotificationHelper.sendNotificationNearbyObjects(getApplicationContext());
                        break;
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public double calculateDistance(LatLng a, LatLng b) {

        Location locA = new Location("pointA");
        locA.setLatitude(a.latitude);
        locA.setLongitude(a.longitude);

        Location locB = new Location("pointB");
        locB.setLatitude(b.latitude);
        locB.setLongitude(b.longitude);

        double distance = locA.distanceTo(locB);
        return distance;

    }

}