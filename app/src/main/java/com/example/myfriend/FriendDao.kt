package com.example.myfriend

import androidx.room.*

@Dao
interface FriendDao {
    @Query("SELECT * FROM friends")
    suspend fun getAll(): List<Friend>

    @Query("SELECT * FROM friends WHERE name LIKE :query OR school LIKE :query")
    suspend fun search(query: String): List<Friend>

    @Insert
    suspend fun insert(friend: Friend)

    @Update
    suspend fun update(friend: Friend)

    @Delete
    suspend fun delete(friend: Friend)
}