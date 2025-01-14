package com.example.kisanbuddy;

public class HelperClass {
    private String cropName;
    private String quantity;
    private String productionCostPerKg;

    public HelperClass() {}

    public HelperClass(String cropName, String quantity, String productionCostPerKg) {
        this.cropName = cropName;
        this.quantity = quantity;
        this.productionCostPerKg = productionCostPerKg;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getProductionCostPerKg() {
        return productionCostPerKg;
    }

    public void setProductionCostPerKg(String productionCostPerKg) {
        this.productionCostPerKg = productionCostPerKg;
    }
}
