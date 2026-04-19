package dev.vskelk.cdf.ui.quarantine

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.vskelk.cdf.R
import dev.vskelk.cdf.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuarantineScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.quarantine_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VespaBackground,
                    titleContentColor = VespaOnSurface
                )
            )
        },
        containerColor = VespaBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Pending,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = VespaOnSurfaceLow
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Sin elementos pendientes",
                    style = MaterialTheme.typography.titleMedium,
                    color = VespaOnSurfaceMid
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Los fragmentos normativos investigados aparecerán aquí para tu revisión.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VespaOnSurfaceLow
                )
            }
        }
    }
}
