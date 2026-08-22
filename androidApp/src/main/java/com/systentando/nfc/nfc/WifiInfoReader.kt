package com.systentando.nfc.nfc

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build

object WifiInfoReader {

    @SuppressLint("MissingPermission")
    fun readCurrent(context: Context): WifiConnectionInfo? {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

            val ssid: String?
            val bssid: String?

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val network = cm.activeNetwork ?: return null
                val capabilities = cm.getNetworkCapabilities(network) ?: return null
                if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return null

                val wifiInfo = capabilities.transportInfo as? WifiInfo
                ssid = wifiInfo?.ssid?.removeSurrounding("\"")
                bssid = wifiInfo?.bssid
            } else {
                @Suppress("DEPRECATION")
                val wifiInfo = wifiManager.connectionInfo
                @Suppress("DEPRECATION")
                ssid = wifiInfo?.ssid?.removeSurrounding("\"")
                @Suppress("DEPRECATION")
                bssid = wifiInfo?.bssid
            }

            if (ssid.isNullOrBlank()) return null

            WifiConnectionInfo(
                ssid = ssid,
                bssid = bssid
            )
        } catch (e: Exception) {
            null
        }
    }

    data class WifiConnectionInfo(
        val ssid: String,
        val bssid: String?
    )
}
