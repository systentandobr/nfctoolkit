package com.systentando.nfc

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.systentando.nfc.nfc.NFCManager
import com.systentando.nfc.ui.navigation.AppNavigation
import com.systentando.nfc.ui.theme.NFCToolkitTheme
import com.systentando.nfc.ui.viewmodels.NFCViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var nfcManager: NFCManager

    private val nfcViewModel: NFCViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcManager.setActivity(this)
        handleNfcIntent(intent)
        setContent {
            NFCToolkitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(nfcViewModel = nfcViewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcManager.setActivity(this)
        nfcViewModel.startPolling()
    }

    override fun onPause() {
        super.onPause()
        nfcViewModel.stopPolling()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleNfcIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        nfcManager.setActivity(null)
    }

    private fun handleNfcIntent(intent: Intent?) {
        val action = intent?.action ?: return
        if (action != NfcAdapter.ACTION_NDEF_DISCOVERED &&
            action != NfcAdapter.ACTION_TECH_DISCOVERED &&
            action != NfcAdapter.ACTION_TAG_DISCOVERED
        ) {
            return
        }

        val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        } ?: return

        nfcViewModel.handleNfcTag(tag)
    }
}
