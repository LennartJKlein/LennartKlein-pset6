package nl.lennartklein.lennartklein_pset6;

import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.BottomNavigationView;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    // UI references
    Fragment activeFragment;

    // Sign in authentication
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set animation
        getWindow().setAllowEnterTransitionOverlap(false);
        getWindow().setAllowReturnTransitionOverlap(false);

        // Get authentication instance
        mAuth = FirebaseAuth.getInstance();

        // Set navigation
        setNavigation();

        // Hide action bar
        getSupportActionBar().hide();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, "activeFragment", activeFragment);
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        showFragment(getSupportFragmentManager().getFragment(inState, "activeFragment"));
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly
        verifyUser(mAuth.getCurrentUser());
    }

    @Override
    protected void onResume() {
        super.onResume();
        verifyUser(mAuth.getCurrentUser());
        showFragment(activeFragment);
    }

    public void verifyUser(FirebaseUser user) {
        if (user == null) {
            signOut();
        }
    }

    public void signOut() {
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }
    
    private void setNavigation() {
        // Set UI references
        BottomNavigationView navigation = findViewById(R.id.navigation);
        Menu bottomMenu = navigation.getMenu();

        // Set on selected listener
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                activateFragment(item);
                return true;
            }
        });

        // Set the first fragment
        activateFragment(bottomMenu.getItem(1));
    }

    /**
     * Perform action when any menu item is selected.
     *
     * @param item Item that is selected.
     */
    protected void activateFragment(MenuItem item) {
        item.setChecked(true);

        switch (item.getItemId()) {
            case R.id.navigation_collections:
                showFragment(new CollectionsFragment());
                break;
            case R.id.navigation_explore:
                showFragment(new ExploreFragment());
                break;
            case R.id.navigation_community:
                showFragment(new CommunityFragment());
                break;
        }
    }

    /**
     * Method to push any fragment into the page.
     *
     * @param fragment An instance of Fragment to show into the page.
     */
    protected void showFragment(Fragment fragment) {
        if (fragment == null) {
            return;
        }
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        ft.replace(R.id.page, fragment);
        ft.commit();

        activeFragment = fragment;
    }

}
