package nl.lennartklein.lennartklein_pset6;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

public class PostAdapter extends ArrayAdapter<HashMap<String, String>> {

    private int resourceID;
    private Context mContext;
    private ArrayList<HashMap<String, String>> data = new ArrayList<>();

    PostAdapter(Context context, int listResourceID, ArrayList<HashMap<String, String>> list) {
        super(context, 0, list);
        data = list;
        mContext = context;
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
        SquareImageView aImage = convertView.findViewById(R.id.photo_image);
        TextView aTitle = convertView.findViewById(R.id.photo_title);
        TextView aExplanation = convertView.findViewById(R.id.photo_explanation);
        TextView aDate = convertView.findViewById(R.id.photo_date);

        // Populate the data into the template view using the data object
        loadImageFromStorage(photo.get("date"), aImage);
        aTitle.setText(photo.get("title"));
        aExplanation.setText(photo.get("explanation"));
        aDate.setText(photo.get("date"));

        // Return the completed view to render on screen
        return convertView;
    }

    /**
     * Load an image from storage and set it in an ImageView
     * @param name:     filename (without extension)
     * @param view:     the ImageView that the image has to be loaded into
     */
    private void loadImageFromStorage(String name, SquareImageView view)
    {
        try {
            ContextWrapper cw = new ContextWrapper(mContext.getApplicationContext());
            File directory = cw.getDir("images", Context.MODE_PRIVATE);
            File f = new File(directory, name + ".jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            view.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

    }

}

