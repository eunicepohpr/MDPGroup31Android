package com.example.mdpandroid.Version3;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mdpandroid.R;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity3 extends AppCompatActivity {
    private static final String TAG = "BluetoothActivity3";
    private BluetoothAdapter btAdapter;
    BluetoothConnectionService mBluetoothConnection;
    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static BluetoothDevice mBTDevice;

    private TextView btTextView;
    private Toolbar btToolBar;
    private ArrayAdapter<String> newDevicesArrayAdapter, pairedDevicesArrayAdapter;
    public ArrayList<BluetoothDevice> newDevicesList, pairedDevicesList;
    private ListView lvPairedDevices, lvAvailDevices;
    private TextView tvNoPairDevices, tvNoAvailDevices;
    private Button btnDiscover, btnRefresh, btnScan, btnBack;
    private String device;
    ProgressDialog myDialog;

    private ProgressBar pbPair, pbAvail;
    private ProgressDialog progress;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    boolean retryConnection = false;
    Handler reconnectionHandler = new Handler();

    Runnable reconnectionRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (BluetoothConnectionService.BluetoothConnectionStatus == false) {
                    startBTConnection(mBTDevice, myUUID);
                    showToast("Reconnection Success");
                }
                reconnectionHandler.removeCallbacks(reconnectionRunnable);
                retryConnection = false;
            } catch (Exception e) {
                showToast("Failed to reconnect, trying in 5 second");
            }
        }
    };

    public void startConnection() {
        startBTConnection(mBTDevice, myUUID);
    }

    public void startBTConnection(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection");
        mBluetoothConnection.startClientThread(device, uuid);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth3);

        // bluetooth status toolbar
        btTextView = findViewById(R.id.btTV);
        btToolBar = findViewById(R.id.btTB);

        device = "";
        sharedPreferences = getApplicationContext().getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
        if (sharedPreferences.contains("connStatus"))
            device = sharedPreferences.getString("connStatus", "");
        updateBluetoothTBStatus(device);

        myDialog = new ProgressDialog(getApplicationContext());
        myDialog.setMessage("Waiting for other device to reconnect...");
        myDialog.setCancelable(false);
        myDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor = sharedPreferences.edit();
                editor.putString("connStatus", device);
                editor.commit();
                finish();
            }
        });

        pbPair = findViewById(R.id.pbPair);
        pbAvail = findViewById(R.id.pbAvail);

        tvNoPairDevices = findViewById(R.id.tvPaired);
        tvNoAvailDevices = findViewById(R.id.tvAvail);

        lvPairedDevices = findViewById(R.id.lvPairedDevices);
        lvAvailDevices = findViewById(R.id.lvAvailableDevices);
        pairedDevicesList = new ArrayList<>();
        pairedDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name);
        lvPairedDevices.setAdapter(pairedDevicesArrayAdapter);
        newDevicesList = new ArrayList<>();
        newDevicesArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        lvAvailDevices.setAdapter(newDevicesArrayAdapter);
        lvPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                btAdapter.cancelDiscovery();
                // create ProgressDialog to let user know that application is trying to connect to device
                progress = ProgressDialog.show(BluetoothActivity3.this, "Connecting...", "Please wait");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                    }
                }, 500); //delay of 1s
                mBluetoothConnection = new BluetoothConnectionService(BluetoothActivity3.this);
                mBTDevice = pairedDevicesList.get(i);
                startConnection();
            }
        });
        lvAvailDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                btAdapter.cancelDiscovery();
                progress = ProgressDialog.show(BluetoothActivity3.this, "Connecting...", "Please wait");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                    }
                }, 500); //delay of 1s
                newDevicesList.get(i).createBond();
                mBluetoothConnection = new BluetoothConnectionService(BluetoothActivity3.this);
                mBTDevice = newDevicesList.get(i);
                startConnection();
            }
        });

        btnDiscover = findViewById(R.id.btnDiscover);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnScan = findViewById(R.id.btnScan);
        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableDiscovery();
            }
        });
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePairedDevicesList();
            }
        });
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateAvailableDeviceList();
            }
        });

        // Setup bluetooth
        // Get Device's Bluetooth adapter
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) { // Device doesn't support Bluetooth
            showToast("Bluetooth not supported on this device.");
        } else {
            if (!btAdapter.isEnabled()) { // check if bluetooth enabled
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); // issue request to enable bluetooth
                startActivityForResult(enableBtIntent, 1);
            } else {
            }
            updatePairedDevicesList();
        }

        // Register for broadcasts when a device is discovered.
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
//        registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
//        registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
//        registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));

        IntentFilter filter2 = new IntentFilter("ConnectionStatus");
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver5, filter2);
    }

    // Update the bluetooth toolbar
    public void updateBluetoothTBStatus(String device) {
        if (!(device.equals("") || device == null)) {
            btToolBar.setBackgroundColor(ContextCompat.getColor(this, R.color.BTConnectedG));
            btTextView.setText(getString(R.string.BTConnected, device));
        } else {
            btToolBar.setBackgroundColor(ContextCompat.getColor(this, R.color.BTNotConnected));
            btTextView.setText(getString(R.string.BTNotConnected));
        }
    }

    // update paired devices list
    private void updatePairedDevicesList() {
        // UI Methods
        pairedDevicesArrayAdapter.clear();
        pairedDevicesList.clear();
        btnRefresh.setText(""); // clear btn text
        pbPair.setVisibility(View.VISIBLE); // show progress bar
        lvPairedDevices.setVisibility(View.GONE); // hide list view
        tvNoPairDevices.setVisibility(View.GONE); // hide no devices tv

        // Pair Devices List
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        final boolean hasList = pairedDevices.size() > 0;
        if (hasList) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesList.add(device);
                pairedDevicesArrayAdapter.add(device.getName() + "\n MAC Address: " + device.getAddress());
            }
        }

        new CountDownTimer(500, 1000) {
            @Override
            public void onTick(long l) {
            }

            public void onFinish() {
                lvPairedDevices.setVisibility(View.VISIBLE);
                pbPair.setVisibility(View.GONE);
                btnRefresh.setText(R.string.BTRefresh);
                if (hasList)
                    tvNoPairDevices.setVisibility(View.GONE);
                else
                    tvNoPairDevices.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    // scan available nearby bluetooth devices
    public void updateAvailableDeviceList() {
        if (btAdapter.isDiscovering()) // if device is already discovering, cancel it
            btAdapter.cancelDiscovery();
        if (pbAvail.getVisibility() == View.VISIBLE) {
            btnScan.setText(R.string.BTScan);
            showToast("Scanning stopped"); // notify user of button click
            pbAvail.setVisibility(View.GONE); // hide progress bar
        } else {
            newDevicesArrayAdapter.clear(); // clear list of available devices
            newDevicesList.clear();
            btnScan.setText(R.string.BTStop); // clear btn text
            pbAvail.setVisibility(View.VISIBLE); // show button progress bar
            showToast("Scanning for devices..."); // notify user of button click
            btAdapter.startDiscovery();
            registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

    // method for enabling discovery of the bluetooth device to other devices
    private void enableDiscovery() {
        if (btAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        } else
            showToast("Device is already discoverable");
    }

//    private void connectBluetoothDevice(String MACAddress) {
//        final BluetoothDevice deviceMac = btAdapter.getRemoteDevice(MACAddress);
////        btService = new BluetoothService(getApplicationContext(), mHandler);
//
//        // create ProgressDialog to let user know that application is trying to connect to device
//        progress = ProgressDialog.show(BluetoothActivity3.this, "Connecting...", "Please wait");
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
////                btService.connect(deviceMac, false); // connect to the device that was clicked
//                progress.dismiss();
//            }
//        }, 500); //delay of 1s
//
//    }


    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice object and its info from the Intent.
                tvNoAvailDevices.setVisibility(View.GONE);
                lvAvailDevices.setVisibility(View.VISIBLE);

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                newDevicesList.add(device);
                newDevicesArrayAdapter.add(device.getName() + "\n MAC Address: " + device.getAddress()); // add device to array adapter

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                // when bluetooth has completed scanning
                pbAvail.setVisibility(View.GONE);
                if (newDevicesArrayAdapter.getCount() == 0) {
                    tvNoAvailDevices.setVisibility(View.VISIBLE);
                    lvAvailDevices.setVisibility(View.GONE);
                }
                btnScan.setText(R.string.BTScan);
                pbAvail.setVisibility(View.GONE);
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "ACTION_STATE_CHANGED: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "ACTION_STATE_CHANGED: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "ACTION_STATE_CHANGED: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "ACTION_STATE_CHANGED: STATE TURNING ON");
                        break;
                }
            } else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
                final int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                switch (mode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "ACTION_SCAN_MODE_CHANGED: Discoverability Enabled.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "ACTION_SCAN_MODE_CHANGED: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "ACTION_SCAN_MODE_CHANGED: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "ACTION_SCAN_MODE_CHANGED: Connecting...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "ACTION_SCAN_MODE_CHANGED: Connected.");
                        break;
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) { // device paired
                    updatePairedDevicesList(); // refresh the list of paired device
                    showToast("Sucessfully paired with " + mDevice.getName());
                    mBTDevice = mDevice;
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING)
                    Log.d(TAG, "ACTION_BOND_STATE_CHANGED: BOND_BONDING.");
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE)
                    Log.d(TAG, "ACTION_BOND_STATE_CHANGED: BOND_NONE.");
            }
        }
    };

    private BroadcastReceiver mBroadcastReceiver5 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice mDevice = intent.getParcelableExtra("Device");
            String status = intent.getStringExtra("Status");
            sharedPreferences = getApplicationContext().getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
            if (status.equals("connected")) {
                try {
                    myDialog.dismiss();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                device = mDevice.getName();
                Log.d(TAG, "mBroadcastReceiver5: Device now connected to " + device);
                showToast("Device now connected to " + device);
                editor.putString("connStatus", device);
                updateBluetoothTBStatus(device);
                MapFragment3.updateConnection(true);
            } else if (status.equals("disconnected") && retryConnection == false) {
                Log.d(TAG, "mBroadcastReceiver5: Disconnected from " + mDevice.getName());
                showToast("Disconnected from " + mDevice.getName());
                mBluetoothConnection = new BluetoothConnectionService(BluetoothActivity3.this);
                sharedPreferences = getApplicationContext().getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
                editor = sharedPreferences.edit();
                device = "";
                editor.putString("connStatus", "");
                editor.commit();
                updateBluetoothTBStatus("");
                MapFragment3.updateConnection(false);

                try {
                    myDialog.show();
                } catch (Exception e) {
                    Log.d(TAG, "mBroadcastReceiver5 Dialog show failure");
                }
                retryConnection = true;
                reconnectionHandler.postDelayed(reconnectionRunnable, 5000);
            }
            editor.commit();
        }
    };

    // get sent text from MapFragment
