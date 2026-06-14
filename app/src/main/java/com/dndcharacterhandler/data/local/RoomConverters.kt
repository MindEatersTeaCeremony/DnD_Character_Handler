package com.dndcharacterhandler.data.local

import androidx.room.TypeConverter
import com.dndcharacterhandler.domain.model.ArmorClassMode
import com.dndcharacterhandler.domain.model.AttackCalculationMode
import com.dndcharacterhandler.domain.model.FeatureSource
import com.dndcharacterhandler.domain.model.InventoryArmorType
import com.dndcharacterhandler.domain.model.InventoryCategory
import com.dndcharacterhandler.domain.model.InventoryWeaponClass
import com.dndcharacterhandler.domain.model.InventoryWeaponRangeType
import com.dndcharacterhandler.domain.model.SpellcastingAbility

class RoomConverters {
    @TypeConverter
    fun fromArmorClassMode(value: ArmorClassMode): String = value.name

    @TypeConverter
    fun toArmorClassMode(value: String): ArmorClassMode =
        ArmorClassMode.valueOf(value)

    @TypeConverter
    fun fromAttackCalculationMode(value: AttackCalculationMode): String = value.name

    @TypeConverter
    fun toAttackCalculationMode(value: String): AttackCalculationMode =
        runCatching { AttackCalculationMode.valueOf(value) }.getOrDefault(AttackCalculationMode.AUTOMATIC)

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

    @TypeConverter
    fun fromInventoryWeaponClass(value: InventoryWeaponClass?): String? = value?.name

    @TypeConverter
    fun toInventoryWeaponClass(value: String?): InventoryWeaponClass? =
        value?.let(InventoryWeaponClass::valueOf)

    @TypeConverter
    fun fromInventoryWeaponRangeType(value: InventoryWeaponRangeType?): String? = value?.name

    @TypeConverter
    fun toInventoryWeaponRangeType(value: String?): InventoryWeaponRangeType? =
        value?.let(InventoryWeaponRangeType::valueOf)

    @TypeConverter
    fun fromFeatureSource(value: FeatureSource): String = value.name

    @TypeConverter
    fun toFeatureSource(value: String): FeatureSource =
        runCatching { FeatureSource.valueOf(value) }.getOrDefault(FeatureSource.OTHER)

    @TypeConverter
    fun fromSpellcastingAbility(value: SpellcastingAbility): String = value.name

    @TypeConverter
    fun toSpellcastingAbility(value: String): SpellcastingAbility =
        runCatching { SpellcastingAbility.valueOf(value) }.getOrDefault(SpellcastingAbility.WISDOM)
}
