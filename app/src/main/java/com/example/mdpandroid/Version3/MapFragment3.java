package com.example.mdpandroid.Version3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mdpandroid.JoystickView;
import com.example.mdpandroid.R;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.regex.Pattern;

import static android.content.Context.SENSOR_SERVICE;

public class MapFragment3 extends Fragment implements SensorEventListener {
    private static MapFragment3 instance;

    private Button btnExplore, btnFP, btnRefresh, btnUpdateMap, btnSendWP, btnSendRP;
    private static Button btnCali, btnUp, btnDown, btnLeft, btnRight;
    private Switch switchAU, switchTilt, switchPRP, switchShowImage;
    private static Spinner spinnerROrien;
    private static Chronometer chr, chrFPTimer;
    private static TextView tvRStatus, tvRStartP;
    private TextView tvFPWP;

    private JoystickView joystickRight;

    // tilting
    public boolean tiltEnabled = false;
    public static boolean fastest = false, exploration = false;
    private SensorManager sensorManager;

    public boolean autoUpdate = true, enablePlotRobotPosition = false;
    static String mdfExploredString = "", mdfObstacleString = "";

    public static MazeView3 mazeView;
    static ArrayAdapter<String> directionAdapter;

    public MapFragment3() {
    }

    public static MapFragment3 getInstance() {
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        instance = this;
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map_3, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvRStatus = getView().findViewById(R.id.tvRobotStatus);

        // tilting
        switchTilt = getView().findViewById(R.id.switchTilt);
        sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 200000);
        switchTilt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                tiltEnabled = isChecked;
            }
        });

        // maze view
        mazeView = getView().findViewById(R.id.mazeView2);
        switchAU = getView().findViewById(R.id.switchAutoUp);
        switchAU.setChecked(true);
        switchAU.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    autoUpdate = true;
                    mazeView.invalidate();
                    btnUpdateMap.setEnabled(false);
                    btnUpdateMap.setVisibility(View.INVISIBLE);
                } else {
                    autoUpdate = false;
                    btnUpdateMap.setEnabled(true);
                    btnUpdateMap.setVisibility(View.VISIBLE);
                }
            }
        });

        // dmap
        btnUp = getView().findViewById(R.id.btnUp);
        btnDown = getView().findViewById(R.id.btnDown);
        btnLeft = getView().findViewById(R.id.btnLeft);
        btnRight = getView().findViewById(R.id.btnRight);
        btnCali = getView().findViewById(R.id.btnCali);

        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mazeView.moveUp();
                setRobotPosition(mazeView.getRobotCenter(), mazeView.getRobotAngle());
            }
        });
        btnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mazeView.moveDown();
                setRobotPosition(mazeView.getRobotCenter(), mazeView.getRobotAngle());
            }
        });
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mazeView.moveLeft();
                setRobotPosition(mazeView.getRobotCenter(), mazeView.getRobotAngle());
            }
        });
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mazeView.moveRight();
                setRobotPosition(mazeView.getRobotCenter(), mazeView.getRobotAngle());
            }
        });
        btnCali.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                sendCtrlToBtAct("AR,AN,C");
            }
        });

        //for stopwatch
        chr = getView().findViewById(R.id.chrTimer);
        chrFPTimer = getView().findViewById(R.id.chrFPTimer);

        // restart maze, buttons, textview status and chronometer
        btnRefresh = getView().findViewById(R.id.btnReset);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mazeView.clearExploredGrid();
                mazeView.clearNumID();
                mazeView.clearObstacleGrid();
                mazeView.updateRobotCoords(1, 1, 0);
                setRobotPosition(mazeView.getRobotCenter(), mazeView.getRobotAngle());
                btnExplore.setEnabled(true);
                btnFP.setEnabled(true);
                mazeView.clearObsArray();
                tvRStatus.setText("Waiting for new instructions");
                fastest = false;
                exploration = false;
                chr.stop();
                chrFPTimer.stop();
            }
        });

        // start exploration
        btnExplore = getView().findViewById(R.id.btnExplore);
        btnExplore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // clear maze for new exploration
                mazeView.clearExploredGrid();
                mazeView.clearNumID();
                mazeView.clearObstacleGrid();
                mazeView.clearObsArray();

                sendCtrlToBtAct("PC,AN,E"); // send exploration message to arduino
                btnExplore.setEnabled(false); // disable exploration button
                btnFP.setEnabled(true); // enable fastest button
                tvRStatus.setText("Exploration of maze is in progress..."); // update status
                chr.setBase(SystemClock.elapsedRealtime()); // set stopwatch to 0:00
                chr.stop(); // stop in case there is currently stopwatch running
                chr.setFormat("%s"); // format stopwatch's text
                chr.start(); // start stopwatch
                exploration = true;
            }
        });

        // start fastest path
        btnFP = getView().findViewById(R.id.btnFP);
        btnFP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mazeView.getWaypoint()[0] + 1 < 4 && mazeView.getWaypoint()[1] + 1 < 4) {
                    // way point is inside start area, notify user
                    showToast("Invalid Way-Point Coordinate. Please check again later.");
                } else {
                    sendCtrlToBtAct("PC,AN,FP:" + (mazeView.getWaypoint()[0]) + ":" +
                            (mazeView.getWaypoint()[1])); // send fastest path message to algorithm
                    fastest = true;
                    btnExplore.setEnabled(true); // enable exploration button
                    btnFP.setEnabled(false); // disable fastest button
                    tvRStatus.setText("Finding Fastest path in progress..."); // update status
                    chrFPTimer.setBase(SystemClock.elapsedRealtime()); // set stopwatch to 0:00
                    chrFPTimer.stop(); // stop if it was already running
                    chrFPTimer.setFormat("%s"); // format stopwatch's text
                    chrFPTimer.start(); // start stopwatch
                }
            }
        });

        // Auto map update
        btnUpdateMap = getView().findViewById(R.id.btnUpdateMap);
        btnUpdateMap.setEnabled(false);
        btnUpdateMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mazeView.invalidate();
            }
        });

        // show images instead of id
        switchShowImage = getView().findViewById(R.id.switchShowImage);
        switchShowImage.setChecked(false);
        switchShowImage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton plotRobotPositionBtn, boolean isChecked) {
                mazeView.changeImageRecogniseSettings(isChecked);
            }
        });

        // send waypoint to algorithm
        btnSendWP = getView().findViewById(R.id.btnSendWP);
        btnSendWP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mazeView.getWaypoint()[0] + 1 < 4 && mazeView.getWaypoint()[1] + 1 < 4) {
                    // way point is inside start area, notify user
                    showToast("Invalid Way-Point Coordinate. Please check again later.");
                } else {
                    sendCtrlToBtAct("PC,AN,WP:" + (mazeView.getWaypoint()[0]) + ":" +
                            (mazeView.getWaypoint()[1])); // send message to algorithm
                    showToast("Waypoint has been Sent"); // notify user that waypoint is sent
                }
            }
        });


        // send robot's coordinates & direction facing to algorithm
        btnSendRP = getView().findViewById(R.id.btnSendRP);
        btnSendRP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCtrlToBtAct("PC,AN," + (mazeView.getRobotCenter()[0]) + ":" +
                        (mazeView.getRobotCenter()[1]) + ":" + degreeToDirection(mazeView.angle));
            }
        });


        // show current waypoint X & Y coordinates, (0,0) if not set
        tvFPWP = getView().findViewById(R.id.tvFPWP);
        setWaypointTextView(mazeView.getWaypoint());


        tvRStartP = getView().findViewById(R.id.tvRStart);

        // robot direction
        spinnerROrien = getView().findViewById(R.id.spinnerROrien);
        String[] items = new String[]{"0", "90", "180", "270"};
        directionAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, items);
        spinnerROrien.setAdapter(directionAdapter);
        // drop down menu to set robot's facing direction
        spinnerROrien.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch ((String) parent.getSelectedItem()) {
                    case "0":
                        mazeView.updateRobotCoords(mazeView.getRobotCenter()[0], mazeView.getRobotCenter()[1], 0);
                        break;
                    case "90":
                        mazeView.updateRobotCoords(mazeView.getRobotCenter()[0], mazeView.getRobotCenter()[1], 90);
                        break;
                    case "180":
                        mazeView.updateRobotCoords(mazeView.getRobotCenter()[0], mazeView.getRobotCenter()[1], 180);
                        break;
                    case "270":
                        mazeView.updateRobotCoords(mazeView.getRobotCenter()[0], mazeView.getRobotCenter()[1], 270);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        setRobotPosition(mazeView.getRobotCenter(), mazeView.getRobotAngle());

        // manually set robot's position on maze
        switchPRP = getView().findViewById(R.id.switchPlotRP);
        switchPRP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton plotRobotPositionBtn, boolean isChecked) {
                enablePlotRobotPosition = isChecked;
            }
        });

        // result receiver
