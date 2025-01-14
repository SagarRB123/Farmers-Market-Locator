package com.example.kisanbuddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kisanbuddy.Market_Location.GoogleMapActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private TextView cropNameText, quantityText, productionCostText;
    private Button showMarketsButton;
    private CardView cropDetailsCard;
    private DatabaseReference farmersDataRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
        cropNameText = findViewById(R.id.crop_name_text);
        quantityText = findViewById(R.id.quantity_text);
        productionCostText = findViewById(R.id.production_cost_text);
        showMarketsButton = findViewById(R.id.show_markets_button);
        cropDetailsCard = findViewById(R.id.crop_details_card);

        // Initialize Firebase
        farmersDataRef = FirebaseDatabase.getInstance().getReference("farmers_data");

        // Fetch and display crop details
        fetchCropDetails();

        // Set up button click listener
        showMarketsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToMaps();
            }
        });
    }

    private void fetchCropDetails() {
        farmersDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot cropSnapshot : dataSnapshot.getChildren()) {
                        HelperClass cropData = cropSnapshot.getValue(HelperClass.class);
                        if (cropData != null) {
                            // Update UI with crop details
                            cropNameText.setText("Crop: " + cropData.getCropName());
                            quantityText.setText("Quantity: " + cropData.getQuantity() + " kg");
                            productionCostText.setText("Production Cost: ₹" + cropData.getProductionCostPerKg() + "/kg");

                            // Make card visible
                            cropDetailsCard.setVisibility(View.VISIBLE);
                            return; // Exit after getting first crop data
                        }
                    }
                } else {
                    // No data found
                    Toast.makeText(MainActivity.this, "No crop details found. Please add crop details first.", Toast.LENGTH_LONG).show();
                    // Navigate to StoreDataActivity
                    startActivity(new Intent(MainActivity.this, StoreDataActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToMaps() {
        Intent intent = new Intent(this, GoogleMapActivity.class);
        startActivity(intent);
    }
}