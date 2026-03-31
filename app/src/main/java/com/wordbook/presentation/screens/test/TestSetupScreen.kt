package com.wordbook.presentation.screens.test

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.wordbook.domain.model.TestMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestSetupScreen(
    deckId: Long,
    onBack: () -> Unit,
    onStartTest: (String) -> Unit
) {
    var selectedMode by remember { mutableStateOf(TestMode.SEQUENTIAL) }

    val modeDescriptions = mapOf(
        TestMode.SEQUENTIAL to "Cards are shown in the order they were added.",
        TestMode.RANDOM to "Cards are shown in a random shuffled order.",
        TestMode.NEEDS_REVIEW_FIRST to "Cards marked 'Needs Review' appear first, then the rest.",
        TestMode.SRS_DUE to "Only shows cards due for review today based on spaced repetition."
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test Setup") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Select Test Mode", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Column(Modifier.selectableGroup()) {
                TestMode.entries.forEach { mode ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedMode == mode,
                                onClick = { selectedMode = mode },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedMode == mode)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMode == mode,
                                onClick = null
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(mode.displayName, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    modeDescriptions[mode] ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { onStartTest(selectedMode.name) },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Start Test")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
