#!/bin/bash
set -e

# Configuração do ambiente
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
export PATH="$ANDROID_HOME/platform-tools:$JAVA_HOME/bin:$PATH"

echo "Limpando build anterior..."
./gradlew clean

echo ""
echo "Compilando APK de debug..."
./gradlew :androidApp:assembleDebug

echo ""
echo "Build concluído com sucesso!"
echo "APK disponível em:"
echo "  androidApp/build/outputs/apk/debug/androidApp-debug.apk"

echo ""
echo "Para instalar no dispositivo conectado, execute:"
echo "  ./install_apk.sh"
