package nl.lennartklein.lennartklein_pset6;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * The community fragment shows the collections of other users
 */
public class CommunityFragment extends Fragment {

    // Global references
    Context mContext;
    SwipeRefreshLayout refreshSwipe;
    FirebaseDatabase db;
    DatabaseReference db_users;
    DatabaseReference db_posts;
    DatabaseReference db_all;
    String userID;
    TextView error_posts;
    LinearLayout users_container;
    ValueEventListener listener;

    // Sign in authentication
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get FireBase instances
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        db = FirebaseDatabase.getInstance();
        db_users = db.getReference("users/");
        db_posts = db.getReference("posts/");
        db_all = db.getReference();

        // Set UI references
        mContext = getActivity().getApplicationContext();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        // Set global references
        mContext = getActivity();
        refreshSwipe = view.findViewById(R.id.swipe_refresh);
        error_posts = view.findViewById(R.id.posts_error);
        users_container = view.findViewById(R.id.users_container);

        // Fetch posts from database
        listener = new CommunityFragment.DatabaseListener();
        db_all.addValueEventListener(listener);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Refresh the list
        refreshSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshListContent();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (db_all != null && listener != null) {
            db_all.removeEventListener(listener);
        }
    }

    private class DatabaseListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            refreshSwipe.setRefreshing(true);

            DataSnapshot allPostsSnapshot = dataSnapshot.child("posts");
            DataSnapshot usersSnapshot = dataSnapshot.child("users");

            // Loop through every user
            for (DataSnapshot postsSnapshot : allPostsSnapshot.getChildren()) {

                // Inflate an instance of 'user_posts'
                View user_view = getLayoutInflater().inflate(R.layout.user_posts, null);
                users_container.addView(user_view);

                // Get username from database
                String this_user_id = postsSnapshot.getKey();
                String user_name = usersSnapshot.child(this_user_id).child("username").getValue(String.class);
                if (user_name != null && !Objects.equals(user_name, "")) {
                    TextView user = user_view.findViewById(R.id.user_name);
                    user.setText(user_name);
                }

                // Set up adapter in new GridView
                ArrayList<HashMap<String, String>> user_posts = new ArrayList<>();
                PostAdapter user_adapter = new PostAdapter(mContext, R.layout.grid_item_photo_small, user_posts);
                GridView gv = user_view.findViewById(android.R.id.list);
                gv.setAdapter(user_adapter);

                // Every post of this user (show max. 8)
                int post_counter = 0;
                for (DataSnapshot postSnapshot : postsSnapshot.getChildren()) {
                    if (post_counter <= 8) {
                        Post post = postSnapshot.getValue(Post.class);

                        // Create HashMap of object
                        if (post != null) {
                            HashMap<String, String> postObj = new HashMap<>();
                            postObj.put("date", post.date);
                            postObj.put("title", post.title);
                            postObj.put("explanation", post.explanation);
                            postObj.put("url", post.imagePath);

                            // Add HashMap object to array
                            user_posts.add(postObj);
                            user_adapter.notifyDataSetChanged();

                            post_counter++;
                        }

                    }
                }
            }

            refreshSwipe.setRefreshing(false);

            if (dataSnapshot.getChildrenCount() == 0 ) {
                error_posts.setVisibility(View.VISIBLE);
            } else {
                error_posts.setVisibility(View.GONE);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d("Data","The read failed: " + databaseError.getCode());
        }
    }

    /**
     * Refresh the list of images by deleting and resetting a new listener
     */
    private void refreshListContent() {
        if (db_all != null && listener != null) {
            refreshSwipe.setRefreshing(true);
            db_all.removeEventListener(listener);
            listener = new CommunityFragment.DatabaseListener();
            db_all.addValueEventListener(listener);
            refreshSwipe.setRefreshing(false);
        }
    }


}
