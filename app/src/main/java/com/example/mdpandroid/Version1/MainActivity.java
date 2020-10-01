package com.example.mdpandroid.Version1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mdpandroid.JoystickView;
import com.example.mdpandroid.R;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * class used for homepage activities that can be changed by user
 * UI for homepage including maze and controls
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {


    //navbar
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    //slider panel
    private Button cbBtn;
    private EditText cbEt;

    //chat
    public static MessageListAdapter chatAdapter;
    private RecyclerView chatView;
    private List<BaseMessage> messageList;

    //maze, waypoint, startpoint
    public MazeView mazeView;
    public TextView waypointTextView;
    private TextView robotTextView;
    private Switch plotRobotPosition;
    private Spinner directionDropDown;
    private Switch switchAuto;


    //controls button
    private ImageButton upBtn;
    private ImageButton downBtn;
    private ImageButton leftBtn;
    private ImageButton rightBtn;
    private Button readyBtn;
    private Button refreshBtn;
    private Button explorationBtn;
    private Button fastestBtn;
    private Button manualUpdateBtn;
    private JoystickView joystickRight;
    private Button sendWayPointBtn;
    private Button sendRobotPosition;
    ArrayAdapter<String> directionAdapter;
    private Chronometer chr;

    private int status;
    private String name = "";
    private final String noDeviceMsg = "No device connected";
    private TextView nameTv;
    private TextView statusTv;
    boolean autoUpdate = true;
    private String device;
    String mdfExploredString = "";
    String mdfObstacleString = "";
    public boolean enablePlotRobotPosition = false;

    //tilting
    private boolean tiltEnabled = false;
    private SensorManager sensorManager;
    private Switch tiltSwitch;
    public boolean fastest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //nav bar
        dl = (DrawerLayout) findViewById(R.id.drawer);
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
                        dl.closeDrawers();
                        return true;
                    case R.id.bluetooth:
                        Intent i = new Intent(MainActivity.this, BluetoothActivity.class);
                        //send device name to next activity to allow app to know whether there is still bluetooth connection
                        i.putExtra("device", device);
                        startActivity(i);
                        return true;
                    case R.id.settings:
                        Intent x = new Intent(MainActivity.this, SettingsActivity.class);
                        //send device name to next activity to allow app to know whether there is still bluetooth connection
                        x.putExtra("device", device);
                        startActivity(x);
                        return true;
                    default:
                        return true;
                }
            }
        });

        //tilting
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 200000);
        tiltSwitch = (Switch) findViewById(R.id.switchTilt);

        tiltSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    tiltEnabled = true;
                } else {
                    tiltEnabled = false;
                }
            }
        });

        //retrieve connected device's name
        nameTv = (TextView) findViewById(R.id.deviceConnectedTv);
        nameTv.setText(noDeviceMsg);

        //mazeview
        mazeView = findViewById(R.id.mazeView);

        switchAuto = (Switch) findViewById(R.id.switchAuto);

        switchAuto.setChecked(true);

        switchAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    autoUpdate = true;
                    mazeView.invalidate();
                    manualUpdateBtn.setEnabled(false);
                    manualUpdateBtn.setBackgroundResource(R.drawable.disabledbutton);
                } else {
                    autoUpdate = false;
                    manualUpdateBtn.setEnabled(true);
                    manualUpdateBtn.setBackgroundResource(R.drawable.commonbutton);
                }
            }
        });

        upBtn = findViewById(R.id.upBtn);
        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mazeView.moveUp();
            }
        });
        downBtn = findViewById(R.id.downBtn);
        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mazeView.moveDown();
            }
        });
        leftBtn = findViewById(R.id.leftBtn);
        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mazeView.moveLeft();
            }
        });
        rightBtn = findViewById(R.id.rightBtn);
        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mazeView.moveRight();
            }
        });
        readyBtn = findViewById(R.id.readyBtn);
        readyBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                sendCtrlToBtAct("AR,AN,C");
            }
        });
        //for stopwatch
        chr = (Chronometer) findViewById(R.id.textViewTimer);
        //hide visibility until stopwatch needed
