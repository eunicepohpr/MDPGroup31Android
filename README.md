# CZ3004 MDP Team 31 Android

This application requires **Bluetooth**! minSDK is 23 and *NOT* using androidx

## Documentation References
- [Bluetooth Connection](https://developer.android.com/guide/topics/connectivity/bluetooth.html)
- [Bluetooth Adapter](https://developer.android.com/reference/android/bluetooth/BluetoothAdapter)
- [Android Lifecycle Events](https://developer.android.com/guide/components/activities/activity-lifecycle)
- [Activity launch modes](https://developer.android.com/guide/components/activities/tasks-and-back-stack)
- [Support different screen sizes](https://developer.android.com/training/multiscreen/screensizes)
    - barely support phone (not perfect)

---

## Icons
Credits to the authors for designing the icons used in this application :heart_eyes:
- Icons made by [good-ware](https://www.flaticon.com/authors/good-ware) from [www.flaticon.com](https://www.flaticon.com/)
- Icons made by [prettycons](https://www.flaticon.com/authors/prettycons) from [www.flaticon.com](https://www.flaticon.com/)
- Icons made by [Freepik](http://www.freepik.com/) from [www.flaticon.com](https://www.flaticon.com/)
- Icons made by [Pixel perfect](https://www.flaticon.com/authors/pixel-perfect) from [www.flaticon.com](https://www.flaticon.com/)

---

## Application Versions
This repository currently contains 3 versions of the application. To switch between the different versions, edit the [AndroidManifest](/app/src/main/AndroidManifest.xml) file :poop:

### [Version 1 (reference)](/app/src/main/java/com/example/mdpandroid/Version1)
Original unedited code
- For testing if orignal implementation is working
- Problems:
    - Activity lifecycle not managed properly
    - ~~Its not our coat :')~~

 ### [Version 2 (Leaderboard code :thumbsup:)](/app/src/main/java/com/example/mdpandroid/Version2)
 New Implementation using Original Bluetooth Service
- Change of UI and Activity launch modes
- Problems:
    - Repeated disconnection (by pressing device in list) will lead to broken Bluetooth (I really dk how to fix this :cry:) &#8594; `restart the application or disconnect from rpi`

 ### [Version 3 (unused)](/app/src/main/java/com/example/mdpandroid/Version3)
 New Implementation using Different Bluetooth Service (reconnection broken)
- Same UI as version 2 but, using a different bluetooth service implementation
- Problems:
    - Supposingly a better implementation of passing around text received from bluetooth, however `reconnection is broken`

---

## Android Data Dictionary
`Format: <To>,<From>,data1:data2:data3`
### Inputs
| Input Data                                                                       | Explanation                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
|----------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AN,PC,F(FFFFF)/L/R/G                                                             | Robot movements received during Fastest path:<br>G &#8594; Robot reached goal zone<br> Accepts maximum of 18 characters                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| AN,PC,Explored                                                                   | Signal Exploration successfully completed                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| AN,PC,exploredHex:obstacleHex:robotX:robotY:robotDirection:imageX:imageY:imageID | Receive during exploration (for every robot movement)<br><br>**exploredHex:** 76Hex of robot currently known (explored) arena<br>**obstacleHex:** Hex of the obstacles detected based on currently known arena<br>**robotX, robotY:** robot current x, y position in the arena<br>**robotDirection:** N/S/E/W direction the robot is facing<br><br>**imageX, imageY:** location of recognised image on the arena<br>**imageID:** 1-15<br>Optional include, sent everytime an image is recognised, Android will keep a list                                                |
| AN,PC,robotX:robotY:robotDirection                                               | Change Robot position (receive before fastest path after exploration)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |

Example:
- FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF:0000000000003F000000004000800100000043C08081000220084020F880100000000000080:5:7:N:3:11:1
- FFFFFFFFFFFFFFFFFFFBFFF3FFE7FFC07F807F00FE003C0078000000000000000007000E001F:000000000400000001D00000000010000800000
- FFC07F80FF01FE03FFFFFFF3FFE7FFCFFF9C7F38FE71FCE3F87FF0FFE1FFC3FF87FF0E0E1C1F:00000100001C80000000001C0000080000060001C00000080000

### Outputs
| Output Data       | Explanation                                                                                                      |
|-------------------|------------------------------------------------------------------------------------------------------------------|
| AR,AN,F/R/L       | Send robot movement to Arduino                                                                                   |
| AR,AN,C           | Send to Arduino to Calibrate robot                                                                               |
| PC,AN,E           | Signal for PC to start Exploration                                                                               |
| PC,AN,WP:X:Y      | Send Waypoint coordinates to PC<br>Example: PC,AN,WP:5:7                                                         |
| PC,AN,X:Y:N/S/E/W | Send Robot coordinates and facing direction to the PC<br>Example: PC,AN,1,1,N                                    |
| PC,AN,FP:X:Y      | Signal for PC to start Fastest Path, sent with Waypoint incase waypoint not set<br>Example: PC,AN,FP,5:7         |

---

## Connection and Running Sequence

### Connection sequence
1) Rpi run multi-threading script
2) Android connects via Bluetooth to Rpi
3) Arduino auto connects to Rpi after Android successfully connected
4) Run Algo code to connect PC to Rpi

### Running Seqeunce
1) Android sends `Waypoint` to PC during the 2 mins setup
2) Android sends `Start Exploration` to PC
3) PC updates Android during exploration, sends "Explored" when exploration completed
4) Android sends `Start Fastest Path` to PC
5) PC updates Android with start direction of fastest path (either N or E)
5) PC sends fastest path to Arduino (e.g. FFFFRFFFFLFF)
6) Arduino sends Fastest path movement to Android (e.g FFFF, R, FFFF, L, FF)

