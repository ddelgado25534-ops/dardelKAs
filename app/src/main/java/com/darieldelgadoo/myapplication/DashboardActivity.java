package com.darieldelgadoo.myapplication;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView rvProducts;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvWelcome, tvStatsBar;
    private EditText etSearch;
    private ProductAdapter adapter;
    private final List<Product> productList = new ArrayList<>();
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        sessionManager = new SessionManager(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
        }

        tvWelcome = findViewById(R.id.tvWelcome);
        tvStatsBar = findViewById(R.id.tvStatsBar);
        etSearch = findViewById(R.id.etSearch);
        rvProducts = findViewById(R.id.rvProducts);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        ExtendedFloatingActionButton fabAdd = findViewById(R.id.fabAddProduct);

        String name = getIntent().getStringExtra("USER_NAME");
        String role = getIntent().getStringExtra("USER_ROLE");
        if (name == null) name = sessionManager.getFullName();
        if (role == null) role = sessionManager.getRole();

        String formattedRole = role.substring(0, 1).toUpperCase() + role.substring(1);
        tvWelcome.setText("Welcome, " + name + " (" + formattedRole + ")");

        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(this, productList, product -> {
            Intent intent = new Intent(DashboardActivity.this, ProductDetailActivity.class);
            intent.putExtra("PRODUCT_EXTRA", product);
            startActivity(intent);
        });
        rvProducts.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(() -> fetchProducts(etSearch.getText().toString().trim()));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                fetchProducts(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        fabAdd.setOnClickListener(v -> startActivity(new Intent(this, AddProductActivity.class)));

        btnLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchProducts(etSearch.getText().toString().trim());
    }

    private void fetchProducts(String keyword) {
        swipeRefresh.setRefreshing(true);
        String query = keyword.isEmpty() ? null : keyword;

        RetrofitClient.getApiService().getProducts(query, null).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    productList.clear();
                    productList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    long lowCount = 0;
                    for (Product p : productList) {
                        if (p.isLowStock()) lowCount++;
                    }
                    tvStatsBar.setText(productList.size() + " Active SKUs • " + lowCount + " Low-Stock Alerts");
                } else {
                    Toast.makeText(DashboardActivity.this, "Failed to load inventory", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(DashboardActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}