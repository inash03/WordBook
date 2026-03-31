package com.wordbook.presentation.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordbook.domain.model.Card
import com.wordbook.domain.model.Label
import com.wordbook.domain.model.StudyStatus
import com.wordbook.domain.usecase.card.SearchCardsUseCase
import com.wordbook.domain.usecase.label.GetLabelsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<Card> = emptyList(),
    val selectedLabelIds: Set<Long> = emptySet(),
    val selectedStudyStatus: StudyStatus? = null,
    val allLabels: List<Label> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchCardsUseCase: SearchCardsUseCase,
    private val getLabelsUseCase: GetLabelsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")
    private val labelIdsFlow = MutableStateFlow<Set<Long>>(emptySet())
    private val studyStatusFlow = MutableStateFlow<StudyStatus?>(null)

    init {
        loadLabels()
        observeSearch()
    }

    private fun loadLabels() {
        viewModelScope.launch {
            getLabelsUseCase()
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { labels ->
                    _uiState.update { it.copy(allLabels = labels) }
                }
        }
    }

    private fun observeSearch() {
        viewModelScope.launch {
            combine(
                queryFlow.debounce(300L),
                labelIdsFlow,
                studyStatusFlow
            ) { query, labelIds, status ->
                Triple(query, labelIds, status)
            }
                .flatMapLatest { (query, labelIds, status) ->
                    _uiState.update { it.copy(isLoading = true) }
                    searchCardsUseCase(
                        query = query,
                        deckId = null,
                        labelIds = labelIds.toList(),
                        studyStatus = status
                    )
                }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { cards ->
                    _uiState.update { it.copy(results = cards, isLoading = false) }
                }
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        queryFlow.value = newQuery
    }

    fun onLabelToggle(labelId: Long) {
        val current = labelIdsFlow.value.toMutableSet()
        if (current.contains(labelId)) {
            current.remove(labelId)
        } else {
            current.add(labelId)
        }
        labelIdsFlow.value = current
        _uiState.update { it.copy(selectedLabelIds = current.toSet()) }
    }

    fun onStatusFilter(status: StudyStatus?) {
        studyStatusFlow.value = status
        _uiState.update { it.copy(selectedStudyStatus = status) }
    }

    fun clearFilters() {
        labelIdsFlow.value = emptySet()
        studyStatusFlow.value = null
        _uiState.update {
            it.copy(
                selectedLabelIds = emptySet(),
                selectedStudyStatus = null
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
