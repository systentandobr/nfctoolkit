package com.systentando.nfc.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.systentando.nfc.R
import com.systentando.nfc.ui.theme.GradientEnd
import com.systentando.nfc.ui.theme.GradientStart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcTopAppBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    TopAppBar(
        title = { Text(title, color = colors.onSurface) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = null,
                    tint = colors.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background),
        modifier = modifier
    )
}

@Composable
fun PrimaryGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconRes: Int? = null
) {
    val colors = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(listOf(GradientStart, GradientEnd)),
                    shape = MaterialTheme.shapes.large
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                iconRes?.let { res ->
                    Image(
                        painter = painterResource(id = res),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = text,
                    color = colors.onPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WaitingNfcMessage(
    modifier: Modifier = Modifier,
    imageRes: Int = R.drawable.ic_awaiting_tag,
    title: String = "Aproxime a tag NFC",
    subtitle: String = "Toque o celular em uma tag vazia para gravar."
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StatusMessage(
    success: Boolean,
    error: String?
) {
    val colors = MaterialTheme.colorScheme
    error?.let {
        Text(it, color = colors.error, modifier = Modifier.padding(top = 8.dp))
    }
    if (success) {
        Text("Gravação concluída!", color = colors.secondary, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun NfcCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = MaterialTheme.shapes.large
    ) {
        content()
    }
}

@Composable
fun OutlinedTealButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary)
    ) {
        Text(text)
    }
}

@Composable
fun NfcLabelValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = colors.onSurface,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}
