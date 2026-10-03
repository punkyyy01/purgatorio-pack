# tools/

Utilidades para desarrollar y probar **sin tocar el servidor real**.

| Script | Para que |
|---|---|
| `setup-test-server.sh [--with-graves]` | Crea/actualiza el servidor de pruebas en `~/dev/test-server` (solo `127.0.0.1:25690`, mundo nuevo, heap 2 GB). Copia de solo lectura los mods de terceros necesarios desde el servidor real y el jar de `core/build/libs`. |
| `server.sh start|stop|status|log|rcon` | Arrancar/parar el servidor de pruebas y enviarle comandos por RCON. |
| `rcon.py` | Cliente RCON minimo (stdlib). La contrasena se genera al azar en el `server.properties` local y no se versiona. |

Flujo tipico:

```bash
export JAVA_HOME=~/dev/tools/jdk-25.0.4.1 PATH=$JAVA_HOME/bin:$PATH
(cd core && ./gradlew build)
tools/setup-test-server.sh
tools/server.sh start
tools/server.sh rcon "list"
tools/server.sh stop
```

El servidor de pruebas esta en modo offline pero **solo escucha en localhost**.
