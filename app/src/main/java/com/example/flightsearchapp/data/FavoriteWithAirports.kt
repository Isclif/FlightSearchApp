package com.example.flightsearchapp.data

data class FavoriteWithAirports(
    val favorite_id: Int,
    val departure_code: String,
    val departure_name: String,
    val departure_iata: String,
    val destination_code: String,
    val destination_name: String,
    val destination_iata: String
)
