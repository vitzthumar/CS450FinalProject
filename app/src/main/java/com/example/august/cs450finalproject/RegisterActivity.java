package com.example.august.cs450finalproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText name;
    private EditText password;
    private EditText email;
    private Button button_register;
    private Button button_login;

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

    private boolean[] preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name = (EditText) findViewById(R.id.signup_name_input);
        email = (EditText) findViewById(R.id.signup_email_input);
        password = (EditText) findViewById(R.id.signup_password_input);
        button_register = (Button) findViewById(R.id.button_register);
        button_login = (Button) findViewById(R.id.button_login);
        mAuth = FirebaseAuth.getInstance();

        // preferences
        preferences = new boolean[] {false, false, false, false, false, false, false, false, false};
        button1 = findViewById(R.id.toggle_button1);
        button2 = findViewById(R.id.toggle_button2);
        button3 = findViewById(R.id.toggle_button3);
        button4 = findViewById(R.id.toggle_button4);
        button5 = findViewById(R.id.toggle_button5);
        button6 = findViewById(R.id.toggle_button6);
        button7 = findViewById(R.id.toggle_button7);
        button8 = findViewById(R.id.toggle_button8);
        button9 = findViewById(R.id.toggle_button9);

        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == button_register) {
                    RegisterUser();
                }
            }
        });
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == button_login) {
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                }
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
        button9.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonToggled(isChecked, 9);
            }
        });
    }

    // Run this whenever a preference is changed
    private void buttonToggled(boolean isChecked, int buttonNumber) {
        preferences[buttonNumber - 1] = isChecked;
    }


    public void RegisterUser(){
        final String Name = name.getText().toString().trim();
        final String Email = email.getText().toString().trim();
        String Password = password.getText().toString().trim();
        if (TextUtils.isEmpty(Name)){
            Toast.makeText(this, "A Field is Empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(Email)){
            Toast.makeText(this, "A Field is Empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(Password)){
            Toast.makeText(this, "A Field is Empty", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(Email, Password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        try {
                            //check if successful
                            if (task.isSuccessful()) {
                                //User is successfully registered and logged in
                                //start Profile Activity here
                                // create the new user that will be added from the supplied parameters
                                User newUser = new User(
                                        Name,
                                        Email);

                                DatabaseReference db = FirebaseDatabase.getInstance().getReference("Users");
                                // set the value in the database under the unique ID
                                db.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this, "registration successful", Toast.LENGTH_SHORT).show();

                                            // add the user's preferences
                                            DatabaseReference userReference = db.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                            userReference.child(getResources().getString(R.string.preference1)).setValue(preferences[0]);
                                            userReference.child(getResources().getString(R.string.preference2)).setValue(preferences[1]);
                                            userReference.child(getResources().getString(R.string.preference3)).setValue(preferences[2]);
                                            userReference.child(getResources().getString(R.string.preference4)).setValue(preferences[3]);
                                            userReference.child(getResources().getString(R.string.preference5)).setValue(preferences[4]);
                                            userReference.child(getResources().getString(R.string.preference6)).setValue(preferences[5]);
                                            userReference.child(getResources().getString(R.string.preference7)).setValue(preferences[6]);
                                            userReference.child(getResources().getString(R.string.preference8)).setValue(preferences[7]);
                                            userReference.child(getResources().getString(R.string.preference9)).setValue(preferences[8]);

                                            // set the user's default radius to 50 km
                                            userReference.child("radius").setValue(50);

                                            finish();
                                            startActivity(new Intent(getApplicationContext(), BottomNavigationActivity.class));
                                        } else {
                                            Toast.makeText(RegisterActivity.this, "Could not create user",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }else{
                                Toast.makeText(RegisterActivity.this, "Couldn't register, try again",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
    }
}
