package elfak.mosis.zeljko.citzens_app;

public class User {
    public String  fullName,email,phoneNumber;

    public User(){
    }

    public User(String fullName,String email,String phoneNumber) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber=phoneNumber;
    }

    public String getfullName() { return fullName; }
    public String getEmail() { return email; }
}
