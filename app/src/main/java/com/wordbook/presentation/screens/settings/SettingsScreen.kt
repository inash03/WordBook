package com.wordbook.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wordbook.domain.model.Label
import com.wordbook.domain.model.TestMode
import com.wordbook.presentation.theme.AccentColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val labels by viewModel.labels.collectAsStateWithLifecycle()
    var showNewLabelDialog by remember { mutableStateOf(false) }
    var editingLabel by remember { mutableStateOf<Label?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item { SectionHeader("Appearance") }

            item {
                SwitchPreference(
                    title = "Follow System Theme",
                    checked = prefs.followSystemTheme,
                    onCheckedChange = viewModel::setFollowSystem
                )
            }
            item {
                SwitchPreference(
                    title = "Dark Mode",
                    checked = prefs.isDarkMode,
                    onCheckedChange = viewModel::setDarkMode,
                    enabled = !prefs.followSystemTheme
                )
            }
            item {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Accent Color", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AccentColor.entries.forEach { accent ->
                            val selected = prefs.accentColor == accent.displayName
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(accent.light)
                                    .border(
                                        width = if (selected) 3.dp else 0.dp,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.setAccentColor(accent.displayName) }
                            )
                        }
                    }
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { SectionHeader("Study") }
            item {
                var expanded by remember { mutableStateOf(false) }
                ListItem(
                    headlineContent = { Text("Default Test Mode") },
                    supportingContent = {
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                            OutlinedTextField(
                                value = TestMode.entries.find { it.name == prefs.defaultTestMode }?.displayName ?: prefs.defaultTestMode,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                TestMode.entries.forEach { mode ->
                                    DropdownMenuItem(
                                        text = { Text(mode.displayName) },
                                        onClick = {
                                            viewModel.setDefaultTestMode(mode.name)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader("Labels", padded = false)
                    IconButton(onClick = { showNewLabelDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add label")
                    }
                }
            }
            items(labels) { label ->
                ListItem(
                    headlineContent = { Text(label.name) },
                    leadingContent = {
                        val color = runCatching { Color(android.graphics.Color.parseColor(label.color)) }
                            .getOrDefault(Color(0xFF6200EE))
                        Box(Modifier.size(20.dp).clip(CircleShape).background(color))
                    },
                    trailingContent = {
                        Row {
                            IconButton(onClick = { editingLabel = label }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { viewModel.deleteLabel(label.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                )
            }
        }

        if (showNewLabelDialog || editingLabel != null) {
            LabelEditDialog(
                label = editingLabel,
                onConfirm = { name, color ->
                    val lbl = editingLabel?.copy(name = name, color = color) ?: Label(name = name, color = color)
                    viewModel.saveLabel(lbl)
                    showNewLabelDialog = false
                    editingLabel = null
                },
                onDismiss = { showNewLabelDialog = false; editingLabel = null }
            )
        }
    }
}

@Composable
private fun LabelEditDialog(label: Label?, onConfirm: (String, String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(label?.name ?: "") }
    val colorOptions = listOf("#6200EE", "#1565C0", "#2E7D32", "#E65100", "#00695C", "#C62828")
    var selectedColor by remember { mutableStateOf(label?.color ?: colorOptions[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (label == null) "New Label" else "Edit Label") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Label name") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colorOptions.forEach { hex ->
                        val color = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color.Gray)
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(if (selectedColor == hex) 2.dp else 0.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                                .clickable { selectedColor = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name, selectedColor) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun SectionHeader(title: String, padded: Boolean = true) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = if (padded) Modifier.padding(horizontal = 16.dp, vertical = 4.dp) else Modifier
    )
}

@Composable
private fun SwitchPreference(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, enabled: Boolean = true) {
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
        },
        modifier = Modifier.clickable(enabled = enabled) { onCheckedChange(!checked) }
    )
}
