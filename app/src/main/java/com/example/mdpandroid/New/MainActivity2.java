package com.example.mdpandroid.New;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mdpandroid.MainActivity;
import com.example.mdpandroid.MazeView;
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

//        showToast("MainActivity2 onCreate");

    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle = intent.getExtras();
        device = bundle != null && bundle.containsKey("device") ? bundle.getString("device") : "";
        updateBluetoothTBStatus(device);
    }

    @Override
    public void onBackPressed() { // disable destroying activity
    }

    // update the bluetooth toolbar
    public void updateBluetoothTBStatus(String device) {
        if (!(device.equals("") || device == null)) {
            btToolBar.setBackgroundColor(ContextCompat.getColor(this, R.color.BTConnectedG)); // 0367a1
            btTextView.setText(getString(R.string.BTConnected, device));
        } else {
            btToolBar.setBackgroundColor(ContextCompat.getColor(this, R.color.BTNotConnected));
            btTextView.setText(getString(R.string.BTNotConnected));
        }
    }

    public void showToast(String message) {
        Toast.makeText(MainActivity2.this, message, Toast.LENGTH_LONG).show();
    }
}