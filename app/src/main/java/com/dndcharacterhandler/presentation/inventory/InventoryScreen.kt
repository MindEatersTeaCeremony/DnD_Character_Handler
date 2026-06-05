package com.dndcharacterhandler.presentation.inventory

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dndcharacterhandler.domain.usecase.GetCharacterBundleUseCase
import com.dndcharacterhandler.presentation.BaseCharacterViewModel
import com.dndcharacterhandler.presentation.SelectedCharacterHolder
import com.dndcharacterhandler.presentation.components.PlaceholderScreen

class InventoryViewModel(
    getCharacterBundleUseCase: GetCharacterBundleUseCase,
    selectedCharacterHolder: SelectedCharacterHolder
) : BaseCharacterViewModel(getCharacterBundleUseCase, selectedCharacterHolder)

@Composable
fun InventoryScreen(viewModel: InventoryViewModel) {
    val state = viewModel.uiState.collectAsStateWithLifecycle()
    PlaceholderScreen(
        state = state.value,
        sections = listOf(
            "inventory_section_items_title" to "inventory_section_items_body",
            "inventory_section_loadout_title" to "inventory_section_loadout_body"
        )
    )
}
