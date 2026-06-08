package com.sahyadri.samrakshane

data class Alert(
    val id: String = "",
    val alertType: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = 0L,
    val status: String = "Reported"
)