package com.wordbook.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wordbook.domain.model.Label

@Composable
fun LabelPickerDialog(
    allLabels: List<Label>,
    selectedLabelIds: Set<Long>,
    onConfirm: (Set<Long>) -> Unit,
    onDismiss: () -> Unit,
    onCreateLabel: ((String, String) -> Unit)? = null
) {
    var selected by remember { mutableStateOf(selectedLabelIds) }
    var showCreate by remember { mutableStateOf(false) }
    var newLabelName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Labels") },
        text = {
            Column {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(allLabels) { label ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            LabelChip(label = label, selected = label.id in selected) {
                                selected = if (label.id in selected) selected - label.id else selected + label.id
                            }
                        }
                    }
                }
                if (onCreateLabel != null) {
                    Spacer(Modifier.height(8.dp))
                    if (showCreate) {
                        OutlinedTextField(
                            value = newLabelName,
                            onValueChange = { newLabelName = it },
                            label = { Text("New label name") },
                            singleLine = true,
                            trailingIcon = {
                                TextButton(onClick = {
                                    if (newLabelName.isNotBlank()) {
                                        onCreateLabel(newLabelName, "#6200EE")
                                        newLabelName = ""
                                        showCreate = false
                                    }
                                }) { Text("Add") }
                            }
                        )
                    } else {
                        TextButton(onClick = { showCreate = true }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Create new label")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text("Done") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
