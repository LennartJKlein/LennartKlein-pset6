package nl.lennartklein.lennartklein_pset6;

/**
 * A user in the app
 */
public class User {
    public String id;
    public String mail;
    public String username;

    // Empty constructor for FireBase
    public User() {}

    public User(String id, String mail) {
        this.id = id;
        this.mail = mail;
        this.username = "";
    }

}
