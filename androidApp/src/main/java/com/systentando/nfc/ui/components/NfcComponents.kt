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
import androidx.compose.ui.graphics.painter.Painter
import com.systentando.nfc.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcTopAppBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(title, color = OnSurface) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = null,
                    tint = OnSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark),
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
                    brush = Brush.horizontalGradient(listOf(PrimaryTeal, SecondaryGreen)),
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
                    color = OnSurface,
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
            color = PrimaryTeal,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceMuted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StatusMessage(
    success: Boolean,
    error: String?
) {
    error?.let {
        Text(it, color = ErrorRed, modifier = Modifier.padding(top = 8.dp))
    }
    if (success) {
        Text("Gravação concluída!", color = SuccessGreen, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun NfcCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
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
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryTeal)
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
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = PrimaryTeal
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurface,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = OnSurface,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}
