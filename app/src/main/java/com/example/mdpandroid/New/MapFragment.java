package com.example.mdpandroid.New;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mdpandroid.R;

public class MapFragment extends Fragment {

    private Button btnExplore, btnFP, btnRefresh, btnCali, btnUpdateMap, btnSendWP, btnSendRP;
    private Button btnUp, btnDown, btnLeft, btnRight;
    private Switch switchAU, switchTilt, switchPRP;
    private Spinner spinnerROrien;
    private TextView tvRStatus, tvExpTime, tvFPWP, tvRStartP;

    public MapFragment() {
    }

    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        btnExplore = getView().findViewById(R.id.btnExplore);
        btnFP = getView().findViewById(R.id.btnFP);
        btnRefresh = getView().findViewById(R.id.btnRefresh);
        btnCali = getView().findViewById(R.id.btnCali);
        btnUpdateMap = getView().findViewById(R.id.btnUpdateMap);
        btnSendRP = getView().findViewById(R.id.btnSendRP);
        btnSendWP = getView().findViewById(R.id.btnSendWP);
        btnUp = getView().findViewById(R.id.btnUp);
        btnDown = getView().findViewById(R.id.btnDown);
        btnLeft = getView().findViewById(R.id.btnLeft);
        btnRight = getView().findViewById(R.id.btnRight);

        switchAU = getView().findViewById(R.id.switchAutoUp);
        switchPRP = getView().findViewById(R.id.switchPlotRP);
        switchTilt = getView().findViewById(R.id.switchTilt);
        spinnerROrien = getView().findViewById(R.id.spinnerROrien);


        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToast("Move up");
            }
        });
        btnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToast("Move down");
            }
        });
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToast("Move left");
            }
        });
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToast("Move right");
            }
        });

    }

    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }
}