#!/usr/bin/env bash
# Control del servidor de PRUEBAS. Uso: tools/server.sh start|stop|status|log|rcon "<comando>"
set -euo pipefail
DEST="${TEST_SERVER_DIR:-$HOME/dev/test-server}"
JAVA="${JAVA_BIN:-$HOME/dev/tools/jdk-25.0.4.1/bin/java}"
PIDFILE="$DEST/server.pid"
HERE="$(cd "$(dirname "$0")" && pwd)"

running() { [ -f "$PIDFILE" ] && kill -0 "$(cat "$PIDFILE")" 2>/dev/null; }

case "${1:-}" in
	start)
		running && { echo "ya esta corriendo"; exit 0; }
		cd "$DEST"
		: > logs-run.txt
		nohup nice -n 15 "$JAVA" -Xms512M -Xmx2G -XX:+UseG1GC -jar fabric-server-launch.jar nogui \
			< /dev/null > logs-run.txt 2>&1 &
		echo $! > "$PIDFILE"
		for _ in $(seq 1 180); do
			grep -q 'Done (' logs-run.txt 2>/dev/null && { echo "arrancado"; exit 0; }
			running || { echo "el servidor murio:"; tail -40 logs-run.txt; exit 1; }
			sleep 1
		done
		echo "timeout esperando el arranque"; tail -30 logs-run.txt; exit 1 ;;
	stop)
		running || { echo "no esta corriendo"; exit 0; }
		python3 "$HERE/rcon.py" stop >/dev/null 2>&1 || kill "$(cat "$PIDFILE")"
		for _ in $(seq 1 60); do running || break; sleep 1; done
		running && kill -9 "$(cat "$PIDFILE")"; rm -f "$PIDFILE"; echo "parado" ;;
	status) running && echo "corriendo (pid $(cat "$PIDFILE"))" || echo "parado" ;;
	log) tail -n "${2:-60}" "$DEST/logs-run.txt" ;;
	rcon) shift; python3 "$HERE/rcon.py" "$@" ;;
	*) echo "uso: $0 start|stop|status|log|rcon" >&2; exit 2 ;;
esac
