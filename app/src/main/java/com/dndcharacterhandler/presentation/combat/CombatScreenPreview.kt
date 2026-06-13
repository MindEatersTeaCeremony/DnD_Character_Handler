package com.dndcharacterhandler.presentation.combat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import com.dndcharacterhandler.data.localization.LocalizedStrings
import com.dndcharacterhandler.domain.model.AppLanguage
import com.dndcharacterhandler.domain.model.Attack
import com.dndcharacterhandler.domain.model.CharacterBundle
import com.dndcharacterhandler.domain.model.CombatResource
import com.dndcharacterhandler.presentation.attributes.previewFallbackCharacter
import com.dndcharacterhandler.presentation.localization.LocalStrings
import com.dndcharacterhandler.presentation.theme.DnDTheme

@Preview(
    name = "Combat Screen",
    showBackground = true,
    device = "spec:width=412dp,height=915dp"
)
@Composable
fun CombatScreenPreview() {
    val strings = LocalizedStrings(
        language = AppLanguage.ENGLISH,
        values = mapOf(
            "drawer_open_character_manager" to "Open character manager",
            "overview_settings" to "Settings",
            "placeholder_loading_character" to "Loading character",
            "overview_ac_full" to "Armor Class",
            "combat_spell_bonus" to "Spell Bonus",
            "combat_spell_dc" to "Spell DC",
            "combat_section_attacks_title" to "Attacks",
            "combat_combat_resources_title" to "Combat Resources",
            "combat_add_attack" to "Add Attack",
            "combat_edit_attack" to "Edit Attack",
            "combat_attack_range" to "Range",
            "combat_attack_bonus_or_dc" to "Attack Bonus / DC",
            "combat_attack_damage" to "Damage",
            "combat_attack_damage_type" to "Damage Type",
            "combat_edit_resource" to "Edit Resource",
            "combat_resource_current" to "Current",
            "combat_resource_maximum" to "Maximum",
            "combat_resource_short_rest" to "Restores on short rest",
            "combat_resource_long_rest" to "Restores on long rest",
            "combat_empty_attacks" to "No attacks yet.",
            "combat_empty_resources" to "No combat resources yet.",
            "features_name" to "Name",
            "common_save" to "Save",
            "common_cancel" to "Cancel",
            "inventory_delete_action" to "Delete"
        )
    )
    val character = previewFallbackCharacter().copy(
        armorClass = 15,
        level = 7,
        characterClass = "Wizard",
        subclass = "Divination",
        intelligence = 18
    )
    val attacks = listOf(
        Attack(
            id = 1,
            name = "Quarterstaff",
            icon = "",
            range = "Melee",
            attackBonusOrSaveDc = "+7 Attack",
            damage = "1d8 + 4",
            damageType = "Bludgeoning"
        ),
        Attack(
            id = 2,
            name = "Dagger",
            icon = "",
            range = "Melee",
            attackBonusOrSaveDc = "+7 Attack",
            damage = "1d4 + 4",
            damageType = "Piercing"
        ),
        Attack(
            id = 3,
            name = "Fire Bolt",
            icon = "",
            range = "120 ft",
            attackBonusOrSaveDc = "+7 Attack",
            damage = "2d10",
            damageType = "Fire"
        ),
        Attack(
            id = 4,
            name = "Ray of Frost",
            icon = "",
            range = "60 ft",
            attackBonusOrSaveDc = "+7 Attack",
            damage = "1d8",
            damageType = "Cold"
        )
    )
    val resources = listOf(
        CombatResource(id = 1, name = "Arcane Recovery", currentUses = 99, maximumUses = 99, restoresOnShortRest = false, restoresOnLongRest = true),
        CombatResource(id = 2, name = "Portent", currentUses = 2, maximumUses = 2, restoresOnShortRest = false, restoresOnLongRest = true),
        CombatResource(id = 3, name = "Bardic Inspiration", currentUses = 3, maximumUses = 3, restoresOnShortRest = true, restoresOnLongRest = true),
        CombatResource(id = 4, name = "Second Wind", currentUses = 1, maximumUses = 1, restoresOnShortRest = true, restoresOnLongRest = false)
    )

    CompositionLocalProvider(LocalStrings provides strings) {
        DnDTheme {
            CombatContent(
                characterBundle = CharacterBundle(
                    character = character,
                    skills = emptyList(),
                    attacks = attacks,
                    combatResources = resources,
                    inventoryItems = emptyList(),
                    spells = emptyList(),
                    features = emptyList(),
                    notes = emptyList()
                )
            )
        }
    }
}
