package com.dndcharacterhandler.data.local

import androidx.room.TypeConverter
import com.dndcharacterhandler.domain.model.ArmorClassMode
import com.dndcharacterhandler.domain.model.InventoryArmorType
import com.dndcharacterhandler.domain.model.InventoryCategory

class RoomConverters {
    @TypeConverter
    fun fromArmorClassMode(value: ArmorClassMode): String = value.name

    @TypeConverter
    fun toArmorClassMode(value: String): ArmorClassMode =
        ArmorClassMode.valueOf(value)

    @TypeConverter
    fun fromInventoryCategory(value: InventoryCategory): String = value.name

    @TypeConverter
    fun toInventoryCategory(value: String): InventoryCategory =
        InventoryCategory.valueOf(value)

    @TypeConverter
    fun fromInventoryArmorType(value: InventoryArmorType?): String? = value?.name

    @TypeConverter
    fun toInventoryArmorType(value: String?): InventoryArmorType? =
        value?.let(InventoryArmorType::valueOf)
}
