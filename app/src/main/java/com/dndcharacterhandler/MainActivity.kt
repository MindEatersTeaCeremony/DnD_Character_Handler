package com.dndcharacterhandler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dndcharacterhandler.data.local.AppDatabase
import com.dndcharacterhandler.data.localization.LocalizationRepository
import com.dndcharacterhandler.data.preferences.LanguagePreferencesRepository
import com.dndcharacterhandler.data.repository.CharacterFileRepositoryImpl
import com.dndcharacterhandler.data.repository.CharacterRepositoryImpl
import com.dndcharacterhandler.domain.usecase.GetCharacterBundleUseCase
import com.dndcharacterhandler.presentation.DndCharacterApp
import com.dndcharacterhandler.presentation.DndCharacterAppState
import com.dndcharacterhandler.presentation.SelectedCharacterHolder
import com.dndcharacterhandler.presentation.attributes.AttributesViewModel
import com.dndcharacterhandler.presentation.biography.BiographyViewModel
import com.dndcharacterhandler.presentation.combat.CombatViewModel
import com.dndcharacterhandler.presentation.features.FeaturesViewModel
import com.dndcharacterhandler.presentation.inventory.InventoryViewModel
import com.dndcharacterhandler.presentation.notes.NotesViewModel
import com.dndcharacterhandler.presentation.overview.OverviewViewModel
import com.dndcharacterhandler.presentation.spells.SpellsViewModel
import com.dndcharacterhandler.presentation.theme.DnDTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getInstance(applicationContext)
        val characterRepository = CharacterRepositoryImpl(database.characterDao())
        val fileRepository = CharacterFileRepositoryImpl(
            context = applicationContext,
            characterDao = database.characterDao(),
            characterRepository = characterRepository
        )
        val languagePreferencesRepository = LanguagePreferencesRepository(applicationContext)
        val localizationRepository = LocalizationRepository(applicationContext)
        val getCharacterBundleUseCase = GetCharacterBundleUseCase(characterRepository)
        val selectedCharacterHolder = SelectedCharacterHolder()

        setContent {
            DnDTheme {
                DndCharacterApp(
                    appState = DndCharacterAppState(
                        overviewViewModel = OverviewViewModel(getCharacterBundleUseCase, selectedCharacterHolder),
                        attributesViewModel = AttributesViewModel(getCharacterBundleUseCase, selectedCharacterHolder),
                        combatViewModel = CombatViewModel(getCharacterBundleUseCase, selectedCharacterHolder),
                        inventoryViewModel = InventoryViewModel(getCharacterBundleUseCase, selectedCharacterHolder),
                        spellsViewModel = SpellsViewModel(getCharacterBundleUseCase, selectedCharacterHolder),
                        featuresViewModel = FeaturesViewModel(getCharacterBundleUseCase, selectedCharacterHolder),
                        biographyViewModel = BiographyViewModel(getCharacterBundleUseCase, selectedCharacterHolder),
                        notesViewModel = NotesViewModel(getCharacterBundleUseCase, selectedCharacterHolder),
                        characterManagerViewModel = com.dndcharacterhandler.presentation.components.CharacterManagerViewModel(
                            characterRepository = characterRepository,
                            fileRepository = fileRepository,
                            languagePreferencesRepository = languagePreferencesRepository,
                            selectedCharacterHolder = selectedCharacterHolder
                        ),
                        localizationRepository = localizationRepository
                    )
                )
            }
        }
    }
}
