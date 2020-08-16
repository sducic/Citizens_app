package elfak.mosis.zeljko.citzens_app;

import android.location.Location;

import com.google.android.gms.location.LocationServices;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Object {
    public String name;
    public String category;
    public String description;
    public double longitude;
    public double latitude;
    public String UserID;
    public String date;
    public String imgUri;
    @Exclude
    public String key;
    public Object(){

    }

    public Object(String name, String description, String category, double longitude, double latitude,String UserID,String date,String imgUri)
    {
        this.name = name;
        this.category = category;
        this.description = description;
        this.longitude = longitude;
        this.latitude = latitude;
        this.UserID=UserID;
        this.date=date;
        this.imgUri=imgUri;
    }

    public Object(String name, String description, String category)
    {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public double getLongitude() {
        return longitude;
    }
    public double getLatitude() {
        return latitude;
    }
    public String getName(){
        return name;
    }
    public String getDate(){return date;}
    public String getKey(){return key;}
    public String getImgUri(){return imgUri;}
}

