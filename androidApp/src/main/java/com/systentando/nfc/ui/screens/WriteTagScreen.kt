package com.systentando.nfc.ui.screens

import kotlinx.coroutines.launch

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.systentando.nfc.domain.model.WritePayload
import com.systentando.nfc.nfc.WifiInfoReader
import com.systentando.nfc.util.COUNTRY_CODES
import com.systentando.nfc.util.DEFAULT_COUNTRY
import com.systentando.nfc.util.LocationHelper
import com.systentando.nfc.util.PhoneVisualTransformation
import com.systentando.nfc.util.removeNonDigits
import com.systentando.nfc.ui.components.OutlinedTealButton
import com.systentando.nfc.ui.components.PrimaryGradientButton
import com.systentando.nfc.ui.components.StatusMessage
import com.systentando.nfc.ui.components.WaitingNfcMessage
import com.systentando.nfc.ui.components.WiFiQRCodeGenerator
import com.systentando.nfc.util.openGoogleMaps
import com.systentando.nfc.util.openUrl
import com.systentando.nfc.util.openWhatsApp
import com.systentando.nfc.util.shareWifi
import com.systentando.nfc.ui.theme.*
import com.systentando.nfc.ui.viewmodels.WriteTagViewModel

private val writeOptions = listOf(
    WriteOption(WritePayload.Type.TEXT, "Texto", Icons.Default.TextFields),
    WriteOption(WritePayload.Type.URL, "URL / Link", Icons.Default.Link),
    WriteOption(WritePayload.Type.EMAIL, "E-mail", Icons.Default.Email),
    WriteOption(WritePayload.Type.LOCATION, "Localização", Icons.Default.LocationOn),
    WriteOption(WritePayload.Type.WIFI, "Wi-Fi", Icons.Default.Wifi),
    WriteOption(WritePayload.Type.PHONE, "Telefone", Icons.Default.Phone),
    WriteOption(WritePayload.Type.GOOGLE_BUSINESS, "Google Business", Icons.Default.Star),
    WriteOption(WritePayload.Type.RAW, "Texto Livre", Icons.Default.Code)
)

