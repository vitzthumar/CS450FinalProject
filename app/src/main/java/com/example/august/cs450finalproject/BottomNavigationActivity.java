package com.example.august.cs450finalproject;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class BottomNavigationActivity extends AppCompatActivity implements MainFragment.OnFragmentInteractionListener,
        ProfileFragment.OnFragmentInteractionListener, NotificationFragment.OnFragmentInteractionListener,
        DashboardFragment.OnFragmentInteractionListener, MessagesFragment.OnFragmentInteractionListener {

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    switchToFragment(1);
                    return true;
                case R.id.navigation_dashboard:
                    switchToFragment(2);
                    return true;
                case R.id.navigation_notifications:
                    switchToFragment(3);
                    return true;
                case R.id.navigation_profile:
                    switchToFragment(4);
                    return true;
                case R.id.navigation_messages:
                    switchToFragment(5);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.bottom_nav_container, new MainFragment()).commit();
    }

    public void switchToFragment(int fragmentInt) {
        FragmentManager manager = getSupportFragmentManager();
        if (fragmentInt == 1) {
            manager.beginTransaction().replace(R.id.bottom_nav_container, new MainFragment()).commit();
        } else if (fragmentInt == 2) {
            manager.beginTransaction().replace(R.id.bottom_nav_container, new DashboardFragment()).commit();
        } else if (fragmentInt == 3) {
            manager.beginTransaction().replace(R.id.bottom_nav_container, new NotificationFragment()).commit();
        } else if ( fragmentInt == 4 ) {
            manager.beginTransaction().replace(R.id.bottom_nav_container, new ProfileFragment()).commit();
        } else {
            manager.beginTransaction().replace(R.id.bottom_nav_container, new MessagesFragment()).commit();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
