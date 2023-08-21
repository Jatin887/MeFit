package com.example.mefit.model

data class UserChallenge(
    val name: String = "",
    val desc: String = "",
    val id: String = "",
    val duration: Int = 0,
    val calories: Int = 0,
    val rewards: Int = 0,
    val startTime: Long = 0,
)
