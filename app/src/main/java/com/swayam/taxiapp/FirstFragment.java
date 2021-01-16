package com.swayam.taxiapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FirstFragment extends Fragment
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String TAG = "FirstFragment";
    public static final int REQUEST_CODE = 1000;

    @BindView(R.id.desatination_text)
    EditText destEditText;
    @BindView(R.id.speed_text)
    EditText speedText;
    @BindView(R.id.meters_per_mile)
    EditText metersPerMileText;
    @BindView(R.id.remain_time_text)
    TextView remainingTimeText;
    @BindView(R.id.remain_distance_text)
    TextView remainingDistanceText;

    private Location currentLocation;
    private GoogleApiClient googleApiClient;

    private String destAddress;
    private int speed = 6;
    private int metersPerMile = 1609;
    private TaxiManager taxiManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, getView());

        taxiManager = new TaxiManager();

        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();


        speedText.setText(speed+"");
        metersPerMileText.setText(metersPerMile+"");

    }

    @Override
    public void onStart() {
        super.onStart();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @OnClick(R.id.find_button)
    public void onFindButtonClicked() {

        if (destEditText.getText().toString().equals("") || metersPerMileText.getText().toString().equals("") || speedText.getText().toString().equals("")) {
            Toast.makeText(getContext(), "fields can not be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            destAddress = destEditText.getText().toString();
            speed = Integer.parseInt(speedText.getText().toString());
            metersPerMile = Integer.parseInt(metersPerMileText.getText().toString());
        } catch (Exception e) {
            Toast.makeText(getContext(), "please enter correct number", Toast.LENGTH_SHORT).show();
            return;
        }

        int permission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
            return;
        }


        try {
            updateDestination(destAddress);
        } catch (Exception e) {
            Log.i(TAG, "onFindButtonClicked: " + e.toString());
            return;
        }

        FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
        currentLocation = fusedLocationProviderApi.getLastLocation(googleApiClient);

        onLocationChanged(currentLocation);


    }

    private void updateDestination(String destAddress) throws Exception{
        Geocoder geocoder = new Geocoder(getContext());
        List<Address> addresses = geocoder.getFromLocationName(destAddress, 4);

        if (addresses != null) {

            double latitude = addresses.get(0).getLatitude();
            double longitude = addresses.get(0).getLongitude();
            Location location = new Location("destAddress");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            taxiManager.setDestLocation(location);


        } else {
            Toast.makeText(getContext(), "no address found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "onRequestPermissionsResult: permission granted");
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
        @SuppressLint("RestrictedApi") LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (googleApiClient.isConnected()) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationProviderApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }else {
            googleApiClient.connect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getContext(), "check your location settings", Toast.LENGTH_SHORT).show();

        if (connectionResult.hasResolution()){
            try {
                connectionResult.startResolutionForResult(getActivity(), REQUEST_CODE);
            }catch (Exception e){

            }
        }else {
            Toast.makeText(getContext(), "google play services not working", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && requestCode == getActivity().RESULT_OK){
            googleApiClient.connect();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        if (!taxiManager.isDestLocationAvailable() && destAddress!=null && !destAddress.equals("")){
            try { updateDestination(destAddress); }catch (Exception e){}
        }
        if (currentLocation == null || !taxiManager.isDestLocationAvailable()) {
            Log.i(TAG, "onFindButtonClicked: something went wrong");
            return;
        }

        String time = taxiManager.getTimeForDestination(currentLocation, speed, metersPerMile);
        Log.i(TAG, "onLocationChanged: " + taxiManager.isDestLocationAvailable());
        remainingTimeText.setText(time);
        remainingDistanceText.setText("" + taxiManager.getMilesBetweenLocations(currentLocation, metersPerMile));
    }
}