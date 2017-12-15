package nl.lennartklein.lennartklein_pset6;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

/**
 * Shows the NASA apod feed with buttons for saving the images
 */
public class ExploreFragment extends ListFragment {

    // Global references
    String URL_feed = "https://api.nasa.gov/planetary/apod?api_key=NA22X1PXoa6AU9k2VauPtUSKJhHDqvcFu4rbtDff";
    ListView lv;
    Context mContext;
    SwipeRefreshLayout refreshSwipe;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set query URL to current date
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        Date s_date = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(s_date);
        c.add(Calendar.DATE, -25);
        s_date = c.getTime();
        String start_date = formatter.format(s_date);

        Date e_date = new Date();
        Calendar c_end = Calendar.getInstance();
        c_end.setTime(e_date);
        e_date = c_end.getTime();
        String end_date = formatter.format(e_date);

        URL_feed += "&start_date=" + start_date + "&end_date=" + end_date;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        // Set global references
        mContext = getActivity();
        lv = view.findViewById(android.R.id.list);
        refreshSwipe = view.findViewById(R.id.swipe_refresh);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Initiate the list with content from this URL
        setListContent();

        // Refresh the list
        refreshSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setListContent();
            }
        });
    }

    /**
     * Set the content of the ListView with the feed from the API
     */
    private void setListContent() {
        refreshSwipe.setRefreshing(true);

        // Fetch latest content from API and adapt them to the list
        StringRequest request = new StringRequest(URL_feed,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray responseArray;

                        try {
                            // Get JSON array of data
                            responseArray = new JSONArray(response);
                            ArrayList<HashMap<String, String>> photos = new ArrayList<>();

                            // Add every found string to the ArrayList
                            for (int i = responseArray.length() - 1; i >= 0; i--) {
                                JSONObject photo = responseArray.getJSONObject(i);
                                //Log.d("Data", String.valueOf(photo));
                                String photo_type = photo.getString("media_type");

                                // If this is a photo
                                if (Objects.equals(photo_type, "image")) {

                                    // Build an HashMap object for this item (key-value pairs)
                                    HashMap<String, String> photoObj = new HashMap<>();
                                    photoObj.put("date", photo.getString("date"));
                                    photoObj.put("title", photo.getString("title"));
                                    photoObj.put("explanation", photo.getString("explanation"));
                                    photoObj.put("url", photo.getString("url"));

                                    // Add HashMap object to array
                                    photos.add(photoObj);
                                }
                            }
                            // Use an adapter and the ArrayList to feed the list
                            PhotoAdapter adapter = new PhotoAdapter(getActivity(), mContext, photos);
                            lv.setAdapter(adapter);

                            // Hide progress indicator
                            refreshSwipe.setRefreshing(false);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Data", String.valueOf(error));
                        Toast.makeText(mContext, R.string.error_network, Toast.LENGTH_SHORT).show();
                        refreshSwipe.setRefreshing(false);
                    }
                });

        // Give request 10 sec. to respond
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Set and run request
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(request);

    }

}
