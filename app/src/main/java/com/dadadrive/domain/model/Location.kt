package com.dadadrive.domain.model

data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String = "",
    val city: String = ""
)
