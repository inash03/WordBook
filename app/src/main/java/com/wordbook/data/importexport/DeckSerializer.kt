package com.wordbook.data.importexport

import com.wordbook.domain.model.Card
import com.wordbook.domain.model.Deck
import com.wordbook.domain.model.Label
import com.wordbook.domain.model.StudyStatus
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ExportCard(
    val front: String,
    val back: String,
    val studyStatus: String = "NOT_STUDIED",
    val labels: List<String> = emptyList()
)

@Serializable
data class ExportDeck(
    val name: String,
    val description: String = "",
    val labels: List<String> = emptyList(),
    val cards: List<ExportCard>
)

private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

object DeckSerializer {

    fun toJson(deck: Deck, cards: List<Card>): String {
        val export = ExportDeck(
            name = deck.name,
            description = deck.description,
            labels = deck.labels.map { it.name },
            cards = cards.map { card ->
                ExportCard(
                    front = card.front,
                    back = card.back,
                    studyStatus = card.studyStatus.name,
                    labels = card.labels.map { it.name }
                )
            }
        )
        return json.encodeToString(export)
    }

    fun fromJson(jsonString: String): ExportDeck =
        json.decodeFromString(jsonString)

    fun toCsv(deck: Deck, cards: List<Card>): String {
        val header = "front,back,studyStatus,labels\n"
        val rows = cards.joinToString("\n") { card ->
            val labels = card.labels.joinToString("|") { it.name }
            val front = card.front.replace("\"", "\"\"")
            val back = card.back.replace("\"", "\"\"")
            "\"$front\",\"$back\",${card.studyStatus.name},\"$labels\""
        }
        return header + rows
    }

    fun fromCsv(csv: String): ExportDeck {
        val lines = csv.lines().filter { it.isNotBlank() }
        val cards = lines.drop(1).mapNotNull { line ->
            parseCsvLine(line)?.let { cols ->
                if (cols.size < 2) return@mapNotNull null
                ExportCard(
                    front = cols[0],
                    back = cols[1],
                    studyStatus = cols.getOrNull(2)?.let {
                        runCatching { StudyStatus.valueOf(it) }.getOrNull()?.name
                    } ?: "NOT_STUDIED",
                    labels = cols.getOrNull(3)?.split("|")?.filter { it.isNotBlank() } ?: emptyList()
                )
            }
        }
        return ExportDeck(name = "Imported Deck", cards = cards)
    }

    private fun parseCsvLine(line: String): List<String>? {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && inQuotes && i + 1 < line.length && line[i + 1] == '"' -> {
                    current.append('"'); i++
                }
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> { result.add(current.toString()); current = StringBuilder() }
                else -> current.append(c)
            }
            i++
        }
        result.add(current.toString())
        return result
    }
}

fun ExportDeck.toDomainDeck(deckId: Long = 0L, allLabels: List<Label>): Deck {
    val labelMap = allLabels.associateBy { it.name }
    val deckLabels = labels.mapNotNull { labelMap[it] }
    return Deck(
        id = deckId,
        name = name,
        description = description,
        labels = deckLabels
    )
}

fun ExportCard.toDomainCard(deckId: Long, allLabels: List<Label>): Card {
    val labelMap = allLabels.associateBy { it.name }
    val cardLabels = labels.mapNotNull { labelMap[it] }
    return Card(
        deckId = deckId,
        front = front,
        back = back,
        studyStatus = runCatching { StudyStatus.valueOf(studyStatus) }.getOrDefault(StudyStatus.NOT_STUDIED),
        labels = cardLabels
    )
}
