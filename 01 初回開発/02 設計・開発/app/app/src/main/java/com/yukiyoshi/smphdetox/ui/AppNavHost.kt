package com.yukiyoshi.smphdetox.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

private const val ROUTE_TOP = "top"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_RULES = "rules"

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = ROUTE_TOP, modifier = modifier) {
        composable(ROUTE_TOP) {
            TopScreen(
                onNavigateToSettings = { navController.navigate(ROUTE_SETTINGS) },
                onNavigateToRules = { navController.navigate(ROUTE_RULES) },
            )
        }
        composable(ROUTE_SETTINGS) { SettingsScreen() }
        composable(ROUTE_RULES) { RuleManagementScreen() }
    }
}
