package com.dndcharacterhandler.presentation.combat

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dndcharacterhandler.domain.usecase.GetCharacterBundleUseCase
import com.dndcharacterhandler.presentation.BaseCharacterViewModel
import com.dndcharacterhandler.presentation.SelectedCharacterHolder
import com.dndcharacterhandler.presentation.components.PlaceholderScreen

class CombatViewModel(
    getCharacterBundleUseCase: GetCharacterBundleUseCase,
    selectedCharacterHolder: SelectedCharacterHolder
) : BaseCharacterViewModel(getCharacterBundleUseCase, selectedCharacterHolder)

@Composable
fun CombatScreen(viewModel: CombatViewModel) {
    val state = viewModel.uiState.collectAsStateWithLifecycle()
    PlaceholderScreen(
        state = state.value,
        sections = listOf(
            "combat_section_attacks_title" to "combat_section_attacks_body",
            "combat_section_resources_title" to "combat_section_resources_body"
        )
    )
}
