# CZ3004 MDP Team 31 Android

This application requires **Bluetooth**! minSDK is 23 and we *NOT* using androidx :frowning:

## Documentation References
- [Bluetooth Connection](https://developer.android.com/guide/topics/connectivity/bluetooth.html)
- [Activity launch modes](https://developer.android.com/guide/components/activities/tasks-and-back-stack)
- [Support different screen sizes](https://developer.android.com/training/multiscreen/screensizes)
    - We barely support phone :grimacing:

---

## Icons
Special thanks to the authors below for designing the beautiful icons used in this application :heart_eyes:
- Icons made by [good-ware](https://www.flaticon.com/authors/good-ware) from [www.flaticon.com](https://www.flaticon.com/)
- Icons made by [prettycons](https://www.flaticon.com/authors/prettycons) from [www.flaticon.com](https://www.flaticon.com/)
- Icons made by [Freepik](http://www.freepik.com/) from [www.flaticon.com](https://www.flaticon.com/)
- Icons made by [Pixel perfect](https://www.flaticon.com/authors/pixel-perfect) from [www.flaticon.com](https://www.flaticon.com/)

---

## Application Versions
This repository currently contains 3 versions of the application. To switch between the different versions, `edit the AndroidManifest file` :poop:

 1) Original unedited code
    - For testing if orignal implementation is working
    - Problems:
        - Activity lifecycle not managed properly
        - ~~Its not our coat :')~~

 2) New Implementation using Original Bluetooth Service :thumbsup:
    - Change of UI and [Activity launch modes](https://developer.android.com/guide/components/activities/tasks-and-back-stack)
    - Problems:
        - Repeatedly forcing disconnection will lead to broken Bluetooth (I really dk how to fix this :cry:) &#8594; `just restart the application`
        - Disconnecting device from application might break subsequent bluetooth connection (Will try to prevent the accident click) &#8594; `don't press the device in the list when it is connected, disconnect from the other end` :sweat_smile:

 3) New Implementation using Different Bluetooth Service (reconnection broken)
    - Same UI as version 2 but, using a different bluetooth service implementation
    - Problems:
        - Supposingly a better implementation of passing around text received from bluetooth, however `reconnection is broken` :sob:

~~4) Future? Version3 implementation of passing around text with Original Bluetooth Service :cold_sweat:~~

## Android Data Dictionary
Format: \<To>,\<From>,data1:data2:data3
### Inputs
| Input Data                                                                       | Explanation                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
|----------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AN,PC,F/L/R/G                                                                    | Robot movements received during Fastest path from Arduino:<br>G &#8594; Robot reached goal zone<br><br>Android send start FP to Algo, Algo send e.g. "AR,PC,FFFFRFLFRFLFFRFG" to Arduino, each robot movement Arduino will send to Android                                                                                                                                                                                                                                                                                                                                                   |
| AN,PC,Explored                                                                   | Signal Exploration successfully completed to stop timer on Android                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| AN,PC,exploredHex:obstacleHex:robotX:robotY:robotDirection:imageX:imageY:imageID | Receive during exploration (Algo keep sending for every robot movement)<br><br>**exploredHex:** 76Hex of robot currently known (explored) arena<br>**obstacleHex:** Hex of the obstacles known based on currently known arena<br>**robotX, robotY:** robot current position x, y in the arena<br>**robotDirection:** N/S/E/W direction the robot is facing<br><br>**imageX, imageY:** location of recognised image on the arena<br>**imageID:** 1-15<br>Optional include, sent everytime an image is recognised, Android will keep a list<br><br>Example:<br> - FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF:0000000000003F000000004000800100000043C08081000220084020F880100000000000080:5:7:N:3:11:1<br> - FFFFFFFFFFFFFFFFFFFBFFF3FFE7FFC07F807F00FE003C0078000000000000000007000E001F:000000000400000001D00000000010000800000 |

### Outputs
| Output Data       | Explanation                                                                                                      |
|-------------------|------------------------------------------------------------------------------------------------------------------|
| AR,AN,F/R/L       | Send robot movement to Arduino                                                                                   |
| AR,AN,C           | Send to Arduino to Calibrate robot                                                                               |
| PC,AN,E           | Signal for PC (Algo) to start Exploration                                                                        |
| PC,AN,WP:X:Y      | Send Waypoint coordinates to PC<br>Example: PC,AN,WP:5:7                                                         |
| PC,AN,X:Y:N/S/E/W | Send Robot coordinates and facing direction to the PC<br>Example: PC,AN,1,1,N                                    |
| PC,AN,FP:X:Y      | Signal for PC (Algo) to start Fastest Path, sent with Waypoint incase waypoint not set<br>Example: PC,AN,FP,5:7  |
