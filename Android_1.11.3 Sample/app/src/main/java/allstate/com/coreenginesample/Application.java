package allstate.com.coreenginesample;

import android.content.Intent;

import com.allstate.coreEngine.driving.DEMDrivingEngineManager;

import allstate.com.coreenginesample.service.CoreEngineService;


public class Application extends android.app.Application {

    private static Application application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

        /* Setting application context to the SDK  */
        DEMDrivingEngineManager.setContext(getApplicationContext());

        /* Starting a STICKY background service which starts the
           engine and handles all sdk callbacks */
        startService(new Intent(this, CoreEngineService.class));
    }

    /**
     * Returns the application instance
     */
    public static Application getInstance() {
        return application;
    }


    /**
     * Updating the UI
     */
    public void updateUI(String logMessage) {
        Intent intent = new Intent();
        intent.putExtra(this.getString(R.string.uiMessageKey), logMessage);
        intent.setAction(this.getString(R.string.updateUIIntent));
        sendBroadcast(intent);
    }

}