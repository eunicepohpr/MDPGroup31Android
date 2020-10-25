package com.example.mdpandroid.Version1;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mdpandroid.R;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * class used for bluetooth activities that can be changed by user
 * UI for bluetooth connection
 */

public class BluetoothActivity extends AppCompatActivity {

    //scanning-connecting
    Button btnScanPaired;
    Button btnScanNew;
    Button btnEnableDiscoverable;
    ListView devicelist;
    ListView newDeviceList;
    TextView tvPaired;
    TextView tvNew;
    private BluetoothAdapter btAdapter = null;
    private ProgressDialog progress;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String address = "";
    String name = "";
    private String device;

    public static BluetoothService btService = null;
    public static StringBuffer mOutStringBuffer;

    private ArrayAdapter<String> newDevicesArrayAdapter;
    private ArrayAdapter<String> pairedDevicesArrayAdapter;

    //navbar
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_main);

        //get intent
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey("device")) {
                device = bundle.getString("device");
            } else {
                device = "";
            }
        } else {
            device = "";
        }

        //nav bar
        dl = (DrawerLayout) findViewById(R.id.bluetooth_main);
        dl.addDrawerListener(t);
        t = new ActionBarDrawerToggle(this, dl, R.string.app_name, R.string.app_name);
        t.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        nv = (NavigationView) findViewById(R.id.nv);
        nv.setItemIconTintList(null);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.home:
                        Intent i = new Intent(BluetoothActivity.this, MainActivity.class);
                        i.putExtra("name", name);
                        i.putExtra("device", device);
                        startActivity(i);
                        return true;
                    case R.id.bluetooth:
                        dl.closeDrawers();
                        return true;
                    case R.id.settings:
                        Intent x = new Intent(BluetoothActivity.this, SettingsActivity.class);
                        x.putExtra("device", device);
                        startActivity(x);
                        return true;
                    default:
                        return true;
                }

            }
        });

        /**
         * Create SpannableString to modify text's properties for the header "Paired Devices"
         */
        tvPaired = (TextView) findViewById(R.id.textViewPaired);
        String forTvPaired = "Paired Devices";
        SpannableString ss1 = new SpannableString(forTvPaired);
        //set size of text
        ss1.setSpan(new RelativeSizeSpan(1.5f), 0, 14, 0);
        //set colour of text
        ss1.setSpan(new ForegroundColorSpan(Color.BLACK), 0, 14, 0);
        //set underline for text
        //ss1.setSpan(new UnderlineSpan(), 0, 14, 0);
        //bind text to tvPaired
        tvPaired.setText(ss1);

        /**
         * Create SpannableString to modify text's properties for the header "Available Devices"
         */
        tvNew = (TextView) findViewById(R.id.textViewNew);
        String forTvNew = "Available Devices";
        SpannableString ss2 = new SpannableString(forTvNew);
        //set size of text
        ss2.setSpan(new RelativeSizeSpan(1.5f), 0, 17, 0);
        //set colour of text
        ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, 17, 0);
        //set underline for text
        //ss2.setSpan(new UnderlineSpan(), 0, 17, 0);
        //bind text to tvNew
        tvNew.setText(ss2);

        btnScanPaired = (Button) findViewById(R.id.scanPairedButton);
        devicelist = (ListView) findViewById(R.id.devicesListView);
        pairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        devicelist.setAdapter(pairedDevicesArrayAdapter);
        devicelist.setOnItemClickListener(myListClickListener);

        btnScanNew = (Button) findViewById(R.id.scanNewButton);
        newDeviceList = (ListView) findViewById(R.id.newDevicesListView);
        newDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        ArrayList list2 = new ArrayList();
        newDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list2);
        newDeviceList.setAdapter(newDevicesArrayAdapter);
        newDeviceList.setOnItemClickListener(myListClickListener);

        btnEnableDiscoverable = (Button) findViewById(R.id.enableDiscoverableBtn);


        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            //no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            finish();
        } else {
            if (!btAdapter.isEnabled()) {
                //bluetooth not enabled
                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBTon, 1);
            } else {
                //use android's .getBondedDevices() to retrieve a list of paired devices attached to the phone
                Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
                ArrayList list = new ArrayList();

                if (pairedDevices.size() > 0) {
                    //found at least one paired devices
                    for (BluetoothDevice bt : pairedDevices) {
                        //add all paired devices to list
                        list.add(bt.getName() + "\n MAC Address: " + bt.getAddress()); //Get the device's name and the address
                    }
                } else {
                    //no paired devices found on device
                    list.add("No Paired Bluetooth Devices Found");
                }

                //bind list to adapter
                final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
                //set adapter so it can be updated dynamically in the future
                devicelist.setAdapter(adapter);
            }
        }

        btnScanPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairedDevicesList(); //method that will be called
            }
        });

        btnScanNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newDevicesList(); // method that will be called
            }
        });

        btnEnableDiscoverable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableDiscovery(); //method that will be called
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop discovery
        if (btAdapter != null) {
            btAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        try {
            this.unregisterReceiver(bReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }


    //method for enabling discovery of the bluetooth device to other devices
    private void enableDiscovery() {

        if (btAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {

            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    //method called when user manually finds paired devices
    private void pairedDevicesList() {
        //notify user of his button click
        Toast.makeText(this, "Refreshing all paired devices", Toast.LENGTH_SHORT).show();
        //check if user enabled required permissions
        checkBTPermissions();
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            //no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
        } else {
            if (!btAdapter.isEnabled()) {
                //bluetooth not enabled
                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBTon, 1);
            }
        }

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        pairedDevicesArrayAdapter.clear();
        if (pairedDevices.size() > 0) {
            //found at least one paired devices
            for (BluetoothDevice bt : pairedDevices) {
                //add all paired devices to adapter
                pairedDevicesArrayAdapter.add(bt.getName() + "\n MAC Address:- " + bt.getAddress()); //Get the device's name and the address
            }
        } else {
            //no paired devices found
//            pairedDevicesArrayAdapter.clear(); //clear arrayadapter since no paired devices found
            pairedDevicesArrayAdapter.add("No Paired Bluetooth Devices Found"); //set first item to this message to let user know the result
        }
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView av, View v, int arg2, long arg3) {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            address = info.substring(info.length() - 17);
            name = info.substring(0, info.length() - 30);
            final BluetoothDevice device = btAdapter.getRemoteDevice(address);
            btService = new BluetoothService(getApplicationContext(), mHandler);

            //create ProgressDialog to let user know that application is trying to connect to device
            progress = ProgressDialog.show(BluetoothActivity.this, "Connecting...", "Please wait");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    btService.connect(device, false); //connect to the device that was clicked
                    progress.dismiss(); //close ProgressDialog
                    Intent i = new Intent(BluetoothActivity.this, MainActivity.class);
                    registerReceivers(); //register receivers to allow current activity to continue receiving & executing messages
                    startActivity(i); //go to MainActivity page
                }
            }, 1000); //delay of 1s

        }
    };

    //scan available nearby bluetooth devices
    private void newDevicesList() {
        //notify user of button click
        Toast.makeText(this, "Scanning has begun", Toast.LENGTH_SHORT).show();
        //if device is already discovering, cancel it
        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }

        //check if user has enabled required permissions
        checkBTPermissions();

        //start discovering
        btAdapter.startDiscovery();
        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bReceiver, discoverDevicesIntent);
    }

    //broadcast receiver for bluetooth
    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //when a bluetooth device is found
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String newDevice = device.getName() + "\n MAC Address: " + device.getAddress();
                newDevicesArrayAdapter.add(newDevice); //add device to array adapter
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //when bluetooth has completed scanning
                if (newDevicesArrayAdapter.getCount() == 0) {
                    //if no devices found
                    String noDevices = "No devices found"; //set first item to let user know the results
                    newDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };

    /**
     * check if user has enabled permissions for access_fine_location and access_coarse_location
     * required only if user's device's SDK is after LOLLIPOP's version
     */
    private void checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 200);
            // Otherwise, setup the chat session
        } else if (btService == null) {
            setupChat();
        }
    }

    // Initialize the BluetoothService to perform bluetooth connections
    private void setupChat() {
        btService = new BluetoothService(getApplicationContext(), mHandler);
        mOutStringBuffer = new StringBuffer("");
    }

    /**
     * The Handler that gets information back from the BluetoothService
     */
    public final Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            //when notified that bluetooth service has connected to a device
                            sendToMain(name); //send name of device to MainActivity
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            //when notified that bluetooth service is connecting to a device
                            sendToMain(""); //send empty string to MainActivity to notify that no devices connected currently
                            name = ""; //set name to empty string since no devices is currently connected
                            break;
                        case BluetoothService.STATE_LISTEN:
                            //when notified that bluetooth service is listening for devices
                            sendToMain(""); //send empty string to MainActivity to notify that no devices connected currently
                            name = ""; //set name to empty string since no devices is currently connected
                        case BluetoothService.STATE_NONE:
                            sendToMain(""); //send empty string to MainActivity to notify that no devices connected currently
                            name = ""; //set name to empty string since no devices is currently connected
                            break;
                    }
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    sendTextToMain(readMessage); //send message to MainActivity that was received in buffer
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    name = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != getApplicationContext()) {
                        if (name == null) {

                        } else {
                            Toast.makeText(getApplicationContext(), "Connected to " + name, Toast.LENGTH_SHORT).show();
                            //send to mainactivity
                            sendToMain(name); //name of device currently connected
                        }
                    }

                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != getApplicationContext()) {
                        String theMsg = msg.getData().getString(Constants.TOAST);
                        if (theMsg.equalsIgnoreCase("device connection was lost")) {
                            Toast.makeText(getApplicationContext(), theMsg, Toast.LENGTH_SHORT).show();
                            name = ""; //set name to empty string since connection was lost
                            sendToMain(""); //send empty string to mainactivity to notify no device currently connected
                        }
                    }
                    break;
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                dl.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //method to pass data to MainActivity
    private void sendToMain(String msg) {
        Intent intent = new Intent("getConnectedDevice");
        // You can also include some extra data.
        intent.putExtra("message", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    //method to send text received from bluetooth connection
    private void sendTextToMain(String msg) {
        Intent intent = new Intent("getTextFromDevice");
        // You can also include some extra data.
        intent.putExtra("text", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    //get sent text from MainActivity
    private BroadcastReceiver mTextReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String theText = intent.getStringExtra("tts");
            if (theText != null) {
                if (btService.getState() != btService.STATE_CONNECTED) {
                    Toast.makeText(getApplicationContext(), "Connection Lost. Please try again.", Toast.LENGTH_SHORT).show();
                    return;
                }
                //send out message
                byte[] send = theText.getBytes();
                btService.write(send);

                // Reset out string buffer to zero
                mOutStringBuffer.setLength(0);

            }
        }
    };

    //get robot movements from MainActivity
    private BroadcastReceiver mCtrlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String control = intent.getStringExtra("control");
            if (control != null) {
                if (btService.getState() != btService.STATE_CONNECTED) {
                    Toast.makeText(getApplicationContext(), "Connection Lost. Please try again.", Toast.LENGTH_SHORT).show();
                    return;
                }
                //send out message
                byte[] send = control.getBytes();
                btService.write(send);
                mOutStringBuffer.setLength(0);

            }
        }
    };

    //listen for disconnection from MainActivity
    private BroadcastReceiver mDcReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String control = intent.getStringExtra("disconnect");
            if (control != null) {
                if (btService.getState() != btService.STATE_CONNECTED) {
                    Toast.makeText(getApplicationContext(), "Connection Lost. Please try again.", Toast.LENGTH_SHORT).show();
                    return;
                }
                destroyReceivers();
                btService.stop();
                btService.start();
            }
        }
    };

    //register receivers needed
    private void registerReceivers() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mTextReceiver, new IntentFilter("getTextToSend"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mCtrlReceiver, new IntentFilter("getCtrlToSend"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mDcReceiver, new IntentFilter("initiateDc"));
        // Register for broadcasts when discovery has finished
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(bReceiver, filter);
    }

    //destroy all receivers
    private void destroyReceivers() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mTextReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCtrlReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mDcReceiver);
        unregisterReceiver(bReceiver);
    }

}
