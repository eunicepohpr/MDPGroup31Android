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
| Input Data                                                                       | Explanation                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
|----------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AN,PC,F(FFFFF)/L/R/G                                                             | Robot movements received during Fastest path from PC:<br>G &#8594; Robot reached goal zone<br> Accepts maximum of 18 characters                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| AN,PC,Explored                                                                   | Signal Exploration successfully completed to stop timer on Android                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| AN,PC,exploredHex:obstacleHex:robotX:<br>robotY:robotDirection:imageX:imageY:imageID | Receive during exploration (Algo keep sending for every robot movement)<br><br>**exploredHex:** 76Hex of robot currently known (explored) arena<br>**obstacleHex:** Hex of the obstacles known based on currently known arena<br>**robotX, robotY:** robot current position x, y in the arena<br>**robotDirection:** N/S/E/W direction the robot is facing<br><br>**imageX, imageY:** location of recognised image on the arena<br>**imageID:** 1-15<br>Optional include, sent everytime an image is recognised, Android will keep a list                                                |
| AN,PC,robotX:robotY:robotDirection                                               | Change Robot position (receive before fastest path after sending waypoint)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |

Example:
- FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF:0000000000003F000000004000800100000043C08081000220084020F880100000000000080:5:7:N:3:11:1
- FFFFFFFFFFFFFFFFFFFBFFF3FFE7FFC07F807F00FE003C0078000000000000000007000E001F:000000000400000001D00000000010000800000
- FFC07F80FF01FE03FFFFFFF3FFE7FFCFFF9C7F38FE71FCE3F87FF0FFE1FFC3FF87FF0E0E1C1F:00000100001C80000000001C0000080000060001C00000080000

### Outputs
| Output Data       | Explanation                                                                                                      |
|-------------------|------------------------------------------------------------------------------------------------------------------|
| AR,AN,F/R/L       | Send robot movement to Arduino                                                                                   |
| AR,AN,C           | Send to Arduino to Calibrate robot                                                                               |
| PC,AN,E           | Signal for PC (Algo) to start Exploration                                                                        |
| PC,AN,WP:X:Y      | Send Waypoint coordinates to PC<br>Example: PC,AN,WP:5:7                                                         |
| PC,AN,X:Y:N/S/E/W | Send Robot coordinates and facing direction to the PC<br>Example: PC,AN,1,1,N                                    |
| PC,AN,FP:X:Y      | Signal for PC (Algo) to start Fastest Path, sent with Waypoint incase waypoint not set<br>Example: PC,AN,FP,5:7  |

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
