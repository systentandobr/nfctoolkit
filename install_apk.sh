#!/bin/bash
set -e

# Configuração do ambiente
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
export PATH="$ANDROID_HOME/platform-tools:$JAVA_HOME/bin:$PATH"

APK_PATH="androidApp/build/outputs/apk/debug/androidApp-debug.apk"

echo "Verificando dispositivos conectados..."
adb devices

echo ""
echo "Instalando APK..."
adb install -r "$APK_PATH"

echo ""
echo "Abrindo o app..."
adb shell am start -n com.systentando.nfc/.MainActivity

echo ""
echo "Para ver os logs, execute:"
echo "  adb logcat -s NFCManager:D NFCTagReader:D NFCTagWriter:D NFCViewModel:D"
