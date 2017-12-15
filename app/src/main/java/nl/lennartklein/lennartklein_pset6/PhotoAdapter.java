package nl.lennartklein.lennartklein_pset6;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class PhotoAdapter extends ArrayAdapter<HashMap<String, String>> {

    private int resourceID;
    private Context mContext;
    private ArrayList<HashMap<String, String>> data = new ArrayList<>();
    private Activity mActivity;

    PhotoAdapter(Activity activity, Context context, int listResourceID, ArrayList<HashMap<String, String>> list) {
        super(context, 0, list);
        data = list;
        mContext = context;
        mActivity = activity;
        resourceID = listResourceID;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        HashMap<String, String> photo = data.get(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(resourceID, parent, false);
        }

        // Lookup view for data population
        ImageView aImage = convertView.findViewById(R.id.photo_image);
        ImageButton aSave = convertView.findViewById(R.id.action_photo_save);
        TextView aTitle = convertView.findViewById(R.id.photo_title);
        TextView aExplanation = convertView.findViewById(R.id.photo_explanation);
        TextView aDate = convertView.findViewById(R.id.photo_date);

        // Populate the data into the template view using the data object
        UrlImageViewHelper.setUrlDrawable(aImage, photo.get("url"), R.drawable.no_image);
        aSave.setOnClickListener(new AddToCollection());
        aTitle.setText(photo.get("title"));
        aExplanation.setText(photo.get("explanation"));
        aDate.setText(photo.get("date"));

        // Return the completed view to render on screen
        return convertView;
    }

    private class AddToCollection implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            // Get all the siblings of this button
            ViewGroup parent = (ViewGroup) v.getParent();

            // Fetch data of this post
            TextView vDate = parent.findViewById(R.id.photo_date);
            String date = vDate.getText().toString();

            TextView vTitle = parent.findViewById(R.id.photo_title);
            String title = vTitle.getText().toString();

            TextView vExplanation = parent.findViewById(R.id.photo_explanation);
            String explanation = vExplanation.getText().toString();

            ImageView vImage = parent.findViewById(R.id.photo_image);
            Bitmap bitmap = ((BitmapDrawable)vImage.getDrawable()).getBitmap();
            String imagePath = saveToInternalStorage(bitmap, date);

            // Get current user ID
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            String userID = mAuth.getCurrentUser().getUid();

            // Write to database
            Post thisPost = new Post(date, title, explanation, imagePath);
            DatabaseReference db = FirebaseDatabase.getInstance().getReference("posts");
            db.child(userID).child(date).setValue(thisPost);

            Toast.makeText(mContext, R.string.photo_saved, Toast.LENGTH_SHORT).show();
        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage, String name){
        ContextWrapper cw = new ContextWrapper(mContext.getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("images", Context.MODE_PRIVATE);
        File myPath = new File(directory, name + ".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myPath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return name + ".jpg";
    }

}

