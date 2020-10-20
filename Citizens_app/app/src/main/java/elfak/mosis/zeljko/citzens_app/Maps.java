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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
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

    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient client;

    private SearchView searchView;



    private double lon,lat;

    private Button addObject;
    private Button radius;
    private boolean selCoorsEnabled = false;
    private LatLng placeLoc;
    public static final int SELECT_COORDINATES = 2;
    private int state = 0;
    static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    public static final int SHOW_MAP = 0;
    public static final int CENTER_PLACE_ON_MAP = 1;


    private Location currentLocation;
    private Location currentLoc;

    private Location userLoc;

    private List<Location> locationsAR;
    private List<Object> listOfObject;
    private List<Marker> AllMarkers;
    private List<Circle> circles;


    private Spinner spinner;
    private Button btnAddVirtual;
    private Circle circle;
    private EditText textMeters;
    private Button btn_add_object;

    private double curLat; //in function for currentLocation
    private double curLon;

    private DatabaseReference database; //used in func for radius

    int position = -1;

    private Bundle extras;
    private Intent intent;

    private String izabranaKategorija;




    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        //inicijalizacija
        database = FirebaseDatabase.getInstance().getReference().child("my-objects");
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);
        spinner = (Spinner) findViewById(R.id.spinner2);
        btnAddVirtual =(Button) findViewById(R.id.btn_add_virtual);
        searchView = findViewById(R.id.sv_location);
        addObject = findViewById(R.id.add_object_btn);
        radius = findViewById(R.id.radius);
        btn_add_object = findViewById(R.id.add_object_btn);

        userLoc = new Location(LOCATION_SERVICE); //used get current for AR
        currentLoc = new Location(NETWORK_STATS_SERVICE); //used in radius

        locationsAR = new ArrayList<Location>();
        listOfObject = new ArrayList<Object>();
        AllMarkers = new ArrayList<Marker>();

        circles = new ArrayList<>();

         intent = getIntent();
         extras = intent.getExtras();

        Intent listIntent = getIntent();
        Bundle positionBundle = listIntent.getExtras();
        if(positionBundle != null)
            position = positionBundle.getInt("position");


        //pribavljanje Google mapa, inicijalizacija mape
        mapFragment.getMapAsync(this);


        //initialize fused loc
        client = LocationServices.getFusedLocationProviderClient(this);

        //check permission
        if(ActivityCompat.checkSelfPermission(Maps.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //call method
            if(extras == null){

                //u slucaju da se otvara iz feed-a
                getCurrentLocation();
                getCategories();
                getArObject();

                getProblemsLocations();

               // setOnMapClickListener();
            }
            else {

                getCategories();
                getArObject();
                getProblemsLocations();


                //setOnMapClickListener();
            }

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


        //add object on click on map
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


        radius.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                    funcForCall();


            }
        });

       // getCategories();

        btnAddVirtual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Maps.this, AR_activity_add_new_object.class);
                startActivity(i);
            }
        });

        btn_add_object.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"Choose place on map",Toast.LENGTH_SHORT).show();
                setOnMapClickListener();
            }
        });



    }


    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;

        // u slucaju da je mapa otvorena iz feed dela, da prikaze objekat na mapi
        if(extras != null) {
            double lat = getIntent().getExtras().getDouble("Latitude");
            double lon = getIntent().getExtras().getDouble("Longitude");
            Toast.makeText(getApplicationContext(),lat + " + " + lon, Toast.LENGTH_SHORT);
            LatLng ll = new LatLng(lat, lon);
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(ll)
                    .zoom(17).build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 44){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                if(extras == null){

                    getCurrentLocation();
                   // setOnMapClickListener();


                    getProblemsLocations();
                    getArObject();
                    getCategories();
                }
                else {
                    getCategories();
                //setOnMapClickListener();

                    getArObject();
                    getProblemsLocations();



                }
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

    //lokacija preko GPS-a
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

                            currentLoc.setLatitude(location.getLatitude());
                            currentLoc.setLongitude(location.getLongitude());
                            //create marker
                            MarkerOptions options = new MarkerOptions().position(latLng).title("Your location").icon(getMarkerIcon("#FF032791"));
                            //zoom
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
                            googleMap.addMarker(options);

                        }
                    });
                }
            }
        });


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


    private void getProblemsLocations() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("my-objects");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    double lat = snapshot.child("latitude").getValue(double.class);
                    double lon = snapshot.child("longitude").getValue(double.class);

                    String description = snapshot.child("description").getValue(String.class);
                    String category =  snapshot.child("category").getValue(String.class);
                    LatLng latLng = new LatLng(lat,lon);

                    MarkerOptions options = new MarkerOptions().position(latLng).title(description);

                     Marker marker = map.addMarker(options);
                     AllMarkers.add(marker);

                     Object object = new Object(description,category,lat,lon);
                     listOfObject.add(object);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void radiusFunction()
    {

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


    /*private void getCurrentLocationForAR() {

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
    }*/
    private void getArObject()
    {


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("ar_objects");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    double lat_ar = snapshot.child("latitude").getValue(double.class);
                    double lon_ar = snapshot.child("longitude").getValue(double.class);

                    LatLng latLng = new LatLng(lat_ar, lon_ar);

                    MarkerOptions options = new MarkerOptions().position(latLng).title("AR").icon(getMarkerIcon("#FF619715"));
                    Marker marker = map.addMarker(options);
                    AllMarkers.add(marker);


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

        for(Location location : locationsAR)
        {
            float n = distance(currentLoc.getLatitude(), currentLoc.getLongitude(),location.getLatitude(),location.getLongitude());


            if(n < 10) {

                map.setOnMarkerClickListener(marker -> {
                    if (marker.getTitle().equals("AR")) {

                        if(n< 10) {
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

    public float distance (double lat_a, double lng_a, double lat_b, double lng_b )
    {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b-lat_a);
        double lngDiff = Math.toRadians(lng_b-lng_a);
        double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        return new Float(distance * meterConversion).floatValue();
    }




    private void getCategories()
    {
        final List<String> list = new ArrayList<String>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("my-objects");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String category = snapshot.child("category").getValue(String.class);


                    if(!list.contains(category))
                        list.add(category);

                    ArrayAdapter<String> a = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_spinner_item, list);
                    a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //  Setting the ArrayAdapter data on the Spinner
                    spinner.setAdapter(a);


                }
                String categoryAR = "ar_objects";
                list.add(categoryAR);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        spin();
    }

    private void spin()
    {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                izabranaKategorija = (String) spinner.getSelectedItem();

                removeAllMarkers();
                if(izabranaKategorija.equals("ar_objects"))
                {
                    getArObject();
                }
                else {

                    for (Object obj : listOfObject) {
                        if (obj.category.equals(izabranaKategorija)) {
                            double lat2 = obj.latitude;
                            double lon2 = obj.longitude;

                            String description = obj.description;
                            LatLng latLng = new LatLng(lat2, lon2);

                            MarkerOptions options = new MarkerOptions().position(latLng).title(description);

                            Marker marker = map.addMarker(options);
                            AllMarkers.add(marker);
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}
