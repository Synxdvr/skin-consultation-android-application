package com.example.skinconsultform.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.*
import com.example.skinconsultform.ui.screens.*

sealed class Screen(val route: String) {
    object Welcome      : Screen("welcome")
    object Consultation : Screen("consultation")
    object Results      : Screen("results/{consultationId}") {
        fun createRoute(id: Long) = "results/$id"
    }
    object Admin        : Screen("admin")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StheticNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route,
        modifier = modifier
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController = navController)
        }
        composable(Screen.Consultation.route) {
            ConsultationScreen(navController = navController)
        }
        composable(
            route = Screen.Results.route,
            arguments = listOf(
                navArgument("consultationId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("consultationId") ?: 0L
            ResultsScreen(
                navController    = navController,
                consultationId   = id
            )
        }
        composable(Screen.Admin.route) {
            AdminScreen(navController = navController)
        }
    }
}