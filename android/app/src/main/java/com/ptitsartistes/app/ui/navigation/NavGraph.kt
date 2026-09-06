package com.ptitsartistes.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ptitsartistes.app.ui.auth.ParentLoginScreen
import com.ptitsartistes.app.ui.auth.ParentRegisterScreen
import com.ptitsartistes.app.ui.enfant.EnfantDashboardScreen
import com.ptitsartistes.app.ui.enfant.EnfantDessinUploadScreen
import com.ptitsartistes.app.ui.enfant.EnfantPinScreen
import com.ptitsartistes.app.ui.enfant.EnfantSelectScreen
import com.ptitsartistes.app.ui.home.HomeScreen
import com.ptitsartistes.app.ui.parent.DessinUploadParentScreen
import com.ptitsartistes.app.ui.parent.EnfantFormScreen
import com.ptitsartistes.app.ui.parent.ParentDashboardScreen
import com.ptitsartistes.app.ui.settings.SettingsScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun PtitsArtistesNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                onEnfantClick = { navController.navigate(Routes.ENFANT_SELECT) },
                onParentClick = { navController.navigate(Routes.PARENT_LOGIN) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        // --- Parent -----------------------------------------------------

        composable(Routes.PARENT_LOGIN) {
            ParentLoginScreen(
                onLoggedIn = {
                    navController.navigate(Routes.PARENT_DASHBOARD) {
                        popUpTo(Routes.HOME)
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.PARENT_REGISTER) },
            )
        }

        composable(Routes.PARENT_REGISTER) {
            ParentRegisterScreen(
                onRegistered = {
                    navController.navigate(Routes.PARENT_DASHBOARD) {
                        popUpTo(Routes.HOME)
                    }
                },
                onNavigateToLogin = { navController.popBackStack() },
            )
        }

        composable(Routes.PARENT_DASHBOARD) {
            ParentDashboardScreen(
                onAddEnfant = { navController.navigate(Routes.enfantForm()) },
                onEditEnfant = { id -> navController.navigate(Routes.enfantForm(id)) },
                onAddDessin = { enfantId -> navController.navigate(Routes.dessinUploadParent(enfantId)) },
                onLoggedOut = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = Routes.ENFANT_FORM,
            arguments = listOf(navArgument("enfantId") { type = NavType.IntType; defaultValue = -1 }),
        ) { backStackEntry ->
            val enfantId = backStackEntry.arguments?.getInt("enfantId")?.takeIf { it != -1 }
            EnfantFormScreen(
                enfantId = enfantId,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.DESSIN_UPLOAD_PARENT,
            arguments = listOf(navArgument("enfantId") { type = NavType.IntType; defaultValue = -1 }),
        ) { backStackEntry ->
            val enfantId = backStackEntry.arguments?.getInt("enfantId")?.takeIf { it != -1 }
            DessinUploadParentScreen(
                preselectedEnfantId = enfantId,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }

        // --- Enfant -------------------------------------------------------

        composable(Routes.ENFANT_SELECT) {
            EnfantSelectScreen(
                onEnfantChosen = { id, prenom -> navController.navigate(Routes.enfantPin(id, prenom)) },
                onNavigateToParentLogin = { navController.navigate(Routes.PARENT_LOGIN) },
            )
        }

        composable(
            route = Routes.ENFANT_PIN,
            arguments = listOf(
                navArgument("enfantId") { type = NavType.IntType },
                navArgument("prenom") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val enfantId = backStackEntry.arguments?.getInt("enfantId") ?: return@composable
            val prenomEncoded = backStackEntry.arguments?.getString("prenom").orEmpty()
            val prenom = URLDecoder.decode(prenomEncoded, StandardCharsets.UTF_8.name())
            EnfantPinScreen(
                enfantId = enfantId,
                prenom = prenom,
                onLoggedIn = {
                    navController.navigate(Routes.ENFANT_DASHBOARD) {
                        popUpTo(Routes.HOME)
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.ENFANT_DASHBOARD) {
            EnfantDashboardScreen(
                onAddDessin = { navController.navigate(Routes.ENFANT_DESSIN_UPLOAD) },
                onLoggedOut = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.ENFANT_DESSIN_UPLOAD) {
            EnfantDessinUploadScreen(
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
