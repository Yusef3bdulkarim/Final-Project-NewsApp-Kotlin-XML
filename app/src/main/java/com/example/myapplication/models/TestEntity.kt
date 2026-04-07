package com.example.myapplication.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "test_entities")
data class TestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String
)
