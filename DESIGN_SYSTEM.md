# NFC Toolkit — Design System

## Visão geral

O app segue um **Design System baseado no Material 3**, com um esquema de cores escuro (dark) por padrão e suporte a tema claro (light). Nenhuma tela deve importar cores diretamente do `Color.kt`; todas as cores vêm de `MaterialTheme.colorScheme`.

## Cores

### Fundo
- `background`: `#061616` (dark) / `#F8FAFA` (light)
- `surface`: `#0C1C1C` (dark) / `#FFFFFF` (light)
- `surfaceVariant`: `#122A2A` (dark) / `#E3EBEA` (light)

### Texto
- `onSurface`: `#F0FAFA` (dark) / `#061616` (light)
- `onSurfaceVariant`: `#9EC8C4` (dark) / `#466B68` (light)
- `onPrimary`: `#F0FAFA` (texto sobre botões primários)

### Brand
- `primary`: Teal `#14B8A6` (dark) / `#0D9488` (light)
- `secondary`: Ciano `#06B6D4`
- `tertiary`: Teal claro `#2DD4BF`

### Status
- `error`: `#F87171`
- `secondary` (success): `#34D399`
- `tertiary` (warning): `#FBBF24`

## Como usar

Sempre prefira `MaterialTheme.colorScheme`:

```kotlin
val colors = MaterialTheme.colorScheme
Text("Título", color = colors.onSurface)
Card(colors = CardDefaults.cardColors(containerColor = colors.surface))
```

Use os tokens semânticos em `Color.kt` (`BrandTeal`, `BackgroundBase`, `TextPrimary`, etc.) **apenas** na definição do tema. Telas e componentes não devem usá-los.

## Componentes compartilhados

Arquivo: `androidApp/src/main/java/com/systentando/nfc/ui/components/NfcComponents.kt`

- `NfcTopAppBar`
- `PrimaryGradientButton`
- `WaitingNfcMessage`
- `StatusMessage`
- `NfcCard`
- `OutlinedTealButton`
- `NfcLabelValue`
- `SectionTitle`

Esses componentes já usam `MaterialTheme.colorScheme` e refletem automaticamente Dark/Light.

## Tema

O `NFCToolkitTheme` cria um `MaterialTheme` com `DarkColorScheme` ou `LightColorScheme` baseado em `isSystemInDarkTheme()`. Atualmente o app é sempre dark, mas a estrutura já suporta light.
