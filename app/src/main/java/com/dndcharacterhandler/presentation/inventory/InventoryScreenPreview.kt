package com.dndcharacterhandler.presentation.inventory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import com.dndcharacterhandler.data.localization.LocalizedStrings
import com.dndcharacterhandler.domain.model.AppLanguage
import com.dndcharacterhandler.domain.model.CharacterBundle
import com.dndcharacterhandler.domain.model.InventoryArmorDetails
import com.dndcharacterhandler.domain.model.InventoryArmorType
import com.dndcharacterhandler.domain.model.InventoryCategory
import com.dndcharacterhandler.domain.model.InventoryItem
import com.dndcharacterhandler.presentation.attributes.previewFallbackCharacter
import com.dndcharacterhandler.presentation.localization.LocalStrings
import com.dndcharacterhandler.presentation.theme.DnDTheme

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp"
)
@Composable
fun InventoryScreenPreview() {
    val strings = LocalizedStrings(
        language = AppLanguage.ENGLISH,
        values = mapOf(
            "drawer_open_character_manager" to "Open character manager",
            "overview_settings" to "Settings",
            "placeholder_loading_character" to "Loading character",
            "inventory_search_placeholder" to "Search Inventory",
            "inventory_carry_weight" to "Carry Weight",
            "inventory_category_weapon" to "Weapon",
            "inventory_category_armor" to "Armor",
            "inventory_category_consumable" to "Consumable",
            "inventory_category_other" to "Other",
            "inventory_empty" to "No items found.",
            "inventory_catalog_title" to "Add from catalog",
            "inventory_catalog_loading" to "Loading catalog...",
            "inventory_catalog_empty" to "No matching items found.",
            "inventory_add_item" to "Add item",
            "common_cancel" to "Cancel"
        )
    )
    val items = listOf(
        InventoryItem(1L, "Longsword", InventoryCategory.WEAPON, 3.0, 1, true, ""),
        InventoryItem(2L, "Shortbow", InventoryCategory.WEAPON, 2.0, 1, false, ""),
        InventoryItem(3L, "Dagger", InventoryCategory.WEAPON, 1.0, 3, false, ""),
        InventoryItem(
            4L,
            "Chain Mail",
            InventoryCategory.ARMOR,
            55.0,
            1,
            true,
            "",
            armorDetails = InventoryArmorDetails(
                armorType = InventoryArmorType.HEAVY,
                armorClass = 16,
                appliesDexterityBonus = false,
                maxDexterityBonus = null,
                strengthMinimum = 13,
                hasStealthDisadvantage = true
            )
        ),
        InventoryItem(
            5L,
            "Shield",
            InventoryCategory.ARMOR,
            6.0,
            1,
            true,
            "",
            armorDetails = InventoryArmorDetails(
                armorType = InventoryArmorType.SHIELD,
                armorClass = 2,
                appliesDexterityBonus = false,
                maxDexterityBonus = null,
                strengthMinimum = 0,
                hasStealthDisadvantage = false
            )
        ),
        InventoryItem(6L, "Healing Potion", InventoryCategory.CONSUMABLE, 0.5, 3, false, ""),
        InventoryItem(
            7L,
            "Breastplate",
            InventoryCategory.ARMOR,
            20.0,
            1,
            false,
            "",
            armorDetails = InventoryArmorDetails(
                armorType = InventoryArmorType.MEDIUM,
                armorClass = 14,
                appliesDexterityBonus = true,
                maxDexterityBonus = 2,
                strengthMinimum = 0,
                hasStealthDisadvantage = false
            )
        ),
        InventoryItem(8L, "Rope (50 ft)", InventoryCategory.OTHER, 10.0, 1, false, "")
    )

    CompositionLocalProvider(LocalStrings provides strings) {
        DnDTheme {
            InventoryContent(
                characterBundle = CharacterBundle(
                    character = previewFallbackCharacter(),
                    skills = emptyList(),
                    attacks = emptyList(),
                    combatResources = emptyList(),
                    inventoryItems = items,
                    spells = emptyList(),
                    features = emptyList(),
                    notes = emptyList()
                )
            )
        }
    }
}
