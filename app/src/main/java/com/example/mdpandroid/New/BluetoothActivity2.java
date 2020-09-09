package com.example.mdpandroid.New;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.example.mdpandroid.R;

public class BluetoothActivity2 extends AppCompatActivity {
    private String device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth2);
        showToast("BluetoothActivity2 onCreate");

        Bundle bundle = getIntent().getExtras();
        device = bundle != null && bundle.containsKey("device") ? bundle.getString("device") : "";
        showToast("BAonCreate " + device);
    }

    @Override
    public void onResume() {
        super.onResume();
//        showToast("BluetoothActivity2 onResume");
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle = intent.getExtras();
        device = bundle != null && bundle.containsKey("device") ? bundle.getString("device") : "";
//        showToast("BAonNewIntent " + device);
    }

    @Override
    public void onBackPressed() { // disable destroying activity resume the main ui instead, singleInstance calls onCreate
        Intent i = new Intent(BluetoothActivity2.this, MainActivity2.class);
        i.putExtra("device", "Test");
        startActivity(i);
    }

    public void showToast(String message) {
        Toast.makeText(BluetoothActivity2.this, message, Toast.LENGTH_LONG).show();
    }
}