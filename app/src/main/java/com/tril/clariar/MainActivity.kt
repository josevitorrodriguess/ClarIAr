package com.tril.clariar

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
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

        // Check if the Accessibility Service is enabled
        if (isAccessibilityServiceEnabled(this, MyAccessibilityService::class.java)) {
            // If enabled, finish the activity as there's no need to show the permissions screen
            finish()
        } else {
            // If not enabled, display the PermissionsScreen asking the user to enable the service
            setContent {
                ClarIArTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        PermissionsScreen(
                            onNavigateToSettings = { openAccessibilitySettings() },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }

    // Function to open the device's Accessibility Settings
    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    // Function to check if the specified Accessibility Service is enabled
    private fun isAccessibilityServiceEnabled(
        context: Context, service: Class<out AccessibilityService>
    ): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices =
            am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        for (enabledService in enabledServices) {
            val enabledServiceInfo = enabledService.resolveInfo.serviceInfo
            if (enabledServiceInfo.packageName == context.packageName &&
                enabledServiceInfo.name == service.name) {
                // The service is enabled
                return true
            }
        }
        // The service is not enabled
        return false
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

            // Display the app logo
            Image(
                painter = painterResource(id = R.drawable.app_logo_removebg),
                contentDescription = "ClarIAr logo",
                modifier = Modifier
                    .width(200.dp)
                    .height(100.dp)
                    .padding(bottom = 12.dp)
            )

            // Explanatory text informing about the permission requirement
            Text(text = "O clarIAr precisa de permissão para captura de tela")
            Spacer(modifier = Modifier.height(16.dp))

            // Button that opens the Accessibility Settings when clicked
            Button(onClick = onNavigateToSettings) {
                Text(text = "Abrir configurações")
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