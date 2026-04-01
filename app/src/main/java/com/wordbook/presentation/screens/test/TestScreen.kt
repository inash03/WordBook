package com.wordbook.presentation.screens.test

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wordbook.presentation.components.EmptyState
import com.wordbook.presentation.components.FlippableCard
import com.wordbook.presentation.theme.ColorCorrect
import com.wordbook.presentation.theme.ColorIncorrect
import com.wordbook.presentation.theme.ColorSkipped

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestScreen(
    deckId: Long,
    mode: String,
    onFinished: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: TestViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete && uiState.sessionId != 0L) {
            onFinished(uiState.sessionId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (!uiState.isLoading && uiState.cards.isNotEmpty()) {
                        Text("${uiState.progress} / ${uiState.total}")
                    } else {
                        Text("Test")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.cards.isEmpty() -> EmptyState(
                    title = "No cards to test",
                    subtitle = "No cards are due for this test mode. Try a different mode or study some cards first."
                )
                else -> {
                    val card = uiState.currentCard
                    if (card != null) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LinearProgressIndicator(
                                progress = { uiState.currentIndex.toFloat() / uiState.total },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(16.dp))

                            FlippableCard(
                                front = card.front,
                                back = card.back,
                                isFlipped = uiState.isFlipped,
                                onFlip = viewModel::flipCard,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(16.dp))

                            if (!uiState.isFlipped) {
                                Text(
                                    "Tap card to reveal answer",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text("How did you do?", style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = viewModel::answerCorrect,
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = ColorCorrect)
                                    ) { Text("Correct") }
                                    Button(
                                        onClick = viewModel::answerIncorrect,
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = ColorIncorrect)
                                    ) { Text("Incorrect") }
                                    OutlinedButton(
                                        onClick = viewModel::skip,
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Skip") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
