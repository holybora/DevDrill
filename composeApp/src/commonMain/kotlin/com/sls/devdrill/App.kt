package com.sls.devdrill

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sls.devdrill.navigation.HomeRoute
import com.sls.devdrill.screens.HomeScreen

@Suppress("ModifierMissing")
@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding(),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            NavHost(
                navController = navController,
                startDestination = HomeRoute,
            ) {
                composable<HomeRoute> {
                    HomeScreen()
                }
            }
        }
    }
}
