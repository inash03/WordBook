package com.wordbook.presentation.screens.card

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordbook.domain.model.Card
import com.wordbook.domain.model.Label
import com.wordbook.domain.usecase.card.SaveCardUseCase
import com.wordbook.domain.usecase.label.GetLabelsUseCase
import com.wordbook.domain.usecase.label.SaveLabelUseCase
import com.wordbook.domain.repository.CardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CardEditUiState(
    val front: String = "",
    val back: String = "",
    val selectedLabelIds: Set<Long> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEditMode: Boolean = false
)

sealed class CardEditEvent {
    object SavedSuccessfully : CardEditEvent()
    data class Error(val message: String) : CardEditEvent()
}

@HiltViewModel
class CardEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cardRepository: CardRepository,
    private val saveCardUseCase: SaveCardUseCase,
    private val getLabelsUseCase: GetLabelsUseCase,
    private val saveLabelUseCase: SaveLabelUseCase
) : ViewModel() {

    private val deckId: Long = checkNotNull(savedStateHandle["deckId"])
    private val cardId: Long? = savedStateHandle.get<Long>("cardId")?.takeIf { it != 0L }

    private val _uiState = MutableStateFlow(CardEditUiState(isEditMode = cardId != null))
    val uiState: StateFlow<CardEditUiState> = _uiState

    val allLabels: StateFlow<List<Label>> = getLabelsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _events = Channel<CardEditEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        if (cardId != null) {
            loadCard(cardId)
        }
    }

    private fun loadCard(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val card = cardRepository.getCardById(id)
                if (card != null) {
                    _uiState.update { state ->
                        state.copy(
                            front = card.front,
                            back = card.back,
                            selectedLabelIds = card.labels.map { it.id }.toSet(),
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Card not found") }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Failed to load card")
                }
            }
        }
    }

    fun onFrontChange(value: String) {
        _uiState.update { it.copy(front = value) }
    }

    fun onBackChange(value: String) {
        _uiState.update { it.copy(back = value) }
    }

    fun onLabelToggle(labelId: Long) {
        _uiState.update { state ->
            val current = state.selectedLabelIds
            state.copy(
                selectedLabelIds = if (labelId in current) current - labelId else current + labelId
            )
        }
    }

    fun saveCard() {
        val state = _uiState.value
        if (state.front.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Front (question) cannot be empty") }
            return
        }
        if (state.back.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Back (answer) cannot be empty") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val card = Card(
                    id = cardId ?: 0L,
                    deckId = deckId,
                    front = state.front.trim(),
                    back = state.back.trim()
                )
                saveCardUseCase(card, state.selectedLabelIds.toList())
                _uiState.update { it.copy(isLoading = false) }
                _events.send(CardEditEvent.SavedSuccessfully)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Failed to save card")
                }
                _events.send(CardEditEvent.Error(e.message ?: "Failed to save card"))
            }
        }
    }

    fun createLabel(name: String, color: String) {
        viewModelScope.launch {
            try {
                val label = Label(name = name, color = color)
                val newId = saveLabelUseCase(label)
                _uiState.update { state ->
                    state.copy(selectedLabelIds = state.selectedLabelIds + newId)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to create label") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
