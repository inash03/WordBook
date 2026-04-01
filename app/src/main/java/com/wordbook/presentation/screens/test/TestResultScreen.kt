package com.wordbook.presentation.screens.test

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.wordbook.domain.model.ResultType
import com.wordbook.domain.model.TestResult
import com.wordbook.domain.model.TestSession
import com.wordbook.domain.repository.TestRepository
import com.wordbook.domain.usecase.test.GetTestHistoryUseCase
import com.wordbook.presentation.theme.ColorCorrect
import com.wordbook.presentation.theme.ColorIncorrect
import com.wordbook.presentation.theme.ColorSkipped
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class TestResultUiState(
    val session: TestSession? = null,
    val results: List<TestResult> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class TestResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTestHistoryUseCase: GetTestHistoryUseCase,
    private val testRepository: TestRepository
) : ViewModel() {

    private val sessionId: Long = checkNotNull(savedStateHandle["sessionId"])
    private val _isLoading = MutableStateFlow(true)
    val uiState: StateFlow<TestResultUiState> = combine(
        getTestHistoryUseCase(),
        getTestHistoryUseCase.resultsForSession(sessionId)
    ) { sessions, results ->
        TestResultUiState(
            session = sessions.find { it.id == sessionId },
            results = results,
            isLoading = false
        )
    }.let { flow ->
        var state = TestResultUiState()
        viewModelScope.launch {
            flow.collect { state = it }
        }
        MutableStateFlow(state).also { sf ->
            viewModelScope.launch { flow.collect { sf.value = it } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestResultScreen(
    sessionId: Long,
    onBack: () -> Unit,
    onHome: () -> Unit,
    viewModel: TestResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("All", "Correct", "Incorrect", "Skipped")

    val filteredResults = remember(uiState.results, selectedTab) {
        when (selectedTab) {
            1 -> uiState.results.filter { it.result == ResultType.CORRECT }
            2 -> uiState.results.filter { it.result == ResultType.INCORRECT }
            3 -> uiState.results.filter { it.result == ResultType.SKIPPED }
            else -> uiState.results
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test Results") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val session = uiState.session
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (session != null) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(session.deckName, style = MaterialTheme.typography.titleMedium)
                            Text(
                                SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault())
                                    .format(Date(session.startedAt)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("Mode: ${session.mode.displayName}", style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "${session.accuracyPercent}% Accuracy",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("✓ ${session.correctCount}", color = ColorCorrect)
                                Text("✗ ${session.incorrectCount}", color = ColorIncorrect)
                                Text("— ${session.skippedCount}", color = ColorSkipped)
                            }
                        }
                    }
                }
            }

            item {
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                    }
                }
            }

            items(filteredResults) { result ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(result.cardFront, style = MaterialTheme.typography.bodyMedium)
                            Text(result.cardBack, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        val (label, color) = when (result.result) {
                            ResultType.CORRECT -> "Correct" to ColorCorrect
                            ResultType.INCORRECT -> "Incorrect" to ColorIncorrect
                            ResultType.SKIPPED -> "Skipped" to ColorSkipped
                        }
                        SuggestionChip(onClick = {}, label = { Text(label) },
                            colors = SuggestionChipDefaults.suggestionChipColors(labelColor = color))
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Back to Deck") }
                    Button(onClick = onHome, modifier = Modifier.weight(1f)) { Text("Home") }
                }
            }
        }
    }
}
