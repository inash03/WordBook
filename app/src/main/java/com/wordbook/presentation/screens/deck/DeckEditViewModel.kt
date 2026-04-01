package com.wordbook.presentation.screens.deck

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordbook.domain.model.Deck
import com.wordbook.domain.model.Label
import com.wordbook.domain.usecase.deck.GetDeckByIdUseCase
import com.wordbook.domain.usecase.deck.SaveDeckUseCase
import com.wordbook.domain.usecase.label.GetLabelsUseCase
import com.wordbook.domain.usecase.label.SaveLabelUseCase
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

data class DeckEditUiState(
    val name: String = "",
    val description: String = "",
    val selectedLabelIds: Set<Long> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEditMode: Boolean = false
)

sealed class DeckEditEvent {
    object SavedSuccessfully : DeckEditEvent()
}

@HiltViewModel
class DeckEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getDeckByIdUseCase: GetDeckByIdUseCase,
    private val saveDeckUseCase: SaveDeckUseCase,
    private val getLabelsUseCase: GetLabelsUseCase,
    private val saveLabelUseCase: SaveLabelUseCase
) : ViewModel() {

    private val deckId: Long? = savedStateHandle.get<Long>("deckId")?.takeIf { it != 0L }

    private val _uiState = MutableStateFlow(DeckEditUiState(isEditMode = deckId != null))
    val uiState: StateFlow<DeckEditUiState> = _uiState

    val allLabels: StateFlow<List<Label>> = getLabelsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _events = Channel<DeckEditEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        if (deckId != null) loadDeck(deckId)
    }

    private fun loadDeck(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val deck = getDeckByIdUseCase(id)
            if (deck != null) {
                _uiState.update {
                    it.copy(
                        name = deck.name,
                        description = deck.description,
                        selectedLabelIds = deck.labels.map { l -> l.id }.toSet(),
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Deck not found") }
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }

    fun onLabelToggle(labelId: Long) {
        _uiState.update { state ->
            val current = state.selectedLabelIds
            state.copy(selectedLabelIds = if (labelId in current) current - labelId else current + labelId)
        }
    }

    fun saveDeck() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Deck name cannot be empty") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val deck = Deck(id = deckId ?: 0L, name = state.name.trim(), description = state.description.trim())
                saveDeckUseCase(deck, state.selectedLabelIds.toList())
                _uiState.update { it.copy(isLoading = false) }
                _events.send(DeckEditEvent.SavedSuccessfully)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Failed to save deck") }
            }
        }
    }

    fun createLabel(name: String, color: String) {
        viewModelScope.launch {
            try {
                val newId = saveLabelUseCase(Label(name = name, color = color))
                _uiState.update { state -> state.copy(selectedLabelIds = state.selectedLabelIds + newId) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to create label") }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
