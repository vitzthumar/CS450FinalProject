package com.example.august.cs450finalproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private final static String LOGTAG = ProfileFragment.class.getSimpleName();
    // Auth stuff
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private TextView Name;
    private TextView Email;
    private Button logout;
    private Button deleteAccount;
    private ToggleButton displayLocation;

    private ImageButton profileImage;
    private int PICK_IMAGE_REQUEST = 1;

    // radius views
    private TextView radiusTextView;
    private SeekBar radiusSeekBar;

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
        Name = rootView.findViewById(R.id.profileName);
        Email = rootView.findViewById(R.id.profileEmail);
        mAuth = FirebaseAuth.getInstance();
        logout = rootView.findViewById(R.id.button_logout);
        deleteAccount = rootView.findViewById(R.id.button_delete_account);
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
        }
        // profile image
        profileImage = rootView.findViewById(R.id.profile_image);
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), PICK_IMAGE_REQUEST);
            }
        });

        // logout and delete account on click listeners
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
        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: fix this
            }
        });
        // display location
        displayLocation = rootView.findViewById(R.id.display_location);
        displayLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
                userReference.child("display_location").setValue(isChecked);

            }
        });

        // radius and SeekBar
        radiusTextView = rootView.findViewById(R.id.radius_text_view);
        radiusSeekBar = rootView.findViewById(R.id.radius_seek_bar);
        radiusSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);

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
                button1.setChecked(dataSnapshot.child("interests").child(getResources().getString(R.string.interests1)).getValue(Boolean.class));
                button2.setChecked(dataSnapshot.child("interests").child(getResources().getString(R.string.interests2)).getValue(Boolean.class));
                button3.setChecked(dataSnapshot.child("interests").child(getResources().getString(R.string.interests3)).getValue(Boolean.class));
                button4.setChecked(dataSnapshot.child("interests").child(getResources().getString(R.string.interests4)).getValue(Boolean.class));
                button5.setChecked(dataSnapshot.child("interests").child(getResources().getString(R.string.interests5)).getValue(Boolean.class));
                button6.setChecked(dataSnapshot.child("interests").child(getResources().getString(R.string.interests6)).getValue(Boolean.class));
                button7.setChecked(dataSnapshot.child("interests").child(getResources().getString(R.string.interests7)).getValue(Boolean.class));
                button8.setChecked(dataSnapshot.child("interests").child(getResources().getString(R.string.interests8)).getValue(Boolean.class));
                button9.setChecked(dataSnapshot.child("interests").child(getResources().getString(R.string.interests9)).getValue(Boolean.class));

                // set the radius and SeekBar
                int progress = dataSnapshot.child("radius").getValue(Integer.class);
                radiusSeekBar.setProgress(progress);
                radiusTextView.setText("Radius: " + progress + " kilometers");

                // also set the display location
                displayLocation.setChecked(dataSnapshot.child("display_location").getValue(Boolean.class));
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

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int currentRadius, boolean b) {
            // update the radius text view continuously as the user adjusts the SeekBar
            radiusTextView.setText("Radius: " + currentRadius + " kilometers");
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // called when the user first touches the SeekBar, so do nothing
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // the user has has finished moving the SeekBar, update their radius in Firebase
            DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
            userReference.child("radius").setValue(seekBar.getProgress());
        }
    };

    // Run this whenever a preference is changed
    private void buttonToggled(boolean isChecked, ToggleButton button) {
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        userReference.child("interests").child(String.valueOf(button.getTextOn())).setValue(isChecked);
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

    /*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                resizeBitmapFitXY(profileImage.getWidth(), profileImage.getHeight(), bitmap);

                profileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public Bitmap resizeBitmapFitXY(int width, int height, Bitmap bitmap){
        Bitmap background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        float originalWidth = bitmap.getWidth(), originalHeight = bitmap.getHeight();
        Canvas canvas = new Canvas(background);
        float scale, xTranslation = 0.0f, yTranslation = 0.0f;
        if (originalWidth > originalHeight) {
            scale = height/originalHeight;
            xTranslation = (width - originalWidth * scale)/2.0f;
        }
        else {
            scale = width / originalWidth;
            yTranslation = (height - originalHeight * scale)/2.0f;
        }
        Matrix transformation = new Matrix();
        transformation.postTranslate(xTranslation, yTranslation);
        transformation.preScale(scale, scale);
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        canvas.drawBitmap(bitmap, transformation, paint);
        return background;

    }*/
}
