package elfak.mosis.zeljko.citzens_app;

public class User {

    public String  fullName,email,phoneNumber;
    public double latitude, longitude;
    public int coins;
    public String profileImageUri;


    public User(){
    }

    public User(String fullName,String email,String phoneNumber,String profileImageUri) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber=phoneNumber;
        this.latitude = 0;
        this.longitude = 0;
        this.profileImageUri=profileImageUri;
        this.coins=0;

    }

    public String getfullName() { return fullName; }
    public String getEmail() { return email; }
    public int getCoins(){return coins;};
    public String getProfileImageUri(){return profileImageUri;}
    public void addCoins(int n)
    {
        this.coins += n;
    }

}