//        registerReceivers();

//        joystickRight.setOnMoveListener(new JoystickView.OnMoveListener() {
//            @SuppressLint("DefaultLocale")
//            @Override
//            public void onMove(int angle, int strength) {
//                if (strength > 70 && !device.equalsIgnoreCase("")) {
//                    if (angle >= 315 || angle <= 45)
//                        mazeView.moveRight();
//                    else if (angle >= 225 && angle < 315)
//                        mazeView.moveDown();
//                    else if (angle >= 135 && angle < 225)
//                        mazeView.moveLeft();
//                    else if (angle >= 45 && angle < 135)
//                        mazeView.moveUp();
//                }
//            }
//        });

        // disable all buttons that require bluetooth
        btnUp.setEnabled(false);
        btnDown.setEnabled(false);
        btnLeft.setEnabled(false);
        btnRight.setEnabled(false);
        btnCali.setEnabled(false);
        //joystickRight.setEnabled(false);
        tvRStatus.setText("Offline");

    }

    // Text view created for waypoint setting
    public void setWaypointTextView(int[] waypoint) {
        if (waypoint[0] < 0)
            tvFPWP.setText("(1,1)");
        else
            tvFPWP.setText("(" + (waypoint[0]) + "," + (waypoint[1]) + ")");

    }

    // Change robot coordinates setting
    public static void setRobotPosition(int[] robotpoint, int direction) {
        if (robotpoint[0] < 0)
            tvRStartP.setText("(1,1)");
        else
            tvRStartP.setText("(" + (robotpoint[0]) + "," + (robotpoint[1]) + ")");
        Integer spinnerPosition = directionAdapter.getPosition(String.valueOf(direction));
        spinnerPosition = spinnerPosition == null ? 0 : spinnerPosition;
        spinnerROrien.setSelection(spinnerPosition);
    }

    /*
    - AR,AN,F/R/L
    - Start Exploration: PC,AN,E
    - Send robot coordinates: PC,AN,1:2:N/S/E/W
    - Send Waypoint: PC,AN,WP:1:2
    - Start fastest path: PC,AN,FP:1:2
    - Calibrate Robot: AR,AN,C
     */
    // Send to BluetoothActivity2 to send to RPI
    public void sendCtrlToBtAct(String msg) {
        // You can also include some extra data.
        if (msg.equals("AR,AN,F"))
            tvRStatus.setText("Moving Forward");
        else if (msg.equals("AR,AN,R"))
            tvRStatus.setText("Turning Right");
        else if (msg.equals("AR,AN,L"))
            tvRStatus.setText("Turning Left");
//        Intent intent = new Intent("getCtrlToSend");
//        intent.putExtra("control", msg);
//        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
        if (BluetoothConnectionService.BluetoothConnectionStatus == true) {
            byte[] bytes = msg.getBytes(Charset.defaultCharset());
            BluetoothConnectionService.write(bytes);
        }
    }

    public String degreeToDirection(int degree) {
        String direction = "N";
        if (degree == 0)
            direction = "N";
        else if (degree == 90)
            direction = "E";
        else if (degree == 180)
            direction = "S";
        else if (degree == 270)
            direction = "W";
        return direction;
    }

    public static int directionToDegree(String direction) {
        int degree = 0;
        if (direction.equals("N"))
            degree = 0;
        else if (direction.equals("E"))
            degree = 90;
        else if (direction.equals("S"))
            degree = 180;
        else if (direction.equals("W"))
            degree = 270;
        return degree;
    }

    // Connection change event
