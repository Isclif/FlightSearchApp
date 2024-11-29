package com.example.flightsearchapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File
import java.io.FileOutputStream

abstract class AppDatabase : RoomDatabase() {
    // Déclarez vos DAO ici
    abstract fun flightDao(): FlightDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val dbName = "flightsearch_db"

                // Copier la base de données depuis les assets si nécessaire
                copyDatabaseIfNeeded(context, dbName)

                // Construire la base de données Room
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    dbName
                )
                    .fallbackToDestructiveMigration() // Supprime les migrations si version non compatible
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private fun copyDatabaseIfNeeded(context: Context, dbName: String) {
            val dbPath = context.getDatabasePath(dbName)

            // Vérifiez si la base existe déjà
            if (!dbPath.exists()) {
                dbPath.parentFile?.mkdirs()
                context.assets.open(dbName).use { inputStream ->
                    FileOutputStream(dbPath).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }
    }
}
