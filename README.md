# NFC Toolkit

Aplicativo multiplataforma (Android + iOS) para leitura e escrita de tags NFC.

## Estrutura

```
nfc-toolkit/
├── shared/          # KMP - lógica compartilhada, modelos, NDEF parsers/formatadores
├── androidApp/      # App Android com Jetpack Compose, Hilt, Room
└── iosApp/          # App iOS com SwiftUI e CoreNFC
```

## Design

Design system gerado no StitchMCP:
- Projeto: `projects/3227746024196257030`
- Tema: teal/verde, dark mode, glassmorphism
- Telas geradas: Splash, Onboarding, Gravar Tag, Ler Tag, Detalhes da Tag, Gravar Wi-Fi, Configurações, Comandos Avançados

## Funcionalidades principais

- Leitura contínua em foreground (polling a cada 3s)
- Pausa automática quando apps de carteira estão abertos
- Gravação de: texto, URL, e-mail, localização, Wi-Fi, telefone, Google Business
- QR Code para pré-visualização de Wi-Fi
- Tela de detalhes com ações: copiar, compartilhar, deletar
- Tela de comandos avançados (leitura de memória, formatar, bloquear, senha, APDU)

## Build

```bash
# Configurar o ambiente (executar uma vez)
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"

# Android
./gradlew :androidApp:assembleDebug

# iOS (requer Xcode com iPhone SDK)
./gradlew build
```

## Próximos passos

- Testar leitura e gravação em dispositivo físico Android
- Implementar ações avançadas com tag próxima (formatar, bloquear, APDU)
- Implementar persistência de histórico no Room
- Integrar iOS com `shared` framework quando Xcode estiver disponível
