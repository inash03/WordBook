package com.wordbook.presentation.screens.study

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordbook.domain.model.Card
import com.wordbook.domain.model.StudyStatus
import com.wordbook.domain.usecase.card.GetCardsForDeckUseCase
import com.wordbook.domain.usecase.card.UpdateCardStudyStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudyUiState(
    val cards: List<Card> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val isComplete: Boolean = false,
    val rememberedCount: Int = 0,
    val needsReviewCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val currentCard: Card? get() = cards.getOrNull(currentIndex)
    val total: Int get() = cards.size
    val progress: Float get() = if (total == 0) 0f else currentIndex.toFloat() / total.toFloat()
}

@HiltViewModel
class StudyViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCardsForDeckUseCase: GetCardsForDeckUseCase,
    private val updateCardStudyStatusUseCase: UpdateCardStudyStatusUseCase
) : ViewModel() {

    private val deckId: Long = checkNotNull(savedStateHandle["deckId"])

    private val _uiState = MutableStateFlow(StudyUiState())
    val uiState: StateFlow<StudyUiState> = _uiState

    init {
        loadCards()
    }

    private fun loadCards() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val cards = getCardsForDeckUseCase(deckId).first()
                _uiState.update { it.copy(cards = cards, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Failed to load cards")
                }
            }
        }
    }

    fun flipCard() {
        _uiState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    fun markRemembered() {
        val state = _uiState.value
        val card = state.currentCard ?: return
        viewModelScope.launch {
            try {
                updateCardStudyStatusUseCase(card.id, StudyStatus.REMEMBERED)
                val updatedCards = state.cards.toMutableList().also { list ->
                    list[state.currentIndex] = card.copy(studyStatus = StudyStatus.REMEMBERED)
                }
                _uiState.update {
                    it.copy(cards = updatedCards, rememberedCount = it.rememberedCount + 1)
                }
                advanceCard()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to update card") }
            }
        }
    }

    fun markNeedsReview() {
        val state = _uiState.value
        val card = state.currentCard ?: return
        viewModelScope.launch {
            try {
                updateCardStudyStatusUseCase(card.id, StudyStatus.NEEDS_REVIEW)
                val updatedCards = state.cards.toMutableList().also { list ->
                    list[state.currentIndex] = card.copy(studyStatus = StudyStatus.NEEDS_REVIEW)
                }
                _uiState.update {
                    it.copy(cards = updatedCards, needsReviewCount = it.needsReviewCount + 1)
                }
                advanceCard()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to update card") }
            }
        }
    }

    private fun advanceCard() {
        val state = _uiState.value
        val nextIndex = state.currentIndex + 1
        if (nextIndex >= state.total) {
            _uiState.update { it.copy(isComplete = true, isFlipped = false) }
        } else {
            _uiState.update { it.copy(currentIndex = nextIndex, isFlipped = false) }
        }
    }

    fun nextCard() {
        val state = _uiState.value
        if (state.currentIndex < state.total - 1) {
            _uiState.update { it.copy(currentIndex = it.currentIndex + 1, isFlipped = false) }
        }
    }

    fun previousCard() {
        val state = _uiState.value
        if (state.currentIndex > 0) {
            _uiState.update { it.copy(currentIndex = it.currentIndex - 1, isFlipped = false) }
        }
    }

    fun restart() {
        _uiState.update {
            it.copy(
                currentIndex = 0,
                isFlipped = false,
                isComplete = false,
                rememberedCount = 0,
                needsReviewCount = 0
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
