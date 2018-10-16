package com.map.mapapp;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.DecimalFormat;

import static constants.AppConstants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;

public class MapDistanceActivity extends FragmentActivity implements OnMapReadyCallback{

    private static final float DEFAULT_ZOOM = 6;
    private GoogleMap map;
    private boolean mLocationPermissionGranted;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private LatLng mDefaultLocation;
    private LatLng actualLocation;
    private LatLng fort=new LatLng(25.4579,78.5756);
    private LatLng palace=new LatLng(26.2313, 78.1695);
    private LinearLayout fortLayout,palaceLayout;
    private String FORT="fort";
    private String PALACE="palace";
    private Polyline lineFortToYourLoc;
    private Polyline linePalaceToYourLoc;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distance);
        fortLayout=findViewById(R.id.jhansi_fort);
        palaceLayout=findViewById(R.id.palace_jhansi_rani);
        getLocationPermission();
        SupportMapFragment mapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_frame);
        mapFragment.getMapAsync(this);

        mDefaultLocation=new LatLng(25.4596, 78.5771);

        fortLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                calculateDistanceAndDrawLine(FORT);

            }
        });

        palaceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calculateDistanceAndDrawLine(PALACE);
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map=googleMap;
        updateLocationUI();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }


    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
             mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
                // Construct a FusedLocationProviderClient.
                mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                getDeviceLocation();
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = (Location) task.getResult();
                            actualLocation=new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude());
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            map.addMarker(new MarkerOptions().position(actualLocation).title(getString(R.string.your_location)));
                        } else {
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void calculateDistanceAndDrawLine(String destination)
    {
        switch (destination)
        {
            case "fort":
                if(actualLocation!=null)
                {
                    double dist=calculateDistance(actualLocation,fort);
                     lineFortToYourLoc=map.addPolyline(new PolylineOptions().clickable(true)
                            .add(fort,actualLocation));
                    map.addMarker(new MarkerOptions().position(fort).title(getString(R.string.your_location)
                            +" "+dist+"kms from Jhansi fort"));

                    if(linePalaceToYourLoc!=null)
                    {
                        linePalaceToYourLoc.remove();
                    }



                }
                break;

            case "palace":
                if(actualLocation!=null)
                {
                    double dist=calculateDistance(actualLocation,palace);
                     linePalaceToYourLoc=map.addPolyline(new PolylineOptions().clickable(true)
                            .add(actualLocation,palace));
                    map.addMarker(new MarkerOptions().position(palace).title(getString(R.string.your_location)
                            +" "+dist+"kms from Gwalior fort"));

                    if(lineFortToYourLoc!=null)
                    {
                        lineFortToYourLoc.remove();
                    }
                }
                break;
        }

    }

    private double calculateDistance(LatLng startP,LatLng endP)
    {
        int Radius = 6371;// radius of earth in Km
        double lat1 = startP.latitude;
        double lat2 = endP.latitude;
        double lon1 = startP.longitude;
        double lon2 = endP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }
}
