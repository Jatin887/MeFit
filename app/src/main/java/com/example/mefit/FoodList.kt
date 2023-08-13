package com.example.mefit

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mefit.adapter.AllFoodAdapter
import com.example.mefit.adapter.FoodAdapter
import com.example.mefit.databinding.ActivityFoodListBinding
import com.example.mefit.model.Food
import com.example.mefit.network.FoodApiService
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FoodList : AppCompatActivity() {

    private lateinit var binding: ActivityFoodListBinding
    private lateinit var sharedViewModel: FoodSharedViewModel

    private lateinit var foodApiService: FoodApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedViewModel = ViewModelProvider(this)[FoodSharedViewModel::class.java]

        sharedViewModel.foodList.observe(this) { foodList ->
            binding.foodRecyclerView.layoutManager = LinearLayoutManager(this)
            binding.foodRecyclerView.adapter = FoodAdapter(foodList, sharedViewModel)

        }

        //make api call from retrofit and show data
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.nal.usda.gov/fdc/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient())
            .build()

        foodApiService = retrofit.create(FoodApiService::class.java)

        fetchFoodList()

        sharedViewModel.allFoodList.observe(this){
            binding.allFoodListRecyclerView.layoutManager = LinearLayoutManager(this)
            binding.allFoodListRecyclerView.adapter = AllFoodAdapter(it, sharedViewModel)
        }





    }

    private fun fetchFoodList() {
        val apiKey = BuildConfig.API_KEY


        lifecycleScope.launch {
            try {
                val response = foodApiService.getFoodList(apiKey)
                if (response.isSuccessful) {
                    val foodList = response.body()
                    if (foodList != null) {
                        //change this to food list and then show in recycler view

                        for(food in foodList){
                            if(food.foodNutrients.size == 0 || food.description == null || food.fdcId == null) {
                                continue
                            }
                            Log.d("FoodList----", "fetchFoodList: ${food}")
                            val item = Food(food.fdcId,food.description, food.foodNutrients[0].amount.toInt())
                            Log.d("FoodList----", "fetchFoodList: ${item}")
                            sharedViewModel.addAllFood(item)
                        }


                    }
                } else {
                    // Handle error
                }
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }
}