package com.example.mdpandroid.Version2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mdpandroid.R;

public class MainActivity2 extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TabAdapter tabAdapter;

    private TextView btTextView;
    private Toolbar btToolBar;
    private String device = "";

    public MazeView2 mazeView;

    private int[] tabIcons = {R.drawable.tab_map, R.drawable.tab_com};

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

        tabAdapter = new TabAdapter(getSupportFragmentManager());
        tabAdapter.addFragment(new MapFragment(), getString(R.string.TabMap));
        tabAdapter.addFragment(new ComFragment(), getString(R.string.TabCom));
        viewPager.setAdapter(tabAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);

        btTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity2.this, BluetoothActivity2.class);
                i.putExtra("device", device);
                startActivity(i);
            }
        });

        // result receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mNameReceiver,
                new IntentFilter("getConnectedDevice"));

    }

//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        Bundle bundle = intent.getExtras();
//        device = bundle != null && bundle.containsKey("device") ? bundle.getString("device") : "";
//        updateBluetoothTBStatus(device);
//    }

    @Override
    public void onBackPressed() { // disable destroying activity
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
    private BroadcastReceiver mNameReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String theName = intent.getStringExtra("message"); // Get extra data included in the Intent
            if (theName == "") { // no device connected, disable bluetooth-related actions
                device = "";
                updateBluetoothTBStatus(device);
            } else { // device connected, enable all bluetooth-related actions
                device = theName;
                updateBluetoothTBStatus(device);
            }
        }
    };

    public void showToast(String message) {
        Toast.makeText(MainActivity2.this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        super.onResume();
//        LocalBroadcastManager.getInstance(this).registerReceiver(mNameReceiver,
//                new IntentFilter("getConnectedDevice"));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mNameReceiver);
    }
}