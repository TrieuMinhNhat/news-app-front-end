package com.example.myapplication.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.example.myapplication.MainEvent
import com.example.myapplication.ui.screens.NotificationRoute
import com.example.myapplication.viewmodel.NotificationViewModel
import androidx.compose.runtime.LaunchedEffect // Nhớ import cái này

/**
 * Defines all the possible navigation destinations in the app.
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home?tab={tab}&keyword={keyword}") {
        fun createRoute(tab: Int? = null, keyword: String? = null): String {
            val safeKeyword = Uri.encode(keyword ?: "")
            return if (tab == null) {
                if (safeKeyword.isBlank()) "home" else "home?tab=0&keyword=$safeKeyword"
            } else {
                "home?tab=$tab&keyword=$safeKeyword"
            }
        }
    }
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
    mainEvent: androidx.compose.runtime.MutableState<MainEvent?>,
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    LaunchedEffect(mainEvent.value) {
        val event = mainEvent.value ?: return@LaunchedEffect
        try {
            when (event) {
                is MainEvent.NotificationArrived -> {
                    val notificationId = event.notificationId?.toLongOrNull()
                    val articleId = event.articleId?.toIntOrNull()

                    // 1) Mark as read as early as possible (works for cold-start too:
                    // NotificationViewModel queues pending reads until token/sync is ready).
                    when {
                        notificationId != null -> notificationViewModel.markAsRead(notificationId)
                        articleId != null -> notificationViewModel.markAsReadByArticleId(articleId.toString())
                        event.notificationType != null ->
                            notificationViewModel.markFirstUnreadByTypeAndKeyword(event.notificationType, event.keyword)
                    }

                    when (event.notificationType) {
                        "facebook_post_update", "threads_keyword_update" -> {
                            navController.navigate(Screen.Home.createRoute(tab = 1, keyword = event.keyword)) {
                                launchSingleTop = true
                            }
                        }

                        else -> {
                            if (articleId != null) {
                                navController.navigate(Screen.Detail.createRoute(articleId)) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                }

                is MainEvent.RefreshNotification -> {
                    notificationViewModel.syncFromServer()
                }
            }
        } finally {
            // Always clear the event so it won't re-trigger on recomposition.
            mainEvent.value = null
        }
    }
    NavHost(
        navController = navController,
        startDestination = Screen.Interest.route,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300))
        }

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

        // Notification Screen
        composable(Screen.Notification.route) {
            NotificationRoute(
                onBack = { navController.navigateUp() },
                onNavigateToArticle = {
                    navController.navigate(Screen.Detail.createRoute(it))
                },
                onNavigateToSocial = { keyword ->
                    navController.navigate(Screen.Home.createRoute(tab = 1, keyword = keyword))
                },
                viewModel = notificationViewModel // Truyền viewmodel instance vào
            )
        }
        // Sign Up Screen
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onNavigateBack = { navController.navigateUp() },
                onSignUpSuccess = {
                    navController.navigate(Screen.Home.createRoute()) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Interests Screen
        composable(Screen.Interest.route) {
            InterestsScreen(
                onFinishClicked = {
                    navController.navigate(Screen.Home.createRoute()) {
                        popUpTo(Screen.Interest.route) { inclusive = true }
                    }

                }
            )
        }

        composable(
            route = Screen.Home.route,
            arguments = listOf(
                navArgument("tab") {
                    type = NavType.IntType
                    defaultValue = 0
                },
                navArgument("keyword") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { entry ->
            val initialTabIndex = entry.arguments?.getInt("tab") ?: 0
            val initialSocialKeyword = entry.arguments?.getString("keyword")
                ?.trim()
                ?.takeUnless { it.isEmpty() }
            HomeScreen(
                onArticleClicked = { article ->
                    navController.navigate(Screen.Detail.createRoute(article.id))
                },
                onMenuClicked = { navController.navigate(Screen.Interest.route) },
                onDebugClicked = { navController.navigate(Screen.Debug.route) },
                onNotificationIconClicked = {
                    navController.navigate(Screen.Notification.route)
                },
                initialTabIndex = initialTabIndex,
                initialSocialKeyword = initialSocialKeyword,
                notificationViewModel = notificationViewModel // ✅ INSTANCE
            )
        }

        composable(Screen.Topics.route) {
            TopicsScreen(
                onBackClicked = { navController.navigateUp() }
            )
        }

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
                onBackClicked = { navController.navigateUp()},
            )
        }

        composable(Screen.Debug.route) {
            DebugScreen(
                onBackClicked = { navController.navigateUp() }
            )
        }
    }
}
