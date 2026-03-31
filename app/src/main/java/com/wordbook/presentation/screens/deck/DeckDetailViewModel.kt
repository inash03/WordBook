package com.wordbook.presentation.screens.deck

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordbook.domain.model.Card
import com.wordbook.domain.model.Deck
import com.wordbook.domain.usecase.card.DeleteCardsUseCase
import com.wordbook.domain.usecase.card.GetCardsForDeckUseCase
import com.wordbook.domain.usecase.deck.GetDeckByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeckDetailUiState(
    val deck: Deck? = null,
    val cards: List<Card> = emptyList(),
    val selectedCardIds: Set<Long> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class DeckDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getDeckByIdUseCase: GetDeckByIdUseCase,
    private val getCardsForDeckUseCase: GetCardsForDeckUseCase,
    private val deleteCardsUseCase: DeleteCardsUseCase,
) : ViewModel() {

    private val deckId: Long = checkNotNull(savedStateHandle["deckId"])

    private val _deck = MutableStateFlow<Deck?>(null)
    private val _selectedCardIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val _cards: StateFlow<List<Card>> = getCardsForDeckUseCase(deckId)
        .catch { e -> _errorMessage.update { e.message ?: "Failed to load cards" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<DeckDetailUiState> = combine(
        _deck,
        _cards,
        _selectedCardIds,
        _isLoading,
        _errorMessage,
    ) { deck, cards, selectedCardIds, isLoading, errorMessage ->
        DeckDetailUiState(
            deck = deck,
            cards = cards,
            selectedCardIds = selectedCardIds,
            isLoading = isLoading,
            errorMessage = errorMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DeckDetailUiState(isLoading = true),
    )

    init {
        loadDeck()
    }

    private fun loadDeck() {
        viewModelScope.launch {
            _isLoading.update { true }
            try {
                _deck.update { getDeckByIdUseCase(deckId) }
            } catch (e: Exception) {
                _errorMessage.update { e.message ?: "Failed to load deck" }
            } finally {
                _isLoading.update { false }
            }
        }
    }

    fun deleteCard(id: Long) {
        viewModelScope.launch {
            try {
                deleteCardsUseCase.single(id)
                _selectedCardIds.update { it - id }
            } catch (e: Exception) {
                _errorMessage.update { e.message ?: "Failed to delete card" }
            }
        }
    }

    fun deleteSelectedCards(ids: List<Long>) {
        viewModelScope.launch {
            try {
                deleteCardsUseCase(ids)
                _selectedCardIds.update { emptySet() }
            } catch (e: Exception) {
                _errorMessage.update { e.message ?: "Failed to delete cards" }
            }
        }
    }

    fun toggleCardSelection(id: Long) {
        _selectedCardIds.update { current ->
            if (id in current) current - id else current + id
        }
    }

    fun selectAll() {
        _selectedCardIds.update { _cards.value.map { it.id }.toSet() }
    }

    fun clearSelection() {
        _selectedCardIds.update { emptySet() }
    }

    fun clearError() {
        _errorMessage.update { null }
    }
}
