package com.example.mefit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.mefit.databinding.ActivityProfileBinding
import com.example.mefit.model.Food
import com.example.mefit.model.UserChallenge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Profile : AppCompatActivity() {

    private lateinit var binding : ActivityProfileBinding
    private var db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.submit.setOnClickListener {
            validateAndPushData()
        }

    }


    private fun validateAndPushData() {
        val name = binding.name.text.toString().trim()
        val age = binding.age.text.toString().trim()
        val height = binding.height.text.toString().trim()
        val weight = binding.weight.text.toString().trim()

        if (name.isNotEmpty() && age.isNotEmpty() && height.isNotEmpty() && weight.isNotEmpty()) {
            val selectedGoal = when (binding.goal.checkedRadioButtonId) {
                R.id.weight_gain -> "Weight Gain"
                R.id.weight_loss -> "Weight Loss"
                R.id.muscle_building -> "Muscle Building"
                else -> ""
            }

            //intialise a list of food with values 0
            val foodList = emptyList<Food>()
            foodList.plus(Food(0,"Apple", 0))
            if (selectedGoal.isNotEmpty()) {
                val userData = hashMapOf(
                    "name" to name,
                    "age" to age,
                    "height" to height,
                    "weight" to weight,
                    "goal" to selectedGoal,
                    "consumedCalories" to 0,
                    "totalCalories" to calculateCalories(height.toDouble(), weight.toDouble(), selectedGoal, age.toInt()),
                    "userChallenges" to arrayListOf<UserChallenge>(),
                    "breakfastConsumed" to foodList,
                    "lunchConsumed" to foodList,
                    "snacksConsumed" to foodList,
                    "dinnerConsumed" to foodList,
                    "rewards" to 0
                )


                db.collection("users").document(currentUser.uid)
                    .set(userData)
                    .addOnSuccessListener {

                        Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show()
                        //take to home screen
                        val intent = android.content.Intent(this, CalorieGain::class.java)
                        startActivity(intent)

                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error saving data", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Select a goal", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Fill all the fields", Toast.LENGTH_SHORT).show()
        }
    }


    fun calculateCalories(height: Double, weight: Double, goal: String, age:Int): Double {
        // Constants for BMR calculation
        val BMR_WEIGHT_FACTOR = 10.0 // Calories per kg
        val BMR_HEIGHT_FACTOR = 6.25 // Calories per cm
        val BMR_AGE_FACTOR = 5.0 // Calories per year
        val BMR_MALE_OFFSET = 5.0 // Calories
        val BMR_FEMALE_OFFSET = -161.0 // Calories


        // Calculate BMI
        val bmi = weight / ((height / 100.0) * (height / 100.0))

        // Calculate BMR
        val bmr = if (goal == "Weight Gain" || goal == "Muscle Gain") {
            (BMR_WEIGHT_FACTOR * weight) +
                    (BMR_HEIGHT_FACTOR * height) -
                    (BMR_AGE_FACTOR * age) + BMR_MALE_OFFSET
        } else {
            (BMR_WEIGHT_FACTOR * weight) +
                    (BMR_HEIGHT_FACTOR * height) -
                    (BMR_AGE_FACTOR * age) + BMR_FEMALE_OFFSET
        }

        // Adjust BMR based on goal
        val caloriesPerDay = when (goal) {
            "Weight Gain" -> bmr * 1.2 // Increase by 20% for active individuals
            "Weight Loss" -> bmr * 0.85 // Decrease by 15% for calorie deficit
            "Muscle Gain" -> bmr * 1.15 // Increase slightly for muscle building
            else -> bmr // Maintain for other goals
        }

        return caloriesPerDay
    }
}