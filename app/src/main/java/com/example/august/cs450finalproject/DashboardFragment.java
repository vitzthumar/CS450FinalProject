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

public class DashboardFragment extends Fragment {

    // Instance Variables
    private ArrayList<User> allUsers;
    private User me;
    private TextView dashboard_tv;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);
        dashboard_tv = (TextView)rootView.findViewById(R.id.dashboard_text_view);

        // Get the reference to the DB
        final DatabaseReference db_ref = FirebaseDatabase.getInstance().getReference().child("Users");

        // Get me
        final DatabaseReference myRef = db_ref.child(user.getUid());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String name = dataSnapshot.child("name").getValue().toString();
                final String email = dataSnapshot.child("email").getValue().toString();

                // This is the way to get the geoFire location
                GeoFire geoFire = new GeoFire(myRef);
                geoFire.getLocation("location", new LocationCallback() {
                    @Override
                    public void onLocationResult(String key, GeoLocation location) {
                        if (location != null) {
                            me = new User(name, email, new GeoLocation(location.latitude, location.longitude));


                            // Now that we have a user object. Travers all other members
                            db_ref.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(final DataSnapshot dataSnapshot) {
                                    dashboard_tv.setText("");
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        // Get the user Id of whats being traversed
                                        String uuid = snapshot.getKey();

                                        // Do not traverse the curren users info
                                        if (!uuid.equals(user.getUid())) {
                                            final String name = snapshot.child("name").getValue().toString();
                                            final String email = snapshot.child("email").getValue().toString();
                                            final String[] locs = {"No latitude data", "No longitude data"};

                                            // This is the way to get the geoFire location
                                            GeoFire geoFire = new GeoFire(db_ref.child(uuid));
                                            geoFire.getLocation("location", new LocationCallback() {
                                                @Override
                                                public void onLocationResult(String key, GeoLocation location) {
                                                    if (location != null) {
                                                        locs[0] = String.valueOf(location.latitude);
                                                        locs[1] = String.valueOf(location.longitude);

                                                        dashboard_tv.append(name);
                                                        dashboard_tv.append("\n");
                                                        dashboard_tv.append(email);
                                                        dashboard_tv.append("\n");
                                                        dashboard_tv.append(locs[0]); // latitude
                                                        dashboard_tv.append("\n");
                                                        dashboard_tv.append(locs[1]); // longitude
                                                        dashboard_tv.append("\n");
                                                        double d = distance(
                                                                me.getLocation().latitude,
                                                                me.getLocation().longitude,
                                                                location.latitude,
                                                                location.longitude
                                                        );
                                                        DecimalFormat df = new DecimalFormat("###.##");
                                                        dashboard_tv.append("Distance to " + name + " is: " + df.format(d) + "KM");
                                                        dashboard_tv.append("\n");
                                                        dashboard_tv.append("--------------------\n");
                                                    } else {
                                                        dashboard_tv.append(name);
                                                        dashboard_tv.append("\n");
                                                        dashboard_tv.append(email);
                                                        dashboard_tv.append("\n");
                                                        dashboard_tv.append(locs[0]); // latitude
                                                        dashboard_tv.append("\n");
                                                        dashboard_tv.append(locs[1]); // longitude
                                                        dashboard_tv.append("\n");
                                                        dashboard_tv.append("Cannot find distance to " + name + ". No location data found"); // longitude
                                                        dashboard_tv.append("\n");
                                                        dashboard_tv.append("--------------------\n");
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                    System.err.println("There was an error getting the GeoFire location: " + databaseError);
                                                }
                                            });
                                        }
                                    }

                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.err.println("There was an error getting the GeoFire location: " + databaseError);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
