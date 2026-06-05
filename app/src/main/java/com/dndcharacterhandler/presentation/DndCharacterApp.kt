package com.dndcharacterhandler.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dndcharacterhandler.data.localization.LocalizationRepository
import com.dndcharacterhandler.presentation.attributes.AttributesScreen
import com.dndcharacterhandler.presentation.biography.BiographyScreen
import com.dndcharacterhandler.presentation.combat.CombatScreen
import com.dndcharacterhandler.presentation.components.BottomNavigationBar
import com.dndcharacterhandler.presentation.components.CharacterManagerDrawer
import com.dndcharacterhandler.presentation.features.FeaturesScreen
import com.dndcharacterhandler.presentation.inventory.InventoryScreen
import com.dndcharacterhandler.presentation.localization.LocalStrings
import com.dndcharacterhandler.presentation.localization.text
import com.dndcharacterhandler.presentation.notes.NotesScreen
import com.dndcharacterhandler.presentation.overview.OverviewScreen
import com.dndcharacterhandler.presentation.spells.SpellsScreen
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class DndCharacterAppState(
    val overviewViewModel: com.dndcharacterhandler.presentation.overview.OverviewViewModel,
    val attributesViewModel: com.dndcharacterhandler.presentation.attributes.AttributesViewModel,
    val combatViewModel: com.dndcharacterhandler.presentation.combat.CombatViewModel,
    val inventoryViewModel: com.dndcharacterhandler.presentation.inventory.InventoryViewModel,
    val spellsViewModel: com.dndcharacterhandler.presentation.spells.SpellsViewModel,
    val featuresViewModel: com.dndcharacterhandler.presentation.features.FeaturesViewModel,
    val biographyViewModel: com.dndcharacterhandler.presentation.biography.BiographyViewModel,
    val notesViewModel: com.dndcharacterhandler.presentation.notes.NotesViewModel,
    val characterManagerViewModel: com.dndcharacterhandler.presentation.components.CharacterManagerViewModel,
    val localizationRepository: LocalizationRepository
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DndCharacterApp(appState: DndCharacterAppState) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AppScreen.Overview.route
    val currentScreen = bottomNavigationScreens.firstOrNull { it.route == currentRoute } ?: AppScreen.Overview
    val managerState by appState.characterManagerViewModel.uiState.collectAsStateWithLifecycle()
    val strings = remember(managerState.language) {
        appState.localizationRepository.getStrings(managerState.language)
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val selectedCharacterName = managerState.characters
        .firstOrNull { it.character.id == managerState.selectedCharacterId }
        ?.character
        ?.name
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            appState.characterManagerViewModel.exportCharacter(uri.toString())
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            appState.characterManagerViewModel.importCharacter(uri.toString())
        }
    }

    LaunchedEffect(appState.characterManagerViewModel, strings) {
        appState.characterManagerViewModel.events.collect { messageKey ->
            snackbarHostState.showSnackbar(strings[messageKey])
        }
    }

    CompositionLocalProvider(LocalStrings provides strings) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CharacterManagerDrawer(
                    state = managerState,
                    onSelectCharacter = appState.characterManagerViewModel::selectCharacter,
                    onCreateCharacter = appState.characterManagerViewModel::createCharacter,
                    onExportCharacter = {
                        exportLauncher.launch(suggestCharacterArchiveName(selectedCharacterName))
                    },
                    onDeleteCharacter = appState.characterManagerViewModel::deleteCurrentCharacter,
                    onImportCharacter = {
                        importLauncher.launch(arrayOf("application/octet-stream", "application/zip", "*/*"))
                    },
                    onLanguageSelected = appState.characterManagerViewModel::setLanguage
                )
            }
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets.systemBars,
                topBar = {
                    TopAppBar(
                        windowInsets = TopAppBarDefaults.windowInsets,
                        title = { Text(text(currentScreen.titleKey)) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = text("drawer_open_character_manager")
                                )
                            }
                        }
                    )
                },
                bottomBar = {
                    BottomNavigationBar(
                        currentRoute = currentRoute,
                        screens = bottomNavigationScreens,
                        onNavigate = { screen ->
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                },
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState)
                }
            ) { padding ->
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color.Transparent)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = AppScreen.Overview.route
                    ) {
                        composable(AppScreen.Overview.route) {
                            OverviewScreen(appState.overviewViewModel)
                        }
                        composable(AppScreen.Attributes.route) {
                            AttributesScreen(appState.attributesViewModel)
                        }
                        composable(AppScreen.Combat.route) {
                            CombatScreen(appState.combatViewModel)
                        }
                        composable(AppScreen.Inventory.route) {
                            InventoryScreen(appState.inventoryViewModel)
                        }
                        composable(AppScreen.Spells.route) {
                            SpellsScreen(appState.spellsViewModel)
                        }
                        composable(AppScreen.Features.route) {
                            FeaturesScreen(appState.featuresViewModel)
                        }
                        composable(AppScreen.Biography.route) {
                            BiographyScreen(appState.biographyViewModel)
                        }
                        composable(AppScreen.Notes.route) {
                            NotesScreen(appState.notesViewModel)
                        }
                    }
                }
            }
        }
    }
}

private fun suggestCharacterArchiveName(characterName: String?): String {
    val baseName = characterName
        ?.trim()
        ?.ifBlank { null }
        ?.replace(Regex("[^A-Za-z0-9._-]+"), "_")
        ?: "character"
    return "$baseName.dndchar"
}
