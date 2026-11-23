package com.darieldelgadoo.myapplication;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Product implements Serializable {
    private int id;
    @SerializedName("category_id")
    private int categoryId;
    @SerializedName("category_name")
    private String categoryName;
    private String sku;
    private String name;
    private String description;
    @SerializedName("unit_price")
    private double unitPrice;
    private int quantity;
    @SerializedName("min_stock_threshold")
    private int minStockThreshold;
    @SerializedName("image_path")
    private String imagePath;
    @SerializedName("is_low_stock")
    private boolean isLowStock;

    public int getId() { return id; }
    public int getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName != null ? categoryName : "General"; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getMinStockThreshold() { return minStockThreshold; }
    public String getImagePath() { return imagePath; }
    public boolean isLowStock() {
        return isLowStock || (quantity < minStockThreshold);
    }
}