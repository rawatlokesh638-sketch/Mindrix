package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserStats::class], version = 3, exportSchema = false)
abstract class MindrixDatabase : RoomDatabase() {
    abstract fun userStatsDao(): UserStatsDao
}
