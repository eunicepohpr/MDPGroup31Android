package com.example.mdpandroid.Version3;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mdpandroid.R;

import java.util.UUID;

public class MainActivity3 extends AppCompatActivity {
    private static final String TAG = "MainActivity3";
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TabAdapter3 tabAdapter;

    private TextView btTextView;
    private Toolbar btToolBar;
    private String device = "";
    BluetoothDevice mBTDevice;
    private static UUID myUUID;

    public MazeView3 mazeView;

    private int[] tabIcons = {R.drawable.tab_map, R.drawable.tab_com};

    // Declaration Variables
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static Context context;

    ProgressDialog myDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // mazeview
        mazeView = findViewById(R.id.mazeView2);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        btTextView = findViewById(R.id.btTV);
        btToolBar = findViewById(R.id.btTB);

        tabAdapter = new TabAdapter3(getSupportFragmentManager());
        tabAdapter.addFragment(new MapFragment3(), getString(R.string.TabMap));
        tabAdapter.addFragment(new ComFragment3(), getString(R.string.TabCom));
        viewPager.setAdapter(tabAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);

        btTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(com.example.mdpandroid.Version3.MainActivity3.this, BluetoothActivity3.class);
                i.putExtra("device", device);
                startActivity(i);
            }
        });

        // result receiver
//        LocalBroadcastManager.getInstance(this).registerReceiver(mNameReceiver, new IntentFilter("getConnectedDevice"));
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter("incomingMessage"));

        // Set up sharedPreferences
        com.example.mdpandroid.Version3.MainActivity3.context = getApplicationContext();
        this.sharedPreferences();
        editor.putString("connStatus", "");
        editor.commit();

        myDialog = new ProgressDialog(com.example.mdpandroid.Version3.MainActivity3.this);
        myDialog.setMessage("Waiting for other device to reconnect...");
        myDialog.setCancelable(false);
        myDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // only called once
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
    }

    public static void sharedPreferences() {
        sharedPreferences = com.example.mdpandroid.Version3.MainActivity3.getSharedPreferences(com.example.mdpandroid.Version3.MainActivity3.context);
        editor = sharedPreferences.edit();
    }

    // update the bluetooth toolbar
    public void updateBluetoothTBStatus(String device) {
        if (device != null) {
            if (!(device.equals(""))) {
                btToolBar.setBackgroundColor(ContextCompat.getColor(this, R.color.BTConnectedG)); // 0367a1
                btTextView.setText(getString(R.string.BTConnected, device));
                return;
            }
        }
        btToolBar.setBackgroundColor(ContextCompat.getColor(this, R.color.BTNotConnected));
        btTextView.setText(getString(R.string.BTNotConnected));
    }

    // update status whenever connection changes
//    private BroadcastReceiver mNameReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String theName = intent.getStringExtra("message"); // Get extra data included in the Intent
//            if (theName == "") { // no device connected, disable bluetooth-related actions
//                device = "";
//                updateBluetoothTBStatus(device);
//            } else { // device connected, enable all bluetooth-related actions
//                device = theName;
//                updateBluetoothTBStatus(device);
//            }
//        }
//    };

    private BroadcastReceiver mBroadcastReceiver5 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice mDevice = intent.getParcelableExtra("Device");
            String status = intent.getStringExtra("Status");
            sharedPreferences();

            if (status.equals("connected")) {
                try {
                    myDialog.dismiss();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "mBroadcastReceiver5: Device now connected to " + mDevice.getName());
                showToast("Device now connected to " + mDevice.getName());
                editor.putString("connStatus", "Connected to " + mDevice.getName());
                updateBluetoothTBStatus(mDevice.getName());
            } else if (status.equals("disconnected")) {
                Log.d(TAG, "mBroadcastReceiver5: Disconnected from " + mDevice.getName());
                showToast("Disconnected from " + mDevice.getName());
                editor.putString("connStatus", "");
                updateBluetoothTBStatus("");
                myDialog.show();
            }
            editor.commit();
        }
    };

    BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String theText = intent.getStringExtra("text");
            if (theText.length() > 0) {
                Log.d(TAG, "messageReceiver " + theText);
                MapFragment3.textReceived(theText);
                ComFragment3.refreshReceiveText(theText);
            }
        }
    };

    public void showToast(String message) {
        Toast.makeText(MainActivity3.this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            sharedPreferences = getApplicationContext().getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
            if (sharedPreferences.contains("connStatus"))
                device = sharedPreferences.getString("connStatus", "");
            updateBluetoothTBStatus(device);
            LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver5, new IntentFilter("ConnectionStatus"));

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver5);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver5);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 1:
                if(resultCode == Activity.RESULT_OK){
                    mBTDevice = data.getExtras().getParcelable("mBTDevice");
                    myUUID = (UUID) data.getSerializableExtra("myUUID");
                }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "Entering onSaveInstanceState");
        super.onSaveInstanceState(outState);

        outState.putString(TAG, "onSaveInstanceState");
        Log.d(TAG, "Exiting onSaveInstanceState");
    }

    @Override
    public void onBackPressed() { // disable destroying activity
    }
}