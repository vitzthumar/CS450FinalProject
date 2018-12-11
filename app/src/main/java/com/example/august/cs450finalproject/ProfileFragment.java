package com.example.august.cs450finalproject;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private final static int PERMISSION_REQUEST_CODE = 999;
    private final static String LOGTAG = ProfileFragment.class.getSimpleName();
    // Auth stuff
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseStorage firebaseStorage;

    private TextView Name;
    private TextView Email;
    private Button logout;
    private ToggleButton displayLocation;

    private ImageButton profileImage;
    private Bitmap profileBitmap;
    private static final int PROFILE_WDITH = 400;
    private int PICK_IMAGE_REQUEST = 1;

    // radius views
    private TextView radiusTextView;
    private SeekBar radiusSeekBar;

    // toggle buttons
    private CheckBox button1, button2, button3, button4, button5, button6, button7, button8;

    // interests
    private int[] interests = {R.string.interests1, R.string.interests2, R.string.interests3, R.string.interests4, R.string.interests5, R.string.interests6, R.string.interests7, R.string.interests8};

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

        // get read external storage permissions
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        // Auth stuff
        Name = rootView.findViewById(R.id.profileName);
        Email = rootView.findViewById(R.id.profileEmail);
        mAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        logout = rootView.findViewById(R.id.button_logout);
        user = mAuth.getCurrentUser();

        // profile image
        profileImage = rootView.findViewById(R.id.profile_image);

        // load in the image referenced on Firebase
        DatabaseReference urlReference = FirebaseDatabase.getInstance().getReference("URL");
        urlReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                downloadFromURL(dataSnapshot.child(user.getUid()).getValue(String.class));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


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
        button1 = (CheckBox) rootView.findViewById(R.id.profile_toggle_button1);
        button2 = (CheckBox) rootView.findViewById(R.id.profile_toggle_button2);
        button3 = (CheckBox) rootView.findViewById(R.id.profile_toggle_button3);
        button4 = (CheckBox) rootView.findViewById(R.id.profile_toggle_button4);
        button5 = (CheckBox) rootView.findViewById(R.id.profile_toggle_button5);
        button6 = (CheckBox) rootView.findViewById(R.id.profile_toggle_button6);
        button7 = (CheckBox) rootView.findViewById(R.id.profile_toggle_button7);
        button8 = (CheckBox) rootView.findViewById(R.id.profile_toggle_button8);
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
                buttonToggled(isChecked, 1);
            }
        });
        button2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonToggled(isChecked, 2);
            }
        });
        button3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonToggled(isChecked, 3);
            }
        });
        button4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonToggled(isChecked, 4);
            }
        });
        button5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonToggled(isChecked, 5);
            }
        });
        button6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonToggled(isChecked, 6);
            }
        });
        button7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonToggled(isChecked, 7);
            }
        });
        button8.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonToggled(isChecked, 8);
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
    private void buttonToggled(boolean isChecked, int buttonNum) {
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        userReference.child("interests").child(getResources().getString(interests[buttonNum-1])).setValue(isChecked);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                profileBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);

                // get the path and then flip the orientation if it is not upright
                String absolutePath = getPathFromUri(getContext(), uri);

                Bitmap modifiedBitmap = modifyOrientation(profileBitmap, absolutePath);
                modifiedBitmap = scaleCenterCrop(modifiedBitmap, PROFILE_WDITH, PROFILE_WDITH);

                // update the profile image to the new bitmap
                updateProfileImage(modifiedBitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        // Compute the scaling factors to fit the new height and width, respectively.
        // To cover the final image, the final scaling will be the bigger
        // of these two.
        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        // Now get the size of the source bitmap when scaled
        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        // Let's find out the upper left coordinates if the scaled bitmap
        // should be centered in the new size give by the parameters
        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        // Finally, we create a new bitmap of the specified size and draw our new,
        // scaled bitmap onto it.
        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, null, targetRect, null);

        return dest;
    }

    public static Bitmap modifyOrientation(Bitmap bitmap, String imagePath) throws IOException {
        ExifInterface ei = new ExifInterface(imagePath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotate(bitmap, 90);

            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotate(bitmap, 180);

            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotate(bitmap, 270);

            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                return flip(bitmap, true, false);

            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                return flip(bitmap, false, true);

            default:
                return bitmap;
        }
    }

    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static String getPathFromUri(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {


        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    private void updateProfileImage(Bitmap newBitmap) {

        profileImage.setEnabled(false);

        Thread thread = new Thread() {
            @Override
            public void run() {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                newBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] data = baos.toByteArray();

                String storagePath = user.getUid() + ".png";
                StorageReference storageReference = firebaseStorage.getReference(storagePath);

                UploadTask uploadTask = storageReference.putBytes(data);
                uploadTask.addOnSuccessListener(getActivity(), new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        profileImage.setImageDrawable(null);
                        profileImage.setImageBitmap(newBitmap);
                        profileImage.setEnabled(true);

                        // upload the new url
                        Uri url = taskSnapshot.getDownloadUrl();
                        uploadURL(url);
                    }
                });
            }
        };
        thread.start();
    }

    // Used to upload a url to Firebase realtime database that corresponds to that user's profile image
    private void uploadURL(Uri url) {
        DatabaseReference urlReference = FirebaseDatabase.getInstance().getReference("URL");
        urlReference.child(user.getUid()).setValue(url.toString());
    }


    private void downloadFromURL(String url) {

        StorageReference storageReference = firebaseStorage.getReferenceFromUrl(url);

        final long ONE_MEGABYTE = 1024 * 1024;
        storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
              @Override
              public void onSuccess(byte[] bytes) {


                  // get the bitmap and then update the profile image
                  Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                  profileImage.setImageDrawable(null);
                  profileImage.setImageBitmap(bitmap);
                  profileImage.setEnabled(true);
              }
              }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e(LOGTAG, "Failed to download");
                }
        });
    }
}
