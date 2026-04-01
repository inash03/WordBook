package com.wordbook.presentation.screens.test

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordbook.domain.model.Card
import com.wordbook.domain.model.ResultType
import com.wordbook.domain.model.TestMode
import com.wordbook.domain.model.TestResult
import com.wordbook.domain.repository.CardRepository
import com.wordbook.domain.usecase.card.GetCardsForDeckUseCase
import com.wordbook.domain.usecase.deck.GetDeckByIdUseCase
import com.wordbook.domain.usecase.srs.UpdateSrsUseCase
import com.wordbook.domain.usecase.test.FinishTestSessionUseCase
import com.wordbook.domain.usecase.test.SaveTestResultUseCase
import com.wordbook.domain.usecase.test.StartTestSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TestUiState(
    val cards: List<Card> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val sessionId: Long = 0L,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val skippedCount: Int = 0,
    val isComplete: Boolean = false,
    val testResults: List<TestResult> = emptyList(),
    val isLoading: Boolean = true,
    val deckName: String = ""
) {
    val currentCard: Card? get() = cards.getOrNull(currentIndex)
    val progress: Int get() = if (cards.isEmpty()) 0 else currentIndex + 1
    val total: Int get() = cards.size
}

@HiltViewModel
class TestViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getDeckByIdUseCase: GetDeckByIdUseCase,
    private val getCardsForDeckUseCase: GetCardsForDeckUseCase,
    private val cardRepository: CardRepository,
    private val startTestSessionUseCase: StartTestSessionUseCase,
    private val finishTestSessionUseCase: FinishTestSessionUseCase,
    private val saveTestResultUseCase: SaveTestResultUseCase,
    private val updateSrsUseCase: UpdateSrsUseCase
) : ViewModel() {

    private val deckId: Long = checkNotNull(savedStateHandle["deckId"])
    private val modeStr: String = checkNotNull(savedStateHandle["mode"])
    private val mode: TestMode = TestMode.valueOf(modeStr)

    private val _uiState = MutableStateFlow(TestUiState())
    val uiState: StateFlow<TestUiState> = _uiState

    init {
        loadTest()
    }

    private fun loadTest() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val deck = getDeckByIdUseCase(deckId)
                val deckName = deck?.name ?: ""
                val allCards = getCardsForDeckUseCase(deckId).first()
                val cards = orderCards(allCards)
                if (cards.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, deckName = deckName, cards = emptyList()) }
                    return@launch
                }
                val sessionId = startTestSessionUseCase(deckId, deckName, mode, cards.size)
                _uiState.update {
                    it.copy(isLoading = false, cards = cards, sessionId = sessionId, deckName = deckName)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun orderCards(cards: List<Card>): List<Card> = when (mode) {
        TestMode.SEQUENTIAL -> cards.sortedBy { it.createdAt }
        TestMode.RANDOM -> cards.shuffled()
        TestMode.NEEDS_REVIEW_FIRST -> cards.sortedWith(
            compareBy({ if (it.studyStatus.name == "NEEDS_REVIEW") 0 else 1 }, { it.createdAt })
        )
        TestMode.SRS_DUE -> {
            val now = System.currentTimeMillis()
            cards.filter { it.srsRepetitions > 0 && it.srsDueDate <= now }.sortedBy { it.srsDueDate }
        }
    }

    fun flipCard() = _uiState.update { it.copy(isFlipped = !it.isFlipped) }

    fun answerCorrect() = recordAnswer(ResultType.CORRECT)
    fun answerIncorrect() = recordAnswer(ResultType.INCORRECT)
    fun skip() = recordAnswer(ResultType.SKIPPED)

    private fun recordAnswer(result: ResultType) {
        val state = _uiState.value
        val card = state.currentCard ?: return
        viewModelScope.launch {
            val testResult = TestResult(
                sessionId = state.sessionId,
                cardId = card.id,
                cardFront = card.front,
                cardBack = card.back,
                result = result,
                answeredAt = System.currentTimeMillis()
            )
            saveTestResultUseCase(testResult)

            if (result != ResultType.SKIPPED) {
                updateSrsUseCase(card.id, card.srsInterval, card.srsEaseFactor, card.srsRepetitions, result)
            }

            val newResults = state.testResults + testResult
            val newCorrect = if (result == ResultType.CORRECT) state.correctCount + 1 else state.correctCount
            val newIncorrect = if (result == ResultType.INCORRECT) state.incorrectCount + 1 else state.incorrectCount
            val newSkipped = if (result == ResultType.SKIPPED) state.skippedCount + 1 else state.skippedCount
            val nextIndex = state.currentIndex + 1
            val isComplete = nextIndex >= state.cards.size

            if (isComplete) {
                finishTestSessionUseCase(state.sessionId, newCorrect, newIncorrect, newSkipped)
            }

            _uiState.update {
                it.copy(
                    currentIndex = if (isComplete) it.currentIndex else nextIndex,
                    isFlipped = false,
                    correctCount = newCorrect,
                    incorrectCount = newIncorrect,
                    skippedCount = newSkipped,
                    isComplete = isComplete,
                    testResults = newResults
                )
            }
        }
    }

    fun restart() {
        loadTest()
    }
}
