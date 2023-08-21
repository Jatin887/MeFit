package com.example.mefit.model

data class FoodApiResponseItem(
    val brandOwner: String,
    val dataType: String,
    val description: String,
    val fdcId: Int,
    val foodCode: String,
    val foodNutrients: List<FoodNutrient>,
    val gtinUpc: String,
    val ndbNumber: Int,
    val publicationDate: String
)