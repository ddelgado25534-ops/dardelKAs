package com.darieldelgadoo.myapplication;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    private Product product;
    private int currentQuantity;
    private TextView tvDetailQuantity;
    private EditText etDetailName, etDetailPrice, etDetailMinThreshold;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        sessionManager = new SessionManager(this);
        product = (Product) getIntent().getSerializableExtra("PRODUCT_EXTRA");
        if (product == null) {
            finish();
            return;
        }

        ImageView ivImage = findViewById(R.id.ivDetailImage);
        TextView tvSku = findViewById(R.id.tvDetailSku);
        etDetailName = findViewById(R.id.etDetailName);
        etDetailPrice = findViewById(R.id.etDetailPrice);
        etDetailMinThreshold = findViewById(R.id.etDetailMinThreshold);
        tvDetailQuantity = findViewById(R.id.tvDetailQuantity);

        MaterialButton btnMinus = findViewById(R.id.btnMinusOne);
        MaterialButton btnPlus = findViewById(R.id.btnPlusOne);
        MaterialButton btnSave = findViewById(R.id.btnSaveChanges);
        MaterialButton btnDelete = findViewById(R.id.btnDeleteProduct);

        currentQuantity = product.getQuantity();
        tvSku.setText("SKU: " + product.getSku() + " • " + product.getCategoryName());
        etDetailName.setText(product.getName());
        etDetailPrice.setText(String.valueOf(product.getUnitPrice()));
        etDetailMinThreshold.setText(String.valueOf(product.getMinStockThreshold()));
        tvDetailQuantity.setText(String.valueOf(currentQuantity));

        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            Picasso.get()
                    .load(RetrofitClient.BASE_URL + product.getImagePath())
                    .placeholder(R.drawable.ic_placeholder_box)
                    .error(R.drawable.ic_placeholder_box)
                    .into(ivImage);
        }

        btnMinus.setOnClickListener(v -> {
            if (currentQuantity > 0) {
                currentQuantity--;
                tvDetailQuantity.setText(String.valueOf(currentQuantity));
                sendPutUpdate("STOCK_OUT", -1);
            }
        });

        btnPlus.setOnClickListener(v -> {
            currentQuantity++;
            tvDetailQuantity.setText(String.valueOf(currentQuantity));
            sendPutUpdate("STOCK_IN", 1);
        });

        btnSave.setOnClickListener(v -> sendPutUpdate("UPDATED", 0));

        btnDelete.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Permanently remove " + product.getName() + " from database and server storage?")
                .setPositiveButton("Delete", (dialog, which) -> deleteProduct())
                .setNegativeButton("Cancel", null)
                .show());
    }

    private void sendPutUpdate(String actionType, int delta) {
        String updatedName = etDetailName.getText().toString().trim();
        double updatedPrice = Double.parseDouble(etDetailPrice.getText().toString().trim());
        int updatedMinThresh = Integer.parseInt(etDetailMinThreshold.getText().toString().trim());

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", product.getId());
        payload.put("user_id", sessionManager.getUserId());
        payload.put("platform", "MOBILE");
        payload.put("action_type", actionType);
        payload.put("quantity_changed", delta);
        payload.put("name", updatedName);
        payload.put("unit_price", updatedPrice);
        payload.put("quantity", currentQuantity);
        payload.put("min_stock_threshold", updatedMinThresh);

        RetrofitClient.getApiService().updateProduct(payload).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ProductDetailActivity.this, "Stock synced with MySQL", Toast.LENGTH_SHORT).show();

                    if (response.body().isLowStock() || currentQuantity < updatedMinThresh) {
                        AlertHelper.triggerLowStockAlert(
                                ProductDetailActivity.this,
                                updatedName,
                                currentQuantity,
                                updatedMinThresh
                        );
                    }
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Update failed: HTTP " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(ProductDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteProduct() {
        RetrofitClient.getApiService().deleteProduct(
                product.getId(),
                sessionManager.getUserId(),
                "MOBILE"
        ).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProductDetailActivity.this, "Product deleted", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Delete failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(ProductDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}