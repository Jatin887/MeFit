package com.example.mefit


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

        //get the arguments from the intent
        val type = intent.getStringExtra("type")
        sharedViewModel.type.postValue(type)
        sharedViewModel.updateAllFoodLists()
        sharedViewModel.isLoading.postValue(true)
        
        sharedViewModel.isLoading.observe(this) {
            if(it){
                binding.progressBarAddFood.visibility = android.view.View.VISIBLE
            }else{
                binding.progressBarAddFood.visibility = android.view.View.GONE
            }
        }

        if(type=="breakfast"){
            sharedViewModel.breakfastList.observe(this) {
                binding.foodRecyclerView.layoutManager = LinearLayoutManager(this)
                binding.foodRecyclerView.adapter = FoodAdapter(it, sharedViewModel)
                sharedViewModel.foodList.postValue(it)
                if(it.isNotEmpty()){
                    binding.foodListPlaceholderText.visibility = android.view.View.GONE
                    binding.foodRecyclerView.visibility = android.view.View.VISIBLE
                }else{
                    binding.foodListPlaceholderText.visibility = android.view.View.VISIBLE
                    binding.foodRecyclerView.visibility = android.view.View.GONE
                }
            }
        }
        else if(type=="lunch"){
            sharedViewModel.lunchList.observe(this) {
                binding.foodRecyclerView.layoutManager = LinearLayoutManager(this)
                binding.foodRecyclerView.adapter = FoodAdapter(it, sharedViewModel)
                sharedViewModel.foodList.postValue(it)

                if(it.isNotEmpty()){
                    binding.foodListPlaceholderText.visibility = android.view.View.GONE
                    binding.foodRecyclerView.visibility = android.view.View.VISIBLE
                }else{
                    binding.foodListPlaceholderText.visibility = android.view.View.VISIBLE
                    binding.foodRecyclerView.visibility = android.view.View.GONE
                }
            }


        }
        else if(type=="snacks"){
            sharedViewModel.snacksList.observe(this) {
                binding.foodRecyclerView.layoutManager = LinearLayoutManager(this)
                binding.foodRecyclerView.adapter = FoodAdapter(it, sharedViewModel)
                sharedViewModel.foodList.postValue(it)
                if(it.isNotEmpty()){
                    binding.foodListPlaceholderText.visibility = android.view.View.GONE
                    binding.foodRecyclerView.visibility = android.view.View.VISIBLE
                }else{
                    binding.foodListPlaceholderText.visibility = android.view.View.VISIBLE
                    binding.foodRecyclerView.visibility = android.view.View.GONE
                }
            }
        }
        else if(type=="dinner"){
            sharedViewModel.dinnerList.observe(this) {
                binding.foodRecyclerView.layoutManager = LinearLayoutManager(this)
                binding.foodRecyclerView.adapter = FoodAdapter(it, sharedViewModel)
                sharedViewModel.foodList.postValue(it)
                if(it.isNotEmpty()){
                    binding.foodListPlaceholderText.visibility = android.view.View.GONE
                    binding.foodRecyclerView.visibility = android.view.View.VISIBLE
                }else{
                    binding.foodListPlaceholderText.visibility = android.view.View.VISIBLE
                    binding.foodRecyclerView.visibility = android.view.View.GONE
                }
            }
        }


        sharedViewModel.type.observe(this){
            binding.caloriesConsumedText.text = it.toString().uppercase()
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
            if(it.isNotEmpty()){
                sharedViewModel.isLoading.postValue(false)
            }
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
                            if(food.foodNutrients.isEmpty() || food.description == null || food.fdcId == null) {
                                continue
                            }

                            val item = Food(food.fdcId.toLong(),food.description, food.foodNutrients[0].amount.toLong())

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