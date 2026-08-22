package com.systentando.nfc.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

data class CountryCode(
    val code: String,
    val country: String,
    val flag: String,
    val mask: String
)

val DEFAULT_COUNTRY = CountryCode(
    code = "+55",
    country = "Brasil",
    flag = "🇧🇷",
    mask = "(##) #####-####"
)

val COUNTRY_CODES = listOf(
    DEFAULT_COUNTRY,
    CountryCode("+1", "Estados Unidos", "🇺🇸", "(###) ###-####"),
    CountryCode("+44", "Reino Unido", "🇬🇧", "#### ######"),
    CountryCode("+49", "Alemanha", "🇩🇪", "#### #######"),
    CountryCode("+33", "França", "🇫🇷", "# ## ## ## ##"),
    CountryCode("+34", "Espanha", "🇪🇸", "### ### ###"),
    CountryCode("+39", "Itália", "🇮🇹", "### ### ####"),
    CountryCode("+351", "Portugal", "🇵🇹", "### ### ###"),
    CountryCode("+54", "Argentina", "🇦🇷", "## ####-####"),
    CountryCode("+56", "Chile", "🇨🇱", "# #### ####"),
    CountryCode("+52", "México", "🇲🇽", "(###) ### ####"),
    CountryCode("+57", "Colômbia", "🇨🇴", "### ### ####"),
    CountryCode("+81", "Japão", "🇯🇵", "## #### ####"),
    CountryCode("+86", "China", "🇨🇳", "### #### ####"),
    CountryCode("+91", "Índia", "🇮🇳", "##### #####"),
    CountryCode("+7", "Rússia", "🇷🇺", "(###) ###-##-##"),
    CountryCode("+27", "África do Sul", "🇿🇦", "## ### ####"),
    CountryCode("+61", "Austrália", "🇦🇺", "#### ### ###"),
    CountryCode("+1", "Canadá", "🇨🇦", "(###) ###-####")
)

fun removeNonDigits(value: String): String {
    return value.filter { it.isDigit() }
}

fun applyPhoneMask(digits: String, mask: String): String {
    val clean = digits.filter { it.isDigit() }
    val result = StringBuilder()
    var i = 0
    for (m in mask) {
        if (i >= clean.length) break
        if (m == '#') {
            result.append(clean[i])
            i++
        } else {
            result.append(m)
        }
    }
    return result.toString()
}

class PhoneVisualTransformation(private val mask: String) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }
        val masked = applyPhoneMask(digits, mask)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var count = 0
                var transformed = 0
                for (m in masked) {
                    if (count >= offset) break
                    if (m == '#') {
                        transformed++
                    } else {
                        transformed++
                    }
                    count++
                }
                return transformed
            }

            override fun transformedToOriginal(offset: Int): Int {
                var original = 0
                var t = 0
                for (m in masked) {
                    if (t >= offset) break
                    if (m.isDigit()) {
                        original++
                    }
                    t++
                }
                return original
            }
        }

        return TransformedText(AnnotatedString(masked), offsetMapping)
    }
}
