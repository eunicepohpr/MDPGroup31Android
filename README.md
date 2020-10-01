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
        - Supposingly a better implementation of passing around text received from bluetooth, however `reconnection is broken ` :sob:

~~4) Future? Version3 implementation of passing around text with Original Bluetooth Service :cold_sweat:~~