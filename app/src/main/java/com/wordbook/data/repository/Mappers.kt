package com.wordbook.data.repository

import com.wordbook.data.database.daos.CardWithLabels
import com.wordbook.data.database.daos.DeckWithLabels
import com.wordbook.data.database.entities.*
import com.wordbook.domain.model.*

// ── Label ────────────────────────────────────────────────────────────────────

fun LabelEntity.toDomain() = Label(id = id, name = name, color = color)
fun Label.toEntity() = LabelEntity(id = id, name = name, color = color)

// ── Card ─────────────────────────────────────────────────────────────────────

fun CardWithLabels.toDomain() = Card(
    id = card.id,
    deckId = card.deckId,
    front = card.front,
    back = card.back,
    createdAt = card.createdAt,
    updatedAt = card.updatedAt,
    studyStatus = StudyStatus.valueOf(card.studyStatus),
    labels = labels.map { it.toDomain() },
    srsInterval = card.srsInterval,
    srsEaseFactor = card.srsEaseFactor,
    srsDueDate = card.srsDueDate,
    srsRepetitions = card.srsRepetitions
)

fun Card.toEntity() = CardEntity(
    id = id,
    deckId = deckId,
    front = front,
    back = back,
    createdAt = createdAt,
    updatedAt = updatedAt,
    studyStatus = studyStatus.name,
    srsInterval = srsInterval,
    srsEaseFactor = srsEaseFactor,
    srsDueDate = srsDueDate,
    srsRepetitions = srsRepetitions
)

// ── Deck ─────────────────────────────────────────────────────────────────────

fun DeckWithLabels.toDomain(
    cardCount: Int = 0,
    rememberedCount: Int = 0,
    needsReviewCount: Int = 0,
    dueCardsCount: Int = 0
) = Deck(
    id = deck.id,
    name = deck.name,
    description = deck.description,
    createdAt = deck.createdAt,
    updatedAt = deck.updatedAt,
    labels = labels.map { it.toDomain() },
    cardCount = cardCount,
    rememberedCount = rememberedCount,
    needsReviewCount = needsReviewCount,
    dueCardsCount = dueCardsCount
)

fun Deck.toEntity() = DeckEntity(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// ── TestSession ───────────────────────────────────────────────────────────────

fun TestSessionEntity.toDomain() = TestSession(
    id = id,
    deckId = deckId,
    deckName = deckName,
    startedAt = startedAt,
    finishedAt = finishedAt,
    mode = TestMode.valueOf(mode),
    totalCards = totalCards,
    correctCount = correctCount,
    incorrectCount = incorrectCount,
    skippedCount = skippedCount
)

fun TestSession.toEntity() = TestSessionEntity(
    id = id,
    deckId = deckId,
    deckName = deckName,
    startedAt = startedAt,
    finishedAt = finishedAt,
    mode = mode.name,
    totalCards = totalCards,
    correctCount = correctCount,
    incorrectCount = incorrectCount,
    skippedCount = skippedCount
)

// ── TestResult ────────────────────────────────────────────────────────────────

fun TestResultEntity.toDomain() = TestResult(
    id = id,
    sessionId = sessionId,
    cardId = cardId,
    cardFront = cardFront,
    cardBack = cardBack,
    result = ResultType.valueOf(result),
    answeredAt = answeredAt
)

fun TestResult.toEntity() = TestResultEntity(
    id = id,
    sessionId = sessionId,
    cardId = cardId,
    cardFront = cardFront,
    cardBack = cardBack,
    result = result.name,
    answeredAt = answeredAt
)
