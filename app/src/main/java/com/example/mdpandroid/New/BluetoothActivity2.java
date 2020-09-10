package com.example.mdpandroid.New;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mdpandroid.BluetoothActivity;
import com.example.mdpandroid.BluetoothService;
import com.example.mdpandroid.Constants;
import com.example.mdpandroid.MainActivity;
import com.example.mdpandroid.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BluetoothActivity2 extends AppCompatActivity {
    private String device = "";
    private String btnScanTx, btnRefreshTx;

    private TextView btTextView;
    private Toolbar btToolBar;
    private ListView lvPairedDevices, lvAvailDevices;
    private TextView tvNoPairDevices, tvNoAvailDevices;
    private Button btnDiscover, btnRefresh, btnScan;

    private ArrayAdapter<String> newDevicesArrayAdapter;
    private ArrayAdapter<String> pairedDevicesArrayAdapter;

    private ProgressBar pbPair, pbAvail;
    private ProgressDialog progress;

    private BluetoothAdapter btAdapter = null;
    public static BluetoothService btService = null;
    public static StringBuffer mOutStringBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth2);

        // bluetooth status toolbar
        btTextView = findViewById(R.id.btTV);
        btToolBar = findViewById(R.id.btTB);

        // update connected device status
        Bundle bundle = getIntent().getExtras();
        device = bundle != null && bundle.containsKey("device") ? bundle.getString("device") : "";
        updateBluetoothTBStatus(device);

        lvPairedDevices = findViewById(R.id.lvPairedDevices);
        lvAvailDevices = findViewById(R.id.lvAvailableDevices);
        tvNoPairDevices = findViewById(R.id.tvPaired);
        tvNoAvailDevices = findViewById(R.id.tvAvail);

        btnDiscover = findViewById(R.id.btnDiscover);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnScan = findViewById(R.id.btnScan);

        pbPair = findViewById(R.id.pbPair);
        pbAvail = findViewById(R.id.pbAvail);

        pairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        lvPairedDevices.setAdapter(pairedDevicesArrayAdapter);
        lvPairedDevices.setOnItemClickListener(myListClickListener);

        newDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        lvAvailDevices.setAdapter(newDevicesArrayAdapter);
        lvAvailDevices.setOnItemClickListener(myListClickListener);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) { // no bluetooth adapter
            showToast("Bluetooth Device Not Available");
//            finish();
        } else {
            if (!btAdapter.isEnabled()) // bluetooth not enabled
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1);
            else
                updatePairDevicesList();
        }

        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableDiscovery();
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePairDevicesList();
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateAvailableDeviceList();
            }
        });

    }


    /**
     * check if user has enabled permissions for access_fine_location and access_coarse_location
     * required only if user's device's SDK is after LOLLIPOP's version
     */
    private void checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0)
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
        }
    }

    // ------------------------- UI Methods

    // Update the bluetooth toolbar
    public void updateBluetoothTBStatus(String device) {
        if (!(device.equals("") || device == null)) {
            btToolBar.setBackgroundColor(ContextCompat.getColor(this, R.color.BTConnectedG)); // 0367a1
            btTextView.setText(getString(R.string.BTConnected, device));
        } else {
            btToolBar.setBackgroundColor(ContextCompat.getColor(this, R.color.BTNotConnected));
            btTextView.setText(getString(R.string.BTNotConnected));
        }
    }

    // method for enabling discovery of the bluetooth device to other devices
    private void enableDiscovery() {
        if (btAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 5);
            startActivity(discoverableIntent);
        }
    }

    // scan available nearby bluetooth devices
    public void updateAvailableDeviceList() {
        String btnStatus = String.valueOf(btnScan.getText()); // save btn text
        if (btnStatus.equals("Stop")) {
            if (btAdapter.isDiscovering()) // if device is already discovering, cancel it
                btAdapter.cancelDiscovery();
            btnScan.setText(btnScanTx);
            showToast("Scanning stopped"); // notify user of button click
            pbAvail.setVisibility(View.GONE); // hide progress bar

        } else {
            newDevicesArrayAdapter.clear(); // clear list of available devices
            btnScanTx = btnStatus; // save btn text
            btnScan.setText("Stop"); // clear btn text
            tvNoAvailDevices.setVisibility(View.GONE);
            pbAvail.setVisibility(View.VISIBLE); // show button progress bar
            showToast("Scanning for devices..."); // notify user of button click

            if (btAdapter.isDiscovering()) // if device is already discovering, cancel it
                btAdapter.cancelDiscovery();

            checkBTPermissions(); // check if user has enabled required permissions

            btAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(bReceiver, discoverDevicesIntent);
        }
    }

    public void updatePairDevicesList() { // use android's .getBondedDevices() to retrieve list of paired devices attached to phone
        pairedDevicesArrayAdapter.clear();
        btnRefreshTx = String.valueOf(btnRefresh.getText()); // save btn text
        btnRefresh.setText(""); // clear btn text
        pbPair.setVisibility(View.VISIBLE); // show progress bar
        lvPairedDevices.setVisibility(View.GONE); // hide list view
        tvNoPairDevices.setVisibility(View.GONE); // hide no devices tv

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        ArrayList<String> list = new ArrayList<String>();
        final boolean hasList = pairedDevices.size() > 0;

        if (hasList) { // found at least one paired devices
            for (BluetoothDevice bt : pairedDevices)// add all paired devices to list
                list.add(bt.getName() + "\n MAC Address: " + bt.getAddress());
            pairedDevicesArrayAdapter.addAll(list);
        }

        new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long l) {
            }

            public void onFinish() {
                lvPairedDevices.setVisibility(View.VISIBLE);
                pbPair.setVisibility(View.GONE);
                btnRefresh.setText(btnRefreshTx);
                if (hasList)
                    tvNoPairDevices.setVisibility(View.GONE);
                else
                    tvNoPairDevices.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView av, View v, int arg2, long arg3) {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            device = info.substring(0, info.length() - 30);
            final BluetoothDevice deviceMac = btAdapter.getRemoteDevice(address);
            btService = new BluetoothService(getApplicationContext(), mHandler);

            // create ProgressDialog to let user know that application is trying to connect to device
            progress = ProgressDialog.show(BluetoothActivity2.this, "Connecting...", "Please wait");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    btService.connect(deviceMac, false); // connect to the device that was clicked
                    progress.dismiss(); //close ProgressDialog
//                    Intent i = new Intent(BluetoothActivity2.this, MainActivity.class);
                    registerReceivers(); // register receivers to allow current activity to continue receiving & executing messages
//                    startActivity(i); // go to MainActivity page
                }
            }, 1000); //delay of 1s

        }
    };

    /**
     * The Handler that gets information back from the BluetoothService
     */
    public final Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED: // bluetooth service has connected to a device
                            sendToMain(device); // send name of device to MainActivity
                            break;
                        case BluetoothService.STATE_CONNECTING: // bluetooth service is connecting to a device
                            sendToMain(""); //send empty string to MainActivity to notify that no devices connected currently
                            device = ""; // set name to empty string since no devices is currently connected
                            break;
                        case BluetoothService.STATE_LISTEN: // bluetooth service is listening for devices
                            sendToMain(""); //send empty string to MainActivity to notify that no devices connected currently
                            device = ""; // set name to empty string since no devices is currently connected
                        case BluetoothService.STATE_NONE:
                            sendToMain(""); // send empty string to MainActivity to notify that no devices connected currently
                            device = ""; // set name to empty string since no devices is currently connected
                            break;
                    }
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1); // construct a string from the valid bytes in the buffer
                    sendTextToMain(readMessage); // send message to MainActivity that was received in buffer
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    device = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != getApplicationContext()) {
                        if (device != null) {
                            showToast("Connected to " + device);
                            // send to mainactivity
                            sendToMain(device); //name of device currently connected
                        }
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != getApplicationContext()) {
                        String theMsg = msg.getData().getString(Constants.TOAST);
                        if (theMsg.equalsIgnoreCase("device connection was lost")) {
                            Toast.makeText(getApplicationContext(), theMsg, Toast.LENGTH_SHORT).show();
                            device = ""; // set name to empty string since connection was lost
                            sendToMain(""); // send empty string to mainactivity to notify no device currently connected
                        }
                    }
                    break;
            }
        }
    };

    // method to pass data to MainActivity
    private void sendToMain(String msg) {
        updateBluetoothTBStatus(msg);
        Intent intent = new Intent("getConnectedDevice");
        intent.putExtra("message", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    // method to send text received from bluetooth connection
    private void sendTextToMain(String msg) {
        Intent intent = new Intent("getTextFromDevice");
        // You can also include some extra data.
        intent.putExtra("text", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

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

    // broadcast receiver for bluetooth
    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            showToast(action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) { // when a remote device is found during discovery
                tvNoAvailDevices.setVisibility(View.GONE);
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String newDevice = device.getName() + "\n MAC Address: " + device.getAddress();
                newDevicesArrayAdapter.add(newDevice); // add device to array adapter
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) { // when bluetooth has completed scanning
                pbAvail.setVisibility(View.GONE);
                if (newDevicesArrayAdapter.getCount() == 0) { // if no devices found
                    tvNoAvailDevices.setVisibility(View.VISIBLE);
                }
                btnScan.setText(btnScanTx); // clear btn text
                btnScan.setEnabled(true); // disable button click
                pbAvail.setVisibility(View.GONE); // show button progress bar
            }
        }
    };

    // get sent text from MainActivity
    private BroadcastReceiver mTextReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String theText = intent.getStringExtra("tts"); // Get extra data included in the Intent
            if (theText != null) {
                if (btService.getState() != btService.STATE_CONNECTED) {
                    showToast("Connection Lost. Please try again.");
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

    // get robot movements from MainActivity
    private BroadcastReceiver mCtrlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String control = intent.getStringExtra("control");
            if (control != null) {
                if (btService.getState() != btService.STATE_CONNECTED) {
                    showToast("Connection Lost. Please try again.");
                    return;
                }
                //send out message
                byte[] send = control.getBytes();
                btService.write(send);
                mOutStringBuffer.setLength(0);
            }
        }
    };

    // listen for disconnection from MainActivity
    private BroadcastReceiver mDcReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String control = intent.getStringExtra("disconnect");
            if (control != null) {
                if (btService.getState() != btService.STATE_CONNECTED) {
                    showToast("Connection Lost. Please try again.");
                    return;
                }
                destroyReceivers();
                btService.stop();
                btService.start();
            }
        }
    };

    public void showToast(String message) {
        Toast.makeText(BluetoothActivity2.this, message, Toast.LENGTH_LONG).show();
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle = intent.getExtras();
        device = bundle != null && bundle.containsKey("device") ? bundle.getString("device") : "";
        updateBluetoothTBStatus(device);
    }

    @Override
    public void onBackPressed() {
        // disable destroying activity resume the main ui instead, singleInstance calls onCreate
        Intent i = new Intent(BluetoothActivity2.this, MainActivity2.class);
        i.putExtra("device", device);
        startActivity(i);
    }
}