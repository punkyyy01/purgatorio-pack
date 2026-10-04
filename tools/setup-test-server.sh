#!/usr/bin/env bash
# Crea (o actualiza) el servidor de PRUEBAS aislado. Nunca toca el servidor real.
#   - escucha solo en 127.0.0.1, puerto 25690 (RCON 25691)
#   - mundo nuevo (flat, sin estructuras), online-mode=false
#   - heap 2 GB
# Uso: tools/setup-test-server.sh [--with-graves | --full-pack]
set -euo pipefail

REPO="$(cd "$(dirname "$0")/.." && pwd)"
DEST="${TEST_SERVER_DIR:-$HOME/dev/test-server}"
PROD_MODS="${PROD_MODS:-$HOME/umbrel/app-data/brcly-crafty/data/servers/9a7d7fb1-352c-415e-bf8a-89bdc88c8cd7/mods}"
MC=26.3; LOADER=0.19.5; INSTALLER=1.1.2

mkdir -p "$DEST/mods" "$DEST/config/polymer"
cd "$DEST"

if [ ! -f fabric-server-launch.jar ]; then
	curl -fsSL -o fabric-server-launch.jar \
		"https://meta.fabricmc.net/v2/versions/loader/$MC/$LOADER/$INSTALLER/server/jar"
fi

echo "eula=true" > eula.txt   # ya aceptado por el propietario en el servidor real

if [ ! -f server.properties ]; then
	RCON_PASS="$(head -c 18 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c 20)"
	sed "s/__RCON_PASSWORD__/$RCON_PASS/" "${PROPS_TEMPLATE:-$REPO/tools/test-server.properties.template}" > server.properties
fi

# Mods de terceros necesarios (copiados del servidor real, solo lectura)
rm -f mods/*.jar
for pattern in "fabric-api-*" "polymer-bundled-*" "filament-*"; do
	cp "$PROD_MODS"/$pattern.jar mods/
done
if [ "${1:-}" = "--with-graves" ]; then
	cp "$PROD_MODS"/graves-*.jar "$PROD_MODS"/down-but-not-out-*.jar mods/
fi
# Prueba de compatibilidad: TODOS los mods del servidor real (copia de solo lectura)
if [ "${1:-}" = "--full-pack" ]; then
	cp "$PROD_MODS"/*.jar mods/
fi

# Mods extra opcionales (p. ej. un datapack-mod de poda en pruebas): EXTRA_MODS=/ruta/con/jars
[ -n "${EXTRA_MODS:-}" ] && cp "$EXTRA_MODS"/*.jar mods/

# Polymer: sin autohost en pruebas automaticas (el pack se genera en polymer/resource_pack.zip).
# AUTOHOST_ENABLED=1 (pruebas con cliente real): el pack se sirve por el mismo puerto y es obligatorio, como en produccion.
if [ "${AUTOHOST_ENABLED:-}" = "1" ]; then
	cat > config/polymer/auto-host.json <<'J'
{ "enabled": true, "type": "polymer:automatic", "required": true }
J
else
	cat > config/polymer/auto-host.json <<'J'
{ "enabled": false }
J
fi

# Nuestro mod + mod de pruebas (este ultimo SOLO existe en el servidor de pruebas)
cp "$REPO"/core/build/libs/purgatorio-core-*[0-9].jar mods/
[ -f "$REPO/build/i18n/zz_purgatorio_es-1.1.0.jar" ] && cp "$REPO"/build/i18n/zz_purgatorio_es-1.1.0.jar mods/   # espanol (tools/build-i18n.py)
[ "${NO_TESTMOD:-}" = "1" ] || cp "$REPO"/core/build/libs/purgatorio-core-testmod-*.jar mods/
echo "Servidor de pruebas listo en $DEST"
ls mods
