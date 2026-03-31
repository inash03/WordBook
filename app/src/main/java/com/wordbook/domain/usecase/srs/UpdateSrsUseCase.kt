package com.wordbook.domain.usecase.srs

import com.wordbook.domain.model.ResultType
import com.wordbook.domain.repository.CardRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToInt

class UpdateSrsUseCase @Inject constructor(
    private val repository: CardRepository
) {
    suspend operator fun invoke(
        cardId: Long,
        currentInterval: Int,
        currentEaseFactor: Float,
        currentRepetitions: Int,
        result: ResultType
    ) {
        val quality = when (result) {
            ResultType.CORRECT -> 4
            ResultType.INCORRECT -> 1
            ResultType.SKIPPED -> return
        }

        val (newInterval, newEaseFactor, newRepetitions) = calculateSm2(
            quality, currentInterval, currentEaseFactor, currentRepetitions
        )

        val newDueDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(newInterval.toLong())

        repository.updateCardSrs(
            id = cardId,
            interval = newInterval,
            easeFactor = newEaseFactor,
            dueDate = newDueDate,
            repetitions = newRepetitions
        )
    }

    private data class SrsUpdate(val interval: Int, val easeFactor: Float, val repetitions: Int)

    private fun calculateSm2(
        quality: Int,
        currentInterval: Int,
        currentEaseFactor: Float,
        currentRepetitions: Int
    ): SrsUpdate {
        return if (quality >= 3) {
            val newInterval = when (currentRepetitions) {
                0 -> 1
                1 -> 6
                else -> (currentInterval * currentEaseFactor).roundToInt()
            }
            val efDelta = 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02)
            val newEaseFactor = max(1.3f, currentEaseFactor + efDelta.toFloat())
            SrsUpdate(newInterval, newEaseFactor, currentRepetitions + 1)
        } else {
            SrsUpdate(1, currentEaseFactor, 0)
        }
    }
}
