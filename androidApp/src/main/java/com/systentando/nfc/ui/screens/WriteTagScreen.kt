package com.systentando.nfc.ui.screens

import kotlinx.coroutines.launch

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.systentando.nfc.R
import com.systentando.nfc.domain.model.WritePayload
import com.systentando.nfc.nfc.WifiInfoReader
import com.systentando.nfc.util.COUNTRY_CODES
import com.systentando.nfc.util.DEFAULT_COUNTRY
import com.systentando.nfc.util.LocationHelper
import com.systentando.nfc.util.PhoneVisualTransformation
import com.systentando.nfc.util.removeNonDigits
import com.systentando.nfc.ui.components.WiFiQRCodeGenerator
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

    LaunchedEffect(state.payload) {
        if (state.payload != null) {
            viewModel.startWaitingForTag()
        } else {
            viewModel.cancelWriting()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("O que deseja gravar?", color = OnSurface) },
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
        if (state.payload == null) {
            TypeSelectionGrid(
                options = writeOptions,
                onTypeSelected = { viewModel.selectType(it) },
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark)
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
                    .background(BackgroundDark)
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
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
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
                tint = PrimaryTeal,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = option.label,
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurface,
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
            color = OnSurface,
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

        Button(
            onClick = onWriteClick,
            modifier = Modifier
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
                Text(
                    text = "GRAVAR",
                    color = OnSurface,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurface)
            ) {
                Text("Voltar")
            }
            TextButton(
                onClick = onReset,
                modifier = Modifier.weight(1f)
            ) {
                Text("Limpar", color = OnSurfaceMuted)
            }
        }

        if (state.isWriting) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.writing_nfc),
                    contentDescription = null,
                    modifier = Modifier.size(180.dp)
                )
                Text(
                    text = "Aguardando tag NFC...",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryTeal,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Aproxime o celular de uma tag vazia para gravar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        state.error?.let { Text(it, color = ErrorRed, modifier = Modifier.padding(top = 8.dp)) }
        if (state.writeSuccess) {
            Text("Gravação concluída!", color = SuccessGreen, modifier = Modifier.padding(top = 8.dp))
        }

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
            OutlinedButton(
                onClick = { openUrl(context, payload.url) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryTeal)
            ) {
                Text("Abrir no navegador e confirmar")
            }
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
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
            ) {
                Text("Buscar", color = OnSurface)
            }

            OutlinedButton(
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
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryTeal)
            ) {
                Text("Usar local atual")
            }
        }

        if (isLoading) {
            CircularProgressIndicator(color = PrimaryTeal, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        error?.let { Text(it, color = ErrorRed) }

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

        OutlinedButton(
            onClick = { openGoogleMaps(context, payload.latitude, payload.longitude) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryTeal)
        ) {
            Text("Abrir no Google Maps")
        }
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
            OutlinedButton(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    } else {
                        val info = WifiInfoReader.readCurrent(context)
                        info?.let { onChange(payload.copy(ssid = it.ssid)) }
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryTeal)
            ) {
                Text("Usar Wi-Fi conectada")
            }
            OutlinedButton(
                onClick = { shareWifi(context, payload.ssid, payload.password) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryTeal)
            ) {
                Text("Compartilhar")
            }
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
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark,
                    focusedTextColor = OnSurface,
                    unfocusedTextColor = OnSurface,
                    focusedLabelColor = PrimaryTeal,
                    unfocusedLabelColor = OnSurfaceMuted
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(SurfaceDark)
            ) {
                COUNTRY_CODES.forEach { country ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "${country.flag} ${country.code} - ${country.country}",
                                color = OnSurface
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
            OutlinedButton(
                onClick = { openWhatsApp(context, payload.countryCode + digitsOnly) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryTeal)
            ) {
                Text("Abrir conversa no WhatsApp")
            }
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
            focusedContainerColor = SurfaceDark,
            unfocusedContainerColor = SurfaceDark,
            focusedTextColor = OnSurface,
            unfocusedTextColor = OnSurface,
            focusedLabelColor = PrimaryTeal,
            unfocusedLabelColor = OnSurfaceMuted,
            focusedIndicatorColor = PrimaryTeal,
            unfocusedIndicatorColor = OnSurfaceMuted
        ),
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation
    )
}

private fun openUrl(context: Context, url: String) {
    val fixedUrl = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fixedUrl))
    context.startActivity(intent)
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

private fun openWhatsApp(context: Context, number: String) {
    val digits = number.replace(Regex("[^0-9]"), "")
    val uri = Uri.parse("https://wa.me/$digits")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(intent)
}

private fun shareWifi(context: Context, ssid: String, password: String) {
    val text = buildString {
        appendLine("Rede Wi-Fi")
        appendLine("SSID: $ssid")
        if (password.isNotBlank()) appendLine("Senha: $password")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(intent, "Compartilhar Wi-Fi")
    context.startActivity(chooser)
}
