# RemoteServiceMapSample

Aim of this sample is to show Android IPC communication via Messenger, plus basic GoogleMap usage.

The sample app has service in separate process which loops through predefined list of points on map every 30 seconds.
For each point service gets reverse geocoding information (e.g. address) and sends it to UI.
UI displays current point on GoogleMap and StreetView and time to the next point.
This is real-life-ish example, so there are mechanisms to restore current point if app goes to background,
keeping wakelock while executing reverse geocoding etc, recovering from process death etc.

In order to run you'll need GoogleMaps Android API key:
https://developers.google.com/maps/documentation/android-api/signup
(Just paste it to String defined in AndroidManifest.xml)

Made as vanilla Android, no libraries except google maps / support library.
This is a project made as job interview assignment, thought it might come in handy to someone.
