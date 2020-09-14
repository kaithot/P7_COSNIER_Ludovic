package com.ludovic.go4lunch;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.ludovic.go4lunch.fragments.ListFragment;
import com.ludovic.go4lunch.fragments.MapsFragment;
import com.ludovic.go4lunch.fragments.WorkmatesFragment;
import com.ludovic.go4lunch.utils.BaseActivity;

public class LunchActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //FOR DESIGN
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;

    //FOR PROFILE
    private TextView userNameTextView;
    private ImageView avatarImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lunch_activity);

        this.configureToolBar();
        this.configureDrawerLayout();
        this.configureNavigationView();
        this.configureBottomNavigationView();
        this.updateUIWhenCreating();

    }

    @Override
    public void onBackPressed() {

        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.nav_myresto:
                break;
            case R.id.nav_settings:
                break;
            case R.id.nav_logout:
                this.signOutFromFirebase();
                break;
            default:
                break;
        }

        this.drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    // CONFIGURATION

    // Configure Toolbar
    private void configureToolBar() {

        this.toolbar = (Toolbar) findViewById(R.id.activity_lunch_toolbar);
        setSupportActionBar(toolbar);

    }

    // Configure Drawer Layout
    private void configureDrawerLayout() {

        this.drawerLayout = (DrawerLayout) findViewById(R.id.activity_lunch_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,
                R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

    }

    // Configure NavigationView
    private void configureNavigationView() {

        this.navigationView = (NavigationView) findViewById(R.id.activity_lunch_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    private void configureBottomNavigationView() {

        this.bottomNavigationView = (BottomNavigationView) findViewById(R.id.main_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.activity_lunch_frame_layout,
                new MapsFragment()).commit();

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    switch (item.getItemId()) {
                        case R.id.nav_map:
                            selectedFragment = new MapsFragment();
                            break;
                        case R.id.nav_list:
                            selectedFragment = new ListFragment();
                            break;
                        case R.id.nav_workmates:
                            selectedFragment = new WorkmatesFragment();
                            break;
                    }
                    assert selectedFragment != null;
                    getSupportFragmentManager().beginTransaction().replace(R.id.activity_lunch_frame_layout,
                           selectedFragment).commit();
                    return true;
                }
            };


    //---------------------
    //REST REQUESTS
    //---------------------
    // Create http requests (SignOut)
    private void signOutFromFirebase() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted());
    }

    // Create onCompleteListener called after tasks ended
    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted() {
        return aVoid -> {
            finish();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        };
    }
    //  Update UI when activity is creating
    private void updateUIWhenCreating(){

        userNameTextView = navigationView.getHeaderView(0).findViewById(R.id.userName);
        avatarImageView = navigationView.getHeaderView(0).findViewById(R.id.avatar);

        if (this.getCurrentUser() != null){
            //Get picture URL from Firebase
            if (this.getCurrentUser().getPhotoUrl() != null) {
                Glide.with(this)
                        .load(this.getCurrentUser().getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(avatarImageView);
            }

            //Get username from Firebase
            String username = TextUtils.isEmpty(this.getCurrentUser().getDisplayName()) ? getString(R.string.info_no_username_found) : this.getCurrentUser().getDisplayName();

            //Update views with data
            this.userNameTextView.setText(username);
        }
    }

   @Override
   public int getFragmentLayout() {
       return R.layout.lunch_activity;
   }
    
}