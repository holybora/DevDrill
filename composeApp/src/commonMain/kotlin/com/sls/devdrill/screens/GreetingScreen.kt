package com.sls.devdrill.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sls.devdrill.Greeting
import devdrill.composeapp.generated.resources.Res
import devdrill.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource

@Composable
fun GreetingScreen(
    onNavigateBack: () -> Unit,
) {
    val greeting = remember { Greeting().greet() }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.compose_multiplatform),
            contentDescription = "Compose Multiplatform logo",
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Compose: $greeting",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onNavigateBack) {
            Text("Go back")
        }
    }
}
