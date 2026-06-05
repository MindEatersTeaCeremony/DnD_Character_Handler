package com.dndcharacterhandler.presentation.spells

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dndcharacterhandler.domain.usecase.GetCharacterBundleUseCase
import com.dndcharacterhandler.presentation.BaseCharacterViewModel
import com.dndcharacterhandler.presentation.SelectedCharacterHolder
import com.dndcharacterhandler.presentation.components.PlaceholderScreen

class SpellsViewModel(
    getCharacterBundleUseCase: GetCharacterBundleUseCase,
    selectedCharacterHolder: SelectedCharacterHolder
) : BaseCharacterViewModel(getCharacterBundleUseCase, selectedCharacterHolder)

@Composable
fun SpellsScreen(viewModel: SpellsViewModel) {
    val state = viewModel.uiState.collectAsStateWithLifecycle()
    PlaceholderScreen(
        state = state.value,
        sections = listOf(
            "spells_section_spellbook_title" to "spells_section_spellbook_body",
            "spells_section_casting_title" to "spells_section_casting_body"
        )
    )
}
