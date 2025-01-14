package com.example.kisanbuddy.Market_Location;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NearbyPlaces extends AsyncTask<Object, String, String> {
    private String googleplaceData;
    private GoogleMap mMap;
    private String url;
    private List<MarketInfo> marketsList;
    private MarketAdapter marketAdapter;
    private static final String TAG = "NearbyPlaces";
    private FirebaseDatabase database;
    private DatabaseReference nearbySupermarketsRef;

    LatLng firstMarketLocation = null;
    Location userLocation = null;

    public NearbyPlaces(List<MarketInfo> marketsList, MarketAdapter marketAdapter) {
        this.marketsList = marketsList;
        this.marketAdapter = marketAdapter;
        this.database = FirebaseDatabase.getInstance();
        this.nearbySupermarketsRef = database.getReference("nearbySupermarkets");
    }

    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap) objects[0];
        url = (String) objects[1];

        DataDownloader dataDownloader = new DataDownloader();
        try {
            googleplaceData = dataDownloader.ReadTheURL(url);
        } catch (IOException e) {
            Log.e(TAG, "Error downloading URL: " + e.getMessage());
            return null;
        }

        return googleplaceData;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result == null) {
            Log.e(TAG, "Failed to get places data");
            return;
        }

        List<HashMap<String, String>> nearByPlacesList = null;
        DataConverter dataConverter = new DataConverter();
        nearByPlacesList = dataConverter.parse(result);

        if (nearByPlacesList == null) {
            Log.e(TAG, "Failed to parse places data");
            return;
        }

        DisplayNearbyPlaces(nearByPlacesList);
    }

    private void DisplayNearbyPlaces(List<HashMap<String, String>> nearByPlacesList) {
        marketsList.clear();
        mMap.clear();

        for (int i = 0; i < nearByPlacesList.size(); i++) {
            HashMap<String, String> googleNearbyPlace = nearByPlacesList.get(i);

            String placeName = googleNearbyPlace.get("place_name");
            String vicinity = googleNearbyPlace.get("vicinity");
            String reference = googleNearbyPlace.get("reference");

            double lat = 0;
            double lng = 0;
            try {
                lat = Double.parseDouble(googleNearbyPlace.get("lat"));
                lng = Double.parseDouble(googleNearbyPlace.get("lng"));
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing coordinates for place: " + placeName);
                continue;
            }

            if (i == 0) {
                firstMarketLocation = new LatLng(lat, lng);
            }

            // Calculate distance from user's location if available
            float distance = -1;
            if (userLocation != null) {
                Location marketLocation = new Location("");
                marketLocation.setLatitude(lat);
                marketLocation.setLongitude(lng);
                distance = userLocation.distanceTo(marketLocation) / 1000; // Convert to kilometers
            }

            final double finalLat = lat;
            final double finalLng = lng;
            final float finalDistance = distance;

            // First check if this market already exists in Firebase
            nearbySupermarketsRef.orderByChild("reference").equalTo(reference)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Map<String, Object> marketData = new HashMap<>();
                            marketData.put("name", placeName);
                            marketData.put("vicinity", vicinity);
                            marketData.put("latitude", finalLat);
                            marketData.put("longitude", finalLng);
                            marketData.put("reference", reference);

                            if (finalDistance >= 0) {
                                marketData.put("distance", String.format("%.2f", finalDistance));
                            }

                            if (!dataSnapshot.exists()) {
                                // Market doesn't exist, create new entry
                                String marketId = nearbySupermarketsRef.push().getKey();
                                if (marketId != null) {
                                    nearbySupermarketsRef.child(marketId).setValue(marketData);
                                }
                            } else {
                                // Market exists, update existing entry while preserving distance
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    // If there's an existing distance, preserve it
                                    if (snapshot.hasChild("distance")) {
                                        marketData.put("distance", snapshot.child("distance").getValue());
                                    }
                                    snapshot.getRef().updateChildren(marketData);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(TAG, "Database error: " + databaseError.getMessage());
                        }
                    });

            // Add marker to map
            LatLng latLng = new LatLng(lat, lng);
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(placeName)
                    .snippet(vicinity)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

            mMap.addMarker(markerOptions);

            MarketInfo marketInfo = new MarketInfo(
                    placeName,
                    vicinity,
                    lat,
                    lng,
                    reference
            );

            marketsList.add(marketInfo);
        }

        if (marketAdapter != null) {
            marketAdapter.notifyDataSetChanged();
        }

        if (firstMarketLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(firstMarketLocation));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
        }
    }

    public void setUserLocation(Location location) {
        if (location != null) {
            this.userLocation = location;
            // Calculate and update distances for all markets
            updateDistancesForAllMarkets();
        }
    }

    private void updateDistancesForAllMarkets() {
        nearbySupermarketsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot marketSnapshot : dataSnapshot.getChildren()) {
                    try {
                        double lat = marketSnapshot.child("latitude").getValue(Double.class);
                        double lng = marketSnapshot.child("longitude").getValue(Double.class);

                        Location marketLocation = new Location("");
                        marketLocation.setLatitude(lat);
                        marketLocation.setLongitude(lng);

                        float distanceInMeters = userLocation.distanceTo(marketLocation);
                        double distanceInKm = Math.round((distanceInMeters / 1000.0) * 100.0) / 100.0;

                        marketSnapshot.getRef().child("distance").setValue(distanceInKm);
                    } catch (Exception e) {
                        Log.e(TAG, "Error updating distance: " + e.getMessage());
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