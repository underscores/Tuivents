package com.m2a.teamdelta.tuivents;

import android.app.AlertDialog;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.content.DialogInterface;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;


public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    public Calendar date = new GregorianCalendar(); //initialize Calendar with current date and time. The Calendar object can be used to set dates to search for events on.
    private String today = "heute "; //variable for specialized output. If no date was chosen, the Alertdialog for the case that no events were found will display that there weren't any events today, otherwise just that there weren't any events.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*The following two lines are needed for the app to be able to connect to the database on it's main thread
         *Usually, network stuff can only be done in async threads, which is a bit elaborate, so we do this workaround
         *Changing this to a threaded thing could make a first proposal for improvements*/
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /*
     * Secure shutdown.
     */
    @Override
    public void onBackPressed(){
        //Safety Question if the user presses the Backbutton, to prevent accidental closing
        AlertDialog.Builder back = new AlertDialog.Builder(this);
        back.setTitle("Beenden")
            .setMessage("Möchten Sie die App wirklich schließen?")
            .setPositiveButton("JA", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            })
            .setNegativeButton("NEIN", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // App not gonna get closed yet, do nothing.
                }
            });
        back.show();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //center the map over the Campus
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50.683032, 10.936282), 15.0f), 4000, null);
        //open DB connection
        DBVerbindung db = new DBVerbindung("tuivents", "root", "M2A2015");
        db.open();
        //get all the events for a given date
        Set<Integer> events = db.getEventsByDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH)+1, date.get(Calendar.DAY_OF_MONTH));

        if(events.isEmpty()){ //if no events were found for the given date...
            AlertDialog.Builder none = new AlertDialog.Builder(this);
            none.setMessage("Es wurden "+today+"keine Events gefunden.")//...tell the user that there are none
                    .setPositiveButton("Schade", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //Do nothing.
                        }
                    });
            none.show(); //This thing is fast, it'll propably show up even before the map is fully loaded.
        }
        //If we found events - display them on the map
        for(Integer ID : events){
            mMap.addMarker(new MarkerOptions().position(db.getEventGeoLoc(ID))
                                              .title(db.getEventName(ID)));
        }
        //If we're done, close the Db connection again.
        db.close();
    }

    private void changeDate(int year, int month, int day){
        date.set(year, month-1, day);
        today = ""; //If the date was changed, we suppose that it's not set to today anymore - even if it still is.
    }
}