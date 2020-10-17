package elfak.mosis.zeljko.citzens_app;

import com.google.firebase.database.DatabaseReference;

public class AR_object {



    public double latitude;
    public double longitude;
    //public String description;
    public String key;

    public AR_object()
    {}

    public AR_object(double lat, double lon)
    {
        this.latitude = lat;
        this.longitude = lon;

    }


public String getKey(){return  this.key;}
    public double getLatitude(){return this.latitude;}
    public double getLongitude(){return this.longitude;}
}
