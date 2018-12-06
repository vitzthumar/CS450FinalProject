package com.example.august.cs450finalproject;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.function.DoubleToLongFunction;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private final static String LOGTAG = MapsActivity.class.getSimpleName();
    private final static int ICON_SIZE_MODIFIER = 12;
    private FirebaseStorage firebaseStorage;
    private DatabaseReference database;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        firebaseStorage = FirebaseStorage.getInstance();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // get the information on each user's location
        Intent receiveIntent = this.getIntent();
        Double userLat = Double.parseDouble(receiveIntent.getStringExtra("USER_LAT"));
        Double userLon = Double.parseDouble(receiveIntent.getStringExtra("USER_LON"));
        Double otherLat = Double.parseDouble(receiveIntent.getStringExtra("OTHER_LAT"));
        Double otherLon = Double.parseDouble(receiveIntent.getStringExtra("OTHER_LON"));
        String otherName = getIntent().getStringExtra("OTHER_NAME");
        String userID = receiveIntent.getStringExtra("USER_ID");
        String otherID = receiveIntent.getStringExtra("OTHER_ID");

        // add a marker at both users' locations
        LatLng userLocation = new LatLng(userLat, userLon);
        LatLng otherLocation = new LatLng(otherLat, otherLon);

        // position and zoom the camera to capture both points
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(userLocation);
        builder.include(otherLocation);
        LatLngBounds bounds = builder.build();
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.2); // offset from edges of the map 10% of screen
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        googleMap.animateCamera(cu);

        // load in the image referenced on Firebase
        DatabaseReference urlReference = FirebaseDatabase.getInstance().getReference("URL");
        urlReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // get the two URLs
                String userURL = dataSnapshot.child(userID).getValue(String.class);
                String otherURL = dataSnapshot.child(otherID).getValue(String.class);

                // download the bitmaps from these URLs
                downloadFromURLs(userURL, userLocation, "My location");
                downloadFromURLs(otherURL, otherLocation, (otherName + "'s location"));

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    private void downloadFromURLs(String url, LatLng location, String tag) {

        StorageReference storageReference = firebaseStorage.getReferenceFromUrl(url);

        final long ONE_MEGABYTE = 1024 * 1024;
        storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {

                // get the bitmap and then update the user icon
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(createUserBitmap(bitmap));

                Marker newMarker = mMap.addMarker(new MarkerOptions().position(location).title(tag));
                newMarker.setIcon(icon);

            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e(LOGTAG, "Failed to download URL");
            }
        });
    }

    private Bitmap createUserBitmap(Bitmap profileBitmap) {
        Bitmap result = null;
        try {
            result = Bitmap.createBitmap(pxToDp(80), pxToDp(80), Bitmap.Config.ARGB_8888);
            result.eraseColor(Color.TRANSPARENT);
            Canvas canvas = new Canvas(result);
            Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.icon, null);
            drawable.setBounds(0, 0, pxToDp(80), pxToDp(80));
            drawable.draw(canvas);

            Paint roundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            RectF bitmapRect = new RectF();

            canvas.save();

            if (profileBitmap != null) {

                canvas.translate(pxToDp(12), pxToDp(2));

                BitmapShader shader = new BitmapShader(profileBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                Matrix matrix = new Matrix();
                float scale = pxToDp(56) / (float) profileBitmap.getWidth();
                matrix.postTranslate(pxToDp(0), pxToDp(0));
                matrix.postScale(scale, scale);
                roundPaint.setShader(shader);
                shader.setLocalMatrix(matrix);

                bitmapRect.set(0, 0, pxToDp(56), pxToDp(56));
                canvas.drawRoundRect(bitmapRect, pxToDp(28), pxToDp(28), roundPaint);
            }
            canvas.restore();
            try {
                canvas.setBitmap(null);
            } catch (Exception e) {}
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return result;
    }

    public static int pxToDp(int px) {

        int modifiedPX = px * ICON_SIZE_MODIFIER;

        return (int) (modifiedPX / Resources.getSystem().getDisplayMetrics().density);
    }
}
