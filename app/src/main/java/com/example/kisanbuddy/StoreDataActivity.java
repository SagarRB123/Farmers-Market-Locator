package com.example.kisanbuddy;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StoreDataActivity extends AppCompatActivity {

    EditText cropName, quantity, productionCost;
    Button continueButton;
    FirebaseDatabase database;
    DatabaseReference farmersDataRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_data);

        // Initialize UI elements
        cropName = findViewById(R.id.crop_name);
        quantity = findViewById(R.id.quantity);
        productionCost = findViewById(R.id.cost);
        continueButton = findViewById(R.id.continue_button);

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance();
        farmersDataRef = database.getReference("farmers_data");

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get input values
                String crop = cropName.getText().toString().trim();
                String qty = quantity.getText().toString().trim();
                String cost = productionCost.getText().toString().trim();

                // Validate inputs
                if (crop.isEmpty() || qty.isEmpty() || cost.isEmpty()) {
                    Toast.makeText(StoreDataActivity.this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Clear all old crop data
                farmersDataRef.removeValue()
                        .addOnSuccessListener(aVoid -> {
                            // Save new crop data
                            saveNewCropData(crop, qty, cost);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(StoreDataActivity.this, "Failed to clear old data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    private void saveNewCropData(String crop, String qty, String cost) {
        // Create farmer data using HelperClass
        HelperClass helperClass = new HelperClass(crop, qty, cost);

        // Use crop name as the unique key
        farmersDataRef.child(crop).setValue(helperClass)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(StoreDataActivity.this, "Data saved successfully!", Toast.LENGTH_SHORT).show();

                    // Clear input fields
                    cropName.setText("");
                    quantity.setText("");
                    productionCost.setText("");

                    // Redirect to MainActivity
                    Intent intent = new Intent(StoreDataActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(StoreDataActivity.this, "Failed to save data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
