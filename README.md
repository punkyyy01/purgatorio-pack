# purgatorio-pack

Modpack **server-side** de Purgatorio (Minecraft 26.3, Fabric). Los jugadores entran con un cliente vanilla; todo el contenido lo pone el servidor (Polymer + resource pack automatico).

Este repositorio es la **fuente de verdad** del codigo, datos y configuracion propios del pack. El servidor solo ejecuta una copia.

> **Atencion — hosting del resource pack de produccion.** GitHub Pages sirve esta rama `main` (raiz) y el servidor real descarga de aqui el resource pack de Polymer (`polymer/resources+<sha1>.zip`, ver `config/polymer/auto-host.json` del servidor). **No muevas ni borres `polymer/` en `main`** sin coordinar el cambio con el servidor.

## Estructura

| Ruta | Contenido |
|---|---|
| `core/` | Mod Fabric propio `purgatorio_core` (Java, Gradle). Alma, forja, menu, comportamientos. |
| `datapack/purgatorio/` | Contenido data-driven: logros (Diario), funciones, loot, recetas de forja, items de Filament. Se empaqueta dentro del jar de `core`. |
| `docs/` | Diseno (`diseno-fase1.md`, fuente de verdad), inspeccion tecnica y arquitectura. |
| `tools/` | Scripts de entorno de pruebas y utilidades. |
| `polymer/` | Resource pack de produccion servido por GitHub Pages (no tocar). |

## Compilar

Requiere JDK 25.

```bash
cd core
./gradlew build      # genera core/build/libs/purgatorio-core-*.jar
./gradlew test       # pruebas unitarias
```

## Entorno de pruebas

Nunca se prueba en el servidor real. Ver `tools/README.md`.

## Filosofia de diseno

Ver `docs/diseno-fase1.md`. Resumen: RPG de aventura, progresion sin puertas, el equipo es la principal fuente de poder, y **Alma** (0-100, max +10% de dano) es la experiencia del jugador.
