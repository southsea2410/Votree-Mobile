package com.example.votree.data.productCatagories

import androidx.room.TypeConverter

enum class SuitEnvironment {
    INDOOR,
    OUTDOOR,
    SHADE,
    SUN,
    WATER,
    DRY,
    HUMID,
    WET,
    AQUATIC,
    TERRESTRIAL,
    SEMI_AQUATIC,
    ARID,
    MOUNTAINOUS,
    TROPICAL,
    TEMPERATE,
    SUBTROPICAL,
    ALPINE,
    MARITIME,
    DESERT
}

class SuitEnvironmentConverter {
    @TypeConverter
    fun fromSuitEnvironment(value: SuitEnvironment): String {
        return value.name
    }

    @TypeConverter
    fun toSuitEnvironment(value: String): SuitEnvironment {
        return enumValueOf(value)
    }
}