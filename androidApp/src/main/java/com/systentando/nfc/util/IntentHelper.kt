package com.systentando.nfc.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

private val whatsAppPackages = listOf(
    "com.whatsapp",
    "com.whatsapp.w4b"
)

fun openWhatsApp(context: Context, number: String) {
    val digits = number.replace(Regex("[^0-9]"), "")
    if (digits.isBlank()) return

    val whatsAppPackage = whatsAppPackages.firstOrNull { packageInstalled(context, it) }

    if (whatsAppPackage != null) {
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$digits")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(whatsAppPackage)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            return
        }
    }

    val fallback = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits"))
    if (fallback.resolveActivity(context.packageManager) != null) {
        context.startActivity(fallback)
    }
}

private fun packageInstalled(context: Context, packageName: String): Boolean {
    return try {
        context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

fun openGoogleMaps(context: Context, latitude: Double, longitude: Double) {
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

fun openUrl(context: Context, url: String) {
    val fixedUrl = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fixedUrl))
    context.startActivity(intent)
}

fun shareText(context: Context, text: String, title: String = "Compartilhar") {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(intent, title)
    context.startActivity(chooser)
}

fun shareWifi(context: Context, ssid: String, password: String) {
    val text = buildString {
        appendLine("Rede Wi-Fi")
        appendLine("SSID: $ssid")
        if (password.isNotBlank()) appendLine("Senha: $password")
    }
    shareText(context, text, "Compartilhar Wi-Fi")
}
