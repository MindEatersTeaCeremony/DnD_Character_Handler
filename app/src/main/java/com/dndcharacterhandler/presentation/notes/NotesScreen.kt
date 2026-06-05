package com.dndcharacterhandler.presentation.notes

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dndcharacterhandler.domain.usecase.GetCharacterBundleUseCase
import com.dndcharacterhandler.presentation.BaseCharacterViewModel
import com.dndcharacterhandler.presentation.SelectedCharacterHolder
import com.dndcharacterhandler.presentation.components.PlaceholderScreen

class NotesViewModel(
    getCharacterBundleUseCase: GetCharacterBundleUseCase,
    selectedCharacterHolder: SelectedCharacterHolder
) : BaseCharacterViewModel(getCharacterBundleUseCase, selectedCharacterHolder)

@Composable
fun NotesScreen(viewModel: NotesViewModel) {
    val state = viewModel.uiState.collectAsStateWithLifecycle()
    PlaceholderScreen(
        state = state.value,
        sections = listOf(
            "notes_section_adventure_title" to "notes_section_adventure_body",
            "notes_section_session_title" to "notes_section_session_body"
        )
    )
}
