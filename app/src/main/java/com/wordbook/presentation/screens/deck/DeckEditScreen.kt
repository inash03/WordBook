package com.wordbook.presentation.screens.deck

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wordbook.presentation.components.LabelChip
import com.wordbook.presentation.components.LabelPickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckEditScreen(
    deckId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: DeckEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allLabels by viewModel.allLabels.collectAsStateWithLifecycle()
    var showLabelPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is DeckEditEvent.SavedSuccessfully) onSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit Deck" else "Create Deck") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveDeck() }, enabled = !uiState.isLoading) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Deck Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.errorMessage != null && uiState.name.isBlank()
            )

            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // Label section
            Column {
                Text("Labels", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                val selectedLabels = allLabels.filter { it.id in uiState.selectedLabelIds }
                if (selectedLabels.isNotEmpty()) {
                    LazyRow {
                        items(selectedLabels) { label ->
                            LabelChip(label = label, selected = true)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                OutlinedButton(onClick = { showLabelPicker = true }) {
                    Icon(Icons.Default.Label, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Manage Labels")
                }
            }

            uiState.errorMessage?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.saveDeck() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Text(if (uiState.isEditMode) "Save Changes" else "Create Deck")
            }
        }

        if (showLabelPicker) {
            LabelPickerDialog(
                allLabels = allLabels,
                selectedLabelIds = uiState.selectedLabelIds,
                onConfirm = { selected ->
                    selected.forEach { id ->
                        if (id !in uiState.selectedLabelIds) viewModel.onLabelToggle(id)
                    }
                    uiState.selectedLabelIds.forEach { id ->
                        if (id !in selected) viewModel.onLabelToggle(id)
                    }
                    showLabelPicker = false
                },
                onDismiss = { showLabelPicker = false },
                onCreateLabel = { name, color ->
                    viewModel.createLabel(name, color)
                }
            )
        }
    }
}
