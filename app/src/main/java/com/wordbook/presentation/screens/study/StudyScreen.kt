package com.wordbook.presentation.screens.study

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wordbook.presentation.components.EmptyState
import com.wordbook.presentation.components.FlippableCard
import com.wordbook.presentation.theme.ColorNeedsReview
import com.wordbook.presentation.theme.ColorRemembered

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    deckId: Long,
    onBack: () -> Unit,
    viewModel: StudyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Study") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.cards.isEmpty() -> {
                EmptyState(
                    title = "No cards to study",
                    subtitle = "Add some cards to this deck first",
                    modifier = Modifier.padding(innerPadding)
                )
            }

            uiState.isComplete -> {
                StudyCompleteScreen(
                    rememberedCount = uiState.rememberedCount,
                    needsReviewCount = uiState.needsReviewCount,
                    total = uiState.total,
                    onRestart = viewModel::restart,
                    onBack = onBack,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            else -> {
                val card = uiState.currentCard
                if (card != null) {
                    StudyCardContent(
                        uiState = uiState,
                        onFlip = viewModel::flipCard,
                        onMarkRemembered = viewModel::markRemembered,
                        onMarkNeedsReview = viewModel::markNeedsReview,
                        onPrevious = viewModel::previousCard,
                        onNext = viewModel::nextCard,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
private fun StudyCardContent(
    uiState: StudyUiState,
    onFlip: () -> Unit,
    onMarkRemembered: () -> Unit,
    onMarkNeedsReview: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val card = uiState.currentCard ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Progress indicator
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${uiState.currentIndex + 1} / ${uiState.total}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${uiState.rememberedCount} remembered",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ColorRemembered
                )
            }
            LinearProgressIndicator(
                progress = { (uiState.currentIndex + 1).toFloat() / uiState.total.toFloat() },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Flippable card
        FlippableCard(
            front = card.front,
            back = card.back,
            isFlipped = uiState.isFlipped,
            onFlip = onFlip,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        // Instruction text
        Text(
            text = if (uiState.isFlipped) "How well did you remember?" else "Tap card to flip",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Action buttons — shown only after flip
        if (uiState.isFlipped) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onMarkNeedsReview,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorNeedsReview)
                ) {
                    Text("Needs Review")
                }
                Button(
                    onClick = onMarkRemembered,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorRemembered)
                ) {
                    Text("Remembered")
                }
            }
        }

        // Navigation arrows
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPrevious,
                enabled = uiState.currentIndex > 0
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous card"
                )
            }
            Text(
                text = "${uiState.currentIndex + 1} of ${uiState.total}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(
                onClick = onNext,
                enabled = uiState.currentIndex < uiState.total - 1
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next card"
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun StudyCompleteScreen(
    rememberedCount: Int,
    needsReviewCount: Int,
    total: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Session Complete!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Results",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Cards")
                    Text("$total", style = MaterialTheme.typography.bodyLarge)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Remembered", color = ColorRemembered)
                    Text(
                        "$rememberedCount",
                        color = ColorRemembered,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Needs Review", color = ColorNeedsReview)
                    Text(
                        "$needsReviewCount",
                        color = ColorNeedsReview,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Study Again")
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Deck")
        }
    }
}
