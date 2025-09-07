package com.example.myfriend

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "friends")
data class Friend(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val school: String,
    val photoUri: String?,
    val bio: String
)