package com.dndcharacterhandler.presentation.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dndcharacterhandler.data.preferences.LanguagePreferencesRepository
import com.dndcharacterhandler.domain.model.AppLanguage
import com.dndcharacterhandler.domain.model.CharacterBundle
import com.dndcharacterhandler.domain.model.defaultCharacterBundle
import com.dndcharacterhandler.domain.repository.CharacterFileRepository
import com.dndcharacterhandler.domain.repository.CharacterRepository
import com.dndcharacterhandler.presentation.SelectedCharacterHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class CharacterManagerUiState(
    val characters: List<CharacterBundle> = emptyList(),
    val selectedCharacterId: Long? = null,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val exportStatusKey: String? = null,
    val importStatusKey: String? = null
)

class CharacterManagerViewModel(
    private val characterRepository: CharacterRepository,
    private val fileRepository: CharacterFileRepository,
    private val languagePreferencesRepository: LanguagePreferencesRepository,
    private val selectedCharacterHolder: SelectedCharacterHolder
) : ViewModel() {
    private val _uiState = MutableStateFlow(CharacterManagerUiState())
    val uiState: StateFlow<CharacterManagerUiState> = _uiState.asStateFlow()
    private var seededDefaultCharacter = false

    init {
        viewModelScope.launch {
            languagePreferencesRepository.language.collectLatest { language ->
                _uiState.value = _uiState.value.copy(language = language)
            }
        }
        viewModelScope.launch {
            characterRepository.observeCharacters().collectLatest { characters ->
                if (characters.isEmpty()) {
                    if (!seededDefaultCharacter) {
                        seededDefaultCharacter = true
                        val id = characterRepository.createCharacter(defaultCharacterBundle())
                        selectedCharacterHolder.setSelectedCharacterId(id)
                        _uiState.value = _uiState.value.copy(selectedCharacterId = id)
                    }
                } else {
                    val selected = _uiState.value.selectedCharacterId ?: characters.first().character.id
                    selectedCharacterHolder.setSelectedCharacterId(selected)
                    _uiState.value = _uiState.value.copy(
                        characters = characters,
                        selectedCharacterId = selected
                    )
                }
            }
        }
    }

    fun selectCharacter(characterId: Long) {
        selectedCharacterHolder.setSelectedCharacterId(characterId)
        _uiState.value = _uiState.value.copy(selectedCharacterId = characterId)
    }

    fun createCharacter() {
        viewModelScope.launch {
            val id = characterRepository.createCharacter(defaultCharacterBundle())
            selectedCharacterHolder.setSelectedCharacterId(id)
            _uiState.value = _uiState.value.copy(selectedCharacterId = id)
        }
    }

    fun exportCharacter() {
        viewModelScope.launch {
            val selectedId = _uiState.value.selectedCharacterId ?: return@launch
            val result = fileRepository.exportCharacter(selectedId)
            _uiState.value = _uiState.value.copy(
                exportStatusKey = if (result.isSuccess) "drawer_export_success" else "drawer_export_error"
            )
        }
    }

    fun importCharacter() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                importStatusKey = "drawer_import_pending"
            )
        }
    }

    fun deleteCurrentCharacter() {
        viewModelScope.launch {
            val selectedId = _uiState.value.selectedCharacterId ?: return@launch
            characterRepository.deleteCharacter(selectedId)
            selectedCharacterHolder.setSelectedCharacterId(null)
            _uiState.value = _uiState.value.copy(selectedCharacterId = null)
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            languagePreferencesRepository.setLanguage(language)
        }
    }
}
