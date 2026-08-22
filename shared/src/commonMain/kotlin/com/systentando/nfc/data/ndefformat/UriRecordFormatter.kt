package com.systentando.nfc.data.ndefformat

import com.systentando.nfc.domain.model.NDEFRecord
import com.systentando.nfc.domain.model.WritePayload

class UriRecordFormatter : NDEFRecordFormatter {
    override fun supports(payload: WritePayload): Boolean {
        return payload is WritePayload.Url ||
            payload is WritePayload.Email ||
            payload is WritePayload.Location ||
            payload is WritePayload.Phone ||
            payload is WritePayload.GoogleBusiness
    }

    override fun format(payload: WritePayload): NDEFRecord {
        val uri = when (payload) {
            is WritePayload.Url -> payload.url
            is WritePayload.Email -> buildEmailUri(payload)
            is WritePayload.Location -> buildGeoUri(payload)
            is WritePayload.Phone -> "tel:${payload.countryCode}${payload.number.replace("[^0-9]".toRegex(), "")}"
            is WritePayload.GoogleBusiness -> payload.link
            else -> throw IllegalArgumentException("Unsupported URI payload")
        }
        return NDEFRecord.Uri(id = "", uri = uri)
    }

    private fun buildEmailUri(payload: WritePayload.Email): String {
        val subject = if (payload.subject.isNotBlank()) "?subject=${encode(payload.subject)}" else ""
        val body = if (payload.body.isNotBlank()) {
            val prefix = if (subject.isNotBlank()) "&" else "?"
            "$prefix body=${encode(payload.body)}"
        } else ""
        return "mailto:${payload.address}$subject$body"
    }

    private fun buildGeoUri(payload: WritePayload.Location): String {
        return "geo:${payload.latitude},${payload.longitude}"
    }

    private fun encode(value: String): String {
        return value.replace(" ", "%20").replace("?", "%3F").replace("&", "%26")
    }
}
