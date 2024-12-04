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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tril.clariar.ui.theme.ClarIArTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Scaffold
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ClarIArTheme {
                val navController = rememberNavController()

                Scaffold { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "welcome_screen",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("welcome_screen") {
                            WelcomeScreen(
                                onHowToUseClick = {
                                    navController.navigate("how_to_use_screen")
                                }
                            )
                        }
                        composable("how_to_use_screen") {
                            HowToUseScreen(
                                onNavigateToPermissions = {
                                    navController.navigate("permissions_screen")
                                }
                            )
                        }
                        composable("permissions_screen") {
                            PermissionsScreen(
                                onNavigateToSettings = { openAccessibilitySettings() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    private fun isAccessibilityServiceEnabled(
        context: Context, service: Class<out AccessibilityService>
    ): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices =
            am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        for (enabledService in enabledServices) {
            val enabledServiceInfo = enabledService.resolveInfo.serviceInfo
            if (enabledServiceInfo.packageName == context.packageName &&
                enabledServiceInfo.name == service.name
            ) {
                return true
            }
        }
        return false
    }
}

@Composable
fun WelcomeScreen(onHowToUseClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo_removebg),
                contentDescription = "Logo do aplicativo ClarIAr",
                modifier = Modifier
                    .width(200.dp)
                    .height(100.dp)
                    .padding(bottom = 24.dp)
            )

            Text(
                text = "Bem-vindo ao ClarIAr",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )

            Text(
                text = "O ClarIAr utiliza inteligência artificial para descrever imagens na tela do seu dispositivo, tornando-o mais inclusivo. Experimente uma navegação mais acessível e compreensível.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 32.dp),
                textAlign = TextAlign.Justify
            )

            Button(
                onClick = onHowToUseClick,
                modifier = Modifier.width(180.dp)
            ) {
                Text(text = "Como usar o ClarIAr")
            }
        }
    }
}

@Composable
fun HowToUseScreen(onNavigateToPermissions: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Título
            Text(
                text = "Como usar o ClarIAr",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Siga as etapas abaixo para usar o ClarIAr:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )

            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "1. Toque em qualquer imagem na tela para iniciar a descrição.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = "2. Aguarde a descrição detalhada ser reproduzida em áudio.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = "3. Para interromper, toque novamente na imagem.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onNavigateToPermissions,
                modifier = Modifier.width(220.dp)
                    .padding(horizontal = 16.dp)
                    .height(48.dp)
            ) {
                Text(text = "Conceder Permissões")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Permissões são necessárias para que o ClarIAr funcione corretamente.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(onNavigateToSettings: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Permissões Necessárias") },
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Para que o ClarIAr funcione corretamente, é necessário conceder algumas permissões.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Siga os passos abaixo:",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(16.dp))

                val steps = listOf(
                    "Toque no botão abaixo para acessar as configurações de acessibilidade.",
                    "Na lista de serviços, selecione 'ClarIAr'.",
                    "Ative o serviço de acessibilidade para o ClarIAr.",
                    "Na tela de permissões, toque em 'Permitir' para conceder controle total do dispositivo.",
                    "Uma notificação aparecerá solicitando permissão para capturar a tela. Confirme a permissão."
                )

                steps.forEachIndexed { index, step ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = "${index + 1}.",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Após seguir estes passos, o ClarIAr estará pronto para uso.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(200.dp)
                        .height(48.dp)
                ) {
                    Text(text = "Abrir Configurações")
                }
            }
        }
    )
}
