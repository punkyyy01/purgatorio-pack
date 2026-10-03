#!/usr/bin/env bash
# Reconstruye el resource pack de Polymer a partir del repo (assets de core/ + datapack Filament).
# Salida: build/resource-pack/resources+<sha1>.zip (ignorado por git; es un artefacto regenerable).
#   tools/build-resource-pack.sh              -> solo Polymer + Filament + purgatorio_core
#   FULL_PACK=1 tools/build-resource-pack.sh  -> con TODOS los mods del servidor real (el pack de produccion)
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(dirname "$HERE")"
DEST="${TEST_SERVER_DIR:-$HOME/dev/test-server}"
JAVA_HOME="${JAVA_HOME:-$HOME/dev/tools/jdk-25.0.4.1}"; export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH" GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/dev/.gradle}"

(cd "$REPO/core" && ./gradlew --no-daemon -q build)
"$HERE/server.sh" stop >/dev/null 2>&1 || true
"$HERE/setup-test-server.sh" ${FULL_PACK:+--full-pack} >/dev/null
rm -f "$DEST"/mods/purgatorio-core-testmod-*.jar "$DEST/polymer/resource_pack.zip"
rm -rf "$DEST/test-world"
"$HERE/server.sh" start
"$HERE/server.sh" rcon "polymer generate-pack" >/dev/null
for _ in $(seq 1 120); do [ -f "$DEST/polymer/resource_pack.zip" ] && break; sleep 1; done
"$HERE/server.sh" stop
sleep 1
SHA="$(sha1sum "$DEST/polymer/resource_pack.zip" | cut -d' ' -f1)"
mkdir -p "$REPO/build/resource-pack"
cp "$DEST/polymer/resource_pack.zip" "$REPO/build/resource-pack/resources+$SHA.zip"
echo "Pack generado: build/resource-pack/resources+$SHA.zip"
