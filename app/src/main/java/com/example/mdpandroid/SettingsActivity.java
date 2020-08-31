package com.example.mdpandroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * class for Settings UI
 * used for saving custom messages on app
 */
public class SettingsActivity extends AppCompatActivity {

    private Button prefixedOne;
    private Button prefixedTwo;
    private Button saveBtn;
    private Button editBtn;
    private EditText etOne;
    private EditText etTwo;
    private TextView tvOne;
    private TextView tvTwo;
    private String device;

    //navbar
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //get intent
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            if (bundle.containsKey("device")){
                device = bundle.getString("device");
            }
        } else {
            device = "";
        }

        prefixedOne = findViewById(R.id.button1);
        prefixedTwo = findViewById(R.id.button2);
        saveBtn = findViewById(R.id.buttonSave);
        editBtn = findViewById(R.id.buttonEdit);
        etOne = findViewById(R.id.editText1);
        etTwo = findViewById(R.id.editText2);
        tvOne = findViewById(R.id.textView5);
        tvTwo = findViewById(R.id.textView6);

        saveBtn.setVisibility(View.INVISIBLE);
        etOne.setVisibility(View.INVISIBLE);
        etTwo.setVisibility(View.INVISIBLE);
        tvOne.setVisibility(View.INVISIBLE);
        tvTwo.setVisibility(View.INVISIBLE);

        //nav bar
        dl = (DrawerLayout)findViewById(R.id.activity_settings);
        //--
        dl.addDrawerListener(t);
        t = new ActionBarDrawerToggle(this, dl,R.string.app_name, R.string.app_name);
        t.syncState();
        //--
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        nv = (NavigationView)findViewById(R.id.nv);
        nv.setItemIconTintList(null);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.home:
                        Intent i = new Intent(SettingsActivity.this, MainActivity.class);
                        i.putExtra("device", device);
                        startActivity(i);
                        return true;
                    case R.id.bluetooth:
                        Intent x = new Intent(SettingsActivity.this, BluetoothActivity.class);
                        x.putExtra("device", device);
                        startActivity(x);
                        return true;
                    case R.id.settings:
                        dl.closeDrawers();
                        return true;
                    default:
                        return true;
                }

            }
        });

        /**
         * enable custom messages to be edited
         */
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBtn.setVisibility(View.VISIBLE);
                etOne.setVisibility(View.VISIBLE);
                etTwo.setVisibility(View.VISIBLE);
                tvOne.setVisibility(View.VISIBLE);
                tvTwo.setVisibility(View.VISIBLE);
                editBtn.setEnabled(false);
                editBtn.setVisibility(View.INVISIBLE);

                SharedPreferences sharedPref = getSharedPreferences("pref", Context.MODE_PRIVATE);
                if (sharedPref.contains("value1")){
                    etOne.setText(sharedPref.getString("value1", ""));
                } else {
                    etOne.setText("Default Message 1");
                }
                if (sharedPref.contains("value2")){
                    etTwo.setText(sharedPref.getString("value2", ""));
                } else {
                    etTwo.setText("Default Message 2");
                }
            }
        });

        /**
         * save custom messages onto app's SharedPreferences
         */
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check if any of the edit texts are empty
                if (etOne.getText().toString().trim().equalsIgnoreCase("") ||
                        etTwo.getText().toString().trim().equalsIgnoreCase("")){
                    Toast.makeText(SettingsActivity.this, "Custom messages cannot be empty", Toast.LENGTH_SHORT).show();
                }
                //use .comit() to save onto app's SharedPreferences
                else {
                    saveBtn.setVisibility(View.INVISIBLE);
                    etOne.setVisibility(View.INVISIBLE);
                    etTwo.setVisibility(View.INVISIBLE);
                    tvOne.setVisibility(View.INVISIBLE);
                    tvTwo.setVisibility(View.INVISIBLE);
                    editBtn.setEnabled(true);
                    editBtn.setVisibility(View.VISIBLE);

                    SharedPreferences sharedPref = getSharedPreferences("pref", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("value1", etOne.getText().toString());
                    editor.putString("value2", etTwo.getText().toString());
                    editor.commit();
                    Toast.makeText(SettingsActivity.this, "Custom messages saved", Toast.LENGTH_SHORT).show();
                }

            }
        });

        /**
         * send out custom message 1 that is saved on app's SharedPreferences
         * if no custom message 1 set on app's SharedPreferences, send "Default Message 1"
         */
        prefixedOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set default message
                String value = "Default Message 1";
                //retrieve app's SharedPreferences
                SharedPreferences sharedPref = getSharedPreferences("pref", Context.MODE_PRIVATE);
                //if app's SharedPreferences has "value1" as key
                if (sharedPref.contains("value1")){
                    //change default message to value retrieved from SharedPreferences
                    value = sharedPref.getString("value1", "");
                }
                //send ToastMessage to notify user that message is sent
                Toast.makeText(SettingsActivity.this, "Custom Message 1 sent", Toast.LENGTH_SHORT).show();
                //use method to send to BluetoothActivity's handler to send out message
                sendToBtAct(value);
            }
        });

        /**
         * send out custom message 2 that is saved on app's SharedPreferences
         * if no custom message 2 set on app's SharedPreferences, send "Default Message 2"
         */
        prefixedTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set default message
                String value = "Default-Message 2";
                //retrieve app's SharedPreferences
                SharedPreferences sharedPref = getSharedPreferences("pref", Context.MODE_PRIVATE);
                //if app's SharedPreferences has "value2" as key
                if (sharedPref.contains("value2")){
                    //change default message to value retrieved from SharedPreferences
                    value = sharedPref.getString("value2", "");
                }
                //send ToastMessage to notify user that message is sent
                Toast.makeText(SettingsActivity.this, "Custom Message 2 sent", Toast.LENGTH_SHORT).show();
                //use method to send to BluetoothActivity's handler to send out message
                sendToBtAct(value);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                dl.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //to send bluetoothactivity for bluetooth chat
    private void sendToBtAct(String msg) {
        Intent intent = new Intent("getTextToSend");
        // You can also include some extra data.
        intent.putExtra("tts", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


}
