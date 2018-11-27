package com.example.august.cs450finalproject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {
    // Auth stuff
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private TextView Name;
    private TextView Email;
    private TextView Uid;
    private Button logout;

    // toggle buttons
    private ToggleButton button1;
    private ToggleButton button2;
    private ToggleButton button3;
    private ToggleButton button4;
    private ToggleButton button5;
    private ToggleButton button6;
    private ToggleButton button7;
    private ToggleButton button8;
    private ToggleButton button9;

    private OnFragmentInteractionListener mListener;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        // Auth stuff
        Name = (TextView)rootView.findViewById(R.id.profileName);
        Email = (TextView)rootView.findViewById(R.id.profileEmail);
        Uid = (TextView)rootView.findViewById(R.id.profileUid);
        mAuth = FirebaseAuth.getInstance();
        logout = (Button)rootView.findViewById(R.id.button_logout);
        user = mAuth.getCurrentUser();

        // is there a current user?
        if (user != null){
            String email = user.getEmail();
            String uid = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).child("name");
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.getValue(String.class);
                    Name.setText(name);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            Email.setText(email);
            Uid.setText(uid);
        }

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user !=null){
                    mAuth.signOut();
                    getActivity().finish();
                    startActivity(new Intent(getContext(), LoginActivity.class));
                }
            }
        });
        // preferences
        button1 = rootView.findViewById(R.id.toggle_button1);
        button2 = rootView.findViewById(R.id.toggle_button2);
        button3 = rootView.findViewById(R.id.toggle_button3);
        button4 = rootView.findViewById(R.id.toggle_button4);
        button5 = rootView.findViewById(R.id.toggle_button5);
        button6 = rootView.findViewById(R.id.toggle_button6);
        button7 = rootView.findViewById(R.id.toggle_button7);
        button8 = rootView.findViewById(R.id.toggle_button8);
        button9 = rootView.findViewById(R.id.toggle_button9);
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // set the buttons to the checked values in Firebase
                button1.setChecked(dataSnapshot.child(getResources().getString(R.string.preference1)).getValue(Boolean.class));
                button2.setChecked(dataSnapshot.child(getResources().getString(R.string.preference2)).getValue(Boolean.class));
                button3.setChecked(dataSnapshot.child(getResources().getString(R.string.preference3)).getValue(Boolean.class));
                button4.setChecked(dataSnapshot.child(getResources().getString(R.string.preference4)).getValue(Boolean.class));
                button5.setChecked(dataSnapshot.child(getResources().getString(R.string.preference5)).getValue(Boolean.class));
                button6.setChecked(dataSnapshot.child(getResources().getString(R.string.preference6)).getValue(Boolean.class));
                button7.setChecked(dataSnapshot.child(getResources().getString(R.string.preference7)).getValue(Boolean.class));
                button8.setChecked(dataSnapshot.child(getResources().getString(R.string.preference8)).getValue(Boolean.class));
                button9.setChecked(dataSnapshot.child(getResources().getString(R.string.preference9)).getValue(Boolean.class));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        // toggle button click listeners
        button1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonToggled(isChecked, button1);
            }
        });
        button2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonToggled(isChecked, button2);
            }
        });
        button3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonToggled(isChecked, button3);
            }
        });
        button4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonToggled(isChecked, button4);
            }
        });
        button5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonToggled(isChecked, button5);
            }
        });
        button6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonToggled(isChecked, button6);
            }
        });
        button7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonToggled(isChecked, button7);
            }
        });
        button8.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonToggled(isChecked, button8);
            }
        });
        button9.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonToggled(isChecked, button9);
            }
        });

        return rootView;
    }

    // Run this whenever a preference is changed
    private void buttonToggled(boolean isChecked, ToggleButton button) {
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        userReference.child(String.valueOf(button.getTextOn())).setValue(isChecked);
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
