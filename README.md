# ride_rage_android
Created by Matti Mäki-Kihniä & Daniel Zakharin

RideRage is an Android application that uses data from OBD readers with ELM327 microcontroller and bluetooth adapters. RideRage keeps 
track of your vehicle's speed and RPM while you drive and after finishing your ride it'll show you a summary of the ride with information such as average RPMs, speed and maximum speed.

#### Key functionalities:
- Connects to an OBD device via bluetooth
- Creates a data point object every two second that includes:
  - Speed
  - RPM
  - Longitude & latitude
  - Timestamp
- Draws the route on Google Maps using different colours to show where the user has been driving economically
- Start / stop a trip
- Browse old trips
- Delete trip
- Name / rename the trip

### Techincal
- Three different fragments:
  - Gauges fragment for the main screen
  - Result fragment for the result screen
  - List fragment for the trip list screen
- Obd commands are run in a service as well as listening to location updates
- SQLite database to store trip data
- Service creates and stores a datapoint every two seconds

### APIs used
- [OBD Java API by Pires](https://github.com/pires/obd-java-api/), used for sending commands to the OBD device
- [MPAndroidCHart by PhilJay](https://github.com/PhilJay/MPAndroidChart), used for creating a chart in the result screen

### Screenshots
<center><img src="/screenshots/main.jpg" width="250"> <img src="/screenshots/list.jpg" width="250"> <img src="/screenshots/result.jpg" width="250"></center>
