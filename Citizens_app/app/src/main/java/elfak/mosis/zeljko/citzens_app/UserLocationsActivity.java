
package elfak.mosis.zeljko.citzens_app;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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

public class UserLocationsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int REQUEST_CODE_ASK_PERMISSIONS = 1803;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location myLocation;
    private Marker myMarker;
    private boolean isLocationServiceRunning;
    private boolean onCreateZoom;

    private DatabaseReference mLocationsDatabaseRef;
    private List<Marker> markerList;
    private HashMap<String,String> mMarkerMap;
    private String user_id;
    private FirebaseAuth mAuth;
    private ImageView icLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_locations);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLocationsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("UsersLocation");
        onCreateZoom = true;
        markerList = new ArrayList<>();
        mMarkerMap = new HashMap<>();
        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getUid();
        icLocation = (ImageView)findViewById(R.id.ic_location);
        isLocationServiceRunning = LocationServiceHelper.isLocationServiceRunning(getApplicationContext());

        if(requestPermission()) {
            if(!isLocationServiceRunning) {
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
                buildLocationCallback();
                buildLocationRequest();
            }
        }


        retrieveUsersLocations();

        icLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomToMyLocation();
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!isLocationServiceRunning && locationCallback != null && locationRequest != null && fusedLocationProviderClient != null)
        {
            stopLocationsUpdates();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isLocationServiceRunning && locationCallback != null && locationRequest != null && fusedLocationProviderClient != null)
            startLocationUpdates();

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(myLocation != null) {
            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            myMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Me"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));


        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String usid = mMarkerMap.get(marker.getId());
                if(usid != null) {
                    Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
                    intent.putExtra("user_id", usid);
                    startActivity(intent);
                }
                return false;
            }
        });


    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if(locationResult != null) {
                    myLocation = locationResult.getLastLocation();
                    replaceMyMarker(myLocation);
                }
            }
        };
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setSmallestDisplacement(1);
    }

    private void startLocationUpdates() {
        fusedLocationProviderClient
                .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationsUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void replaceMyMarker(Location loc) {
        if(loc != null) {
            if(myMarker != null)
                myMarker.remove();
            LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
            myMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Me")
                    .icon(getMarkerIcon("#447dc9")));
            if(onCreateZoom) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                onCreateZoom=false;
            }

        }
    }

    private void retrieveUsersLocations() {
        mLocationsDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                removeMarkers();
                mMarkerMap.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()) {

                    if(ds.getKey().equals(user_id) && !isLocationServiceRunning)
                        continue;

                    double lon = ds.child("longitude").getValue(double.class);
                    double lat = ds.child("latitude").getValue(double.class);

                    LatLng latLng = new LatLng(lat, lon);
                    MarkerOptions options = new MarkerOptions().position(latLng);
                    Bitmap bmp = HomePage.profileImages.get(ds.getKey());
                    if (bmp != null) {
                        options.icon(BitmapDescriptorFactory.fromBitmap(bmp));
                        options.title("Friend");
                    }
                    else if (ds.getKey().equals(user_id)){
                        options.title("Me");
                        options.icon(getMarkerIcon("#447dc9"));
                        myLocation = new Location("dbp");
                        myLocation.setLatitude(lat);
                        myLocation.setLongitude(lon);
                    }
                    else {
                        options.title("User");
                    }

                    Marker marker = mMap.addMarker(options);
                    markerList.add(marker);
                    mMarkerMap.put(marker.getId(), ds.getKey());

                    if(ds.getKey().equals(user_id) && onCreateZoom) {
                        onCreateZoom = false;
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
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

    private void zoomToMyLocation() {
        if(myLocation != null) {
            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
        }
    }

    public BitmapDescriptor getMarkerIcon(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }

    private boolean requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat
                    .requestPermissions(UserLocationsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ASK_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
                buildLocationCallback();
                buildLocationRequest();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

