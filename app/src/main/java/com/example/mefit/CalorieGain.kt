package com.example.mefit


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.mefit.databinding.ActivityCalorieGainBinding
import com.google.firebase.auth.FirebaseAuth


class CalorieGain : AppCompatActivity() {

    private lateinit var binding : ActivityCalorieGainBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalorieGainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(HomeFragment())


        binding.bottomNavigationView.setOnItemSelectedListener {

            when(it.itemId){

                R.id.home -> replaceFragment(HomeFragment())
                R.id.food -> replaceFragment(FoodFragment())
                R.id.challenge -> replaceFragment(ChallengeFragment())
                R.id.location -> replaceFragment(LocateFragment())

                else ->{

                    replaceFragment(HomeFragment())

                }

            }

            true

        }




    }

    override fun onResume() {
        super.onResume()


    }

    private fun replaceFragment(fragment : Fragment){

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()


    }




}