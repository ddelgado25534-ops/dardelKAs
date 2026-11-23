package com.darieldelgadoo.myapplication;

import com.google.gson.annotations.SerializedName;

public class ApiResponse {
    private String status;
    private String message;
    @SerializedName("is_low_stock")
    private boolean isLowStock;
    private Product product;

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public boolean isLowStock() { return isLowStock; }
    public Product getProduct() { return product; }
}