package com.example.mefit

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mefit.model.Food
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FoodSharedViewModel: ViewModel(){

    private var db = Firebase.firestore
    private var user = FirebaseAuth.getInstance().currentUser!!
    private var document = db.collection("users").document(user.uid)


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




    var type = MutableLiveData<String>().apply { value = "breakfast" }
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
        updateAllFoodLists()
    }

    fun updateAllFoodLists(){

        document.get().addOnSuccessListener {
            val breakfastConsumedList = it.get("breakfastConsumed") as? List<Map<String, Any>>
            if (breakfastConsumedList != null) {
                // Convert each Map to your Food model
                val breakfastFoodList = breakfastConsumedList.map { foodMap ->
                    Food(
                        id = foodMap["id"] as Long,
                        name = foodMap["name"] as String,
                        calories = foodMap["calories"] as Long,
                    )
                }

                breakfastList.postValue(breakfastFoodList)

            }

            if(breakfastList.value!=null){
                for(food in breakfastList.value!!){
                    breakFastCalories.value = breakFastCalories.value?.plus(food.calories)
                }
            }


            lunchList.value = it["lunchConsumed"] as List<Food>
            snacksList.value = it["snacksConsumed"] as List<Food>
            dinnerList.value = it["dinnerConsumed"] as List<Food>



            /*for(food in breakfastList.value!!){
                breakFastCalories.value = breakFastCalories.value?.plus(food.calories)
            }
            for(food in lunchList.value!!){
                lunchCalories.value = lunchCalories.value?.plus(food.calories)
            }
            for(food in snacksList.value!!){
                snacksCalories.value = snacksCalories.value?.plus(food.calories)
            }
            for(food in dinnerList.value!!){
                dinnerCalories.value = dinnerCalories.value?.plus(food.calories)
            }*/
            totalCaloriesConsumed.value = breakFastCalories.value?.plus(lunchCalories.value!!)?.plus(snacksCalories.value!!)?.plus(dinnerCalories.value!!)
            val totalCalories = it.getLong("totalCalories") ?: 0
            remainingCalories.value = (totalCalories - totalCaloriesConsumed.value!!)
            document.update("consumedCalories", totalCaloriesConsumed.value!!)

        }
    }

    fun updateType(type: String) {
        this.type.postValue(type)
        Log.d("FoodSharedViewModel", "updateType: $type")
        when(type){
            "breakfast" -> foodList = breakfastList
            "lunch" -> foodList = lunchList
            "snacks" -> foodList = snacksList
            "dinner" -> foodList = dinnerList
        }
    }
}
