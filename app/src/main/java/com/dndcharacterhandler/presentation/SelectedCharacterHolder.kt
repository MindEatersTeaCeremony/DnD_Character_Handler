package com.dndcharacterhandler.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SelectedCharacterHolder {
    private val _selectedCharacterId = MutableStateFlow<Long?>(null)
    val selectedCharacterId: StateFlow<Long?> = _selectedCharacterId.asStateFlow()

    fun setSelectedCharacterId(characterId: Long?) {
        _selectedCharacterId.value = characterId
    }
}

