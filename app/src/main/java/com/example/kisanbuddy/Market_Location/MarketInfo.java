
package com.example.kisanbuddy.Market_Location;

public class MarketInfo {
    private String name;
    private String vicinity;
    private double latitude;
    private double longitude;
    private String reference;

    public MarketInfo(String name, String vicinity, double latitude, double longitude, String reference) {
        this.name = name;
        this.vicinity = vicinity;
        this.latitude = latitude;
        this.longitude = longitude;
        this.reference = reference;
    }

    // Getters
    public String getName() { return name; }
    public String getVicinity() { return vicinity; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getReference() { return reference; }
}




