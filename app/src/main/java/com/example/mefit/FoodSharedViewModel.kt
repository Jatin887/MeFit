package com.example.mefit

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mefit.model.Food
import com.example.mefit.model.UserChallenge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FoodSharedViewModel: ViewModel(){

    private var db = Firebase.firestore
    private var user = FirebaseAuth.getInstance().currentUser!!
    private var document = db.collection("users").document(user.uid)
    var isLoading = MutableLiveData<Boolean>().apply { value = false }

    var breakfastList: MutableLiveData<List<Food>> = MutableLiveData()
    var lunchList= MutableLiveData<List<Food>>()
    var snacksList= MutableLiveData<List<Food>>()
    var dinnerList= MutableLiveData<List<Food>>()

    var breakFastCalories = MutableLiveData<Long>().apply { value = 0 }
    var lunchCalories = MutableLiveData<Long>().apply { value = 0 }
    var snacksCalories = MutableLiveData<Long>().apply { value = 0 }
    var dinnerCalories = MutableLiveData<Long>().apply { value = 0 }
    var totalCaloriesConsumed = MutableLiveData<Long>().apply { value = 0 }
    var remainingCalories = MutableLiveData<Long>().apply { value = 0 }
    var calorieGoal= MutableLiveData<Long>().apply { value = 0 }




    var type = MutableLiveData<String>()
    var foodList = MutableLiveData<List<Food>>()
    var allFoodList = MutableLiveData<List<Food>>()



    fun addFood(food: Food){
        val currentList = foodList.value
        if(currentList == null){
            foodList.value = listOf(food)
        }else{
            val updatedList = currentList.toMutableList()
            updatedList.add(food)
            foodList.value = updatedList
        }
    }

    fun addAllFood(food: Food){
        val currentList = allFoodList.value
        if(currentList == null){
            allFoodList.value = listOf(food)
        }else{
            val updatedList = currentList.toMutableList()
            updatedList.add(food)
            allFoodList.value = updatedList
        }
    }

    fun addFoodToFirebase(food: Food) {
        addFood(food)
        document.update(type.value + "Consumed", foodList.value)
        document.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null) {
                    var consumedCalories = documentSnapshot.get("consumedCalories") as Long
                    consumedCalories += food.calories
                    document.update("consumedCalories", consumedCalories)
                } else {
                    // Document not found
                }
            }
            .addOnFailureListener { exception ->
                // Handle the failure
            }


        updateAllFoodLists()
    }


    fun deleteFoodFromFirebase(food: Food) {
        val currentList = foodList.value
        if(currentList != null){
            val updatedList = currentList.toMutableList()
            updatedList.remove(food)
            foodList.value = updatedList
        }
        db.collection("users").document(user.uid).update(type.value + "Consumed", foodList.value)
        document.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null) {
                    var consumedCalories = documentSnapshot.get("consumedCalories") as Long
                    consumedCalories -= food.calories
                    document.update("consumedCalories", consumedCalories)
                } else {
                    // Document not found
                }
            }
            .addOnFailureListener { exception ->
                // Handle the failure
            }
        updateAllFoodLists()
    }

    fun updateAllFoodLists(){

        document.get().addOnSuccessListener {
            val breakfastConsumedList = it.get("breakfastConsumed") as? List<Map<String, Any>>
            if (breakfastConsumedList != null) {
                val breakfastFoodList = breakfastConsumedList.map { foodMap ->
                    Food(
                        id = foodMap["id"] as Long,
                        name = foodMap["name"] as String,
                        calories = foodMap["calories"] as Long,
                    )
                }


                breakfastList.postValue(breakfastFoodList)

                if(breakfastFoodList.size>0){
                    breakFastCalories.value = 0
                    for(food in breakfastFoodList){
                        breakFastCalories.value = breakFastCalories.value?.plus(food.calories)
                    }
                }

            }

            val lunchConsumedList = it.get("lunchConsumed") as? List<Map<String, Any>>
            if (lunchConsumedList != null) {
                val lunchFoodList = lunchConsumedList.map { foodMap ->
                    Food(
                        id = foodMap["id"] as Long,
                        name = foodMap["name"] as String,
                        calories = foodMap["calories"] as Long,
                    )
                }


                lunchList.postValue(lunchFoodList)

                if(lunchFoodList.size>0){
                    lunchCalories.value = 0
                    for(food in lunchFoodList){
                        lunchCalories.value = lunchCalories.value?.plus(food.calories)
                    }
                }

            }

            val snacksConsumedList = it.get("snacksConsumed") as? List<Map<String, Any>>
            if (snacksConsumedList != null) {
                val snacksFoodList = snacksConsumedList.map { foodMap ->
                    Food(
                        id = foodMap["id"] as Long,
                        name = foodMap["name"] as String,
                        calories = foodMap["calories"] as Long,
                    )
                }

                snacksList.postValue(snacksFoodList)

                if(snacksFoodList.size>0){
                    snacksCalories.value = 0
                    for(food in snacksFoodList){
                        snacksCalories.value = snacksCalories.value?.plus(food.calories)
                    }
                }

            }

            val dinnerConsumedList = it.get("dinnerConsumed") as? List<Map<String, Any>>
            if (dinnerConsumedList != null) {
                val dinnerFoodList = dinnerConsumedList.map { foodMap ->
                    Food(
                        id = foodMap["id"] as Long,
                        name = foodMap["name"] as String,
                        calories = foodMap["calories"] as Long,
                    )
                }

                dinnerList.postValue(dinnerFoodList)

                if(dinnerFoodList.size>0){
                    dinnerCalories.value = 0
                    for(food in dinnerFoodList){
                        dinnerCalories.value = dinnerCalories.value?.plus(food.calories)
                    }
                }

            }
            totalCaloriesConsumed.value = breakFastCalories.value?.plus(lunchCalories.value!!)?.plus(snacksCalories.value!!)?.plus(dinnerCalories.value!!)
            val totalCalories = it.getLong("totalCalories") ?: 0
            calorieGoal.value = totalCalories
            remainingCalories.value = (totalCalories - totalCaloriesConsumed.value!!)
            document.update("consumedCalories", totalCaloriesConsumed.value!!)

        }
    }

    fun updateType(type_: String) {
        /*type.postValue(type_)

        when(type_){
            "breakfast" -> foodList.postValue(breakfastList.value)
            "lunch" -> foodList.postValue(lunchList.value)
            "snacks" -> foodList.postValue(snacksList.value)
            "dinner" -> foodList.postValue(dinnerList.value)
        }*/
    }





}
