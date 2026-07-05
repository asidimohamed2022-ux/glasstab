package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.GameRepository

class GlassTapApplication : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "glass_tap_database"
        ).build()
    }
    
    val repository: GameRepository by lazy {
        GameRepository(database.highScoreDao())
    }
}
