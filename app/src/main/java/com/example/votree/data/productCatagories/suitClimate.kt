package com.example.votree.data.productCatagories

import androidx.room.TypeConverter

enum class SuitClimate {
    TROPICAL,
    TEMPERATE,
    ARID,
    SEMI_ARID,
    MEDITERRANEAN,
    SUBTROPICAL,
    POLAR,
    ALPINE,
    DESERT,
    MARITIME,
    CONTINENTAL,
    HUMID_SUBTROPICAL,
    HUMID_CONTINENTAL,
    MONSOONAL,
    OCEANIC,
    STEPPE
}

class SuitClimateConverter {
    @TypeConverter
    fun fromSuitClimate(value: SuitClimate): String {
        return value.name
    }

    @TypeConverter
    fun toSuitClimate(value: String): SuitClimate {
        return enumValueOf(value)
    }
}
