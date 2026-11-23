package com.darieldelgadoo.myapplication;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {

    @FormUrlEncoded
    @POST("api/login.php")
    Call<LoginResponse> loginUser(
            @Field("email") String email,
            @Field("password") String password
    );

    @GET("api/products.php")
    Call<List<Product>> getProducts(
            @Query("search") String search,
            @Query("category_id") Integer categoryId
    );

    @GET("api/categories.php")
    Call<List<Category>> getCategories();

    @Multipart
    @POST("api/products.php")
    Call<ApiResponse> createProduct(
            @Part("user_id") RequestBody userId,
            @Part("platform") RequestBody platform,
            @Part("category_id") RequestBody categoryId,
            @Part("sku") RequestBody sku,
            @Part("name") RequestBody name,
            @Part("description") RequestBody description,
            @Part("unit_price") RequestBody unitPrice,
            @Part("quantity") RequestBody quantity,
            @Part("min_stock_threshold") RequestBody minStockThreshold,
            @Part MultipartBody.Part image
    );

    @PUT("api/products.php")
    Call<ApiResponse> updateProduct(@Body Map<String, Object> payload);

    @DELETE("api/products.php")
    Call<ApiResponse> deleteProduct(
            @Query("id") int productId,
            @Query("user_id") int userId,
            @Query("platform") String platform
    );
}