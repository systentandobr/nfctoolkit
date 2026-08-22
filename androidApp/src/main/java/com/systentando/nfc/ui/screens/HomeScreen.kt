package com.systentando.nfc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.systentando.nfc.R
import com.systentando.nfc.domain.model.NFCTag
import com.systentando.nfc.ui.theme.GradientEnd
import com.systentando.nfc.ui.theme.GradientStart
import com.systentando.nfc.ui.viewmodels.NFCViewModel

@Composable
fun HomeScreen(
    viewModel: NFCViewModel,
    onWriteClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAdvancedClick: () -> Unit = {},
    onTagClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.pendingTag) {
        if (state.pendingTag != null) {
            onTagClick()
            viewModel.consumePendingTag()
        }
    }

    val colors = MaterialTheme.colorScheme
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onHistoryClick = { },
                onAdvancedClick = onAdvancedClick,
                onSettingsClick = onSettingsClick
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                PollingStatusIndicator(
                    isPolling = state.isPolling,
                    isWalletDetected = state.isWalletDetected,
                    nfcEnabled = state.nfcEnabled,
                    message = state.errorMessage
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onWriteClick,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .size(160.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(GradientStart, GradientEnd)
                                ),
                                shape = CircleShape
                            )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(id = R.string.write_button_label),
                                color = colors.onPrimary,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(id = R.string.recent_tags),
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            items(state.recentTags.size) { index ->
                val tag = state.recentTags[index]
                RecentTagCard(
                    tag = tag,
                    onClick = {
                        viewModel.selectTag(tag)
                        onTagClick()
                    },
                    onDelete = {
                        viewModel.deleteTag(tag)
                    }
                )
            }

            if (state.recentTags.isEmpty()) {
                item {
                    Text(
                        text = "Aproxime uma tag para começar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PollingStatusIndicator(
    isPolling: Boolean,
    isWalletDetected: Boolean,
    nfcEnabled: Boolean,
    message: String?
) {
    val colors = MaterialTheme.colorScheme
    val color = when {
        !nfcEnabled -> colors.error
        isWalletDetected -> colors.tertiary
        isPolling -> colors.secondary
        else -> colors.onSurfaceVariant
    }
    val label = when {
        !nfcEnabled -> message ?: stringResource(id = R.string.nfc_disabled)
        isWalletDetected -> stringResource(id = R.string.polling_paused)
        isPolling -> stringResource(id = R.string.ready_to_scan)
        else -> "Aguardando NFC..."
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface
        )
    }
}

@Composable
fun RecentTagCard(
    tag: NFCTag,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tag.chipType,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.primary
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = colors.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = tag.uid,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurface
            )
            Text(
                text = "NTAG213 • ${formatTime(tag.readAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    return when {
        seconds < 60 -> "agora"
        seconds < 3600 -> "${seconds / 60} min atrás"
        else -> "${seconds / 3600} h atrás"
    }
}

@Composable
fun BottomNavigationBar(
    onHistoryClick: () -> Unit,
    onAdvancedClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    NavigationBar(
        containerColor = colors.surface
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.History, contentDescription = null) },
            label = { Text(stringResource(id = R.string.history)) },
            selected = false,
            onClick = onHistoryClick
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            label = { Text("Avançado") },
            selected = false,
            onClick = onAdvancedClick
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text(stringResource(id = R.string.settings)) },
            selected = false,
            onClick = onSettingsClick
        )
    }
}
