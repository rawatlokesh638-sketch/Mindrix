package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE isLoggedIn = 1 LIMIT 1")
    fun getLoggedInUser(): Flow<UserStats?>

    @Query("SELECT * FROM user_stats ORDER BY bestScore DESC")
    fun getAllUsers(): Flow<List<UserStats>>

    @Query("SELECT * FROM user_stats ORDER BY aiRating DESC")
    fun getAllUsersByRating(): Flow<List<UserStats>>

    @Query("SELECT * FROM user_stats WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserStats?

    @Query("UPDATE user_stats SET isLoggedIn = 0")
    suspend fun logoutAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: UserStats)
}
