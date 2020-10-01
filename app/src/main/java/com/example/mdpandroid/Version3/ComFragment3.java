package com.example.mdpandroid.Version3;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mdpandroid.R;

import java.nio.charset.Charset;

public class ComFragment3 extends Fragment {
    private static final String TAG = "ComFragment3";
    private static TextView tvReceiveText;
    private EditText etTransText, etPersistentText;
    private Button btnTransSend, btnTransClear, btnPersistSend, btnPersistSave, btnRecReset;
    private static ScrollView sv;
    private static int noOfItems = 1;
    private static boolean svDyanmic = false;
    static Context mContext;
    private static SharedPreferences sharedPreferences;

    public ComFragment3() {
        // Required empty public constructor
    }

    public static com.example.mdpandroid.Version2.ComFragment newInstance() {
        com.example.mdpandroid.Version2.ComFragment fragment = new com.example.mdpandroid.Version2.ComFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getContext();
        return inflater.inflate(R.layout.fragment_com, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sv = getView().findViewById(R.id.sv);
        tvReceiveText = getView().findViewById(R.id.tvComRecTxt);
        etPersistentText = getView().findViewById(R.id.etComPS);
        etTransText = getView().findViewById(R.id.etComTrans);
        btnTransClear = getView().findViewById(R.id.btnTransClear);
        btnTransSend = getView().findViewById(R.id.btnTransSend);
        btnPersistSave = getView().findViewById(R.id.btnPSSave);
        btnPersistSend = getView().findViewById(R.id.btnPSSend);
        btnRecReset = getView().findViewById(R.id.btnComRecReset);

        sharedPreferences = getActivity().getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
        etPersistentText.setText(sharedPreferences.contains("value1") ?
                sharedPreferences.getString("value1", "") : "Default Message 1");

        etPersistentText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    hideSoftKeyboard(v);
            }
        });

        etTransText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    hideSoftKeyboard(v);
            }
        });

        btnTransSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyboard(view);
                String text = etTransText.getText().toString();
                if (text.isEmpty()) {
                    showToast("Text box is empty!");
                } else {
                    sendToBtAct(text);
                    showToast("Message sent!");
                }
            }
        });

        btnTransClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etTransText.getText().clear();
            }
        });

        btnPersistSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyboard(view);
                String text = etPersistentText.getText().toString();
                if (text.isEmpty()) {
                    showToast("Text box is empty!");
                } else {
                    sendToBtAct(text);
                    showToast("Custom message sent!");
                }
            }
        });

        btnPersistSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyboard(view);
                // check if any of the edit texts are empty
                if (etPersistentText.getText().toString().trim().equalsIgnoreCase(""))
                    showToast("Custom messages cannot be empty");
                else { // use .commit() to save onto app's SharedPreferences
                    SharedPreferences sharedPref = getActivity().getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("value1", etPersistentText.getText().toString());
                    editor.commit();
                    showToast("Custom message saved");
                }
            }
        });

        btnRecReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvReceiveText.setText(R.string.ComRecTxtHelp); // reset scrollview layout settings
                noOfItems = 1;
                svDyanmic = false;
                sv.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        });
    }

    // receive bluetooth text event
    public static void refreshReceiveText(String theText) {
        if (theText.length() > 0) {
            String text = tvReceiveText.getText().toString();
            Log.d(TAG, theText);
            if (text.equals(mContext.getResources().getString(R.string.ComRecTxtHelp))) {
                tvReceiveText.setText(theText);
                noOfItems = 1;
                svDyanmic = false;
                sv.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
            } else {
                tvReceiveText.setText(text + "\n" + theText);
                noOfItems += 1;
                if (noOfItems >= 10 && !svDyanmic) {
                    svDyanmic = true;
                    sv.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300));
                }
            }
        }
    }

    // to send text to bluetooth service
    private void sendToBtAct(String msg) {
        if (BluetoothConnectionService.BluetoothConnectionStatus == true) {
            byte[] bytes = msg.getBytes(Charset.defaultCharset());
            BluetoothConnectionService.write(bytes);
        }
    }

    public void hideSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(
                Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

}