package com.dndcharacterhandler.data.local

import androidx.room.TypeConverter
import com.dndcharacterhandler.domain.model.InventoryCategory

class RoomConverters {
    @TypeConverter
    fun fromInventoryCategory(value: InventoryCategory): String = value.name

    @TypeConverter
    fun toInventoryCategory(value: String): InventoryCategory =
        InventoryCategory.valueOf(value)
}

