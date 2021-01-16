package com.swayam.taxiapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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
import com.google.android.gms.location.LocationServices;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FirstFragment extends Fragment
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,getView());

        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

    }

    @Override
    public void onStart() {
        super.onStart();

        if (googleApiClient != null){
            googleApiClient.connect();
        }
    }

    @OnClick(R.id.find_button)
    public void onFindButtonClicked(){
        int permission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permission != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
            return;
        }

        FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
        currentLocation = fusedLocationProviderApi.getLastLocation(googleApiClient);

        if (currentLocation == null){
            Log.i(TAG, "onFindButtonClicked: something went wrong");
            return;
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Log.i(TAG, "onRequestPermissionsResult: permission granted");
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

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
}