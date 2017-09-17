package allstate.com.coreenginesample;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.allstate.coreEngine.constants.DEMEngineMode;
import com.allstate.coreEngine.driving.DEMDrivingEngineManager;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    Application mApplication;


    double mMockCadence;
    String mMockMotionPath;
    String mMockLocationPath;
    boolean mMockEnableFastMocking;
    private TextView tv;

    private ProgressBar mProgressBar = null;

    UpdateUIReceiver updateUIReceiver;
    private IntentFilter filter = null;
    private AlertDialog alertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mApplication = (Application) getApplicationContext();

        initIntentFilter();initIntentFilter();

        LocalBroadcastManager.getInstance(this).registerReceiver(mTripBroadcastReceiver, filter);

        LocalBroadcastManager.getInstance(this).registerReceiver(mTripBroadcastReceiver, filter);

        initUI();

        registerUpdateUIReceiver();

        if(!handlePermissionsForDrivingEngine()) {

             /*
            * Starting the engine after every allow or deny from user to test the sdk behaviour.
            * */
            DEMDrivingEngineManager.getInstance().startEngine();
            Application.getInstance().updateUI("*** startEngine is called, Engine Started *** \n");
            Log.d(Constants.ApplicationNameTag,"*** startEngine is called, Engine Started ***");
        }

        if (DEMDrivingEngineManager.getInstance().getEngineMode() == DEMEngineMode.ENGINE_MODE_DRIVING){
            progressBar(true);
        }


    }

    /*
    * broadcast receiver for trip events thrown by sdkx*/
    private BroadcastReceiver mTripBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onTripEvent(intent.getAction(), intent);
        }
    };

    /*
    * Handling Android M permissions
    * Client app can either choose to ask for these at their app start
    * OR
    * ask for the permission when permission related DEMError comes from SDK
    * */
    public boolean handlePermissionsForDrivingEngine() {

        //Handling permissions for Marshmallow devices
        ArrayList<String> lPermissionNeeded = new ArrayList<>();
        getPermissionDeniedList(lPermissionNeeded, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        getPermissionDeniedList(lPermissionNeeded, Manifest.permission.READ_EXTERNAL_STORAGE);
        getPermissionDeniedList(lPermissionNeeded, Manifest.permission.ACCESS_FINE_LOCATION);

        if (lPermissionNeeded.size() > 0) {
            ActivityCompat.requestPermissions(this,
                    lPermissionNeeded.toArray(new String[lPermissionNeeded.size()]),
                    Constants.PERMISSIONS_REQUEST_ACCESS);
            return true;
        }
        return false;
    }

    /*
    * Permissions check
    * */
    private void getPermissionDeniedList(ArrayList<String> lPermissionNeeded, String permission) {

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            lPermissionNeeded.add(permission);
        }
    }

    private void initUI() {
        mProgressBar = (ProgressBar) findViewById(R.id.pbLoading);
        tv = (TextView) findViewById(R.id.textview);
        tv.setMovementMethod(new ScrollingMovementMethod());

    }


    /**
     * UpdateUIReceiver for updating the UI values
     */
    public class UpdateUIReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String message = intent.getStringExtra(mApplication.getString(R.string.uiMessageKey));
            updateUI(message);
        }
    }

    /*
     * Extracts the message from intent and updates in the UI.
     */

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra(Constants.LOG_MESSAGE);
            updateUI(message);
        }
    };


    /**
     * Register the UI update receiver
     */
    private void registerUpdateUIReceiver() {
        IntentFilter intentFilter = new IntentFilter(this.getString(R.string.updateUIIntent));
        updateUIReceiver = new UpdateUIReceiver();
        registerReceiver(updateUIReceiver, intentFilter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_new_fast_mock) {
            if(!handlePermissionsForDrivingEngine()) {
                if (DEMDrivingEngineManager.getInstance().getEngineMode() != DEMEngineMode.ENGINE_MODE_DRIVING) {
                    startMockTrip(true);
                } else {
                    updateUI("A trip is in progress! \n");
                }
            }
            return true;
        } else if (id == R.id.action_new_slow_mock) {
            if(!handlePermissionsForDrivingEngine()) {
                if (DEMDrivingEngineManager.getInstance().getEngineMode() != DEMEngineMode.ENGINE_MODE_DRIVING) {
                    startMockTrip(false);
                } else {
                    updateUI("A trip is in progress! \n");
                }
            }
            return true;
        } else if (id == R.id.action_new_stop_trip) {
            DEMDrivingEngineManager.getInstance().stopTripRecording();
            return true;
        }



        return super.onOptionsItemSelected(item);
    }

    /* this method starts the mock trip */
    private void startMockTrip(boolean isFastMock) {

        //mMockMotionPath = "/storage/emulated/0/Download/MockActivity.txt"; // provide motion file.txt path
        //mMockLocationPath = "/storage/emulated/0/Download/MockLocation.txt"; // provide location file.txt path

        mMockMotionPath = "/storage/MockActivity.txt"; // provide motion file.txt path
        mMockLocationPath = "/storage/MockLocation.txt"; // provide location file.txt path

        mMockEnableFastMocking = isFastMock;
        mMockCadence = 0.3;

        /* Provides mock data in the form of a CSV or txt file to be used
           to exercise the sdk without having to drive */
        DEMDrivingEngineManager.getInstance().startMockTrip(mMockMotionPath, mMockLocationPath, mMockEnableFastMocking, mMockCadence);

        updateUI("Initiating mock... \n");
        Log.d(Constants.ApplicationNameTag,"Initiating mock...");

    }


    private void updateUI(String logMessage) {

        final StringBuilder newMessage = new StringBuilder();
        newMessage.append(logMessage);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                tv.append(newMessage.toString());
            }
        });

    }


    private void initIntentFilter() {

        filter = new IntentFilter();
        filter.addAction(Constants.ACTION_TRIP_STARTED);
        filter.addAction(Constants.ACTION_TRIP_INVALID);
        filter.addAction(Constants.ACTION_TRIP_STOPPED);
        filter.addAction(Constants.ACTION_ERROR);
    }

    protected void onTripEvent(String tripAction, Intent intent) {

        switch (tripAction) {
            case Constants.ACTION_TRIP_STARTED: {
                progressBar(true);

                break;
            }
            case Constants.ACTION_TRIP_STOPPED: {
                progressBar(false);

                break;
            }
            case Constants.ACTION_TRIP_INVALID: {
                progressBar(false);

                break;
            }
            case Constants.ACTION_ERROR: {
                progressBar(false);

                break;
            }
        }
    }

    public void progressBar(final boolean v) {

        try {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setVisibility(v == true ? View.VISIBLE : View.INVISIBLE);
                }
            });
        } catch (Exception ex) {
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mTripBroadcastReceiver != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mTripBroadcastReceiver);

        if (updateUIReceiver != null)
            unregisterReceiver(updateUIReceiver);

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);


    }
    @Override
    public void onBackPressed() {
        progressBar(false);
        this.moveTaskToBack(true);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Constants.PERMISSIONS_REQUEST_WRITE_READ_STORAGE:
                //finish();
                break;
            case Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];

                    if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION) || permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                                //handle Never Ask Again condition by showing Dialog Box which leads the user to Settings Page where the user can change Permission status.
                                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                                    showDialog(this);
                                }
                            }
                            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                                //handle Never Ask Again condition by showing Dialog Box which leads the user to Settings Page where the user can change Permission status.
                                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                                    showDialog(this);
                                }

                            }
                        }
                    }
                }
                break;
        }

        if(!handlePermissionsForDrivingEngine()){
            /*
            * Starting the engine after every allow or deny from user to test the sdk behaviour.
            * */
            DEMDrivingEngineManager.getInstance().startEngine();
            Application.getInstance().updateUI("*** startEngine is called, Engine Started *** \n");
            Log.d(Constants.ApplicationNameTag,"startEngine is called, Engine Started");

        }


    }

     /*
      Displays the Dialog Box that directs the user to App Settings Page.
     */

    private void showDialog(final Activity activity) {

        if (alertDialog != null && alertDialog.isShowing()) {
            return;
        }

        final AlertDialog.Builder builderInner = new AlertDialog.Builder(
                MainActivity.this);
        String message = "Please allow the following Permissions to proceed : \n 1. Location \n 2. Storage";
        builderInner.setMessage(message);
        builderInner.setTitle("Permission(s) Denied ! ");
        builderInner.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            startInstalledAppDetailsActivity(activity);
                        }
                    }
                });
        builderInner.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

        alertDialog = builderInner.show();
    }

     /*
      Loads the Settings of the App.
      Enables the user to edit App related Settings like Permissions
     */

    private void startInstalledAppDetailsActivity(final Activity context) {
        if (context == null) {
            return;
        }
        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }

}
