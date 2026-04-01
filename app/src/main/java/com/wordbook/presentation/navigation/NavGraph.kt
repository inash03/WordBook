package com.wordbook.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.wordbook.presentation.screens.card.CardEditScreen
import com.wordbook.presentation.screens.deck.DeckDetailScreen
import com.wordbook.presentation.screens.deck.DeckEditScreen
import com.wordbook.presentation.screens.history.HistoryDetailScreen
import com.wordbook.presentation.screens.history.HistoryScreen
import com.wordbook.presentation.screens.home.HomeScreen
import com.wordbook.presentation.screens.search.SearchScreen
import com.wordbook.presentation.screens.settings.SettingsScreen
import com.wordbook.presentation.screens.stats.StatsScreen
import com.wordbook.presentation.screens.study.StudyScreen
import com.wordbook.presentation.screens.test.TestResultScreen
import com.wordbook.presentation.screens.test.TestScreen
import com.wordbook.presentation.screens.test.TestSetupScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            HomeScreen(
                onDeckClick = { navController.navigate(Screen.DeckDetail.createRoute(it)) },
                onCreateDeck = { navController.navigate(Screen.DeckEdit.createRoute()) },
                onEditDeck = { navController.navigate(Screen.DeckEdit.createRoute(it)) },
                onSearchClick = { navController.navigate(Screen.Search.route) },
                onHistoryClick = { navController.navigate(Screen.History.route) },
                onStatsClick = { navController.navigate(Screen.Stats.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(
            route = Screen.DeckDetail.route,
            arguments = listOf(navArgument("deckId") { type = NavType.LongType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments!!.getLong("deckId")
            DeckDetailScreen(
                deckId = deckId,
                onBack = { navController.popBackStack() },
                onEditDeck = { navController.navigate(Screen.DeckEdit.createRoute(deckId)) },
                onAddCard = { navController.navigate(Screen.CardEdit.createRoute(deckId)) },
                onEditCard = { cardId -> navController.navigate(Screen.CardEdit.createRoute(deckId, cardId)) },
                onStudy = { navController.navigate(Screen.Study.createRoute(deckId)) },
                onTest = { navController.navigate(Screen.TestSetup.createRoute(deckId)) }
            )
        }

        composable(
            route = Screen.DeckEdit.route,
            arguments = listOf(navArgument("deckId") { type = NavType.LongType; defaultValue = 0L })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId")?.takeIf { it != 0L }
            DeckEditScreen(
                deckId = deckId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CardEdit.route,
            arguments = listOf(
                navArgument("deckId") { type = NavType.LongType },
                navArgument("cardId") { type = NavType.LongType; defaultValue = 0L }
            )
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments!!.getLong("deckId")
            val cardId = backStackEntry.arguments?.getLong("cardId")?.takeIf { it != 0L }
            CardEditScreen(
                deckId = deckId,
                cardId = cardId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Study.route,
            arguments = listOf(navArgument("deckId") { type = NavType.LongType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments!!.getLong("deckId")
            StudyScreen(
                deckId = deckId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.TestSetup.route,
            arguments = listOf(navArgument("deckId") { type = NavType.LongType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments!!.getLong("deckId")
            TestSetupScreen(
                deckId = deckId,
                onBack = { navController.popBackStack() },
                onStartTest = { mode ->
                    navController.navigate(Screen.Test.createRoute(deckId, mode))
                }
            )
        }

        composable(
            route = Screen.Test.route,
            arguments = listOf(
                navArgument("deckId") { type = NavType.LongType },
                navArgument("mode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments!!.getLong("deckId")
            val mode = backStackEntry.arguments!!.getString("mode")!!
            TestScreen(
                deckId = deckId,
                mode = mode,
                onFinished = { sessionId ->
                    navController.navigate(Screen.TestResult.createRoute(sessionId)) {
                        popUpTo(Screen.DeckDetail.createRoute(deckId))
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.TestResult.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments!!.getLong("sessionId")
            TestResultScreen(
                sessionId = sessionId,
                onBack = { navController.popBackStack() },
                onHome = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                onSessionClick = { navController.navigate(Screen.HistoryDetail.createRoute(it)) }
            )
        }

        composable(
            route = Screen.HistoryDetail.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments!!.getLong("sessionId")
            HistoryDetailScreen(
                sessionId = sessionId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onCardDeckClick = { deckId: Long -> navController.navigate(Screen.DeckDetail.createRoute(deckId)) }
            )
        }

        composable(Screen.Stats.route) {
            StatsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
