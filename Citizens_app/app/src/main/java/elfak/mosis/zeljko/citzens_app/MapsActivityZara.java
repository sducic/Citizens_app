package elfak.mosis.zeljko.citzens_app;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ThrowOnExtraProperties;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivityZara extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private DatabaseReference mLocationsDatabaseRef;
    private DatabaseReference mFriendsDatabaseRef;
    private StorageReference mUsersImagesStorage;
    private List<Marker> markerList = new ArrayList<Marker>();
    private FirebaseAuth mAuth;
    private LocationManager locationManager;
    private Location myLocation;
    private HashMap<String, Bitmap> friendsImages;
    private HashMap<String, String> mMarkerMap;
    private Marker myMarker;
    private String focusUserId;
    private boolean focusFlag;

    private final int MIN_TIME = 500;
    private final int MIN_DISTANCE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_zara);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLocationsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("UsersLocation");
        mUsersImagesStorage = FirebaseStorage.getInstance().getReference().child("profileImages");
        mFriendsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        mMarkerMap = new HashMap<>();
        focusUserId = getIntent().getStringExtra("user_id");
        if(focusUserId == null)
            focusFlag=false;
        else
            focusFlag=true;

        mAuth = FirebaseAuth.getInstance();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        friendsImages = new HashMap<String, Bitmap>();
        locationManagerInit();
        Toast.makeText(getApplicationContext(), String.valueOf(HomePage.profileImages.size()), Toast.LENGTH_SHORT).show();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(myLocation != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            prefs.edit().putLong("myLatitude", (long) myLocation.getLatitude()).apply();
            prefs.edit().putLong("myLongitude", (long) myLocation.getLongitude()).apply();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        retrieveUsersLocations();
        // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 12));
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String user_id = mMarkerMap.get(marker.getId());
                if (user_id != null) {
                    Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
                    intent.putExtra("user_id", user_id);
                    startActivity(intent);
                }
                return false;
            }
        });


        LatLng latLng;
        if(myLocation == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            double lat = (double)prefs.getLong("myLatitude", 0);
            double lon = (double)prefs.getLong("myLongitude", 0);
            latLng = new LatLng(lat,lon);
        }
        else {
            latLng =  new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        }

        MarkerOptions options = new MarkerOptions().position(latLng).title("Me");
        myMarker = mMap.addMarker(options);
        if(!focusFlag)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,12));

    }

    private void retrieveUsersLocations() {
        mLocationsDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                removeMarkers();
                mMarkerMap.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()) {

                    if(!ds.getKey().equals(mAuth.getUid())) {

                        double lon = ds.child("longitude").getValue(double.class);
                        double lat = ds.child("latitude").getValue(double.class);

                        LatLng latLng = new LatLng(lat, lon);
                        MarkerOptions options = new MarkerOptions().position(latLng).title("EberFirth");
                        Bitmap bmp = HomePage.profileImages.get(ds.getKey());
                        if (bmp != null) {
                            options.icon(BitmapDescriptorFactory.fromBitmap(bmp));
                        }
                        Marker marker = mMap.addMarker(options);
                        markerList.add(marker);
                        mMarkerMap.put(marker.getId(), ds.getKey());

                        if(ds.getKey().equals(focusUserId) && focusFlag) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
                            focusFlag=false;
                        }
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void removeMarkers() {
        for(Marker marker : markerList) {
            marker.remove();
        }
        markerList.clear();
    }

    private void locationManagerInit() {
        if(locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                } else {
                    Toast.makeText(this, "No provider Enabled", Toast.LENGTH_SHORT).show();
                }
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }

            myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
            if(myMarker != null)
                myMarker.remove();
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions options = new MarkerOptions().position(latLng).title("Me");
            myMarker = mMap.addMarker(options);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
