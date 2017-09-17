package allstate.com.coreenginesample.service;

import android.app.Service;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.allstate.coreEngine.beans.DEMError;
import com.allstate.coreEngine.beans.DEMEventInfo;
import com.allstate.coreEngine.beans.DEMTripInfo;
import com.allstate.coreEngine.configuration.DEMConfiguration;
import com.allstate.coreEngine.constants.DEMEventCaptureMask;
import com.allstate.coreEngine.driving.DEMDrivingEngineManager;
import com.google.gson.Gson;

import allstate.com.coreenginesample.Application;
import allstate.com.coreenginesample.Constants;

import android.widget.Toast;

/**
 * Android Service class which starts engine and handles all sdk callbacks
 */

public class CoreEngineService extends Service implements ComponentCallbacks2, DEMDrivingEngineManager.EventListener {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public final int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Toast toast = Toast.makeText(getBaseContext(), "service onCreate", Toast.LENGTH_SHORT);
        toast.show();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initSDK_RequiredParams();
        initSDK_OptionalParams();
    }

    /**
     * Required SDK APIs
     */
    public void initSDK_RequiredParams() {

        /**
         * REQUIRED : initializing event listener
         **/
        DEMDrivingEngineManager.getInstance().setEventListener(this);

        /**
         * REQUIRED: Register to capture all events, you may choose to listen to one to all
         * required
         **/
        DEMDrivingEngineManager.getInstance().registerForEventCapture(DEMEventCaptureMask.DEM_EVENT_CAPTURE_ALL);

        /** OR To capture one or selective events
         *  Still need to implement all call backs for events, however they will not be notified
         DEMDrivingEngineManager.getInstance().registerForEventCapture(DEMEventCaptureMask.DEM_EVENT_CAPTURE_BRAKING_DETECTED);
         OR
         DEMDrivingEngineManager.getInstance().registerForEventCapture(DEMEventCaptureMask.DEM_EVENT_CAPTURE_BRAKING_DETECTED | DEMEventCaptureMask.DEM_EVENT_CAPTURE_START_OF_SPEEDING_DETECTED); */
        Application.getInstance().updateUI("Event listener registered \n");
    }

    /**
     * Optional SDK APIs
     */
    public void initSDK_OptionalParams() {

        /**
         * OPTIONAL: For overriding the default SDK configurations,
         * Create a DEMConfiguration and set it using setConfiguration.
         *
         * NOTE: An DEMError will throw for any invalid value passed.
         * Refer the DEMConfiguration.java from the documentation for the allowed ranges for each config. 
         */
        DEMConfiguration demConfiguration = DEMConfiguration.getConfiguration();
        demConfiguration.setRawDataEnabled(true); // To collect trip's raw data on device storage in developer mode
        demConfiguration.setEnableDeveloperMode(false); // To enable developer mode, true by default
        demConfiguration.setEnableWebServices(true); // To enable webs services, by default true
        demConfiguration.setSpeedLimit(40f); // To set the speed limit to detect speeding violation
        demConfiguration.setLoggingEnabled(true);
        demConfiguration.setAutoStopDuration(300);
        DEMDrivingEngineManager.getInstance().setConfiguration(demConfiguration);

        /**
         * OPTIONAL: To set a custom path to store trip data in dev mode
         * */
        DEMDrivingEngineManager.getInstance().setApplicationPath(getApplicationContext().getFilesDir().getAbsolutePath());
    }

    /**
     * Callback fired when trip is initialized, and user can return his own TripID(max 40 chars),
     * otherwise sdk will create its own.
     * <p>
     * NOTE: Choose to return your custom tripID (40 chars only), if returned with "" OR null, SDK creates it own.
     **/
    @Override
    public String onTripRecordingStarted() {
        Application.getInstance().updateUI("-----------------------------------------------------------------------------------------\n");
        Application.getInstance().updateUI("*** Trip Recording Started *** \n");
        Log.d(Constants.ApplicationNameTag, "*** Trip Recording Started ***");
        sendLocalBroadcast(new Intent(Constants.ACTION_TRIP_STARTED));
        return null;
    }

    /**
     * Callback fired with an initial draft of the tripInfo object once the trip is started
     * Values available at this point : tripId, startLocation, startTime, and startBatteryLevel
     **/
    @Override
    public void onTripRecordingStarted(DEMTripInfo demTripInfo) {
        sendLocalBroadcast(new Intent(Constants.ACTION_TRIP_STARTED_WITH_DATA));
    }

    /**
     * Callback will be fired at every 5 miles, by default and at the end of the trip
     * NOTE: completionFlag is true : DEMTripInfo will be having complete trip data and trip should be considered as stopped
     **/
    @Override
    public void onTripInformationSaved(DEMTripInfo demTripInfo, boolean completionFlag) {

        if (completionFlag) {

            Application.getInstance().updateUI("*** Saved Trip Info *** \n");

            Application.getInstance().updateUI("# TripID: " + demTripInfo.getTripID().toString() + "\n");
            Log.d(Constants.ApplicationNameTag, "# TripID : " + demTripInfo.getTripID().toString());
            Log.d(Constants.ApplicationNameTag, "# StartBatteryLevel: " + demTripInfo.getStartBatteryLevel());
            Log.d(Constants.ApplicationNameTag, "# EndBatteryLevel: " + demTripInfo.getEndBatteryLevel());
            Log.d(Constants.ApplicationNameTag, "# StartLocation: " + demTripInfo.getStartLocation());
            Log.d(Constants.ApplicationNameTag, "# EndLocation: " + demTripInfo.getEndLocation());
            Log.d(Constants.ApplicationNameTag, "# StartTime: " + demTripInfo.getStartTime());
            Log.d(Constants.ApplicationNameTag, "# EndTime: " + demTripInfo.getEndTime());
            Log.d(Constants.ApplicationNameTag, "# DistanceCovered: " + demTripInfo.getDistanceCovered());
            Log.d(Constants.ApplicationNameTag, "# Duration: " + demTripInfo.getDuration());
            Log.d(Constants.ApplicationNameTag, "# Average Speed : " + demTripInfo.getAverageSpeed());
            Log.d(Constants.ApplicationNameTag, "# Maximum Speed: " + demTripInfo.getMaximumSpeed());
            Log.d(Constants.ApplicationNameTag, "Saved Trip Info on trip end");

            sendLocalBroadcast(new Intent(Constants.ACTION_TRIP_INFO_SAVED));
        }
    }


    /**
     * Callback on trip stop.
     **/
    @Override
    public void onTripRecordingStopped() {
        Application.getInstance().updateUI("*** Trip Recording Stopped ***\n");
        Log.d(Constants.ApplicationNameTag, "*** Trip Recording Stopped ***");
        sendLocalBroadcast(new Intent(Constants.ACTION_TRIP_STOPPED));
    }

    /**
     * Callback on trip stop for an invalid trip, not meeting the thresholds for a valid trip
     * basis on minimum distance and duration (Configurable via setConfiguration() API)
     **/
    @Override
    public void onInvalidTripRecordingStopped() {
        Application.getInstance().updateUI("*** Invalid trip Stopped ***\n");
        Log.d(Constants.ApplicationNameTag, "*** Invalid trip Stopped ***");
        sendLocalBroadcast(new Intent(Constants.ACTION_TRIP_INVALID));
    }

    /**
     * Triggered when braking event occurs
     * demEventInfo will be having event details
     **/
    @Override
    public void onBrakingDetected(DEMEventInfo demEventInfo) {
        Application.getInstance().updateUI("Braking Event Detected at " + demEventInfo.getEventStartTime() + "\n");
        Log.d(Constants.ApplicationNameTag, "Braking Event Detected");
        sendLocalBroadcast(new Intent(Constants.ACTION_TRIP_BRAKING));
    }

    /**
     * Triggered when an acceleration event occurs
     **/
    @Override
    public void onAccelerationDetected(DEMEventInfo demEventInfo) {
        Application.getInstance().updateUI("Acceleration Event Detected at " + demEventInfo.getEventStartTime() + "\n");
        Log.d(Constants.ApplicationNameTag, "Acceleration Event Detected");
        sendLocalBroadcast(new Intent(Constants.ACTION_TRIP_ACC));
    }


    /**
     * Triggered when speeding event starts
     * demEventInfo is having only information only till this point
     **/
    @Override
    public void onStartOfSpeedingDetected(DEMEventInfo demEventInfo) {
        Application.getInstance().updateUI("Start of Speeding Event at " + demEventInfo.getEventStartTime() + "\n");
        Log.d(Constants.ApplicationNameTag, "Start of Speeding Event");
        sendLocalBroadcast(new Intent(Constants.ACTION_TRIP_START_SPEEDING));
    }

    /**
     * Triggered when speeding event ends
     **/
    @Override
    public void onEndOfSpeedingDetected(DEMEventInfo demEventInfo) {
        Application.getInstance().updateUI("End of Speeding Event at " + demEventInfo.getEventStartTime() + "\n");
        Log.d(Constants.ApplicationNameTag, "End of Speeding Event");
        sendLocalBroadcast(new Intent(Constants.ACTION_TRIP_END_SPEEDING));
    }

    /**
     * Triggered when any error/warning occurs within SDK
     **/
    @Override
    public void onError(DEMError demError) {
        if (demError != null) {
            Application.getInstance().updateUI("DEMError code : " + demError.getErrorCode() + "\n");

            Log.d("DEMError code: ", String.valueOf(demError.getErrorCode()));
            Log.d("DEMError category: ", demError.getCategory());

            if (demError.getAdditionalInfo() != null) {
                Application.getInstance().updateUI("Error Description : " + String.valueOf(demError.getAdditionalInfo().get(DEMError.AdditionalInfoKeys.LOCALIZED_DESCRIPTION)) + "\n");
                Log.d("ErrorDescription", String.valueOf(demError.getAdditionalInfo().get(DEMError.AdditionalInfoKeys.LOCALIZED_DESCRIPTION)));
            }

            sendLocalBroadcast((new Intent(Constants.ACTION_ERROR)).putExtra("error", new Gson().toJson(demError)));
        }
    }

    /**
     * Optional : sets the meta data (any additional data to trip info), return null if not needed.
     * Additional data to be uploaded as a part of DEMTripInfo
     **/
    @Override
    public String onRequestMetaData() {
        return "Send the meta data here - max allowed is 4096 characters (underscore discarded). This is trip specific. Coming from the sample app";
    }

    /**
     * Triggered when there is change in gpsAccuracy level,
     * when a trip is in progress.
     **/
    @Override
    public void onGpsAccuracyChangeDetected(int gpsAccuracyLevel) {

    }

    /**
     * Broadcasting message to display on the UI.
     **/
    private void sendLocalBroadcast(Intent action) {
        LocalBroadcastManager.getInstance(Application.getInstance()).sendBroadcast(action);
    }

}
