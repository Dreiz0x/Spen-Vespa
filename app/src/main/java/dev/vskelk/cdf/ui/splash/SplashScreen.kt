package dev.vskelk.cdf.ui.splash

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.vskelk.cdf.R
import dev.vskelk.cdf.core.domain.model.BootstrapState
import dev.vskelk.cdf.ui.theme.*

/**
 * SplashScreen - Pantalla de bienvenida
 *
 * Per spec:
 * - Logo con colorFilter = tint(0xFFCCCCCC)
 * - "VESPA" Barlow Condensed ExtraBold, 64sp, letterSpacing 8sp
 * - Subtítulo "SPEN · PREPARACIÓN INTEGRAL · v2.0"
 * - "BIENVENIDO" + nombre de usuario
 * - Botón COMENZAR solo aparece en BootstrapState.Ready
 */
@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val bootstrapState by viewModel.bootstrapState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VespaBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Logo
            // Per spec: Logo con colorFilter = tint(0xFFCCCCCC)
            Image(
                painter = painterResource(id = R.drawable.ic_vespa_logo),
                contentDescription = stringResource(R.string.cd_logo),
                modifier = Modifier.size(160.dp),
                colorFilter = ColorFilter.tint(VespaLogoTint)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // "VESPA"
            // Per spec: Barlow Condensed ExtraBold, 64sp, letterSpacing 8sp
            Text(
                text = "VESPA",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 64.sp,
                    letterSpacing = 8.sp
                ),
                color = VespaPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtítulo
            // "SPEN · PREPARACIÓN INTEGRAL · v2.0"
            Text(
                text = stringResource(R.string.splash_subtitle),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 3.sp
                ),
                color = VespaOnSurfaceMid
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Línea divisoria
            HorizontalDivider(
                modifier = Modifier.width(80.dp),
                thickness = 1.dp,
                color = VespaOutline
            )

            Spacer(modifier = Modifier.height(24.dp))

            // "BIENVENIDO"
            Text(
                text = stringResource(R.string.splash_welcome),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 4.sp
                ),
                color = VespaOnSurfaceLow
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Nombre de usuario
            Text(
                text = stringResource(R.string.user_name),
                style = MaterialTheme.typography.titleLarge,
                color = VespaOnSurface
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Contenido según estado
            when (val state = bootstrapState) {
                is BootstrapState.Checking -> {
                    LoadingContent()
                }

                is BootstrapState.Seeding -> {
                    SeedingContent(state = state)
                }

                is BootstrapState.Ready -> {
                    ReadyContent(onStart = onNavigateToMain)
                }

                is BootstrapState.Error -> {
                    ErrorContent(
                        error = state,
                        onRetry = { viewModel.retry() }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = VespaPrimary,
            strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.state_loading),
            style = MaterialTheme.typography.bodySmall,
            color = VespaOnSurfaceMid
        )
    }
}

@Composable
private fun SeedingContent(state: BootstrapState.Seeding) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(200.dp)
    ) {
        LinearProgressIndicator(
            progress = { state.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp),
            color = VespaPrimary,
            trackColor = VespaOutline,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = state.message,
            style = MaterialTheme.typography.bodySmall,
            color = VespaOnSurfaceMid,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ReadyContent(onStart: () -> Unit) {
    // Per spec: Botón COMENZAR con fadeIn
    AnimatedVisibility(
        visible = true,
        enter = fadeIn()
    ) {
        OutlinedButton(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = VespaPrimary
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(VespaPrimary)
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                text = stringResource(R.string.splash_start),
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 4.sp
                )
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: BootstrapState.Error,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = VespaErrorContainer.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = error.cause,
                style = MaterialTheme.typography.bodySmall,
                color = VespaError,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (error.canRetry) {
            OutlinedButton(
                onClick = onRetry,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = VespaPrimary
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(VespaPrimary)
                )
            ) {
                Text(text = stringResource(R.string.state_retry))
            }
        }
    }
}
