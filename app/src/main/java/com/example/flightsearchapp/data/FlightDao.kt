package com.example.flightsearchapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface FlightDao {
    // Recherche des suggestions d'aéroports
    @Query("SELECT * FROM airport WHERE iata_code LIKE :query OR name LIKE :query ORDER BY passengers DESC")
    suspend fun searchAirports(query: String): List<Airport>

    // Obtenir tous les favoris
    @Query("SELECT * FROM favorite")
    suspend fun getAllFavorites(): List<Favorite>

    // Ajouter un favori
    @Insert
    suspend fun addFavorite(favorite: Favorite)


    @Query("""
    SELECT 
        f.id AS favorite_id,
        f.departure_code,
        dep_airport.name AS departure_name,
        dep_airport.iata_code AS departure_iata,
        f.destination_code,
        dest_airport.name AS destination_name,
        dest_airport.iata_code AS destination_iata
    FROM 
        favorite f
    JOIN 
        airport dep_airport ON dep_airport.iata_code = f.departure_code
    JOIN 
        airport dest_airport ON dest_airport.iata_code = f.destination_code
    WHERE 
        f.departure_code = :iataCode OR f.destination_code = :iataCode
""")
    suspend fun getAirportsForFavoriteCode(iataCode: String): List<FavoriteWithAirports>

}


//    @Transaction
//    @Query("""
//        SELECT
//            f.id AS favorite_id,
//            f.departure_code,
//            dep_airport.name AS departure_name,
//            dep_airport.iata_code AS departure_iata,
//            f.destination_code,
//            f.destination_code AS destination_name,
//            dest_airport.iata_code AS destination_iata
//        FROM
//            favorite f
//        JOIN
//            airport dep_airport ON dep_airport.iata_code = f.departure_code
//        JOIN
//            airport dest_airport ON dest_airport.iata_code = f.destination_code
//    """)
//    fun getFavoritesWithAirports(): List<FavoriteWithAirports>
//
//    @Query("""
//        SELECT DISTINCT a.*
//        FROM airport a
//        WHERE a.iata_code IN (
//            SELECT f.departure_code FROM favorite f WHERE f.departure_code = :iataCode
//            UNION
//            SELECT f.destination_code FROM favorite f WHERE f.destination_code = :iataCode
//        )
//    """)
//    suspend fun getAirportsFromFavorites(iataCode: String): List<Airport>
