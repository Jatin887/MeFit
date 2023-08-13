package com.example.mefit.model

data class FoodNutrient(
    val amount: Double,
    val derivationCode: String,
    val derivationDescription: String,
    val name: String,
    val number: Int,
    val unitName: String
)