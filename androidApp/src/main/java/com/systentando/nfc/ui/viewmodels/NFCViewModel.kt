package com.systentando.nfc.ui.viewmodels

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systentando.nfc.data.local.TagRepository
import com.systentando.nfc.domain.model.NFCTag
import com.systentando.nfc.nfc.NFCManager
import com.systentando.nfc.nfc.NFCTagReader
import com.systentando.nfc.nfc.NFCTagWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NFCViewModel @Inject constructor(
    private val nfcManager: NFCManager,
    private val tagReader: NFCTagReader,
    private val tagWriter: NFCTagWriter,
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NFCUiState())
    val uiState: StateFlow<NFCUiState> = _uiState.asStateFlow()

    init {
        tagRepository.getAllTags()
            .onEach { tags ->
                _uiState.update { it.copy(recentTags = tags) }
            }
            .launchIn(viewModelScope)
    }

    fun startPolling() {
        _uiState.update { it.copy(isPolling = true) }
        nfcManager.startContinuousPolling(
            onTagDetected = { tag ->
                viewModelScope.launch {
                    val nfcTag = tagReader.read(tag)
                    tagRepository.saveTag(nfcTag)
                    _uiState.update {
                        it.copy(lastTag = nfcTag, recentTags = it.recentTags + nfcTag)
                    }
                }
            },
            onError = { error ->
                _uiState.update {
                    it.copy(errorMessage = error.message, isPolling = false, nfcEnabled = false)
                }
            },
            onWalletDetected = { detected ->
                _uiState.update { it.copy(isWalletDetected = detected) }
            }
        )
    }

    fun stopPolling() {
        nfcManager.stopPolling()
        _uiState.update { it.copy(isPolling = false) }
    }

    fun handleNfcTag(tag: Tag) {
        viewModelScope.launch {
            val nfcTag = tagReader.read(tag)
            tagRepository.saveTag(nfcTag)
            _uiState.update {
                it.copy(
                    lastTag = nfcTag,
                    recentTags = (it.recentTags + nfcTag).takeLast(5),
                    pendingTag = nfcTag,
                    nfcEnabled = true
                )
            }
        }
    }

    fun consumePendingTag() {
        _uiState.update { it.copy(pendingTag = null) }
    }

    fun selectTag(tag: NFCTag) {
        _uiState.update { it.copy(pendingTag = tag) }
    }

    fun deleteTag(tag: NFCTag) {
        viewModelScope.launch {
            val id = "${tag.uid}_${tag.readAt}"
            tagRepository.deleteTag(id)
        }
    }

    fun startErase() {
        _uiState.update { it.copy(
            isWriting = true,
            writeResult = null,
            writeMessage = "Aproxime a tag para apagar os dados"
        ) }
        nfcManager.startWriting(
            onDone = { success ->
                _uiState.update { it.copy(
                    isWriting = false,
                    writeResult = success,
                    writeMessage = if (success) "Tag apagada com sucesso" else "Falha ao apagar a tag"
                ) }
            },
            writeOperation = { tag ->
                tagWriter.erase(tag)
            }
        )
    }

    fun startFormat() {
        _uiState.update { it.copy(
            isWriting = true,
            writeResult = null,
            writeMessage = "Aproxime a tag para formatar"
        ) }
        nfcManager.startWriting(
            onDone = { success ->
                _uiState.update { it.copy(
                    isWriting = false,
                    writeResult = success,
                    writeMessage = if (success) "Tag formatada com sucesso" else "Falha ao formatar a tag"
                ) }
            },
            writeOperation = { tag ->
                val format = android.nfc.tech.NdefFormatable.get(tag) ?: return@startWriting false
                return@startWriting try {
                    format.connect()
                    format.format(android.nfc.NdefMessage(arrayOf()))
                    format.close()
                    true
                } catch (e: Exception) {
                    false
                }
            }
        )
    }

    fun startMemoryRead() {
        _uiState.update { it.copy(
            isWriting = true,
            writeResult = null,
            writeMessage = "Aproxime a tag para ler memória bruta"
        ) }
        nfcManager.startWriting(
            onDone = { success ->
                _uiState.update { it.copy(
                    isWriting = false,
                    writeResult = success,
                    writeMessage = if (success) "Memória lida com sucesso" else "Falha ao ler a memória"
                ) }
            },
            writeOperation = { _ ->
                true
            }
        )
    }

    fun startLock() {
        _uiState.update { it.copy(
            isWriting = true,
            writeResult = null,
            writeMessage = "Aproxime a tag para aplicar proteção"
        ) }
        nfcManager.startWriting(
            onDone = { success ->
                _uiState.update { it.copy(
                    isWriting = false,
                    writeResult = success,
                    writeMessage = if (success) "Proteção aplicada" else "Falha ao aplicar proteção"
                ) }
            },
            writeOperation = { _ ->
                true
            }
        )
    }

    fun startApdu() {
        _uiState.update { it.copy(
            isWriting = true,
            writeResult = null,
            writeMessage = "Aproxime a tag para enviar APDU"
        ) }
        nfcManager.startWriting(
            onDone = { success ->
                _uiState.update { it.copy(
                    isWriting = false,
                    writeResult = success,
                    writeMessage = if (success) "APDU enviada" else "Falha ao enviar APDU"
                ) }
            },
            writeOperation = { _ ->
                true
            }
        )
    }

    fun cancelOperation() {
        nfcManager.cancelWriting()
        _uiState.update { it.copy(
            isWriting = false,
            writeResult = null,
            writeMessage = null
        ) }
    }

    fun clearWriteMessage() {
        _uiState.update { it.copy(writeMessage = null, writeResult = null) }
    }

    data class NFCUiState(
        val isPolling: Boolean = false,
        val isWalletDetected: Boolean = false,
        val lastTag: NFCTag? = null,
        val pendingTag: NFCTag? = null,
        val recentTags: List<NFCTag> = emptyList(),
        val errorMessage: String? = null,
        val nfcEnabled: Boolean = true,
        val isWriting: Boolean = false,
        val writeResult: Boolean? = null,
        val writeMessage: String? = null
    )
}
