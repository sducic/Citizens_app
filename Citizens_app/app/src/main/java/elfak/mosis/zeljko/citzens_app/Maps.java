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
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Maps extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap map;
    SupportMapFragment mapFragment;
    SearchView searchView;

    Location currentLocation;
    FusedLocationProviderClient client;

    double lon,lat;

    Button addObject;
    Button radius;
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

    Location currentLoc;
    List<Marker> AllMarkers = new ArrayList<Marker>();

    EditText textMeters;
    Circle circle;
    List<Circle> circles;

    User user;

    Bundle extras;
    Intent intent;

    Location userLoc;
    List<Location> locationsAR;
    double curLat;
    double curLon;



    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


         intent = getIntent();
         extras = intent.getExtras();

        database = FirebaseDatabase.getInstance().getReference().child("my-objects");


        locationsAR = new ArrayList<Location>();
        userLoc = new Location(LOCATION_SERVICE);






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
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            //call method
            if(extras == null){

                getCurrentLocation();
            }
            getProblemsLocations();
            setOnMapClickListener();
           // getArObject();




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


        circles = new ArrayList<>();


        currentLoc = new Location(NETWORK_STATS_SERVICE);
        radius = findViewById(R.id.radius);
        radius.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                funcForCall();
            }
        });



        ////////////////




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
                            MarkerOptions options = new MarkerOptions().position(latLng).title("Your location");
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

        // u slucaju da je mapa otvorena iz feed dela, da prikaze objekat na mapi
        if(extras != null) {
            double lat =getIntent().getExtras().getDouble("Latitude");
            double lon = getIntent().getExtras().getDouble("Longitude");
            Toast.makeText(getApplicationContext(),lat + " + " + lon, Toast.LENGTH_SHORT);
            LatLng ll = new LatLng(lat, lon);
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(ll)
                    .zoom(17).build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            //map.animateCamera(CameraUpdateFactory.newLatLngZoom(ll, 170));
           // map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 44){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //getCurrentLocation();

                if(extras == null){

                    getCurrentLocation();
                }
                getProblemsLocations();
                setOnMapClickListener();

                getCurrentLocationForAR();
                getArObject();
            }
        }

    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try{
            if(resultCode == Activity.RESULT_OK)
            {
               // addObjectMarkers();
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



    private void getProblemsLocations() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("my-objects");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    lat = snapshot.child("latitude").getValue(double.class);
                    lon = snapshot.child("longitude").getValue(double.class);

                    String description = snapshot.child("description").getValue(String.class);
                    LatLng latLng = new LatLng(lat,lon);

                    MarkerOptions options = new MarkerOptions().position(latLng).title(description);

                     map.addMarker(options);
                    //map.addMarker(options);
                  //  AllMarkers.add(mLocationMarker);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });





    }



    private void radiusFunction()
    {
        ///current Loc on Map
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
                            currentLoc.setLatitude(location.getLatitude());
                            currentLoc.setLongitude(location.getLongitude());
                        }
                    });
                }
            }
        });

        //show object
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    lat = snapshot.child("latitude").getValue(double.class);
                    lon = snapshot.child("longitude").getValue(double.class);

                    Location objectLoc = new Location("dumyprovider");
                    objectLoc.setLongitude(lon);
                    objectLoc.setLatitude(lat);


                    //take distance value from editText
                    textMeters =  (EditText)findViewById(R.id.meters);
                    String value = textMeters.getText().toString();
                    double distance = currentLoc.distanceTo(objectLoc);



                    //show only object in radius
                    if(distance < Integer.valueOf(value)) {

                        String description = snapshot.child("description").getValue(String.class);
                        LatLng latLng = new LatLng(lat,lon);

                        MarkerOptions options = new MarkerOptions().position(latLng).title(description);

                        Marker mLocationMarker = map.addMarker(options);
                        AllMarkers.add(mLocationMarker);
                    }

                    ///show circle
                    drawCircle(value,currentLoc.getLatitude(),currentLoc.getLongitude());


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void removeAllMarkers() {
        for (Marker mLocationMarker: AllMarkers) {
            mLocationMarker.remove();
        }
        AllMarkers.clear();

    }

    private void clearCircles() {
        for (Circle circle : circles) {
            circle.remove();
        }
        circles.clear();
    }

    private void funcForCall()
    {
        removeAllMarkers();
        radiusFunction();
    }

    private void drawCircle(String value, double lat, double lon)
    {
        clearCircles();
        LatLng ll = new LatLng(currentLoc.getLatitude(), currentLoc.getLongitude());
        CircleOptions circleOptions = new CircleOptions()
                .center(ll)
                .radius(Integer.valueOf(value))
                .strokeWidth(0f)
                .fillColor(0x40ff0000);
        circles.add(map.addCircle(circleOptions));
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(),"Home page",Toast.LENGTH_SHORT).show();
        finish();
        return;
    }



    ///////////////////ar


    private void getCurrentLocationForAR() {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference uidRef = rootRef.child("Users").child(uid);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                curLat = dataSnapshot.child("latitude").getValue(double.class);
                curLon = dataSnapshot.child("longitude").getValue(double.class);

                userLoc.setLatitude(curLat);
                userLoc.setLongitude(curLon);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        uidRef.addListenerForSingleValueEvent(valueEventListener);
    }
    private void getArObject()
    {

        Toast.makeText(getApplicationContext(),"Pera", Toast.LENGTH_SHORT);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("ar_objects");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    double lat_ar = snapshot.child("latitude").getValue(double.class);
                    double lon_ar = snapshot.child("longitude").getValue(double.class);

                    LatLng latLng = new LatLng(lat_ar, lon_ar);

                    MarkerOptions options = new MarkerOptions().position(latLng).title("AR").icon(getMarkerIcon("#FF032791"));
                    map.addMarker(options);


                    Location objectLoc = new Location("dumyprovider");
                    objectLoc.setLatitude(lat_ar);
                    objectLoc.setLongitude(lon_ar);
                    locationsAR.add(objectLoc);


                }
                compareArLocations();

            }




            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }

    private void compareArLocations()
    {

        //Toast.makeText(getApplicationContext(),String.valueOf(locationasAR.size()),Toast.LENGTH_SHORT).show();
     /*   Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null) {
                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            Location userLoc = new Location(LOCATION_SERVICE);
                            userLoc.setLatitude(location.getLatitude());
                            userLoc.setLongitude(location.getLongitude());
                            Toast.makeText(getApplicationContext(),userLoc.getLatitude() + " : " + userLoc.getLongitude(),Toast.LENGTH_SHORT).show();
                        }
                    });
                }}
        });
*/


        for(Location location : locationsAR)
        {
            float n = location.distanceTo(userLoc);


            if(((n/1000)/1000) < 10) {

                map.setOnMarkerClickListener(marker -> {
                    if (marker.getTitle().equals("AR")) {

                        if((n/1000)/1000 < 10) {
                            Toast.makeText(getApplicationContext(),String.valueOf((n/1000)/1000),Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Maps.this, AR_showObject.class);
                            startActivity(intent);
                        }

                    } else {

                        Toast.makeText(getApplicationContext(), marker.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                    return true;
                });
                break;
            }
        }
    }




    //////////////////


    public BitmapDescriptor getMarkerIcon(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }





}