internal data class WriteOption(
    val type: WritePayload.Type,
    val label: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteTagScreen(
    viewModel: WriteTagViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            com.systentando.nfc.ui.components.NfcTopAppBar(
                title = "O que deseja gravar?",
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        if (state.payload == null) {
            TypeSelectionGrid(
                options = writeOptions,
                onTypeSelected = { viewModel.selectType(it) },
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            )
        } else {
            PayloadForm(
                state = state,
                onPayloadChange = viewModel::setPayload,
                onWriteClick = viewModel::startWaitingForTag,
                onBack = viewModel::clearPayload,
                onReset = viewModel::resetCurrentPayload,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
            )
        }
    }
}

@Composable
internal fun TypeSelectionGrid(
    options: List<WriteOption>,
    onTypeSelected: (WritePayload.Type) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(options) { option ->
            WriteOptionCard(
                option = option,
                onClick = { onTypeSelected(option.type) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WriteOptionCard(
    option: WriteOption,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = option.label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun PayloadForm(
    state: WriteTagViewModel.WriteUiState,
    onPayloadChange: (WritePayload) -> Unit,
    onWriteClick: () -> Unit,
    onBack: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Preencha os dados",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        when (val p = state.payload) {
            is WritePayload.Text -> TextForm(p) { onPayloadChange(it) }
            is WritePayload.Url -> UrlForm(p, context) { onPayloadChange(it) }
            is WritePayload.Email -> EmailForm(p) { onPayloadChange(it) }
            is WritePayload.Location -> LocationForm(p, context) { onPayloadChange(it) }
            is WritePayload.WiFi -> WiFiForm(p, context) { onPayloadChange(it) }
            is WritePayload.Phone -> PhoneForm(p, context) { onPayloadChange(it) }
            is WritePayload.GoogleBusiness -> GoogleBusinessForm(p) { onPayloadChange(it) }
            is WritePayload.Raw -> RawForm(p) { onPayloadChange(it) }
            null -> {}
        }

        Spacer(modifier = Modifier.height(20.dp))

        PrimaryGradientButton(
            text = "GRAVAR",
            onClick = onWriteClick,
            iconRes = com.systentando.nfc.R.drawable.ic_nfc_write
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Text("Voltar")
            }
            TextButton(
                onClick = onReset,
                modifier = Modifier.weight(1f)
            ) {
                Text("Limpar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (state.isWriting) {
            WaitingNfcMessage()
        }
        StatusMessage(success = state.writeSuccess, error = state.error)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun TextForm(payload: WritePayload.Text, onChange: (WritePayload.Text) -> Unit) {
    NfcTextField(
        value = payload.text,
        onValueChange = { onChange(payload.copy(text = it)) },
        label = "Texto"
    )
}

@Composable
fun UrlForm(
    payload: WritePayload.Url,
    context: Context,
    onChange: (WritePayload.Url) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NfcTextField(
            value = payload.url,
            onValueChange = { onChange(payload.copy(url = it)) },
            label = "URL"
        )
        if (payload.url.isNotBlank()) {
            OutlinedTealButton(
                text = "Abrir no navegador e confirmar",
                onClick = { openUrl(context, payload.url) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun EmailForm(payload: WritePayload.Email, onChange: (WritePayload.Email) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NfcTextField(
            value = payload.address,
            onValueChange = { onChange(payload.copy(address = it)) },
            label = "E-mail"
        )
        NfcTextField(
            value = payload.subject,
            onValueChange = { onChange(payload.copy(subject = it)) },
            label = "Assunto"
        )
        NfcTextField(
            value = payload.body,
            onValueChange = { onChange(payload.copy(body = it)) },
            label = "Mensagem"
        )
    }
}

@Composable
fun LocationForm(
    payload: WritePayload.Location,
    context: Context,
    onChange: (WritePayload.Location) -> Unit
) {
    val locationHelper = remember { LocationHelper(context) }
    val scope = rememberCoroutineScope()
    var addressQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NfcTextField(
            value = addressQuery,
            onValueChange = { addressQuery = it },
            label = "Endereço ou local"
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        error = null
                        val result = locationHelper.geocodeAddress(addressQuery)
                        if (result != null) {
                            onChange(
                                payload.copy(
                                    latitude = result.latitude,
                                    longitude = result.longitude
                                )
                            )
                        } else {
                            error = "Não foi possível localizar o endereço"
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Buscar", color = MaterialTheme.colorScheme.onSurface)
            }

            OutlinedTealButton(
                text = "Usar local atual",
                onClick = {
                    scope.launch {
                        isLoading = true
                        error = null
                        val result = locationHelper.getCurrentLocation()
                        if (result != null) {
                            onChange(
                                payload.copy(
                                    latitude = result.latitude,
                                    longitude = result.longitude
                                )
                            )
                            addressQuery = result.address ?: ""
                        } else {
                            error = "Não foi possível obter a localização atual"
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }

        if (isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NfcTextField(
                value = payload.latitude.toString(),
                onValueChange = { onChange(payload.copy(latitude = it.toDoubleOrNull() ?: 0.0)) },
                label = "Latitude",
                modifier = Modifier.weight(1f)
            )
            NfcTextField(
                value = payload.longitude.toString(),
                onValueChange = { onChange(payload.copy(longitude = it.toDoubleOrNull() ?: 0.0)) },
                label = "Longitude",
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTealButton(
            text = "Abrir no Google Maps",
            onClick = { openGoogleMaps(context, payload.latitude, payload.longitude) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun WiFiForm(
    payload: WritePayload.WiFi,
    context: Context,
    onChange: (WritePayload.WiFi) -> Unit
) {
    val bitmap: Bitmap? = remember(payload.ssid, payload.password) {
        WiFiQRCodeGenerator.generate(
            ssid = payload.ssid,
            password = payload.password,
            security = when (payload.security) {
                WritePayload.WiFiSecurityType.OPEN -> "nopass"
                WritePayload.WiFiSecurityType.WEP -> "WEP"
                WritePayload.WiFiSecurityType.WPA_WPA2 -> "WPA"
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val info = WifiInfoReader.readCurrent(context)
            info?.let { onChange(payload.copy(ssid = it.ssid)) }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NfcTextField(
            value = payload.ssid,
            onValueChange = { onChange(payload.copy(ssid = it)) },
            label = "Nome da rede (SSID)"
        )
        NfcTextField(
            value = payload.password,
            onValueChange = { onChange(payload.copy(password = it)) },
            label = "Senha"
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTealButton(
                text = "Usar Wi-Fi conectada",
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    } else {
                        val info = WifiInfoReader.readCurrent(context)
                        info?.let { onChange(payload.copy(ssid = it.ssid)) }
                    }
                },
                modifier = Modifier.weight(1f)
            )
            OutlinedTealButton(
                text = "Compartilhar",
                onClick = { shareWifi(context, payload.ssid, payload.password) },
                modifier = Modifier.weight(1f)
            )
        }

        bitmap?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = MaterialTheme.shapes.large
            ) {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "QR Code Wi-Fi",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneForm(
    payload: WritePayload.Phone,
    context: Context,
    onChange: (WritePayload.Phone) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCountry = COUNTRY_CODES.firstOrNull { it.code == payload.countryCode } ?: DEFAULT_COUNTRY
    val digitsOnly = removeNonDigits(payload.number)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = "${selectedCountry.flag} ${selectedCountry.code} - ${selectedCountry.country}",
                onValueChange = {},
                readOnly = true,
                label = { Text("País") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                COUNTRY_CODES.forEach { country ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "${country.flag} ${country.code} - ${country.country}",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            onChange(
                                payload.copy(
                                    countryCode = country.code,
                                    number = ""
                                )
                            )
                            expanded = false
                        }
                    )
                }
            }
        }

        NfcTextField(
            value = payload.number,
            onValueChange = {
                val digits = removeNonDigits(it)
                onChange(payload.copy(number = digits))
            },
            label = "Número",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = PhoneVisualTransformation(selectedCountry.mask)
        )

        if (payload.countryCode.isNotBlank() && digitsOnly.isNotBlank()) {
            OutlinedTealButton(
                text = "Abrir conversa no WhatsApp",
                onClick = { openWhatsApp(context, payload.countryCode + digitsOnly) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun GoogleBusinessForm(payload: WritePayload.GoogleBusiness, onChange: (WritePayload.GoogleBusiness) -> Unit) {
    NfcTextField(
        value = payload.link,
        onValueChange = { onChange(payload.copy(link = it)) },
        label = "Link Google Business"
    )
}

@Composable
fun RawForm(payload: WritePayload.Raw, onChange: (WritePayload.Raw) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NfcTextField(
            value = payload.mimeType,
            onValueChange = { onChange(payload.copy(mimeType = it)) },
            label = "MIME Type"
        )
        NfcTextField(
            value = payload.content,
            onValueChange = { onChange(payload.copy(content = it)) },
            label = "Conteúdo"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation
    )
}


