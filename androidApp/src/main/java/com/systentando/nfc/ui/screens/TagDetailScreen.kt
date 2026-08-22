package com.systentando.nfc.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.systentando.nfc.domain.model.NDEFRecord
import com.systentando.nfc.domain.model.NFCTag
import com.systentando.nfc.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagDetailScreen(
    tag: NFCTag?,
    viewModel: com.systentando.nfc.ui.viewmodels.NFCViewModel,
    onBackClick: () -> Unit,
    onDeleteTag: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    if (tag == null) {
        onBackClick()
        return
    }

    val shareText = buildShareText(tag)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes da Tag", color = OnSurface) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                MetadataCard(tag = tag)
            }

            item {
                Text(
                    text = "Registros NDEF",
                    style = MaterialTheme.typography.titleLarge,
                    color = OnSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(tag.records.size) { index ->
                RecordCard(record = tag.records[index])
            }

            if (tag.records.isEmpty()) {
                item {
                    Text(
                        text = "Nenhum registro NDEF encontrado",
                        color = OnSurfaceMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                ActionRow(
                    onCopy = { clipboard.setText(AnnotatedString(shareText)) },
                    onShare = { shareText(context, shareText) },
                    onDelete = {
                        viewModel.startErase()
                        onDeleteTag()
                    }
                )
            }
        }
    }
}

private fun buildShareText(tag: NFCTag): String {
    val sb = StringBuilder()
    sb.appendLine("UID: ${tag.uid}")
    sb.appendLine("Chip: ${tag.chipType}")
    sb.appendLine("Tecnologia: ${tag.technology}")
    if (tag.records.isNotEmpty()) {
        sb.appendLine()
        tag.records.forEachIndexed { index, record ->
            sb.appendLine("--- Registro ${index + 1} ---")
            sb.appendLine(record.asShareText())
        }
    }
    return sb.toString().trim()
}

private fun NDEFRecord.asShareText(): String {
    return when (this) {
        is NDEFRecord.Text -> "Texto: $text"
        is NDEFRecord.Uri -> "URL: $uri"
        is NDEFRecord.Mime -> "MIME [$type]: $payload"
        is NDEFRecord.External -> "[$type]"
        is NDEFRecord.Unknown -> "[$type]"
    }
}

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(intent, "Compartilhar dados da tag")
    context.startActivity(chooser)
}

@Composable
fun MetadataCard(tag: NFCTag) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LabeledValue(label = "UID", value = tag.uid)
            LabeledValue(label = "Chip", value = tag.chipType)
            LabeledValue(label = "Tecnologia", value = tag.technology)

            MemoryBar(used = tag.usedBytes, total = tag.sizeBytes)
        }
    }
}

@Composable
fun LabeledValue(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = PrimaryTeal
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurface,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun MemoryBar(used: Int, total: Int) {
    val ratio = if (total > 0) used.toFloat() / total else 0f
    Column {
        Text(
            text = "Memória: $used / $total bytes",
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceMuted
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(SurfaceVariant, RoundedCornerShape(6.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(PrimaryTeal, RoundedCornerShape(6.dp))
            )
        }
    }
}

enum class UriKind { NONE, GEO, PHONE, OTHER }

@Composable
fun RecordCard(record: NDEFRecord) {
    val context = LocalContext.current
    val (icon, label, content, kind) = when (record) {
        is NDEFRecord.Text -> RecordInfo(Icons.Default.TextFields, "Texto", record.text, UriKind.NONE)
        is NDEFRecord.Uri -> {
            when {
                record.uri.startsWith("geo:") -> RecordInfo(Icons.Default.LocationOn, "Localização", record.uri, UriKind.GEO)
                record.uri.startsWith("tel:") -> RecordInfo(Icons.Default.Phone, "Telefone", record.uri, UriKind.PHONE)
                else -> RecordInfo(Icons.Default.Link, "URL", record.uri, UriKind.OTHER)
            }
        }
        is NDEFRecord.Mime -> RecordInfo(Icons.Default.AttachFile, record.type, record.payload, UriKind.NONE)
        is NDEFRecord.External -> RecordInfo(Icons.Default.Memory, record.type, "<dados binários>", UriKind.NONE)
        is NDEFRecord.Unknown -> RecordInfo(Icons.Default.Help, record.type, "<desconhecido>", UriKind.NONE)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryTeal,
                    modifier = Modifier.size(28.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = PrimaryTeal
                    )
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnSurface
                    )
                }
            }

            if (record is NDEFRecord.Uri) {
                when (kind) {
                    UriKind.GEO -> {
                        val coords = record.uri.removePrefix("geo:").split(",")
                        if (coords.size >= 2) {
                            val lat = coords[0].toDoubleOrNull()
                            val lng = coords[1].toDoubleOrNull()
                            if (lat != null && lng != null) {
                                OutlinedButton(
                                    onClick = { openGoogleMaps(context, lat, lng) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryTeal)
                                ) {
                                    Text("Abrir no Google Maps")
                                }
                            }
                        }
                    }
                    UriKind.PHONE -> {
                        val number = record.uri.removePrefix("tel:")
                        OutlinedButton(
                            onClick = { openWhatsApp(context, number) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryTeal)
                        ) {
                            Text("Abrir no WhatsApp")
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

data class RecordInfo(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
    val content: String,
    val kind: UriKind
)

private fun openWhatsApp(context: Context, number: String) {
    val digits = number.replace("[^0-9]".toRegex(), "")
    val uri = Uri.parse("https://wa.me/$digits")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    }
}

private fun openGoogleMaps(context: Context, latitude: Double, longitude: Double) {
    val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.setPackage("com.google.android.apps.maps")
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        val fallback = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"))
        context.startActivity(fallback)
    }
}


@Composable
fun ActionRow(
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionButton(
            icon = Icons.Default.ContentCopy,
            label = "Copiar",
            onClick = onCopy,
            modifier = Modifier.weight(1f)
        )
        ActionButton(
            icon = Icons.Default.Share,
            label = "Compartilhar",
            onClick = onShare,
            modifier = Modifier.weight(1f)
        )
        ActionButton(
            icon = Icons.Default.Delete,
            label = "Deletar",
            onClick = onDelete,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurface)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = null, tint = PrimaryTeal)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}
