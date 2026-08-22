package com.systentando.nfc.domain.model

sealed class WritePayload {
    abstract val type: Type

    enum class Type {
        TEXT, URL, EMAIL, LOCATION, WIFI, PHONE, GOOGLE_BUSINESS, RAW
    }

    data class Text(override val type: Type = Type.TEXT, val text: String) : WritePayload()
    data class Url(override val type: Type = Type.URL, val url: String) : WritePayload()
    data class Email(override val type: Type = Type.EMAIL, val address: String, val subject: String = "", val body: String = "") : WritePayload()
    data class Location(override val type: Type = Type.LOCATION, val latitude: Double, val longitude: Double) : WritePayload()
    data class WiFi(
        override val type: Type = Type.WIFI,
        val ssid: String,
        val password: String,
        val security: WiFiSecurityType
    ) : WritePayload()

    data class Phone(
        override val type: Type = Type.PHONE,
        val countryCode: String = "+55",
        val number: String = ""
    ) : WritePayload()
    data class GoogleBusiness(override val type: Type = Type.GOOGLE_BUSINESS, val link: String) : WritePayload()
    data class Raw(override val type: Type = Type.RAW, val mimeType: String, val content: String) : WritePayload()

    enum class WiFiSecurityType {
        OPEN, WEP, WPA_WPA2
    }
}
