package elfak.mosis.zeljko.citzens_app;

public class User {
    public String  fullName,email,phoneNumber;
    public int coins;
    public String profileImageUri;

    public User(){
    }

    public User(String fullName,String email,String phoneNumber,String profileImageUri) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber=phoneNumber;
        this.profileImageUri=profileImageUri;
        this.coins=1000;
    }

    public String getfullName() { return fullName; }
    public String getEmail() { return email; }
    public int getCoins(){return coins;};
    public String getProfileImageUri(){return profileImageUri;}
}
