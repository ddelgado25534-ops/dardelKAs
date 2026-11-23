package com.darieldelgadoo.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddProductActivity extends AppCompatActivity {

    private ImageView ivPreview;
    private Spinner spinnerCategory;
    private EditText etSku, etName, etDescription, etPrice, etQuantity, etMinThreshold;
    private File selectedImageFile = null;
    private final List<Category> categoryList = new ArrayList<>();
    private SessionManager sessionManager;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                    ivPreview.setImageBitmap(photo);
                    try {
                        selectedImageFile = FileUtil.getFileFromBitmap(this, photo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    ivPreview.setImageURI(uri);
                    try {
                        selectedImageFile = FileUtil.getFileFromUri(this, uri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        sessionManager = new SessionManager(this);

        ivPreview = findViewById(R.id.ivPreview);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        etSku = findViewById(R.id.etSku);
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etQuantity = findViewById(R.id.etQuantity);
        etMinThreshold = findViewById(R.id.etMinThreshold);

        MaterialButton btnCamera = findViewById(R.id.btnCamera);
        MaterialButton btnGallery = findViewById(R.id.btnGallery);
        MaterialButton btnSubmit = findViewById(R.id.btnSubmitProduct);

        btnCamera.setOnClickListener(v -> {
            Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(camIntent);
        });

        btnGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        loadCategories();

        btnSubmit.setOnClickListener(v -> submitProduct());
    }

    private void loadCategories() {
        RetrofitClient.getApiService().getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryList.clear();
                    categoryList.addAll(response.body());
                    ArrayAdapter<Category> adapter = new ArrayAdapter<>(
                            AddProductActivity.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            categoryList
                    );
                    spinnerCategory.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(AddProductActivity.this, "Could not load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitProduct() {
        if (categoryList.isEmpty()) {
            Toast.makeText(this, "Wait for categories to load", Toast.LENGTH_SHORT).show();
            return;
        }

        String sku = etSku.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String qty = etQuantity.getText().toString().trim();
        String minThresh = etMinThreshold.getText().toString().trim();

        if (sku.isEmpty() || name.isEmpty() || price.isEmpty() || qty.isEmpty() || minThresh.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Category selectedCat = (Category) spinnerCategory.getSelectedItem();

        RequestBody rbUserId = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(sessionManager.getUserId()));
        RequestBody rbPlatform = RequestBody.create(MediaType.parse("text/plain"), "MOBILE");
        RequestBody rbCatId = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(selectedCat.getId()));
        RequestBody rbSku = RequestBody.create(MediaType.parse("text/plain"), sku);
        RequestBody rbName = RequestBody.create(MediaType.parse("text/plain"), name);
        RequestBody rbDesc = RequestBody.create(MediaType.parse("text/plain"), desc);
        RequestBody rbPrice = RequestBody.create(MediaType.parse("text/plain"), price);
        RequestBody rbQty = RequestBody.create(MediaType.parse("text/plain"), qty);
        RequestBody rbMin = RequestBody.create(MediaType.parse("text/plain"), minThresh);

        MultipartBody.Part imagePart = null;
        if (selectedImageFile != null) {
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/jpeg"), selectedImageFile);
            imagePart = MultipartBody.Part.createFormData("image", selectedImageFile.getName(), reqFile);
        }

        RetrofitClient.getApiService().createProduct(
                rbUserId, rbPlatform, rbCatId, rbSku, rbName, rbDesc, rbPrice, rbQty, rbMin, imagePart
        ).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddProductActivity.this, "Product added & synced!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddProductActivity.this, "Upload error: HTTP " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(AddProductActivity.this, "Network failure: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}