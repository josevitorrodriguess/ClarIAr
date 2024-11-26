package com.tril.clariar

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tril.clariar.ui.theme.ClarIArTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClarIArTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PermissionsScreen(
                        onNavigateToSettings = { openAppSettings() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun openAppSettings() {
        // Intent to open app settings
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }
}

@Composable
fun PermissionsScreen(onNavigateToSettings: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Displays the app logo
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "ClarIAr logo",
                modifier = Modifier
                    .width(200.dp) // Define a largura
                    .height(100.dp) // Define a altura
                    .padding(bottom = 12.dp) // Espaçamento abaixo da logo
            )

            // Explanatory text
            Text(text = "O app precisa de permissão para captura de tela.")
            Spacer(modifier = Modifier.height(16.dp))

            // Button to open settings
            Button(onClick = onNavigateToSettings) {
                Text(text = "Abrir Configurações")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionsScreenPreview() {
    ClarIArTheme {
        PermissionsScreen(onNavigateToSettings = {})
    }
}