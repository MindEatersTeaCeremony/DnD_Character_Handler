package com.dndcharacterhandler.domain.model

enum class InventoryCatalogSource { EQUIPMENT, MAGIC_ITEM }

data class InventoryCatalogItem(
    val id: String,
    val name: String,
    val category: InventoryCategory,
    val weight: Double,
    val description: String,
    val source: InventoryCatalogSource,
    val detailLine: String? = null,
    val costQuantity: Int? = null,
    val costUnit: String? = null,
    val armorDetails: InventoryArmorDetails? = null
) {
    fun toInventoryItem(): InventoryItem =
        InventoryItem(
            name = name,
            category = category,
            weight = weight,
            quantity = 1,
            isEquipped = false,
            icon = "",
            costQuantity = costQuantity,
            costUnit = costUnit,
            armorDetails = armorDetails
        )
}
