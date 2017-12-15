package nl.lennartklein.lennartklein_pset6;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.PopupMenu;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
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
 * Shows the collected images of the current user and some account settings
 */
public class CollectionsFragment extends Fragment {

    // Global references
    GridView lv;
    Context mContext;
    SwipeRefreshLayout refreshSwipe;
    FirebaseDatabase db;
    DatabaseReference db_user;
    DatabaseReference db_posts;
    ValueEventListener listener;
    String userID;
    ArrayList<HashMap<String, String>> posts = new ArrayList<>();
    PostAdapter adapter;
    TextView error_posts;
    String clicked_date;

    // Sign in authentication
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get FireBase instances
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        db = FirebaseDatabase.getInstance();
        db_user = db.getReference("users/" + userID);
        db_posts = db.getReference("posts/" + userID);

        // Set UI references
        mContext = getActivity().getApplicationContext();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collections, container, false);

        // Set global references
        mContext = getActivity();
        refreshSwipe = view.findViewById(R.id.swipe_refresh);
        lv = view.findViewById(android.R.id.list);
        error_posts = view.findViewById(R.id.posts_error);

        // Create and set adapter
        adapter = new PostAdapter(mContext, R.layout.grid_item_photo, posts);
        lv.setAdapter(adapter);
        lv.setOnItemLongClickListener(new LongClickListener());
        listener = new DatabaseListener();
        db_posts.addValueEventListener(listener);

        // Set 'more' submenu
        setMoreMenu(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (db_posts != null && listener != null) {
            db_posts.removeEventListener(listener);

            listener = new DatabaseListener();
            db_posts.addValueEventListener(listener);
        }
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
        if (db_posts != null && listener != null) {
            db_posts.removeEventListener(listener);
        }
    }

    /**
     * A database listener for the saved images of this user
     */
    private class DatabaseListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            // Clear the list
            posts.clear();

            // Verify a logged in user
            verifyUser();
            Log.d("Data", String.valueOf(dataSnapshot.getChildrenCount()));

            // Loop through every post
            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                Post post = postSnapshot.getValue(Post.class);

                // Create HashMap of object
                HashMap<String, String> postObj = new HashMap<>();
                postObj.put("date", post.date);
                postObj.put("title", post.title);
                postObj.put("explanation", post.explanation);
                postObj.put("url", post.imagePath);

                // Add HashMap object to array
                posts.add(postObj);
                adapter.notifyDataSetChanged();
            }

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
     * Update the data in the GridView by notifying the adapter
     */
    private void refreshListContent() {
        refreshSwipe.setRefreshing(true);
        adapter.notifyDataSetChanged();
        refreshSwipe.setRefreshing(false);
    }

    /**
     * A click listener for removing an image from the collection
     */
    private class LongClickListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, final long l) {

            clicked_date = ((TextView) view.findViewById(R.id.photo_date)).getText().toString();

            // Set up dialog
            AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
            alert.setTitle(R.string.unsave_photo);

            // Create a delete button
            alert.setPositiveButton(R.string.label_delete, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    // Delete this item from the database
                    db_posts.child(clicked_date).removeValue();
                }
            });

            // Create a cancel button
            alert.setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            alert.show();
            return true;
        }
    }

    /**
     * Set a submenu on an ImageButton and add click listeners
     * @param view: the view that triggered the function
     */
    private void setMoreMenu(View view) {
        ImageButton menuToggle = view.findViewById(R.id.button_menu_toggle);
        final PopupMenu mPopupMenu = new PopupMenu(getActivity(), menuToggle);

        MenuInflater mi = mPopupMenu.getMenuInflater();
        mi.inflate(R.menu.menu_user, mPopupMenu.getMenu());

        // Set click listener for toggle
        menuToggle.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mPopupMenu.show();
            }
        });

        // Set click listener for menu items
        mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_sign_out:
                        signOut();
                        break;
                    case R.id.action_set_username:
                        setUsername();
                }
                return true;
            }
        });
    }

    /**
     * Show an alert to set a username in the database
     */
    private void setUsername() {
        // Set up dialog
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle(R.string.title_rename);

        // Create input field
        final EditText input = new EditText(mContext);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setSingleLine();
        LinearLayout linearLayout = new LinearLayout(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        input.setLayoutParams(layoutParams);

        // Get username from database
        db_user.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User this_user = dataSnapshot.getValue(User.class);
                if (this_user != null && !Objects.equals(this_user.username, "")) {
                    Log.d("Data", String.valueOf(this_user.username));
                    input.setText(this_user.username, TextView.BufferType.EDITABLE);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Data", "load user:onCancelled", databaseError.toException());
            }
        });

        // Set input field
        linearLayout.addView(input);
        linearLayout.setPadding(36, 0, 36, 0);
        alert.setView(linearLayout);

        // Create a delete button
        alert.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Save input to the database
                String name = input.getText().toString();
                db_user.child("username").setValue(name);
            }
        });

        // Create a cancel button
        alert.setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        alert.show();
    }

    /**
     * If there is no current user, sign out
     */
    public void verifyUser() {
        if (mAuth.getCurrentUser() == null) {
            signOut();
        }
    }

    /**
     * Signs the current user out and starts the login activity
     */
    public void signOut() {
        mAuth.signOut();
        startActivity(new Intent(getActivity(), SignInActivity.class));
        getActivity().finish();
    }

}