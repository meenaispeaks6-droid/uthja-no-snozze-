package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.DismissType
import com.example.data.model.WakeMode

class Converters {
    @TypeConverter
    fun fromWakeMode(mode: WakeMode?): String = mode?.name ?: WakeMode.FOCUS.name

    @TypeConverter
    fun toWakeMode(value: String?): WakeMode =
        try {
            WakeMode.valueOf(value ?: WakeMode.FOCUS.name)
        } catch (e: Exception) {
            WakeMode.FOCUS
        }

    @TypeConverter
    fun fromDismissType(type: DismissType?): String = type?.name ?: DismissType.MATH.name

    @TypeConverter
    fun toDismissType(value: String?): DismissType =
        try {
            DismissType.valueOf(value ?: DismissType.MATH.name)
        } catch (e: Exception) {
            DismissType.MATH
        }
}
