package com.dndcharacterhandler.presentation.attributes

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dndcharacterhandler.domain.usecase.GetCharacterBundleUseCase
import com.dndcharacterhandler.presentation.BaseCharacterViewModel
import com.dndcharacterhandler.presentation.SelectedCharacterHolder
import com.dndcharacterhandler.presentation.components.PlaceholderScreen

class AttributesViewModel(
    getCharacterBundleUseCase: GetCharacterBundleUseCase,
    selectedCharacterHolder: SelectedCharacterHolder
) : BaseCharacterViewModel(getCharacterBundleUseCase, selectedCharacterHolder)

@Composable
fun AttributesScreen(viewModel: AttributesViewModel) {
    val state = viewModel.uiState.collectAsStateWithLifecycle()
    PlaceholderScreen(
        state = state.value,
        sections = listOf(
            "attributes_section_scores_title" to "attributes_section_scores_body",
            "attributes_section_skills_title" to "attributes_section_skills_body"
        )
    )
}