---

## UI Design

### Android Activity Lifecycle and Activity Stack
To implement the UI design using Android, a clear understanding of the Android Activity Lifecycle and Android Activity Stack is required. For good practice, we have used fragments inside an activity. For example, we have used “MainActivity'' as the main container of the app with multiple fragments, “Map” and “Communication” to handle the different interfaces. Fragments bring more flexibility in handling app functionalities and user interfaces. “Bluetooth'' activity is a separate activity that is used to handle communications between the android tablet and RPI. Both activities launchMode are set as singleInstance to ensure that only one activity is created every time the user navigates between MainActivity and BluetoothActivity. 

### Support different devices
We aim to build dynamic user interfaces such that the application has no format issues when used on other devices with different screen sizes. A combination of LinearLayout and RelativeLayout with weightSum are used to ensure that the position of UI elements will always remain in the same location regardless of device screen sizes. To specify different dimensions for different screen sizes, two values folders “values” and “values-large” are created in the res directory. By default, Android will render content in the “values” folder and for larger screen devices it will replace elements that are specified in “values-large”.

---

## Implementation Design
### Bluetooth Implementation
`BluetoothActivity.java`: Handles UI interaction related to Bluetooth

`BluetoothService.java`: Setting and managing Bluetooth connection with other devices

The communication protocol can be summarised into 4 steps:
1. Pairing process
    - One device, a discoverable device makes itself available, another device find a discoverable device using a service discovery process
2. Bonding process
    - The discoverable device accepts the pairing request and exchange security keys
3. Information Exchange
4. Session Completed
    - The device that initiates the pairing request release channel linked to the discoverable device. Two devices remain bonded so they can reconnect automatically during a future session.

Below are the Bluetooth Status used to receive the different events in the application

| Status                                | Event                                                                                                                                 |
|---------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| `ACTION_REQUEST_DISCOVERABLE`         | Enable Device to be discoverable to other devices                                                                                     |
| `ACTION_FOUND`                        | The remote device is found during discovery and the device Name and MAC address will be displayed in the list under Available Devices |
| `ACTION_DISCOVERY_FINISHED`           | When Bluetooth has completed scanning for devices                                                                                     |
|`ACTION_BOND_STATE_CHANGED BOND_BONDED`| The device has successfully paired with another device                                                                                |
| `ACTION_ACL_DISCONNECTED`             | Bluetooth disconnected event                                                                                                          |
| `STATE_CONNECTED`                     | Bluetooth is connected to another device                                                                                              |
| `ACTION_REQUEST_ENABLE`               | Prompt user to turn on Bluetooth                                                                                                      |


### Functionalities Implementation
#### BluetoothActivity
Main objectives of Bluetooth Activity:
1. The Android application is able to transmit and receive text strings over the Bluetooth serial communication link.
2. The Android application can initiate scanning, selection, and connection with Bluetooth device
3. The Android app provides robust Bluetooth connectivity

Once connected, BluetoothActivity will broadcast messages and connection status and also register itself to receive controls from the other fragments using LocalBroadcastManager. 

IntentFilter used for LocalBroadcastManager

| Intent Action      | Type of message                                                                                 | Send by               | Received by                        |
|--------------------|-------------------------------------------------------------------------------------------------|-----------------------|------------------------------------|
|`getConnectedDevice`| Bluetooth Connection status broadcast to update the toolbar                                     | BluetoothActivity     | MainActivity                       |
| `getTextFromDevice`| All messages received via Bluetooth                                                             | BluetoothActivity     | MapFragment, CommunicationFragment |
| `getTextToSend`    | Message strings to send                                                                         | CommunicationFragment | BluetoothActivity                  |
| `getCtrlToSend`    | Robot controls start exploration and fastest path, send waypoint and robot coordinates commands | MapFragment           | BluetoothActivity                  |

#### MapFragment
Main Objectives of Map Tab:
1. Provides interactive control of robot movement (via Bluetooth link)
2. Provides an indication of the current status of the robot
3. Update Fastest Path Waypoint & Robot Start coordinates
4. Display the maze with its current known obstacle, robot position, and Number ID of image recognized in the grid map
5. Provides a selection of Manual/Auto display update mode

For creating the maze environment, MazeView.java is used where `canvas.drawLine()` and `canvas.drawRect()` function is used to draw the grids required to create a 15 x 20 maze. 

Whenever there are controls such as “Start Exploration”, “Start Fastest Path”, “Send Waypoint” or Robot movements to send to PC or Arduino, MapFragment will broadcast the data using LocalBroadcastManager class separated by “:” in the following format **“To:From:data1:data2..”** with the IntentFilter `“getCtrlToSend”`. BluetoothActivity will listen for the following IntentFilter and send out the data received via Bluetooth to the RPi.

MapFragment will listen for messages received from the IntentFilter `“getTextFromDevice”` and update the interface accordingly. Below are the four types of information received that MapFragment needs to consider:
1. During Exploration of robot
    - P1 (Exploration status of the map)
    - P2 (Obstacle detected based on explored areas of the map)
    - Robot current position and facing direction
    - Images detected (if any)
2. Receiving Exploration complete
3. Change the robot position or direction (prepare for fastest path)
4. Receiving movements during the fastest path

#### Communication Fragment
Main objectives of Communication Tab:
1. The Android application is able to support persistent user reconfigurable string
2. Send and receive messages via Bluetooth link

CommunicationFragment will listen for messages received from the IntentFilter `“getTextFromDevice”` and display them under Receive Text. Similar to MapFragment, CommunicationFragment will send messages using the IntentFilter `“getTextToSend”` and BluetoothActivity will listen for that IntentFilter
