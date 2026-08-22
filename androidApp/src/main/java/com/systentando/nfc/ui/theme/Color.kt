package com.systentando.nfc.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================
// NFC Toolkit — Design System Tokens
// ============================================
// Não use essas cores diretamente nas telas.
// Use MaterialTheme.colorScheme ou os tokens semânticos abaixo.
// ============================================

// Brand — teal/ciano como cor primária, sem verde saturado
val BrandTeal = Color(0xFF14B8A6)
val BrandTealLight = Color(0xFF2DD4BF)
val BrandTealDark = Color(0xFF0D9488)
val BrandCyan = Color(0xFF06B6D4)

// Surfaces — fundo escuro profundo #061616, camadas acima progressivamente mais claras
val BackgroundBase = Color(0xFF061616)
val Surface01 = Color(0xFF0C1C1C)
val Surface02 = Color(0xFF122A2A)
val Surface03 = Color(0xFF183635)

// Text
val TextPrimary = Color(0xFFF0FAFA)
val TextSecondary = Color(0xFF9EC8C4)
val TextTertiary = Color(0xFF6C9692)
val TextDisabled = Color(0xFF466B68)

// Status
val Success = Color(0xFF34D399)
val Warning = Color(0xFFFBBF24)
val Error = Color(0xFFF87171)
val Info = Color(0xFF38BDF8)

// Gradientes — use via Brush em componentes genéricos
val GradientStart = BrandTeal
val GradientEnd = BrandCyan

// Mapeamento legacy para não quebrar imports (deprecated, remover gradualmente)
@Deprecated("Use MaterialTheme.colorScheme.primary", ReplaceWith("MaterialTheme.colorScheme.primary"))
val PrimaryTeal = BrandTeal

@Deprecated("Use MaterialTheme.colorScheme.background", ReplaceWith("MaterialTheme.colorScheme.background"))
val BackgroundDark = BackgroundBase

@Deprecated("Use MaterialTheme.colorScheme.surface", ReplaceWith("MaterialTheme.colorScheme.surface"))
val SurfaceDark = Surface01

@Deprecated("Use MaterialTheme.colorScheme.surfaceVariant", ReplaceWith("MaterialTheme.colorScheme.surfaceVariant"))
val SurfaceVariant = Surface02

@Deprecated("Use MaterialTheme.colorScheme.onSurface", ReplaceWith("MaterialTheme.colorScheme.onSurface"))
val OnSurface = TextPrimary

@Deprecated("Use MaterialTheme.colorScheme.onSurfaceVariant", ReplaceWith("MaterialTheme.colorScheme.onSurfaceVariant"))
val OnSurfaceMuted = TextSecondary

@Deprecated("Use MaterialTheme.colorScheme.error", ReplaceWith("MaterialTheme.colorScheme.error"))
val ErrorRed = Error

@Deprecated("Use MaterialTheme.colorScheme.secondary", ReplaceWith("MaterialTheme.colorScheme.secondary"))
val SuccessGreen = Success
