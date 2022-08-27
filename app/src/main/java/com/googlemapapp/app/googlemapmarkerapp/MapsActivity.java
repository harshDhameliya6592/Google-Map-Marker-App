package com.googlemapapp.app.googlemapmarkerapp;

import static com.googlemapapp.app.googlemapmarkerapp.Utility.decodePoly;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.ButtCap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.googlemapapp.app.googlemapmarkerapp.databinding.ActivityMapsBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    Geocoder geocoder;

    //Required_permissions
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"};
    private final int REQUEST_CODE_PERMISSIONS = 1001;

    //Location
    ArrayList<LatLng> latLngArrayList;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    Marker userLocationMarker;
    ApiInterface apiInterface;
    ArrayList<LatLng> apiLatLngList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        geocoder = new Geocoder(this);

        Retrofit retrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl("https://maps.googleapis.com/")
                .build();

        apiInterface = retrofit.create(ApiInterface.class);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        latLngArrayList = new ArrayList<>();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (mMap != null) {
                    setUserLocationMarker(Objects.requireNonNull(locationResult.getLastLocation()));
                }
            }
        };
    }

    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
               /* getLastLocation();
                checkSettingAndStartLocationUpdate();
                enableUserLocation();
                zoomToUserLocation();*/
//                enableUserLocation();
                checkSettingAndStartLocationUpdate();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerDragListener(this);

        //this for location name to LatLng
        /*try {
            List<Address> addresses = geocoder.getFromLocationName("Surat", 1);
//            Utility.PrintLog("Address",addresses.get(0).toString());
            LatLng latLng = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());

            MarkerOptions markerOptions = new MarkerOptions().draggable(true).position(latLng)
                    .title("Silver Business Point").snippet("It Park");
            mMap.addMarker(markerOptions);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
            mMap.animateCamera(cameraUpdate);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//           enableUserLocation();
            checkSettingAndStartLocationUpdate();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    public void onMarkerDrag(@NonNull Marker marker) {
//        Utility.PrintLog("onMarkerDrag","onMarkerDrag");
    }

    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {
        /*Utility.PrintLog("onMarkerDragEnd","onMarkerDragEnd");
        LatLng latLng = marker.getPosition();
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if (addresses.size() <=0){
                Toast.makeText(MapsActivity.this,"Address not detected",Toast.LENGTH_SHORT).show();
            }else{
                marker.setTitle(addresses.get(0).getAddressLine(0));
            }
        }catch (Exception e){
            Utility.PrintLog("Error in onMarkerDragEnd",e.getMessage());
        }*/
    }

    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {
//        /**/Utility.PrintLog("onMarkerDragStart","onMarkerDragStart");
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
       /* Utility.PrintLog("onMapLongClick", latLng.latitude + " Latitude " + " Longitude" + latLng.longitude);
        mMap.clear();
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if (addresses.size() <=0){
                Toast.makeText(MapsActivity.this,"Address not detected",Toast.LENGTH_SHORT).show();
            }else{
                mMap.addMarker(new MarkerOptions().draggable(true).position(latLng).title(addresses.get(0)
                        .getLocality()).snippet(addresses.get(0).getAddressLine(0)));
            }

        }catch (Exception e){
            Utility.PrintLog("Error in onMapLongClick",e.getMessage());
        }*/
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return;
        }
        mMap.setMyLocationEnabled(true);

    }

    private void checkSettingAndStartLocationUpdate() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();
        SettingsClient client = LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);

        locationSettingsResponseTask.addOnSuccessListener(locationSettingsResponse -> {
            //setting of device are satisfied and we can start location updates
            enableUserLocation();
            zoomToUserLocation();
//            startLocationUpdate();

        }).addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                ResolvableApiException apiException = (ResolvableApiException) e;
                try {
                    apiException.startResolutionForResult(MapsActivity.this, 1000);
                } catch (IntentSender.SendIntentException ex) {
                    ex.printStackTrace();
                    Utility.PrintLog("Error in checkSettingAndStartLocationUpdate", ex.getMessage());
                }
            }
        });
    }

    private void getDirection(String origin, String destination) {

        apiInterface.getDirection("driving", "less_driving", origin, destination,
                        "AIzaSyDqxjK-8RC3D316zSgdDLWHdNZevj4dYSY").subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<com.googlemapapp.app.googlemapmarkerapp.Result>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.e("onSubscribe", "onSubscribe" + d.isDisposed());
//                        if (!d.isDisposed()){
//                            getDirection("21.231681" + "," + "72.866267", "21.237464" + "," + "72.877420");
//                        }
                    }

                    @Override
                    public void onSuccess(com.googlemapapp.app.googlemapmarkerapp.Result result) {
                        Log.e("Susss", "SSS");
                        apiLatLngList = new ArrayList<>();
                        List<Route> routes = result.getRoutes();
                        Utility.PrintLog("RouteSize", "" + routes.size());
                        for (Route route : routes) {
                            String polyline = route.getOverview_polyline().getPoints();
                            apiLatLngList.addAll(decodePoly(polyline));
                        }
                        if (apiLatLngList != null) {
                            Log.e("onSuccess", "apiLatLngList != null");
                            startLocationUpdate();
                        } else {
                            Log.e("onSuccess", "apiLatLngList != null" + "else");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("OnError:-", e.getMessage());
                    }
                });
    }

    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Utility.PrintLog("Error in startLocationUpdate ", "and checkSelfPermission");
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void setUserLocationMarker(Location location) {
        Log.e("setUserLocationMarker", "setUserLocationMarker start");
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        latLngArrayList.add(latLng);

        if (userLocationMarker == null) {
            //Create a new marker
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            if (location.getBearing() >= 260.0F) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.b2));
                markerOptions.rotation(90F);
            } else {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.a));
                markerOptions.rotation(location.getBearing());
            }
