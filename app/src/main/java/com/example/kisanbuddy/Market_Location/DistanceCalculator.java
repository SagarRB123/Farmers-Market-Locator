package com.example.kisanbuddy.Market_Location;

import android.location.Location;
import android.util.Log;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DistanceCalculator {
    private static final String TAG = "DistanceCalculator";
    private DatabaseReference nearbySupermarketsRef;
    private Location userLocation;

    public DistanceCalculator() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        nearbySupermarketsRef = database.getReference("nearbySupermarkets");
    }

    public void calculateAndUpdateDistances(Location currentLocation) {
        if (currentLocation == null) {
            Log.e(TAG, "User location is null");
            return;
        }

        this.userLocation = currentLocation;

        nearbySupermarketsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot supermarketSnapshot : dataSnapshot.getChildren()) {
                    try {
                        // Get latitude and longitude from Firebase
                        double marketLat = supermarketSnapshot.child("latitude").getValue(Double.class);
                        double marketLng = supermarketSnapshot.child("longitude").getValue(Double.class);

                        // Calculate distance
                        Location marketLocation = new Location("");
                        marketLocation.setLatitude(marketLat);
                        marketLocation.setLongitude(marketLng);

                        float distanceInMeters = userLocation.distanceTo(marketLocation);
                        double distanceInKm = distanceInMeters / 1000.0; // Convert to kilometers

                        // Round to 2 decimal places
                        double roundedDistance = Math.round(distanceInKm * 100.0) / 100.0;

                        // Update distance in Firebase
                        supermarketSnapshot.getRef().child("distance").setValue(roundedDistance);

                        Log.d(TAG, "Updated distance for supermarket: " + supermarketSnapshot.child("name").getValue(String.class) + " - Distance: " + roundedDistance + " km");

                    } catch (Exception e) {
                        Log.e(TAG, "Error calculating distance for supermarket: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });
    }
}