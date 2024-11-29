package com.example.flightsearchapp.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.flightsearchapp.data.FavoriteTrip
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.firstOrNull

// Création d'une instance de DataStore en utilisant une propriété d'extension
val Context.dataStore by preferencesDataStore(name = "user_preferences")

class PreferencesManager(context: Context) {

    companion object {
        private val FAVORITE_TRIPS = stringPreferencesKey("favorite_trips")
    }

    private val dataStore = context.dataStore

    suspend fun saveFavoriteTrip(context: Context, trip: FavoriteTrip) : Boolean {

        // recuperer les favoris existant
        val existingFavorites = getFavoriteTrips().firstOrNull() ?: emptyList()

        // Ajouter le nouveau trajet à la liste
        val updatedFavorites = existingFavorites + trip

        // Convertir en JSON et enregistrer
        val favoritesJson = Gson().toJson(updatedFavorites)

        return if (existingFavorites.any { it.departure_name == trip.departure_name && it.departure_code == trip.departure_code && it.destination_name == trip.destination_name && it.destination_code == trip.destination_code }) {
            false
        } else {
            dataStore.edit { preferences ->
                preferences[FAVORITE_TRIPS] = favoritesJson
            }

            true
        }

    }
    // Supression d'une reference dans la dataStore'
    suspend fun clearFavoriteTrips() : Boolean {
        return try {
            var keyExists = false

            dataStore.edit { preferences ->
                // Vérifiez si la clé existe
                if (preferences.contains(FAVORITE_TRIPS)) {
                    preferences.remove(FAVORITE_TRIPS)
                    keyExists = true // La clé a été supprimée
                }
            }

            keyExists // Retourne true si la clé a été supprimée, sinon false
        } catch (e: Exception) {
            e.printStackTrace()
            false // Si une erreur se produit, retourne false
        }
    }


    fun getFavoriteTrips(): Flow<List<FavoriteTrip>> {
        return dataStore.data.map { preferences ->
            val favoritesJson = preferences[FAVORITE_TRIPS] ?: "[]"
            Gson().fromJson(favoritesJson, object : TypeToken<List<FavoriteTrip>>() {}.type)
        }
    }
}

