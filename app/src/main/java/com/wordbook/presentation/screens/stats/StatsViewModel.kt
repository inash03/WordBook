package com.wordbook.presentation.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordbook.domain.model.Deck
import com.wordbook.domain.model.TestSession
import com.wordbook.domain.usecase.deck.GetDecksUseCase
import com.wordbook.domain.usecase.test.GetTestHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class DeckStat(
    val deckName: String,
    val total: Int,
    val remembered: Int,
    val progress: Float get() = if (total == 0) 0f else remembered.toFloat() / total
)

data class StatsUiState(
    val totalCards: Int = 0,
    val rememberedCount: Int = 0,
    val needsReviewCount: Int = 0,
    val notStudiedCount: Int = 0,
    val dueToday: Int = 0,
    val currentStreak: Int = 0,
    val recentSessions: List<TestSession> = emptyList(),
    val deckStats: List<DeckStat> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getDecksUseCase: GetDecksUseCase,
    private val getTestHistoryUseCase: GetTestHistoryUseCase
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        getDecksUseCase(),
        getTestHistoryUseCase()
    ) { decks, sessions ->
        StatsUiState(
            totalCards = decks.sumOf { it.cardCount },
            rememberedCount = decks.sumOf { it.rememberedCount },
            needsReviewCount = decks.sumOf { it.needsReviewCount },
            notStudiedCount = decks.sumOf { it.cardCount - it.rememberedCount - it.needsReviewCount },
            dueToday = decks.sumOf { it.dueCardsCount },
            currentStreak = calculateStreak(sessions),
            recentSessions = sessions.take(5),
            deckStats = decks.map { DeckStat(it.name, it.cardCount, it.rememberedCount) },
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUiState())

    private fun calculateStreak(sessions: List<TestSession>): Int {
        if (sessions.isEmpty()) return 0
        val today = startOfDay(System.currentTimeMillis())
        val sessionDays = sessions
            .mapNotNull { it.finishedAt }
            .map { startOfDay(it) }
            .toSortedSet(reverseOrder())

        var streak = 0
        var expected = today
        for (day in sessionDays) {
            if (day == expected) {
                streak++
                expected -= TimeUnit.DAYS.toMillis(1)
            } else if (day < expected) {
                break
            }
        }
        return streak
    }

    private fun startOfDay(millis: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
