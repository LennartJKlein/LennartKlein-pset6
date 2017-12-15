package nl.lennartklein.lennartklein_pset6;

import android.graphics.Bitmap;

public class Post {
    public String date;
    public String title;
    public String explanation;
    public String imagePath;

    // Empty constructor for Firebase
    public Post() {}

    public Post(String date, String title, String explanation, String imagePath) {
        this.date = date;
        this.title = title;
        this.explanation = explanation;
        this.imagePath = imagePath;
    }

}