//    private BroadcastReceiver mNameReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String theName = intent.getStringExtra("message"); // Get extra data included in the Intent
//            if (theName.equals("")) { // no device connected, disable bluetooth-related actions
//                //joystickRight.setEnabled(false);
//                //--dcBtn.setEnabled(false);
//                btnUp.setEnabled(false);  // check
//                btnDown.setEnabled(false);  // check
//                btnLeft.setEnabled(false);   // check
//                btnRight.setEnabled(false);  // check
//                btnCali.setEnabled(false);  // check
//                tvRStatus.setText("Offline");
//            } else { // device connected, enable all bluetooth-related actions
//                // joystickRight.setEnabled(true);
//                //--dcBtn.setEnabled(true);
//                btnUp.setEnabled(true);
//                btnDown.setEnabled(true);
//                btnLeft.setEnabled(true);
//                btnRight.setEnabled(true);
//                btnCali.setEnabled(true);
//                tvRStatus.setText("Waiting for instructions");
//            }
//        }
//    };

    // Exploration and Fastest Path event
    private BroadcastReceiver mTextReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String theText = intent.getStringExtra("text"); // Get extra data included in the Intent
            if (theText.length() > 0) {
                if (theText.length() < 15 && fastest && (theText.contains("R") ||
                        theText.contains("L") || theText.contains("F") || theText.contains("G"))) { // checking for fastestpath string
                    int forwardDistance;
                    String[] fastestCommands = theText.split(""); // split string to get direction & tiles to move
                    // move robot in order of command received
                    for (int i = 0; i < fastestCommands.length; i++) {
                        if (fastestCommands[i].equals("F")) {
                            mazeView.robotX.add(mazeView.robotCenter[0]);
                            mazeView.robotY.add(mazeView.robotCenter[1]);
                            mazeView.moveForward();
                        } else if (fastestCommands[i].equals("R"))
                            mazeView.turnRight();
                        else if (fastestCommands[i].equals("L"))
                            mazeView.turnLeft();
                        else if (fastestCommands[i].equals("G")) { // reached goal zone
                            tvRStatus.setText("Fastest Path Completed"); // update status
                            fastest = false;
                            chrFPTimer.stop();
                        } else { // Exception catching in case the string format is wrong
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
                } else if (theText.length() > 77 && theText.contains(":") && !fastest) {
                    // Identifying mdf string send for real-time update of maze during exploration
                    String[] stringItems = theText.split(":");

                    String[] exploredString = hexToBinary(stringItems[0]).split(""); // Getting the explored grids from MDF string
                    String bin = "";
                    int[] exploredGrid = new int[exploredString.length - 5]; // -5 to make it 300
                    for (int i = 0; i < exploredGrid.length; i++) {
                        exploredGrid[i] = Integer.parseInt(exploredString[i + 3]); // because first element is ""
                        bin += exploredGrid[i];
                        // Storing explored and obstacle strings
                        mdfExploredString = stringItems[0];
                        mdfObstacleString = stringItems[1];
                    }

                    // Getting the obstacle grids from MDF string
                    String[] obstacleString = hexToBinary(stringItems[1]).split("");
                    int[] obstacleGrid = new int[obstacleString.length - 1];
                    for (int i = 0; i < obstacleGrid.length; i++)
                        obstacleGrid[i] = Integer.parseInt(obstacleString[i + 1]);

                    int gridExplored = 0, inc2 = 0;
                    for (int y = 0; y < 20; y++) {
                        for (int x = 0; x < 15; x++) { // For explored grids, draw obstacle if any
                            if (exploredGrid != null && exploredGrid[gridExplored] == 1) { // grid explored
                                if (obstacleGrid != null && !(inc2 > obstacleGrid.length - 1))
                                    if (obstacleGrid[inc2] == 1) // the explored grid is an obstacle
                                        mazeView.setObsArray(x, y);
                                inc2++;
                            }
                            gridExplored++;
                        }
                    }

                    mazeView.updateMaze(exploredGrid, obstacleGrid); // update the obstacles and explored grids

                    // Getting the direction the robot is facing
                    if (stringItems.length >= 5) {
                        int direction = 0;
                        direction = directionToDegree(stringItems[4]);

                        // Updating coordinates of robot according to string receive from algorithm
                        mazeView.updateRobotCoords(Integer.parseInt(stringItems[2]),
                                Integer.parseInt(stringItems[3]), direction);
                        setRobotPosition(mazeView.getRobotCenter(), mazeView.getRobotAngle());
                    }

                    // This segment of the string stores information of identified image and their coordinates
                    if (stringItems.length >= 6) {
                        // changing string to int
                        int numberX = Integer.parseInt(stringItems[5]);
                        int numberY = Integer.parseInt(stringItems[6]);
                        // Checking to see if received a valid number id
                        boolean correctId = Pattern.matches("^[1-9][0-5]?$", stringItems[7]);
                        if (correctId) {
                            ArrayList<String> tempObsArray = mazeView.getObsArray();
                            String tempPos = (numberX) + "," + (numberY);
                            boolean checkObs = false;
                            for (int i = 0; i < tempObsArray.size(); i++)
                                if (tempObsArray.get(i).equals(tempPos))
                                    checkObs = true;
                            if (checkObs) {
                                mazeView.updateNumberID(numberX, numberY, stringItems[7]);
                                mazeView.updateImageID(numberX, numberY, Integer.parseInt(stringItems[7]));
                            }
                        }
                    }

                } else if (theText.contains("Explored") && exploration) { // exploration completed
                    exploration = false;
                    chr.stop(); // stop stopwatch
                    tvRStatus.setText("Exploration has been successfully completed"); // update status
                    String imageStr = ""; // create string to store information on images found
                    if (mazeView.numberID != null) { // if images were found, loop through X, Y, ID and add to string
                        for (int i = 0; i < mazeView.numberID.size(); i++) {
                            imageStr = imageStr + "(" + (mazeView.numberIDX.get(i) - 1) + ", " +
                                    (mazeView.numberIDY.get(i) - 1) + ", " + mazeView.numberID.get(i) + ") ";
                        }
                    }

                    // message that contains MDF and image information
                    String message = "MDF String: " + mdfExploredString + ":" + mdfObstacleString +
                            "\nImage String(X, Y, ID): " + imageStr;
                    Log.d("Exploration: ", message);
                    // send to communication fragment
                    Intent i = new Intent("getTextFromDevice");
                    i.putExtra("text", "displayExplored");
                    i.putExtra("message", message);
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            sendCtrlToBtAct("AR,AN,C"); // send to arduino to calibrate
//                        }
//                    }, 2000); // 2 seconds later
                }

            }
        }
    };

    public static void textReceived(String theText) {
        if (theText.length() > 0) {
            if (theText.length() < 15 && fastest && (theText.contains("R") ||
                    theText.contains("L") || theText.contains("F") || theText.contains("G"))) { // checking for fastestpath string
                int forwardDistance;
                String[] fastestCommands = theText.split(""); // split string to get direction & tiles to move
                // move robot in order of command received
                for (int i = 0; i < fastestCommands.length; i++) {
                    if (fastestCommands[i].equals("F")) {
                        mazeView.robotX.add(mazeView.robotCenter[0]);
                        mazeView.robotY.add(mazeView.robotCenter[1]);
                        mazeView.moveForward();
                    } else if (fastestCommands[i].equals("R"))
                        mazeView.turnRight();
                    else if (fastestCommands[i].equals("L"))
                        mazeView.turnLeft();
                    else if (fastestCommands[i].equals("G")) { // reached goal zone
                        tvRStatus.setText("Fastest Path Completed"); // update status
                        fastest = false;
                        chrFPTimer.stop();
                    } else { // Exception catching in case the string format is wrong
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
            } else if (theText.length() > 77 && theText.contains(":") && !fastest) {
                // Identifying mdf string send for real-time update of maze during exploration
                String[] stringItems = theText.split(":");

                String[] exploredString = hexToBinary(stringItems[0]).split(""); // Getting the explored grids from MDF string
                String bin = "";
                int[] exploredGrid = new int[exploredString.length - 5]; // -5 to make it 300
                for (int i = 0; i < exploredGrid.length; i++) {
                    exploredGrid[i] = Integer.parseInt(exploredString[i + 3]); // because first element is ""
                    bin += exploredGrid[i];
                    // Storing explored and obstacle strings
                    mdfExploredString = stringItems[0];
                    mdfObstacleString = stringItems[1];
                }

                // Getting the obstacle grids from MDF string
                String[] obstacleString = hexToBinary(stringItems[1]).split("");
                int[] obstacleGrid = new int[obstacleString.length - 1];
                for (int i = 0; i < obstacleGrid.length; i++)
                    obstacleGrid[i] = Integer.parseInt(obstacleString[i + 1]);

                int gridExplored = 0, inc2 = 0;
                for (int y = 0; y < 20; y++) {
                    for (int x = 0; x < 15; x++) { // For explored grids, draw obstacle if any
                        if (exploredGrid != null && exploredGrid[gridExplored] == 1) { // grid explored
                            if (obstacleGrid != null && !(inc2 > obstacleGrid.length - 1))
                                if (obstacleGrid[inc2] == 1) // the explored grid is an obstacle
                                    mazeView.setObsArray(x, y);
                            inc2++;
                        }
                        gridExplored++;
                    }
                }

                mazeView.updateMaze(exploredGrid, obstacleGrid); // update the obstacles and explored grids

                // Getting the direction the robot is facing
                if (stringItems.length >= 5) {
                    int direction = 0;
                    direction = directionToDegree(stringItems[4]);

                    // Updating coordinates of robot according to string receive from algorithm
                    mazeView.updateRobotCoords(Integer.parseInt(stringItems[2]),
                            Integer.parseInt(stringItems[3]), direction);
                    setRobotPosition(mazeView.getRobotCenter(), mazeView.getRobotAngle());
                }

                // This segment of the string stores information of identified image and their coordinates
                if (stringItems.length >= 6) {
                    // changing string to int
                    int numberX = Integer.parseInt(stringItems[5]);
                    int numberY = Integer.parseInt(stringItems[6]);
                    // Checking to see if received a valid number id
                    boolean correctId = Pattern.matches("^[1-9][0-5]?$", stringItems[7]);
                    if (correctId) {
                        ArrayList<String> tempObsArray = mazeView.getObsArray();
                        String tempPos = (numberX) + "," + (numberY);
                        boolean checkObs = false;
                        for (int i = 0; i < tempObsArray.size(); i++)
                            if (tempObsArray.get(i).equals(tempPos))
                                checkObs = true;
                        if (checkObs) {
                            mazeView.updateNumberID(numberX, numberY, stringItems[7]);
                            mazeView.updateImageID(numberX, numberY, Integer.parseInt(stringItems[7]));
                        }
                    }
                }

            } else if (theText.contains("Explored") && exploration) { // exploration completed
                exploration = false;
                chr.stop(); // stop stopwatch
                tvRStatus.setText("Exploration has been successfully completed"); // update status
                String imageStr = ""; // create string to store information on images found
                if (mazeView.numberID != null) { // if images were found, loop through X, Y, ID and add to string
                    for (int i = 0; i < mazeView.numberID.size(); i++)
                        imageStr = imageStr + "(" + (mazeView.numberIDX.get(i) - 1) + ", " +
                                (mazeView.numberIDY.get(i) - 1) + ", " + mazeView.numberID.get(i) + ") ";
                }

                // message that contains MDF and image information
                String message = "MDF String: " + mdfExploredString + ":" + mdfObstacleString +
                        "\nImage String(X, Y, ID): " + imageStr;
                Log.d("Exploration: ", message);
                // send to communication fragment
                ComFragment3.refreshReceiveText(message);

            }

        }
    }

    public boolean getEnablePlotRobotPosition() {
        return enablePlotRobotPosition;
    }

    // method to convert hex to 300 bits binary
    private static String hexToBinary(String hex) {
        int pointer = 0;
        String binary = "", partial;
        // 1 Hex digits each time to prevent overflow and recognize leading 0000
        while (hex.length() - pointer > 0) {
            partial = hex.substring(pointer, pointer + 1);
            String bin;
            bin = Integer.toBinaryString(Integer.parseInt(partial, 16));
            for (int i = 0; i < 4 - bin.length(); i++)
                binary = binary.concat("0");  // padding 0 in front
            binary = binary.concat(bin); // then add in the converted hextobin
            pointer += 1;
        }
        return binary;
    }


    // Listen for events from Bluetooth Activity
//    public void registerReceivers() {
//        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mNameReceiver,
//                new IntentFilter("getConnectedDevice"));
//        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mTextReceiver,
//                new IntentFilter("getTextFromDevice"));
//    }

    // Stop listening events from BluetoothActivity
//    public void unregisterReceivers() {
//        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mTextReceiver);
//        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mNameReceiver);
//    }

    public static void updateConnection(boolean connected) {
        if (!connected) { // no device connected, disable bluetooth-related actions
            //joystickRight.setEnabled(false);
            btnUp.setEnabled(false);  // check
            btnDown.setEnabled(false);  // check
            btnLeft.setEnabled(false);   // check
            btnRight.setEnabled(false);  // check
            btnCali.setEnabled(false);  // check
            tvRStatus.setText("Offline");
        } else { // device connected, enable all bluetooth-related actions
            // joystickRight.setEnabled(true);
            btnUp.setEnabled(true);
            btnDown.setEnabled(true);
            btnLeft.setEnabled(true);
            btnRight.setEnabled(true);
            btnCali.setEnabled(true);
            tvRStatus.setText("Waiting for instructions");
        }
    }

    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    // for tilting function
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        if (tiltEnabled) { // move robot only if tilt has been enabled
            if (y < -5) { // device has been tilted forward
                mazeView.moveUp();
                tvRStatus.setText("Moving Forward");
            } else if (x < -5) { // device has been tilted to the right
                mazeView.moveRight();
                tvRStatus.setText("Turning Right");
            } else if (x > 5) {// device has been tilted to the left
                mazeView.moveLeft();
                tvRStatus.setText("Turning Left");
            } else if (y > 5)  // device tilted to the bottom
                mazeView.moveDown();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        unregisterReceivers(); // Unregister since the activity is about to be closed.
    }
}