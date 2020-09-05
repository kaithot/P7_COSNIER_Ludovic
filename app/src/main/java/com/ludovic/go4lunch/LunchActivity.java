package com.ludovic.go4lunch;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ludovic.go4lunch.fragments.ListFragment;
import com.ludovic.go4lunch.fragments.MapsFragment;
import com.ludovic.go4lunch.fragments.WorkmatesFragment;

public class LunchActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lunch_activity);

        BottomNavigationView mMainNav = findViewById(R.id.main_nav);
        mMainNav.setOnNavigationItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,
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
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,
                                selectedFragment).commit();
                        return true;
                    }
                };
    }