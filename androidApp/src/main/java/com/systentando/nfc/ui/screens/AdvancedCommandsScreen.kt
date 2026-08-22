package com.systentando.nfc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.systentando.nfc.ui.theme.*
import com.systentando.nfc.ui.viewmodels.NFCViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedCommandsScreen(
    viewModel: NFCViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    val commands = listOf(
        CommandItem(Icons.Default.Memory, "Ler memória bruta", "Lê setores da tag sem interpretar NDEF") {
            viewModel.startMemoryRead()
        },
        CommandItem(Icons.Default.DeleteForever, "Formatar tag", "Apaga todos os dados e formata como NDEF") {
            viewModel.startFormat()
        },
        CommandItem(Icons.Default.Lock, "Bloquear tag", "Torna a tag somente leitura") {
            viewModel.startLock()
        },
        CommandItem(Icons.Default.Password, "Definir senha", "Configura proteção por senha (Mifare)") {
            viewModel.startLock()
        },
        CommandItem(Icons.Default.Terminal, "Enviar APDU", "Envia comando APDU personalizado") {
            viewModel.startApdu()
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comandos Avançados", color = OnSurface) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = null,
                            tint = OnSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(commands.size) { index ->
                CommandCard(command = commands[index], onClick = commands[index].onClick)
            }

            item {
                if (state.isWriting) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        CircularProgressIndicator(color = PrimaryTeal)
                        Text(
                            text = state.writeMessage ?: "Aproxime a tag NFC...",
                            color = OnSurfaceMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                state.writeMessage?.takeIf { !state.isWriting }?.let { message ->
                    Text(
                        text = message,
                        color = if (state.writeResult == true) SuccessGreen else ErrorRed,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Text(
                    text = "Aproxime a tag NFC antes de executar um comando.",
                    color = OnSurfaceMuted,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

private data class CommandItem(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommandCard(
    command: CommandItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = command.icon,
                contentDescription = null,
                tint = PrimaryTeal,
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = command.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = command.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceMuted
                )
            }
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = OnSurfaceMuted
            )
        }
    }
}
