package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "high_scores")
data class HighScore(
    @PrimaryKey val id: Int = 1,
    val score: Int
)

@Dao
interface HighScoreDao {
    @Query("SELECT * FROM high_scores WHERE id = 1 LIMIT 1")
    fun getHighScoreFlow(): Flow<HighScore?>

    @Query("SELECT * FROM high_scores WHERE id = 1 LIMIT 1")
    suspend fun getHighScore(): HighScore?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighScore(highScore: HighScore)
}

@Database(entities = [HighScore::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun highScoreDao(): HighScoreDao
}

class GameRepository(private val highScoreDao: HighScoreDao) {
    val highScoreFlow: Flow<HighScore?> = highScoreDao.getHighScoreFlow()

    suspend fun getHighScoreValue(): Int {
        return highScoreDao.getHighScore()?.score ?: 0
    }

    suspend fun saveHighScore(score: Int) {
        highScoreDao.insertHighScore(HighScore(id = 1, score = score))
    }
}
