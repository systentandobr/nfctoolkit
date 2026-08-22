package com.systentando.nfc.ui.viewmodels

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systentando.nfc.data.local.FormStateRepository
import com.systentando.nfc.domain.model.WritePayload
import com.systentando.nfc.nfc.NFCManager
import com.systentando.nfc.nfc.NFCTagWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WriteTagViewModel @Inject constructor(
    private val tagWriter: NFCTagWriter,
    private val nfcManager: NFCManager,
    private val formStateRepository: FormStateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WriteUiState())
    val uiState: StateFlow<WriteUiState> = _uiState.asStateFlow()

    fun selectType(type: WritePayload.Type) {
        viewModelScope.launch {
            val saved = formStateRepository.load(type)
            _uiState.update {
                it.copy(
                    selectedType = type,
                    payload = saved ?: buildInitialPayload(type),
                    writeSuccess = false,
                    error = null
                )
            }
        }
    }

    fun setPayload(payload: WritePayload) {
        _uiState.update { it.copy(payload = payload) }
        viewModelScope.launch {
            formStateRepository.save(payload.type, payload)
        }
    }

    fun clearPayload() {
        val type = _uiState.value.selectedType
        viewModelScope.launch {
            formStateRepository.clear(type)
        }
        cancelWriting()
        _uiState.update { it.copy(payload = null, writeSuccess = false, error = null, isWriting = false) }
    }

    fun cancelWriting() {
        nfcManager.cancelWriting()
    }

    fun resetCurrentPayload() {
        val type = _uiState.value.selectedType
        val initial = buildInitialPayload(type)
        _uiState.update { it.copy(payload = initial) }
        viewModelScope.launch {
            formStateRepository.save(type, initial)
        }
    }

    fun startWaitingForTag() {
        val payload = _uiState.value.payload ?: return
        _uiState.update { it.copy(isWriting = true, error = null, writeSuccess = false) }

        nfcManager.startWriting(
            onDone = { success ->
                _uiState.update {
                    if (success) {
                        it.copy(isWriting = false, writeSuccess = true)
                    } else {
                        it.copy(isWriting = false, error = "Falha ao gravar a tag. Verifique se ela é gravável.")
                    }
                }
            },
            writeOperation = { tag ->
                tagWriter.write(tag, payload)
            }
        )
    }

    fun setWriting(writing: Boolean) {
        _uiState.update { it.copy(isWriting = writing) }
    }

    fun setError(error: String?) {
        _uiState.update { it.copy(error = error, isWriting = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun buildInitialPayload(type: WritePayload.Type): WritePayload {
        return when (type) {
            WritePayload.Type.TEXT -> WritePayload.Text(text = "")
            WritePayload.Type.URL -> WritePayload.Url(url = "")
            WritePayload.Type.EMAIL -> WritePayload.Email(address = "")
            WritePayload.Type.LOCATION -> WritePayload.Location(latitude = 0.0, longitude = 0.0)
            WritePayload.Type.WIFI -> WritePayload.WiFi(
                ssid = "",
                password = "",
                security = WritePayload.WiFiSecurityType.WPA_WPA2
            )
            WritePayload.Type.PHONE -> WritePayload.Phone(countryCode = "+55", number = "")
            WritePayload.Type.GOOGLE_BUSINESS -> WritePayload.GoogleBusiness(link = "")
            WritePayload.Type.RAW -> WritePayload.Raw(mimeType = "text/plain", content = "")
        }
    }

    data class WriteUiState(
        val selectedType: WritePayload.Type = WritePayload.Type.TEXT,
        val payload: WritePayload? = null,
        val isWriting: Boolean = false,
        val writeSuccess: Boolean = false,
        val error: String? = null
    )
}