//            Utility.PrintLog("Rotation",location.getBearing()+"");
            markerOptions.anchor(0.5f, 0.5f);
            userLocationMarker = mMap.addMarker(markerOptions);
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
        } else {
            //use the previously created marker
            userLocationMarker.setPosition(latLng);
            if (location.getBearing() >= 260.0F) {
                userLocationMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.b2));
                userLocationMarker.setRotation(90F);
            } else {
                userLocationMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.a));
                userLocationMarker.setRotation(location.getBearing());
            }
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
        }
        Utility.PrintLog("Rotation", location.getBearing() + "");
        addPolyline();
    }

    private void addPolyline() {
        Log.e("addPolyline", "addPolyline start");
        if (apiLatLngList != null) {
            Log.e("apiLatLngList", "apiLatLngList before remove size" + apiLatLngList.size());
            Log.e("apiLatLngListBefore", "apiLatLngList before remove size" + apiLatLngList+"<-:and:->"+latLngArrayList);
            apiLatLngList.removeAll(latLngArrayList);
            Log.e("apiLatLngListAfter", "apiLatLngList after remove size" + apiLatLngList);
            Log.e("apiLatLngList", "apiLatLngList after remove size" + apiLatLngList.size());

            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.color(ContextCompat.getColor(getApplicationContext(), R.color.purple_200));
            polylineOptions.jointType(JointType.ROUND);
            polylineOptions.width(15);
            polylineOptions.addAll(apiLatLngList);
            mMap.addPolyline(polylineOptions);

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(latLngArrayList.get(0));
            builder.include(apiLatLngList.get(apiLatLngList.size() - 1));
//        mMap.clear();
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),2000,2000,0));

        } else {
            Toast.makeText(MapsActivity.this, "Sorry data not get", Toast.LENGTH_LONG).show();
        }
    }

    private void stopLocationUpdate() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void zoomToUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(location -> {
            if (location == null) {
                Utility.PrintLog("Location is Null", "");
                zoomToUserLocation();
            } else {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                getDirection(location.getLatitude() + "," + location.getLongitude(), "21.237464" + "," + "72.877420");
//                startLocationUpdate();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            }
        }).addOnFailureListener(e -> {
            Utility.PrintLog("Error in zoomToUserLocation", e.getMessage());
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*if (allPermissionsGranted()) {
//            checkSettingAndStartLocationUpdate();
//            zoomToUserLocation();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }*/
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (resultCode == RESULT_OK) {
                Utility.PrintLog("GPS", "is on");
                checkSettingAndStartLocationUpdate();
            } else {
                Toast.makeText(MapsActivity.this, "Plase On Gps", Toast.LENGTH_LONG).show();
            }
        }
    }
}