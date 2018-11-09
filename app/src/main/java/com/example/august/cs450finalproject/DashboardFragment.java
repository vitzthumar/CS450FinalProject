package com.example.august.cs450finalproject;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class DashboardFragment extends Fragment {

    // Instance Variables
    private TextView dashboard_tv;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private HashMap<String, String> friends;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public DashboardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DashboardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DashboardFragment newInstance(String param1, String param2) {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        friends = new HashMap<>();

        fetchFriends();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);
        dashboard_tv = (TextView)rootView.findViewById(R.id.dashboard_text_view);

        DatabaseReference locationReference = FirebaseDatabase.getInstance().getReference("Locations");

        GeoFire geoFire = new GeoFire(locationReference);

        // Get everyone within 100KM
        // THIS IS A HARD CODED VALUE; HOW CAN WE MAKE IT DYNAMIC?? --> Pass in a constant that is the users current location?
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(40.712776, -74.005974), 100);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                getUser(key, 1);
            }

            @Override
            public void onKeyExited(String key) {
                getUser(key, 0);
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                getUser(key, 2);
            }

            @Override
            public void onGeoQueryReady() {
                System.out.println("All initial data has been loaded and events have been fired!\n");
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                System.err.println("There was an error with this query: " + error);
            }
        });

        return rootView;
    }

    private double distance(double startLat, double startLong, double endLat, double endLong) {

        final int EARTH_RADIUS = 6371; // Approx Earth radius in KM

        // TODO remove these printlns
        System.out.println("Calculating...");
        System.out.println("startLat " + startLat);
        System.out.println("startLon " + startLong);
        System.out.println("endLat " + endLat);
        System.out.println("endLong " + endLong);
        double dLat  = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat   = Math.toRadians(endLat);

        double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        System.out.println("Distance Travlled: " + EARTH_RADIUS * c + " [KM]");
        return EARTH_RADIUS * c; // <-- dist (in KILOMETERS)
    }

    /***
     *
     * @param userId - uuid of user
     * @param flag - If 1: User entered the area, if 0 user left the area, if 2 - user moved
     */
    private void getUser(String userId, final int flag) {
        FirebaseDatabase.getInstance().getReference("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (flag == 1) {
                    dashboard_tv.append(dataSnapshot.child("name").getValue().toString() + " entered the area.\n");
                } else if (flag == 0) {
                    dashboard_tv.append(dataSnapshot.child("name").getValue().toString() + " left the area.\n");
                } else {
                    dashboard_tv.append(dataSnapshot.child("name").getValue().toString() + " moved.\n");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void fetchFriends() {
    }

    private static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
