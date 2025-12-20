package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.myapplication.ui.auth.LoginScreen
import com.example.myapplication.ui.auth.SignUpScreen
import com.example.myapplication.ui.screens.ArticleDetailScreen
import com.example.myapplication.ui.screens.HomeScreen
import com.example.myapplication.ui.screens.DebugScreen
import com.example.myapplication.ui.screens.InterestsScreen
import com.example.newsapp.ui.topics.TopicsScreen
import androidx.lifecycle.viewmodel.compose.viewModel // Make sure you have this
import com.example.myapplication.ui.screens.NotificationScreen
import com.example.myapplication.viewmodel.NotificationViewModel

/**
 * Defines all the possible navigation destinations in the app.
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object Topics : Screen("topics")
    object Debug : Screen("debug")
    object Interest : Screen("interest")

    object Notification: Screen("Notification")
    // Route with argument
    object Detail : Screen("detail/{articleId}") {
        fun createRoute(articleId: Int) = "detail/$articleId"
    }
}

/**
 * Navigation Graph
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    notificationViewModel: NotificationViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Interest.route
    ) {

        // Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Interest.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

// Notification Screen - Uncomment and implement
        composable(Screen.Notification.route) {
            // Collect state from ViewModel
            val notifications by notificationViewModel.notifications.collectAsState()

            // Refresh data when entering screen
            LaunchedEffect(Unit) {
                notificationViewModel.refreshData()
            }

            NotificationScreen(
                notifications = notifications,
                onBackClick = { navController.navigateUp() },
                onNotificationClick = { item ->
                    // 1. Mark as read
                    notificationViewModel.markAsRead(item)
                    // 2. Navigate to article if ID exists
                    item.articleId?.let { id ->
                        val intId = id.toIntOrNull()
                        if (intId != null) {
                            navController.navigate(Screen.Detail.createRoute(intId))
                        }
                    }
                },
                onMarkAllRead = {
                    notificationViewModel.markAllAsRead()
                }
            )
        }
        // Sign Up Screen
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onNavigateBack = { navController.navigateUp() },
                onSignUpSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Interests Screen
        composable(Screen.Interest.route) {
            InterestsScreen(
                onFinishClicked = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Interest.route) { inclusive = true }
                    }
                }
            )
        }

        // Home Screen
        composable(Screen.Home.route) {
            HomeScreen(
                onArticleClicked = { article ->
                    navController.navigate(Screen.Detail.createRoute(article.id))
                },
                onMenuClicked = { navController.navigate(Screen.Interest.route) },
                onDebugClicked = { navController.navigate(Screen.Debug.route) },
                notificationViewModel = notificationViewModel,
                onNotificationIconClicked = { navController.navigate(Screen.Notification.route) }
            )
        }

        // Topics Screen
        composable(Screen.Topics.route) {
            TopicsScreen(
                onBackClicked = { navController.navigateUp() }
            )
        }

        // 🔥 Article Detail + Deep Link
        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("articleId") { type = NavType.IntType }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "myapp://detail/{articleId}"
                }
            )
        ) { entry ->
            val articleId = entry.arguments?.getInt("articleId")

            ArticleDetailScreen(
                articleId = articleId,
                onBackClicked = { navController.navigateUp() }
            )
        }

        // Debug screen
        composable(Screen.Debug.route) {
            DebugScreen(
                onBackClicked = { navController.navigateUp() }
            )
        }
    }
}
