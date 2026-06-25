package com.yukiyoshi.smphdetox.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

private const val ROUTE_TOP = "top"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_RULES = "rules"
private const val ROUTE_RULE_CREATE = "rule_edit"
private const val ROUTE_RULE_EDIT = "rule_edit/{ruleId}"

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
        composable(ROUTE_RULES) {
            RuleManagementScreen(
                onCreateRule = { navController.navigate(ROUTE_RULE_CREATE) },
                onEditRule = { ruleId -> navController.navigate("rule_edit/$ruleId") },
            )
        }
        composable(ROUTE_RULE_CREATE) {
            RuleEditScreen(ruleId = null, onDone = { navController.popBackStack() })
        }
        composable(
            ROUTE_RULE_EDIT,
            arguments = listOf(navArgument("ruleId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val ruleId = backStackEntry.arguments?.getString("ruleId")
            RuleEditScreen(ruleId = ruleId, onDone = { navController.popBackStack() })
        }
    }
}
