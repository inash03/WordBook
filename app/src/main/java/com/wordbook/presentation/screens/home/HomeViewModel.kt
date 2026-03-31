package com.wordbook.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordbook.data.importexport.DeckSerializer
import com.wordbook.data.importexport.toDomainCard
import com.wordbook.data.importexport.toDomainDeck
import com.wordbook.domain.model.Deck
import com.wordbook.domain.model.Label
import com.wordbook.domain.usecase.card.SaveCardUseCase
import com.wordbook.domain.usecase.deck.DeleteDeckUseCase
import com.wordbook.domain.usecase.deck.DuplicateDeckUseCase
import com.wordbook.domain.usecase.deck.GetDecksUseCase
import com.wordbook.domain.usecase.deck.SaveDeckUseCase
import com.wordbook.domain.usecase.label.GetLabelsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val decks: List<Deck> = emptyList(),
    val labels: List<Label> = emptyList(),
    val selectedLabelIds: Set<Long> = emptySet(),
    val query: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

private data class FilterParams(
    val query: String,
    val labelIds: Set<Long>,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getDecksUseCase: GetDecksUseCase,
    private val getLabelsUseCase: GetLabelsUseCase,
    private val saveDeckUseCase: SaveDeckUseCase,
    private val deleteDeckUseCase: DeleteDeckUseCase,
    private val duplicateDeckUseCase: DuplicateDeckUseCase,
    private val saveCardUseCase: SaveCardUseCase,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _selectedLabelIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val _importText = MutableStateFlow("")
    val importText: StateFlow<String> = _importText

    private val _importFormat = MutableStateFlow("JSON")
    val importFormat: StateFlow<String> = _importFormat

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _decks: StateFlow<List<Deck>> = combine(_query, _selectedLabelIds) { query, labelIds ->
        FilterParams(query, labelIds)
    }.flatMapLatest { params ->
        getDecksUseCase(params.query, params.labelIds.toList())
    }.catch { e ->
        _errorMessage.update { e.message ?: "Failed to load decks" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _labels: StateFlow<List<Label>> = getLabelsUseCase()
        .catch { e -> _errorMessage.update { e.message ?: "Failed to load labels" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<HomeUiState> = combine(
        _decks,
        _labels,
        _selectedLabelIds,
        _query,
        _errorMessage,
    ) { decks, labels, selectedLabelIds, query, errorMessage ->
        HomeUiState(
            decks = decks,
            labels = labels,
            selectedLabelIds = selectedLabelIds,
            query = query,
            isLoading = false,
            errorMessage = errorMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true),
    )

    fun onQueryChange(query: String) {
        _query.update { query }
    }

    fun onLabelToggle(labelId: Long) {
        _selectedLabelIds.update { current ->
            if (labelId in current) current - labelId else current + labelId
        }
    }

    fun clearFilters() {
        _query.update { "" }
        _selectedLabelIds.update { emptySet() }
    }

    fun deleteDeck(id: Long) {
        viewModelScope.launch {
            try {
                deleteDeckUseCase(id)
            } catch (e: Exception) {
                _errorMessage.update { e.message ?: "Failed to delete deck" }
            }
        }
    }

    fun duplicateDeck(id: Long) {
        viewModelScope.launch {
            try {
                duplicateDeckUseCase(id)
            } catch (e: Exception) {
                _errorMessage.update { e.message ?: "Failed to duplicate deck" }
            }
        }
    }

    fun onImportTextChange(text: String) {
        _importText.update { text }
    }

    fun onImportFormatChange(format: String) {
        _importFormat.update { format }
    }

    fun importDeck() {
        val text = _importText.value.trim()
        if (text.isEmpty()) {
            _errorMessage.update { "Import text cannot be empty" }
            return
        }
        viewModelScope.launch {
            _isImporting.update { true }
            try {
                val allLabels = _labels.value
                val exportDeck = when (_importFormat.value) {
                    "CSV" -> DeckSerializer.fromCsv(text)
                    else -> DeckSerializer.fromJson(text)
                }
                val domainDeck = exportDeck.toDomainDeck(deckId = 0L, allLabels = allLabels)
                val labelIds = domainDeck.labels.map { it.id }
                val newDeckId = saveDeckUseCase(domainDeck, labelIds)

                exportDeck.cards.forEach { exportCard ->
                    val domainCard = exportCard.toDomainCard(deckId = newDeckId, allLabels = allLabels)
                    val cardLabelIds = domainCard.labels.map { it.id }
                    saveCardUseCase(domainCard, cardLabelIds)
                }

                _importText.update { "" }
            } catch (e: Exception) {
                _errorMessage.update { e.message ?: "Failed to import deck" }
            } finally {
                _isImporting.update { false }
            }
        }
    }

    fun clearError() {
        _errorMessage.update { null }
    }
}
