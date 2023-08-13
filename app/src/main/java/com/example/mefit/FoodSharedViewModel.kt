package com.example.mefit

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


    private var breakfastList= MutableLiveData<List<Food>>()
    private var lunchList= MutableLiveData<List<Food>>()
    private var snacksList= MutableLiveData<List<Food>>()
    private var dinnerList= MutableLiveData<List<Food>>()

    var breakFastCalories = MutableLiveData<Int>()
    var lunchCalories = MutableLiveData<Int>()
    var snacksCalories = MutableLiveData<Int>()
    var dinnerCalories = MutableLiveData<Int>()
    var totalCaloriesConsumed = MutableLiveData<Int>()
    var remainingCalories = MutableLiveData<Int>()



    var type: String = "breakfast"
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
        document.update(type + "Consumed", foodList.value)
        updateAllFoodLists()
    }


    fun deleteFoodFromFirebase(food: Food) {
        val currentList = foodList.value
        if(currentList != null){
            val updatedList = currentList.toMutableList()
            updatedList.remove(food)
            foodList.value = updatedList
        }
        db.collection("users").document(user.uid).update(type + "Consumed", foodList.value)
        updateAllFoodLists()
    }

    fun updateAllFoodLists(){

        document.get().addOnSuccessListener {
            breakfastList = it["breakfastConsumed"] as MutableLiveData<List<Food>>
            lunchList = it["lunchConsumed"] as MutableLiveData<List<Food>>
            snacksList = it["snacksConsumed"] as MutableLiveData<List<Food>>
            dinnerList = it["dinnerConsumed"] as MutableLiveData<List<Food>>


            for(food in breakfastList.value!!){
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
            }
            totalCaloriesConsumed.value = breakFastCalories.value?.plus(lunchCalories.value!!)?.plus(snacksCalories.value!!)?.plus(dinnerCalories.value!!)
            var totalCalories = it["totalCalories"] as Int
            remainingCalories.value = totalCalories - totalCaloriesConsumed.value!!
            document.update("consumedCalories", remainingCalories.value)

        }
    }

    fun updateType(type: String) {
        this.type = type

        when(type){
            "breakfast" -> foodList = breakfastList
            "lunch" -> foodList = lunchList
            "snacks" -> foodList = snacksList
            "dinner" -> foodList = dinnerList
        }
    }
}
