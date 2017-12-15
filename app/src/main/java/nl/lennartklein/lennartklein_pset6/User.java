package nl.lennartklein.lennartklein_pset6;

public class User {
    public String id;
    public String mail;
    public String username;

    // Empty constructor for Firebase
    public User() {}

    public User(String id, String mail) {
        this.id = id;
        this.mail = mail;
        this.username = "";
    }

}
