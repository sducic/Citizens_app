package elfak.mosis.zeljko.citzens_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Maps extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap map;
    SupportMapFragment mapFragment;
    SearchView searchView;

    Location currentLocation;
    FusedLocationProviderClient client;


    Button addObject;
    private boolean selCoorsEnabled = false;
    private LatLng placeLoc;
    public static final int SELECT_COORDINATES = 2;
    private int state = 0;
    static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    public static final int SHOW_MAP = 0;
    public static final int CENTER_PLACE_ON_MAP = 1;


    boolean toAddObject = false;
    DatabaseReference database;
    int position = -1;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        database = FirebaseDatabase.getInstance().getReference().child("my-objects");

        Intent listIntent = getIntent();
        Bundle positionBundle = listIntent.getExtras();
        if(positionBundle != null)
            position = positionBundle.getInt("position");




        searchView = findViewById(R.id.sv_location);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);


        //initialize fused loc
        client = LocationServices.getFusedLocationProviderClient(this);

        //check permission
        if(ActivityCompat.checkSelfPermission(Maps.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            //call method
            getCurrentLocation();
            setOnMapClickListener();
        } else {
            //when permission denied

            ActivityCompat.requestPermissions(Maps.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);

        }


        ///Searching

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchView.getQuery().toString();
                List<Address> addressList = null;

                if(location != null || location.equals("")){
                    Geocoder geocoder = new Geocoder(Maps.this);
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    }catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());
                    map.addMarker(new MarkerOptions().position(latLng).title(location));
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
                }


                return  false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mapFragment.getMapAsync(this);




        //add object on click on map

        addObject = findViewById(R.id.add_object_btn);

        addObject.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if(!selCoorsEnabled) {
                    Toast.makeText(getApplicationContext(),"Select coord",Toast.LENGTH_SHORT).show();
                    selCoorsEnabled = true;
                    setOnMapClickListener();
                                 }


            }
        });



    }

    private void getCurrentLocation() {
        //initialize task location

        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                //when success
                if(location != null) {
                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            //initialize lat lng
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                            //create marker
                            MarkerOptions options = new MarkerOptions().position(latLng).title("I am there");
                            //zoom
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
                            googleMap.addMarker(options);

                        }
                    });
                }
            }
        });


    }


    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
        addObjectMarkers();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 44){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }

    }


    private HashMap<Marker, Integer> markerPlaceIdMap;
    private void addObjectMarkers()
    {
        ArrayList<Object> objects = MyObjectData.getInstance().getMyObjects();
        markerPlaceIdMap = new HashMap<Marker,Integer>((int)((double)objects.size()*1.2));


        String br = Integer.toString(objects.size());
        Toast.makeText(getApplicationContext(),br,Toast.LENGTH_SHORT).show();

        for(int i = 0;i<objects.size();i++) {
            Object object = objects.get(i);
            Toast.makeText(getApplicationContext(),"nssssss",Toast.LENGTH_SHORT).show();
             String lat = object.getLatitude();
             String lon = object.getLongitude();
            LatLng loc = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(loc);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.coins));
            markerOptions.title(object.getName());
            Marker marker = map.addMarker(markerOptions);
            markerPlaceIdMap.put(marker, i);



        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try{
            if(resultCode == Activity.RESULT_OK)
            {
                addObjectMarkers();
            }
        }catch(Exception e)
        {

        }
    }


    private void setOnMapClickListener(){

        if(map!=null) {


            map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                @Override
                public void onMapClick(LatLng latLng) {



                        String lon = Double.toString(latLng.longitude);
                        String lat = Double.toString(latLng.latitude);
                        Intent locationIntent = new Intent();
                        locationIntent.putExtra("lon", lon);
                        locationIntent.putExtra("lat", lat);
                        setResult(Activity.RESULT_OK, locationIntent);


                        Toast.makeText(getApplicationContext(), lon + " : " + lat, Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getApplicationContext(),AddObject.class);
                        i.putExtra("Longitude",lon);
                        i.putExtra("Latitude", lat);
                        startActivityForResult(i,1);

                        finish();





                }
            });
        }
    }




    ////////////////////////////////////////////////////////////////////////


}
