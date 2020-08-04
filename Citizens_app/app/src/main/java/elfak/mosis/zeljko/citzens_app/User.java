package elfak.mosis.zeljko.citzens_app;

public class User {
    public String  fullName,email,phoneNumber;
    public double latitude, longitude;


    public User(){
    }

    public User(String fullName,String email,String phoneNumber) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber=phoneNumber;
        this.latitude = 0;
        this.longitude = 0;

    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }
}
