package com.worldclock.app.data

import androidx.room.TypeConverter

class MeterTypeConverter {
    
    @TypeConverter
    fun fromMeterType(meterType: MeterType): String {
        return meterType.name
    }
    
    @TypeConverter
    fun toMeterType(meterTypeString: String): MeterType {
        return MeterType.valueOf(meterTypeString)
    }
}