//        chr.setVisibility(View.INVISIBLE);

        refreshBtn = findViewById(R.id.refreshBtn);
        //restart maze, buttons, status textview and chronometer
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mazeView.clearExploredGrid();
                mazeView.clearNumID();
                mazeView.clearObstacleGrid();
                mazeView.updateRobotCoords(1, 1, 0);
                explorationBtn.setEnabled(true);
                explorationBtn.setBackgroundResource(R.drawable.commonbutton);
                fastestBtn.setEnabled(true);
                fastestBtn.setBackgroundResource(R.drawable.commonbutton);
                mazeView.clearObsArray();
                statusTv.setText("Waiting for new instructions");
                fastest = false;
//                chr.setVisibility(View.INVISIBLE);

            }
        });
        explorationBtn = findViewById(R.id.explorationBtn);
        //start exploration
        explorationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //clear maze for new exploration
                mazeView.clearExploredGrid();
                mazeView.clearNumID();
                mazeView.clearObstacleGrid();
                mazeView.clearObsArray();

                final int tempX = mazeView.getWaypoint()[0] + 1;
                final int tempY = mazeView.getWaypoint()[1] + 1;

                sendCtrlToBtAct("AR,AN,E"); //send exploration message to arduino
                explorationBtn.setEnabled(false); //disable exploration button
                explorationBtn.setBackgroundResource(R.drawable.disabledbutton); //change button to let user know it cannot be clicked
                fastestBtn.setEnabled(true); //enable fastest button
                fastestBtn.setBackgroundResource(R.drawable.commonbutton); //change button to let user know it can be clicked
                statusTv.setText("Exploration of maze is in progress...."); //update status
                chr.setVisibility(View.VISIBLE); //show stopwatch
                chr.setBase(SystemClock.elapsedRealtime()); //set stopwatch to 0:00
                chr.stop(); //stop in case there is currently stopwatch running
                chr.setFormat("Time: %s"); //format stopwatch's text
                chr.start(); //start stopwatch

            }
        });

        fastestBtn = findViewById(R.id.fastestBtn);
        //start fastest path
        fastestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                sendCtrlToBtAct("PC,AN,FP"); //send fastest path message to algorithm
                fastest = true;
                explorationBtn.setEnabled(true); //enable exploration button
                explorationBtn.setBackgroundResource(R.drawable.commonbutton); //change button to let user know it can be clicked
                fastestBtn.setEnabled(false); //disable fastest button
                fastestBtn.setBackgroundResource(R.drawable.disabledbutton); // change button to let user know it cannot be clicked
                statusTv.setText("Finding Fastest path in progress...."); //update status
                chr.setVisibility(View.VISIBLE); //show stopwatch
                chr.setBase(SystemClock.elapsedRealtime()); //set stopwatch to 0:00
                chr.stop(); //stop if it was already running
                chr.setFormat("Time: %s"); //format stopwatch's text
                chr.start(); //start stopwatch
            }
        });

        manualUpdateBtn = findViewById(R.id.manualUpdate);
        manualUpdateBtn.setEnabled(false);
        manualUpdateBtn.setBackgroundResource(R.drawable.disabledbutton);
        manualUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mazeView.invalidate();
            }
        });

        sendWayPointBtn = findViewById(R.id.sendWaypointBtn);
        //send waypoint to algorithm
        sendWayPointBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mazeView.getWaypoint()[0] + 1 < 4 && mazeView.getWaypoint()[1] + 1 < 4) {
                    //way point is inside start area, notify user
                    Toast.makeText(MainActivity.this, "Invalid Way-Point Coordinate. Please check again later.", Toast.LENGTH_SHORT).show();
                } else {
                    //send message to algorithm
                    sendCtrlToBtAct("PC,AN,WP:" + (mazeView.getWaypoint()[0] + 1) + ":" + (mazeView.getWaypoint()[1] + 1));
                    //notify user that waypoint is sent
                    Toast.makeText(MainActivity.this, "Waypoint has been Sent", Toast.LENGTH_SHORT).show();
                }
            }


        });

        sendRobotPosition = findViewById(R.id.sendRobotPosition);
        //send robot's coordinates & direction facing to algorithm
        sendRobotPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCtrlToBtAct("PC,AN," + (mazeView.getRobotCenter()[0] + 1) + "," + (mazeView.getRobotCenter()[1]) + "," + mazeView.angle);
            }
        });

        waypointTextView = findViewById(R.id.waypointTextView);
        //show current waypoint X & Y coordinates, "--" if not set
        if (mazeView.getWaypoint()[0] < 0) {
            waypointTextView.setText("x:-- , y:--");
        } else {
            waypointTextView.setText("x:" + (mazeView.getWaypoint()[0] + 1) + " , y:" + (mazeView.getWaypoint()[1] + 1));
        }

        robotTextView = findViewById(R.id.robotTextView);
        setRobotTextView(mazeView.getRobotCenter());

        plotRobotPosition = findViewById(R.id.plotRobotPosition);
        //manually set robot's position on maze
        plotRobotPosition.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton plotRobotPositionBtn, boolean isChecked) {
                if (enablePlotRobotPosition) {
                    enablePlotRobotPosition = false;
                } else {
                    enablePlotRobotPosition = true;
                }
            }
        });

        directionDropDown = findViewById(R.id.directionDropDown);
        String[] items = new String[]{"", "0", "90", "180", "270"};
        directionAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, items);
        directionDropDown.setAdapter(directionAdapter);
        //drop down menu to set robot's facing direction
        directionDropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String str = (String) parent.getSelectedItem();
                switch (str) {
                    case "0":
                        mazeView.updateRobotCoords(mazeView.getRobotCenter()[0], mazeView.getRobotCenter()[1], 0);
                        break;
                    case "180":
                        mazeView.updateRobotCoords(mazeView.getRobotCenter()[0], mazeView.getRobotCenter()[1], 180);

                        break;
                    case "270":
                        mazeView.updateRobotCoords(mazeView.getRobotCenter()[0], mazeView.getRobotCenter()[1], 270);

                        break;
                    case "90":
                        mazeView.updateRobotCoords(mazeView.getRobotCenter()[0], mazeView.getRobotCenter()[1], 90);

                        break;
                    default:
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //result receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mNameReceiver,
                new IntentFilter("getConnectedDevice"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mTextReceiver,
                new IntentFilter("getTextFromDevice"));

        // slider panel, joystick & send bluetooth messages
        cbBtn = (Button) findViewById(R.id.chatboxBtn);
        cbEt = (EditText) findViewById(R.id.chatboxEt);
        chatView = (RecyclerView) findViewById(R.id.reyclerview_message_list);
        messageList = new ArrayList<BaseMessage>();
        chatAdapter = new MessageListAdapter(this, messageList);
        chatView.setAdapter(chatAdapter);
        chatView.setLayoutManager(new LinearLayoutManager(this));


        /*--joystickRight.setOnMoveListener(new JoystickView.OnMoveListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onMove(int angle, int strength) {
                if (strength > 70 && !device.equalsIgnoreCase("")) {
                    if (angle >= 315 || angle <= 45) {
                        mazeView.moveRight();
                    } else if (angle >= 225 && angle < 315) {
                        mazeView.moveDown();
                    } else if (angle >= 135 && angle < 225) {
                        mazeView.moveLeft();
                    } else if (angle >= 45 && angle <135){
                        mazeView.moveUp();
                    }
                }
            }
        });*/


        //disable all buttons that require bluetooth
        cbBtn.setEnabled(false);
        upBtn.setEnabled(false);
        downBtn.setEnabled(false);
        leftBtn.setEnabled(false);
        rightBtn.setEnabled(false);
        readyBtn.setEnabled(false);
        readyBtn.setBackgroundResource(R.drawable.disabledbutton);
        //joystickRight.setEnabled(false);
        statusTv = (TextView) findViewById(R.id.robotStatus);
        statusTv.setText("Offline");

        //button to send out messages that user has typed
        cbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageOut = cbEt.getText().toString().trim();
                BaseMessage msgOut = new BaseMessage(0, messageOut); //id = 0 because user is the one who sent this message
                if (messageOut.length() < 1) {
                    //user did not type anything
                    Toast.makeText(MainActivity.this, "Please type something in the box", Toast.LENGTH_SHORT).show();
                } else {
                    //add message to list that contains all messages
                    messageList.add(messageList.size(), msgOut);
                    chatAdapter.notifyDataSetChanged();
                    cbEt.setText(""); //clear out edit text for user
                    //send to bluetoothactivity for processing
                    sendToBtAct(messageOut);
                }
            }
        });

        //get intent
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey("device")) {
                device = bundle.getString("device");

                if (!device.equalsIgnoreCase("")) {
                    //enable all bluetooth-related buttons because there is a device connected
                    nameTv.setText("Connected to: " + device);
                    cbBtn.setEnabled(true);
                    upBtn.setEnabled(true);
                    downBtn.setEnabled(true);
                    leftBtn.setEnabled(true);
                    rightBtn.setEnabled(true);
                    readyBtn.setEnabled(true);
                    readyBtn.setBackgroundResource(R.drawable.commonbutton);
                    //--dcBtn.setEnabled(true);
                    statusTv.setText("Waiting for instructions");
                } else {
                    //disable all bluetooth-related buttons because no device connected
                    nameTv.setText(noDeviceMsg);
                    cbBtn.setEnabled(false);
                    upBtn.setEnabled(false);
                    downBtn.setEnabled(false);
                    leftBtn.setEnabled(false);
                    rightBtn.setEnabled(false);
                    readyBtn.setEnabled(false);
                    readyBtn.setBackgroundResource(R.drawable.disabledbutton);
                    //--dcBtn.setEnabled(false);
                    statusTv.setText("Offline");
                }
            } else {
                device = "";
            }
        } else {
            device = "";
        }

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


    //update status whenever connection changes
    private BroadcastReceiver mNameReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String theName = intent.getStringExtra("message");
            if (theName == "") {
                //no device connected, disable bluetooth-related actions
                nameTv.setText(noDeviceMsg);
                cbBtn.setEnabled(false);  //check
                //joystickRight.setEnabled(false);
                upBtn.setEnabled(false);  //check
                downBtn.setEnabled(false);  //check
                leftBtn.setEnabled(false);   //check
                rightBtn.setEnabled(false);  //check
                readyBtn.setEnabled(false);  //check
                readyBtn.setBackgroundResource(R.drawable.disabledbutton);
                //--dcBtn.setEnabled(false);
                messageList.clear();
                chatAdapter.notifyDataSetChanged();
                statusTv.setText("Offline");
                device = "";
            } else {
                //device connected, enable all bluetooth-related actions
                nameTv.setText("Connected to: " + theName);
                cbBtn.setEnabled(true);
                //joystickRight.setEnabled(true);
                upBtn.setEnabled(true);
                downBtn.setEnabled(true);
                leftBtn.setEnabled(true);
                rightBtn.setEnabled(true);
                readyBtn.setEnabled(true);
                readyBtn.setBackgroundResource(R.drawable.commonbutton);
                //--dcBtn.setEnabled(true);
                statusTv.setText("Waiting for instructions");
                device = theName;
            }
        }
    };

    //update chatbox when receive from bluetooth
    private BroadcastReceiver mTextReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String theText = intent.getStringExtra("text");
            if (theText.length() > 0) {


                if (theText.length() < 15 && fastest && (theText.contains("R") || theText.contains("L") || theText.contains("F"))) {
                    //checking for fastestpath string
                    int forwardDistance;

                    //split string to get direction & tiles to move
                    String[] fastestCommands = theText.split("");
                    //move robot in order of command received
                    for (int i = 0; i < fastestCommands.length; i++) {
                        if (fastestCommands[i].equals("F")) {

                            mazeView.robotX.add(mazeView.robotCenter[0]);
                            mazeView.robotY.add(mazeView.robotCenter[1]);
                            mazeView.moveForward();
                        } else if (fastestCommands[i].equals("R")) {

                            mazeView.turnRight();

                        } else if (fastestCommands[i].equals("L")) {
                            mazeView.turnLeft();

                        } else {

                            //Exception catching in case the string format is wrong
                            try {
                                forwardDistance = Integer.parseInt(fastestCommands[i]);
                                for (int j = 0; j < (forwardDistance + 1); j++) {
                                    mazeView.robotX.add(mazeView.robotCenter[0]);
                                    mazeView.robotY.add(mazeView.robotCenter[1]);
                                    mazeView.moveForward();
                                }
                            } catch (Exception e) {
                                Log.d("FP String", "String format wrong");
                            }

                        }
                    }
                }

                //Identifying mdf string send for real-time update of maze during exploration
                else if (theText.length() > 77 && theText.contains(":") && !fastest) {


                    String[] stringItems = theText.split(":");

                    try {

                    } catch (Exception e) {
                        Log.d("MDF String", "AL format wrong");
                    }

                    //Getting the explored grids from MDF string
                    String[] exploredString = hexToBinary(stringItems[0]).split("");
                    // temporary storage
                    String bin = "";
                    int[] exploredGrid = new int[exploredString.length - 5]; //-5 to make it 300
                    for (int i = 0; i < exploredGrid.length; i++) {
                        exploredGrid[i] = Integer.parseInt(exploredString[i + 3]); // because first element is ""
                        bin += exploredGrid[i];
                        //Storing explored and obstacle strings
                        mdfExploredString = stringItems[0];
                        mdfObstacleString = stringItems[1];
                    }

                    String text = "";
                    //Getting the obstacle grids from MDF string
                    String[] obstacleString = hexToBinary(stringItems[1]).split("");
                    int[] obstacleGrid = new int[obstacleString.length - 1];
                    Log.d("TAG", text);
                    for (int i = 0; i < obstacleGrid.length; i++) {
                        obstacleGrid[i] = Integer.parseInt(obstacleString[i + 1]);
                    }

                    int inc = 0;
                    int inc2 = 0;
                    for (int y = 0; y < 20; y++) {
                        for (int x = 0; x < 15; x++) {
                            //For explored grids, draw obstacle if any
                            if (exploredGrid != null && exploredGrid[inc] == 1) {
                                if (obstacleGrid != null && obstacleGrid[inc2] == 1) {
                                    mazeView.setObsArray(x, y);
                                }
                                inc2++;
                            }
                            inc++;
                        }
                    }
                    //update the obstacles and explored grids
                    mazeView.updateMaze(exploredGrid, obstacleGrid);
                    //Getting the direction the robot is facing
                    if (stringItems.length >= 5) {
                        int direction = 0;
                        if (stringItems[4].equals("N"))
                            direction = 0;
                        else if (stringItems[4].equals("E"))
                            direction = 90;
                        else if (stringItems[4].equals("S"))
                            direction = 180;
                        else if (stringItems[4].equals("W"))
                            direction = 270;

                        //Updating coordinates of robot according to string receive from algorithm
                        mazeView.updateRobotCoords(Integer.parseInt(stringItems[2]), Integer.parseInt(stringItems[3])
                                , direction);
                    }

                    //This segment of the string stores information of identified image and their coordinates
                    if (stringItems.length >= 6) {
                        //changing string to int
                        int numberX = Integer.parseInt(stringItems[5]);
                        int numberY = Integer.parseInt(stringItems[6]);
                        //Checking to see if received a valid numberid
                        boolean correctId = Pattern.matches("^[1-9][0-5]?$", stringItems[7]);
                        if (correctId) {
                            ArrayList<String> tempObsArray = mazeView.getObsArray();
                            String tempPos = (numberX - 1) + "," + (numberY - 1);
                            boolean checkObs = false;
                            for (int i = 0; i < tempObsArray.size(); i++) {
                                if (tempObsArray.get(i).equals(tempPos)) {
                                    checkObs = true;
                                }
                            }
                            if (checkObs) {
                                mazeView.updateNumberID(numberX, numberY, stringItems[7]);
                            }
                        }

                    }
                } else if (theText.equals("Explored")) {
                    //exploration completed
                    chr.stop(); //stop stopwatch
                    statusTv.setText("Exploration has been successfully completed"); //update status
                    String imageStr = ""; //create string to store infomation on images found
                    if (mazeView.numberID != null) {
                        //if images were found, loop through X, Y, ID and add to string
                        for (int i = 0; i < mazeView.numberID.size(); i++) {
                            imageStr = imageStr + "(" + (mazeView.numberIDX.get(i) - 1) + ", " + (mazeView.numberIDY.get(i) - 1) + ", " + mazeView.numberID.get(i) + ")\n";
                        }
                    }

                    //message that contains MDF and image information
                    String message = "MDF String: \n" + mdfExploredString + ":" + mdfObstacleString + "\n\nImage String(X, Y, ID): \n" + imageStr;
                    BaseMessage msgOut = new BaseMessage(0, message); //id=0 because we are the one who send this
                    messageList.add(messageList.size(), msgOut); //add message to array of messages
                    chatAdapter.notifyDataSetChanged(); //update message array
                    chr.stop();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sendCtrlToBtAct("AR,AN,C"); //send to arduino to calibrate
                        }
                    }, 2000); //2 seconds later
                } else {
                    //normal message sent from device
                    BaseMessage msgOut = new BaseMessage(1, theText); //id=1 because message sent from others
                    messageList.add(messageList.size(), msgOut); //add message to array of messages
                    chatAdapter.notifyDataSetChanged(); //update message array
                }

            }
        }
    };

    public boolean getEnablePlotRobotPosition() {
        return enablePlotRobotPosition;
    }

    //to send bluetoothactivity for bluetooth chat
    private void sendToBtAct(String msg) {
        Intent intent = new Intent("getTextToSend");
        // You can also include some extra data.
        intent.putExtra("tts", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    //to send bluetoothactivity for moving robot
    public void sendCtrlToBtAct(String msg) {
        Intent intent = new Intent("getCtrlToSend");
        // You can also include some extra data.
        if (msg.equals("AR,AN,F")) {
            statusTv.setText("Moving Forward");
        } else if (msg.equals("AR,AN,R")) {
            statusTv.setText("Turning Right");
        } else if (msg.equals("AR,AN,L")) {
            statusTv.setText("Turning Left");
        }
        intent.putExtra("control", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    //to send bluetoothactivity for moving robot
    public void initiateDc(String msg) {
        Intent intent = new Intent("initiateDc");
        // You can also include some extra data.
        intent.putExtra("disconnect", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mNameReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mTextReceiver);
    }

    //Text view created for waypoint setting

    public void setWaypointTextView(int[] waypoint) {
        if (waypoint[0] < 0) {
            waypointTextView.setText("x:-- , y:--");
        } else {
            waypointTextView.setText("x:" + (waypoint[0] + 1) + " , y:" + (waypoint[1] + 1));
        }
    }

    //text view created for robot start coordinates setting
    public void setRobotTextView(int[] robotpoint) {
        if (robotpoint[0] < 0) {
            robotTextView.setText("x:-- , y:--");
        } else {
            robotTextView.setText("x:" + (robotpoint[0] + 1) + " , y:" + (robotpoint[1] + 1));
        }
    }

    //method to convert hex to binary
    private String hexToBinary(String hex) {
        int pointer = 0;
        String binary = "";
        String partial;
        // 1 Hex digits each time to prevent overflow and recognize leading 0000
        while (hex.length() - pointer > 0) {
            partial = hex.substring(pointer, pointer + 1);
            String bin;
            bin = Integer.toBinaryString(Integer.parseInt(partial, 16));
            for (int i = 0; i < 4 - bin.length(); i++) {
                binary = binary.concat("0");  // padding 0 in front
            }
            binary = binary.concat(bin); // then add in the converted hextobin
            pointer += 1;
        }
        return binary;
    }

    //for tilting function
    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        if (tiltEnabled) {
            //move robot only if tilt has been enabled
            if (y < -5) {
                //device has been tilted forward
                mazeView.moveUp();
                statusTv.setText("Moving Forward");
            } else if (x < -5) {
                //device has been tilted to the right
                mazeView.moveRight();
                statusTv.setText("Turning Right");
            } else if (x > 5) {
                //device has been tilted to the left
                mazeView.moveLeft();
                statusTv.setText("Turning Left");
            } else if (y > 5) {
                //device tilted to the bottom
                mazeView.moveDown();
            }
        }
    }

    //required method to implement tilting
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //for implement method
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mTextReceiver);
    }
}
