package com.wordbook.presentation.screens.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wordbook.presentation.theme.ColorNeedsReview
import com.wordbook.presentation.theme.ColorNotStudied
import com.wordbook.presentation.theme.ColorRemembered
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Total Cards", uiState.totalCards.toString(), modifier = Modifier.weight(1f))
                    StatCard("Due Today", uiState.dueToday.toString(), modifier = Modifier.weight(1f))
                    StatCard("Streak", "${uiState.currentStreak}d", modifier = Modifier.weight(1f))
                }
            }

            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Study Progress", style = MaterialTheme.typography.titleMedium)
                        ProgressRow("Remembered", uiState.rememberedCount, uiState.totalCards, ColorRemembered)
                        ProgressRow("Needs Review", uiState.needsReviewCount, uiState.totalCards, ColorNeedsReview)
                        ProgressRow("Not Studied", uiState.notStudiedCount, uiState.totalCards, ColorNotStudied)
                    }
                }
            }

            if (uiState.recentSessions.isNotEmpty()) {
                item {
                    Text("Recent Sessions", style = MaterialTheme.typography.titleMedium)
                }
                items(uiState.recentSessions) { session ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(session.deckName, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                                        .format(Date(session.startedAt)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                "${session.accuracyPercent}%",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (uiState.deckStats.isNotEmpty()) {
                item {
                    Text("Per-Deck Progress", style = MaterialTheme.typography.titleMedium)
                }
                items(uiState.deckStats) { stat ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stat.deckName, style = MaterialTheme.typography.bodyMedium)
                                Text("${stat.remembered}/${stat.total}", style = MaterialTheme.typography.bodySmall)
                            }
                            LinearProgressIndicator(
                                progress = { stat.progress },
                                modifier = Modifier.fillMaxWidth(),
                                color = ColorRemembered
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProgressRow(label: String, count: Int, total: Int, color: androidx.compose.ui.graphics.Color) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text(count.toString(), style = MaterialTheme.typography.bodySmall)
        }
        LinearProgressIndicator(
            progress = { if (total == 0) 0f else count.toFloat() / total },
            modifier = Modifier.fillMaxWidth(),
            color = color
        )
    }
}
