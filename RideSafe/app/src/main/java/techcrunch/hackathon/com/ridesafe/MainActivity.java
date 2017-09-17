package techcrunch.hackathon.com.ridesafe;

import android.app.Activity;
import android.os.Bundle;

import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapquest.mapping.MapQuestAccountManager;
import com.mapquest.mapping.constants.Style;
import com.mapquest.mapping.maps.MapView;
import com.mapquest.mapping.maps.MapboxMap;

public class MainActivity extends Activity {
    private static final LatLng SAN_FRANCISCO = new LatLng(37.7757626, -122.385804);

    private MapboxMap mMapboxMap;
    private MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapQuestAccountManager.start(getApplicationContext());

        setContentView(R.layout.activity_main);

       /* mMapView = (MapView) findViewById(R.id.mapquestMapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mMapboxMap = mapboxMap;
                initializeMapView(mMapView, mMapboxMap);
                enableTraffic(mMapboxMap);
                enableIncidents(mMapboxMap);
            }
        });*/
    }

    private void initializeMapView(MapView mapview, MapboxMap mapboxmap) {
        mapview.setStyleUrl(Style.MAPQUEST_STREETS);
        mapboxmap.moveCamera(CameraUpdateFactory.newLatLngZoom(SAN_FRANCISCO, 8));
    }

    private void enableTraffic(MapboxMap mapboxmap) {
        mapboxmap.setTrafficFlowLayerOn();
    }

    private void enableIncidents(MapboxMap mapboxMap) {
        mapboxMap.setTrafficIncidentLayerOn();
    }

    @Override
    public void onResume()
    { super.onResume(); mMapView.onResume(); }

    @Override
    public void onPause()
    { super.onPause(); mMapView.onPause(); }

    @Override
    protected void onDestroy()
    { super.onDestroy(); mMapView.onDestroy(); }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    { super.onSaveInstanceState(outState); mMapView.onSaveInstanceState(outState); }
}