//    private BroadcastReceiver mTextReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String theText = intent.getStringExtra("tts"); // Get extra data included in the Intent
//            Log.d("Bluetooth mTReceiver: ", theText);
//            if (theText != null) {
////                if (btService.getState() != BluetoothService.STATE_CONNECTED) {
////                    showToast("Connection Lost. Please try again." + btService.getState());
////                    device = "";
////                    updateBluetoothTBStatus(device);
////                    sendToMain("");
////                    destroyReceivers();
////                    return;
////                }
////                btService.write(theText.getBytes()); // send out message
////                mOutStringBuffer.setLength(0); // Reset out string buffer to zero
//            }
//        }
//    };

    // get robot movements from MapFragment
//    private BroadcastReceiver mCtrlReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            // Get extra data included in the Intent
//            String control = intent.getStringExtra("control");
//            Log.d("Bluetooth mCReceiver: ", control);
//            if (control != null) {
////                if (btService.getState() != BluetoothService.STATE_CONNECTED) {
////                    showToast("Connection Lost.. Please try again." + btService.getState());
////                    device = "";
////                    sendToMain("");
////                    destroyReceivers();
////                    updateBluetoothTBStatus(device);
////                    return;
////                }
////                //send out message
////                byte[] send = control.getBytes();
////                btService.write(send);
////                mOutStringBuffer.setLength(0);
//            }
//        }
//    };

    public void showToast(String message) {
        Toast.makeText(BluetoothActivity3.this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(receiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver5);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(receiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver5);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra("mBTDevice", mBTDevice);
        data.putExtra("myUUID", myUUID);
        setResult(RESULT_OK, data);
        super.finish();
    }

    @Override
    public void onBackPressed() {
        // disable destroying activity resume the main ui instead, singleInstance calls onCreate
//        Intent i = new Intent(BluetoothActivity3.this, MainActivity2.class);
//        startActivity(i);
    }
}