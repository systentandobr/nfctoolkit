package com.systentando.nfc.nfc

import android.app.ActivityManager
import android.content.Context

object WalletDetector {

    private val walletPackages = setOf(
        "com.google.android.apps.walletnfcrel",
        "com.samsung.android.spay",
        "com.samsung.android.spaymini",
        "com.android.vending",
        "com.visa.android.apps.visacheckout",
        "com.mastercard.mcbp",
        "com.paypal.android.p2pmobile"
    )

    fun isWalletAppInForeground(context: Context): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningApp = am.runningAppProcesses?.firstOrNull { it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND }
        return runningApp?.pkgList?.any { it in walletPackages } ?: false
    }
}
