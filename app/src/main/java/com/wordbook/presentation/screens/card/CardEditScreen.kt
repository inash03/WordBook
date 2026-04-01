package com.wordbook.presentation.screens.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wordbook.presentation.components.LabelChip
import com.wordbook.presentation.components.LabelPickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEditScreen(
    deckId: Long,
    cardId: Long? = null,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: CardEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allLabels by viewModel.allLabels.collectAsStateWithLifecycle()
    var showLabelPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CardEditEvent.SavedSuccessfully -> onSaved()
                is CardEditEvent.Error -> { /* error surfaced via uiState.errorMessage */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit Card" else "Add Card") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveCard() },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.front,
                    onValueChange = viewModel::onFrontChange,
                    label = { Text("Front (Question)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    isError = uiState.front.isBlank() && uiState.errorMessage != null,
                    supportingText = if (uiState.front.isBlank() && uiState.errorMessage?.contains("Front") == true) {
                        { Text("This field is required") }
                    } else null
                )

                OutlinedTextField(
                    value = uiState.back,
                    onValueChange = viewModel::onBackChange,
                    label = { Text("Back (Answer)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    isError = uiState.back.isBlank() && uiState.errorMessage != null,
                    supportingText = if (uiState.back.isBlank() && uiState.errorMessage?.contains("Back") == true) {
                        { Text("This field is required") }
                    } else null
                )

                // Labels section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Labels",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val selectedLabels = allLabels.filter { it.id in uiState.selectedLabelIds }
                    if (selectedLabels.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(selectedLabels) { label ->
                                LabelChip(
                                    label = label,
                                    selected = true,
                                    onToggle = { viewModel.onLabelToggle(label.id) }
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { showLabelPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Label,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Manage Labels")
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.saveCard() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    Text(if (uiState.isEditMode) "Save Changes" else "Add Card")
                }
            }
        }
    }

    if (showLabelPicker) {
        LabelPickerDialog(
            allLabels = allLabels,
            selectedLabelIds = uiState.selectedLabelIds,
            onConfirm = { newSelection ->
                val toRemove = uiState.selectedLabelIds - newSelection
                val toAdd = newSelection - uiState.selectedLabelIds
                toRemove.forEach { viewModel.onLabelToggle(it) }
                toAdd.forEach { viewModel.onLabelToggle(it) }
                showLabelPicker = false
            },
            onDismiss = { showLabelPicker = false },
            onCreateLabel = { name, color ->
                viewModel.createLabel(name, color)
            }
        )
    }
}
