package com.example.mefit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.mefit.databinding.FragmentFoodBinding
import com.example.mefit.model.Food
import com.example.mefit.model.FoodApiResponse
import com.example.mefit.network.FoodApiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class FoodFragment : Fragment() {

    private lateinit var binding: FragmentFoodBinding
    private lateinit var sharedViewModel: FoodSharedViewModel



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentFoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        sharedViewModel = ViewModelProvider(requireActivity())[FoodSharedViewModel::class.java]
        sharedViewModel.updateAllFoodLists()


        sharedViewModel.totalCaloriesConsumed.observe(viewLifecycleOwner) {
          binding.consumedValueTV.text = it.toString()
        }

        sharedViewModel.remainingCalories.observe(viewLifecycleOwner) {
          binding.remainingValueTV.text = it.toString()
        }

        sharedViewModel.calorieGoal.observe(viewLifecycleOwner) {
          binding.calorieGoal.text = it.toString()
        }



        sharedViewModel.breakFastCalories.observe(viewLifecycleOwner) {
            binding.breakfast.text = "Breakfast: $it"
        }
        sharedViewModel.lunchCalories.observe(viewLifecycleOwner) {
            binding.lunch.text = "Lunch: $it"
        }
        sharedViewModel.snacksCalories.observe(viewLifecycleOwner) {
            binding.snacks.text = "Snacks: $it"
        }
        sharedViewModel.dinnerCalories.observe(viewLifecycleOwner) {
            binding.dinner.text = "Dinner: $it"
        }

        var type = ""
        binding.cardView3.setOnClickListener {
            type = "breakfast"
            moveToFoodListActivity(type, sharedViewModel)
        }
        binding.cardView4.setOnClickListener {
            type = "lunch"
            moveToFoodListActivity(type, sharedViewModel)
        }
        binding.cardView5.setOnClickListener {
            type = "snacks"
            moveToFoodListActivity(type, sharedViewModel)
        }
        binding.cardView6.setOnClickListener {
            type = "dinner"
            moveToFoodListActivity(type, sharedViewModel)
        }
    }

    private fun moveToFoodListActivity(type_: String, _sharedViewModel: FoodSharedViewModel) {

        _sharedViewModel.updateType(type_)
        val intent = Intent(requireContext(), FoodList::class.java)
        intent.putExtra("type", type_)
        startActivity(intent)
    }
}