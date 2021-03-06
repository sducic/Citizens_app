package elfak.mosis.zeljko.citzens_app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ThrowOnExtraProperties;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsTrackerActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    Location currentLocation;
    SupportMapFragment mapFragment;
    FusedLocationProviderClient client;
    ArrayList<Marker> markerList = new ArrayList<Marker>();
    private Bundle extras;
    private Intent intent;


     DatabaseReference reference;
    private DatabaseReference latitude;
    private DatabaseReference longitude;
    private DatabaseReference mUsersDatabaseReference;


    private LocationManager manager;
    private FirebaseUser currentUser;
    private String user_id;
    private boolean flag;


    ArrayAdapter<UserLocation> adapter;


    private final int MIN_TIME = 1000; // 1 second
    private final int MIN_DISTANCE = 1; // 1 meter

    double lon, lat;

    String nameForMarker;
    String image;


    Marker myMarker;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_tracker);




        intent = getIntent();
        extras = intent.getExtras();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();

        latitude = FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("latitude");
        longitude = FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("longitude");
        mUsersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        user_id = getIntent().getStringExtra("user_id");
        flag = false;





        mapFragment = (SupportMapFragment)getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //initialize fused loc
        client = LocationServices.getFusedLocationProviderClient(this);
        if(ActivityCompat.checkSelfPermission(MapsTrackerActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            //call method
            getCurrentLocUsers();
            ////ar
           // getCurrentLocationForAR();
           // getArObject();
           // compareArLocations();



        } else {
            //when permission denied

            ActivityCompat.requestPermissions(MapsTrackerActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);

        }


        manager = (LocationManager) getSystemService(LOCATION_SERVICE);



        reference = FirebaseDatabase.getInstance().getReference().child("User-Location");



        getLocationUpdates();





    }






     private void getCurrentLocUsers(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                removeMarkers();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                        lat = snapshot.child("latitude").getValue(double.class);
                        lon = snapshot.child("longitude").getValue(double.class);

                        nameForMarker = snapshot.child("fullName").getValue(String.class);

                        LatLng latLng = new LatLng(lat, lon);

                        MarkerOptions options = new MarkerOptions().position(latLng).title(nameForMarker);


                      /*  if(extras != null) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                            Toast.makeText(getApplicationContext(), "nesto", Toast.LENGTH_SHORT).show();
                        }
                        else{*/
                      if(user_id != null) {
                          if (user_id.equals(snapshot.getKey())) {
                              flag = true;
                              mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                          }
                      }

                      if(!flag)
                          mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));

                      markerList.add(mMap.addMarker(options));


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }




    private void getLocationUpdates() {
        if(manager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                } else if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                } else {
                    Toast.makeText(this, "No provide Enabled", Toast.LENGTH_SHORT).show();
                }
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        }
    }

    private void removeMarkers() {
        for(Marker marker : markerList) {
            marker.remove();
        }
        markerList.clear();
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {



                getCurrentLocUsers();



            }
        }
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
        getLocationUpdates();


    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null)
        {
            saveLocation(location);
        } else {
            Toast.makeText(this,"No location.",Toast.LENGTH_SHORT).show();
        }
    }

    private void saveLocation(Location location) {
        latitude.setValue(location.getLatitude());
        longitude.setValue(location.getLongitude());
    }

    @Override
    public void onBackPressed() {
        mMap.clear();

        Toast.makeText(getApplicationContext(),"Home page",Toast.LENGTH_SHORT).show();
        finish();
        return;
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
