package com.example.data

import kotlinx.coroutines.flow.Flow

class MindrixRepository(private val dao: UserStatsDao) {
    val userStats: Flow<UserStats?> = dao.getLoggedInUser()
    val allUsers: Flow<List<UserStats>> = dao.getAllUsers()
    val allUsersByRating: Flow<List<UserStats>> = dao.getAllUsersByRating()

    suspend fun saveUserStats(stats: UserStats) {
        dao.insertOrUpdate(stats)
    }

    suspend fun getUserByEmail(email: String): UserStats? {
        return dao.getUserByEmail(email)
    }

    suspend fun logoutAll() {
        dao.logoutAll()
    }
}
