package com.example.kisanbuddy;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.database.*;
import java.util.HashMap;
import java.util.Map;

public class FarmerDashboardActivity extends AppCompatActivity {
    private static final String TAG = "FarmerDashboard";
    private static final double COST_PER_KM = 11.0; // rupees per km

    private TextView marketNameText, vicinityText, distanceText;
    private TextView marketPriceText, transportCostText, totalCostText;
    private CardView bestMarketCard;

    private DatabaseReference farmersDataRef;
    private DatabaseReference supermarketsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_dashboard);

        // Initialize views
        initializeViews();

        // Initialize Firebase references
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        farmersDataRef = database.getReference("farmers_data");
        supermarketsRef = database.getReference("nearbySupermarkets");

        // Start calculation process
        calculateBestMarket();
    }

    private void initializeViews() {
        marketNameText = findViewById(R.id.market_name);
        vicinityText = findViewById(R.id.market_vicinity);
        distanceText = findViewById(R.id.distance_text);
        marketPriceText = findViewById(R.id.market_price);
        transportCostText = findViewById(R.id.transport_cost);
        totalCostText = findViewById(R.id.total_cost);
        bestMarketCard = findViewById(R.id.best_market_card);
    }

    private void calculateBestMarket() {
        farmersDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot cropDataSnapshot) {
                // Get crop data
                double totalQuantity = 0;
                double productionCostPerKg = 0;

                for (DataSnapshot crop : cropDataSnapshot.getChildren()) {
                    try {
                        totalQuantity = Double.parseDouble(crop.child("quantity").getValue(String.class));
                        productionCostPerKg = Double.parseDouble(crop.child("productionCostPerKg").getValue(String.class));
                        break; // Get first crop data
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing crop data: " + e.getMessage());
                    }
                }

                final double finalQuantity = totalQuantity;
                final double finalProductionCostPerKg = productionCostPerKg;

                // Calculate market with lowest transaction cost
                supermarketsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot marketsSnapshot) {
                        double lowestTotalCost = Double.MAX_VALUE;
                        Map<String, Object> bestMarket = new HashMap<>();

                        for (DataSnapshot market : marketsSnapshot.getChildren()) {
                            try {
                                double distance = market.child("distance").getValue(Double.class);
                                String name = market.child("name").getValue(String.class);
                                String vicinity = market.child("vicinity").getValue(String.class);

                                // Calculate costs
                                double marketPrice = finalQuantity * finalProductionCostPerKg;
                                double transportCost = distance * COST_PER_KM * finalQuantity;
                                double totalCost = marketPrice + transportCost;

                                if (totalCost < lowestTotalCost) {
                                    lowestTotalCost = totalCost;
                                    bestMarket.put("name", name);
                                    bestMarket.put("vicinity", vicinity);
                                    bestMarket.put("distance", distance);
                                    bestMarket.put("marketPrice", marketPrice);
                                    bestMarket.put("transportCost", transportCost);
                                    bestMarket.put("totalCost", totalCost);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error calculating costs: " + e.getMessage());
                            }
                        }

                        // Update UI with best market info
                        updateUI(bestMarket);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Database error: " + databaseError.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void updateUI(Map<String, Object> bestMarket) {
        marketNameText.setText(String.valueOf(bestMarket.get("name")));
        vicinityText.setText(String.valueOf(bestMarket.get("vicinity")));
        distanceText.setText(String.format("%.2f km", (double) bestMarket.get("distance")));

        double marketPrice = (double) bestMarket.get("marketPrice");
        double transportCost = (double) bestMarket.get("transportCost");
        double totalCost = (double) bestMarket.get("totalCost");

        marketPriceText.setText(String.format("₹%.2f", marketPrice));
        transportCostText.setText(String.format("₹%.2f", transportCost));
        totalCostText.setText(String.format("₹%.2f", totalCost));
    }
}