package nl.lennartklein.lennartklein_pset6;

/**
 * A saved post from the NASA apod feed
 */
public class Post {
    public String date;
    public String title;
    public String explanation;
    public String imagePath;

    // Empty constructor for FireBase
    public Post() {}

    public Post(String date, String title, String explanation, String imagePath) {
        this.date = date;
        this.title = title;
        this.explanation = explanation;
        this.imagePath = imagePath;
    }

}
