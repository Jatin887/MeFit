package com.example.mefit.network

import com.example.mefit.model.FoodApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface FoodApiService {

    @GET("v1/foods/list")
    suspend fun getFoodList(@Query("api_key") apiKey: String): Response<FoodApiResponse>
}
