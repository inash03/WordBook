package com.wordbook.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object DeckDetail : Screen("deck/{deckId}") {
        fun createRoute(deckId: Long) = "deck/$deckId"
    }
    object DeckEdit : Screen("deck_edit?deckId={deckId}") {
        fun createRoute(deckId: Long? = null) = if (deckId != null) "deck_edit?deckId=$deckId" else "deck_edit"
    }
    object CardEdit : Screen("card_edit/{deckId}?cardId={cardId}") {
        fun createRoute(deckId: Long, cardId: Long? = null) =
            if (cardId != null) "card_edit/$deckId?cardId=$cardId" else "card_edit/$deckId"
    }
    object Study : Screen("study/{deckId}") {
        fun createRoute(deckId: Long) = "study/$deckId"
    }
    object TestSetup : Screen("test_setup/{deckId}") {
        fun createRoute(deckId: Long) = "test_setup/$deckId"
    }
    object Test : Screen("test/{deckId}/{mode}") {
        fun createRoute(deckId: Long, mode: String) = "test/$deckId/$mode"
    }
    object TestResult : Screen("test_result/{sessionId}") {
        fun createRoute(sessionId: Long) = "test_result/$sessionId"
    }
    object History : Screen("history")
    object HistoryDetail : Screen("history_detail/{sessionId}") {
        fun createRoute(sessionId: Long) = "history_detail/$sessionId"
    }
    object Search : Screen("search")
    object Stats : Screen("stats")
    object Settings : Screen("settings")
}
