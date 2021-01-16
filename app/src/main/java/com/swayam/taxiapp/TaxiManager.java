package com.swayam.taxiapp;

import android.location.Location;

public class TaxiManager {
    private Location destLocation = null;

    public void setDestLocation(Location destLocation) {
        this.destLocation = destLocation;
    }

    public float getDistanceToDestLocation(Location location){
        if (location != null && destLocation!=null){
            return location.distanceTo(destLocation);
        }else {
            return -1f;
        }
    }

    public String getMilesBetweenLocations(Location location,int metersInMiles){
        int miles = (int) (getDistanceToDestLocation(location) / metersInMiles);
        return (miles == 1) ? "1 mile" : (miles > 1) ? miles + " miles" : "no miles";
    }

    public String getTimeForDestination(Location location, float milesPerHour, int meterPerMile){
        float distanceInMeters = getDistanceToDestLocation(location);
        float timeLeft = distanceInMeters / (milesPerHour * meterPerMile);

        String result = "";

        int timeLeftInHours = (int) timeLeft;
        result += (timeLeftInHours == 1) ? "1 hour" : (timeLeftInHours > 1) ? timeLeftInHours + " hours" : "";

        int timeLeftInMinutes = (int) ((timeLeft - timeLeftInHours) * 60);
        String minutesInString = ((timeLeftInMinutes == 1) ? "1 minute" : (timeLeftInMinutes > 1) ? timeLeftInMinutes + " minutes" : "");
        result += (timeLeftInHours == 0) ? minutesInString : " " + minutesInString ;

        return result;
    }

    public boolean isDestLocationAvailable(){
        return destLocation != null;
    }
}
