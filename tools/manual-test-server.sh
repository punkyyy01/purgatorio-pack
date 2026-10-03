#!/usr/bin/env bash
# Servidor de pruebas para un cliente REAL de Minecraft 26.3. Nunca es el servidor de produccion.
#   - escucha SOLO en la IP de Tailscale (por defecto 100.97.25.10); online-mode activado (cuentas reales)
#   - sin lista blanca: por eso solo es accesible desde tu tailnet
#   - el resource pack se sirve por el mismo puerto (Polymer autohost) y es obligatorio
#   - se apaga solo a las 8 h
# Uso: tools/manual-test-server.sh start | stop | status | log | rcon "<cmd>" | op <usuario>
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(dirname "$HERE")"
export TEST_SERVER_DIR="${MANUAL_SERVER_DIR:-$HOME/dev/manual-test-server}"
BIND_IP="${BIND_IP:-100.97.25.10}"
case "${1:-}" in
	start)
		ip -o addr show | grep -q " $BIND_IP/" || { echo "La IP $BIND_IP no existe en esta maquina (Tailscale caido?). Abortado." >&2; exit 1; }
		JAVA_HOME="${JAVA_HOME:-$HOME/dev/tools/jdk-25.0.4.1}"; export JAVA_HOME PATH="$JAVA_HOME/bin:$PATH" GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/dev/.gradle}"
		(cd "$REPO/core" && ./gradlew --no-daemon -q build)
		"$HERE/server.sh" stop >/dev/null 2>&1 || true
		mkdir -p "$TEST_SERVER_DIR"
		if [ ! -f "$TEST_SERVER_DIR/server.properties" ]; then
			PASS="$(head -c 18 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c 20)"
			sed "s/__BIND_IP__/$BIND_IP/; s/__RCON_PASSWORD__/$PASS/" "$HERE/manual-test-server.properties.template" > "$TEST_SERVER_DIR/server.properties"
		fi
		PROPS_TEMPLATE=/dev/null NO_TESTMOD=1 AUTOHOST_ENABLED=1 "$HERE/setup-test-server.sh" ${WITH_GRAVES:+--with-graves} >/dev/null
		"$HERE/server.sh" start
		( sleep 28800; "$0" stop >/dev/null 2>&1 ) >/dev/null 2>&1 &
		echo "Servidor listo: $BIND_IP:25690 (Minecraft 26.3). Se apagara solo en 8 h."
		ss -tln | grep -E ":2569[01] " ;;
	stop|status|log) "$HERE/server.sh" "$@" ;;
	rcon) shift; "$HERE/server.sh" rcon "$@" ;;
	op) "$HERE/server.sh" rcon "op ${2:?uso: op <usuario>}" ;;
	*) echo "uso: $0 start|stop|status|log|rcon|op" >&2; exit 2 ;;
esac
