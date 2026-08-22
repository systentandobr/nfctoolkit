package com.systentando.nfc.nfc

import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Bundle
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NFCManager @Inject constructor(
    private val context: Context,
    private val nfcAdapter: NfcAdapter?
) {
    private val pollingIntervalMs = 3000L
    private val readWindowMs = 800L
    private val writeWindowMs = 15000L
    @Volatile
    private var isPolling = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pollingJob: Job? = null

    @Volatile
    private var currentActivity: Activity? = null
    private var onTagRead: ((Tag) -> Unit)? = null
    private var onError: ((Throwable) -> Unit)? = null
    private var onWalletDetected: ((Boolean) -> Unit)? = null

    @Volatile
    private var mode: Mode = Mode.READ

    private var writeOperation: ((Tag) -> Boolean)? = null
    private var onWriteDone: ((Boolean) -> Unit)? = null

    private enum class Mode {
        READ,
        WRITE
    }

    fun setActivity(activity: Activity?) {
        currentActivity = activity
    }

    fun startContinuousPolling(
        onTagDetected: (Tag) -> Unit,
        onError: (Throwable) -> Unit,
        onWalletDetected: (Boolean) -> Unit = {}
    ) {
        if (nfcAdapter == null) {
            onError(NFCNotAvailableException("NFC adapter not available"))
            return
        }

        if (!nfcAdapter.isEnabled) {
            onError(NFCNotEnabledException("NFC is disabled"))
            return
        }

        this.onTagRead = onTagDetected
        this.onError = onError
        this.onWalletDetected = onWalletDetected

        if (isPolling) return

        isPolling = true
        if (mode != Mode.WRITE) {
            mode = Mode.READ
        }

        pollingJob?.cancel()
        pollingJob = scope.launch {
            runPollingLoop()
        }
    }

    fun startWriting(
        onDone: (Boolean) -> Unit,
        writeOperation: (Tag) -> Boolean
    ) {
        this.writeOperation = writeOperation
        this.onWriteDone = onDone
        mode = Mode.WRITE

        if (!isPolling) {
            isPolling = true
            pollingJob?.cancel()
            pollingJob = scope.launch {
                runPollingLoop()
            }
        }
    }

    fun cancelWriting() {
        mode = Mode.READ
        writeOperation = null
        onWriteDone = null
    }

    fun stopPolling() {
        isPolling = false
        mode = Mode.READ
        writeOperation = null
        onWriteDone = null
        pollingJob?.cancel()
    }

    private suspend fun runPollingLoop() {
        while (coroutineContext.isActive && isPolling) {
            val walletInForeground = WalletDetector.isWalletAppInForeground(context)
            onWalletDetected?.invoke(walletInForeground)

            if (walletInForeground) {
                delay(pollingIntervalMs)
                continue
            }

            val activity = currentActivity
            if (activity == null) {
                delay(pollingIntervalMs)
                continue
            }

            try {
                withContext(Dispatchers.Main) {
                    nfcAdapter?.enableReaderMode(
                        activity,
                        { tag ->
                            when (mode) {
                                Mode.READ -> onTagRead?.invoke(tag)
                                Mode.WRITE -> {
                                    val operation = writeOperation
                                    val done = onWriteDone
                                    if (operation != null) {
                                        val success = operation(tag)
                                        mode = Mode.READ
                                        writeOperation = null
                                        onWriteDone = null
                                        done?.invoke(success)
                                    }
                                }
                            }
                        },
                        NfcAdapter.FLAG_READER_NFC_A or
                            NfcAdapter.FLAG_READER_NFC_B or
                            NfcAdapter.FLAG_READER_NFC_F or
                            NfcAdapter.FLAG_READER_NFC_V or
                            NfcAdapter.FLAG_READER_NFC_BARCODE,
                        null
                    )
                }

                val window = if (mode == Mode.WRITE) writeWindowMs else readWindowMs
                delay(window)

                withContext(Dispatchers.Main) {
                    nfcAdapter?.disableReaderMode(activity)
                }

                if (mode == Mode.READ) {
                    delay(pollingIntervalMs)
                }
            } catch (e: Exception) {
                onError?.invoke(e)
                if (mode == Mode.WRITE) {
                    onWriteDone?.invoke(false)
                    mode = Mode.READ
                    writeOperation = null
                    onWriteDone = null
                }
            }
        }
    }

    fun writeNdef(tag: Tag, operation: () -> Boolean): Boolean {
        val ndef = Ndef.get(tag) ?: return false
        return try {
            ndef.connect()
            val result = operation()
            ndef.close()
            result
        } catch (e: Exception) {
            false
        }
    }

    fun formatNdef(tag: Tag, operation: () -> Boolean): Boolean {
        val format = NdefFormatable.get(tag) ?: return false
        return try {
            format.connect()
            val result = operation()
            format.close()
            result
        } catch (e: Exception) {
            false
        }
    }
}

class NFCNotAvailableException(message: String) : Exception(message)
class NFCNotEnabledException(message: String) : Exception(message)
