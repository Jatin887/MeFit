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



    private var type = "breakfast"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentFoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        sharedViewModel = ViewModelProvider(requireActivity())[FoodSharedViewModel::class.java]







        binding.card1.setOnClickListener {
            type="breakfast"
            moveToFoodListActivity(type)
        }
        binding.card2.setOnClickListener {
            type="lunch"
            moveToFoodListActivity(type)
        }
        binding.card3.setOnClickListener {
            type="snacks"
            moveToFoodListActivity(type)
        }
        binding.card4.setOnClickListener {
            type="dinner"
            moveToFoodListActivity(type)
        }
    }

    private fun moveToFoodListActivity(type: String) {

        sharedViewModel.updateType(type)
        val REQUEST_CODE_ACTIVITY = 1
        val intent = Intent(requireContext(), FoodList::class.java)
        startActivityForResult(intent, REQUEST_CODE_ACTIVITY)
    }


}