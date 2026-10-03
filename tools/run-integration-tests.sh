#!/usr/bin/env bash
# Compila, reinstala en el servidor de PRUEBAS, ejecuta las suites de integracion y muestra el informe.
# Uso: tools/run-integration-tests.sh [filtro-de-suite]
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(dirname "$HERE")"
DEST="${TEST_SERVER_DIR:-$HOME/dev/test-server}"
JAVA_HOME="${JAVA_HOME:-$HOME/dev/tools/jdk-25.0.4.1}"
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"
export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/dev/.gradle}"

(cd "$REPO/core" && nice -n 10 ./gradlew --no-daemon -q build testmodJar)
"$HERE/server.sh" stop >/dev/null 2>&1 || true
"$HERE/setup-test-server.sh" ${WITH_GRAVES:+--with-graves} >/dev/null
rm -f "$DEST/purgatorio-test-results.txt"
rm -rf "$DEST/test-world"   # mundo nuevo en cada ejecucion: pruebas reproducibles
"$HERE/server.sh" start
sleep 2
CMD="purgatorio_test run"
[ -n "${1:-}" ] && CMD="$CMD $1"
"$HERE/server.sh" rcon "$CMD" >/dev/null
# las pruebas corren como tarea del servidor: esperar al informe (max 5 min)
for _ in $(seq 1 300); do [ -f "$DEST/purgatorio-test-results.txt" ] && break; sleep 1; done
echo "---- informe ----"
cat "$DEST/purgatorio-test-results.txt"
"$HERE/server.sh" stop
grep -q "con fallos" "$DEST/purgatorio-test-results.txt" && ! grep -q "RESUMEN: .* 0 con fallos" "$DEST/purgatorio-test-results.txt" && exit 1 || exit 0
