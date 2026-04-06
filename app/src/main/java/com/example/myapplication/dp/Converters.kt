package com.example.myapplication.dp

import androidx.room.TypeConverter
import com.example.myapplication.models.Source
import com.google.gson.Gson

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromSource(source: Source): String {
        return gson.toJson(source)
    }

    @TypeConverter
    fun toSource(sourceString: String): Source {
        return gson.fromJson(sourceString, Source::class.java)
    }
}