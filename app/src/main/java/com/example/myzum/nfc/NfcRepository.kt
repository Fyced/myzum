package com.example.myzum.nfc

import android.content.Intent
import kotlinx.coroutines.flow.SharedFlow

class NfcRepository(private val nfcManager: NfcManager) {
    val tagDetected: SharedFlow<Intent> = nfcManager.tagDetected

    fun startListening() {
        nfcManager.enableForegroundDispatch()
    }

    fun stopListening() {
        nfcManager.disableForegroundDispatch()
    }
}
