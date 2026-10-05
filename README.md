# Purgatorio

Modpack **de servidor** para Minecraft 26.3 (Fabric). Es un RPG de aventura centrado en explorar, descubrir lugares y mejorar el equipo.

Los jugadores entran con un **cliente de Minecraft normal**, sin instalar nada: el servidor pone todo el contenido y envía el resource pack automáticamente (con [Polymer](https://polymer.pb4.eu/)).

## Qué incluye

**Mod propio: `purgatorio_core`** (en [`core/`](core/))
- **Alma:** reemplaza la experiencia vanilla. Va de 0 a 100 y da hasta +10 % de daño.
- **Forja:** mejora el equipo con recetas propias.
- **Diario:** logros de descubrimiento de lugares.
- Objetos y enemigos propios, como el Colmillo de Ceniza y el Acechador.

**Mod propio: `purgatorio_guia`**: un libro que cada jugador lleva siempre. Sirve para inspeccionar objetos y consultar el catálogo de encantamientos y efectos del servidor.

**Mods de terceros (principales)**

| Categoría | Mods |
|---|---|
| Base | Fabric API, Polymer, Filament, sgui |
| Mundo y estructuras | Terralith, Incendium, Nullscape, Dungeons and Taverns, Repurposed Structures, Structory, DeCubed Dungeons, Roguelike Dungeons, RPG Loot, Ruins and Towers, Deadly Deadly Dungeon, Illager Expansion, Fortress of War, Gigantic Squid |
| Encantamientos | More Enchants, Enchants Plus, Dragonkind Evolved, Advanced Archery |
| Enemigos | Tom's Mobs, Spiders 2.0, Flower Mimics, Naturally Charged Creepers, Giant Spawn, Harder Wardens, More Mobs |
| Calidad de vida | Universal Graves, Down But Not Out, Lootr, sswaystones, Server Backpacks, Better Healthbar, Farmer's Delight, TSA Decorations, More Tools |
| Rendimiento | C2ME, Lithium, ScalableLux, FerriteCore, Krypton, ServerCore, spark |

La lista completa y exacta está en la carpeta `mods/` del servidor; la de arriba reúne los mods que se mencionan en los documentos de [`docs/`](docs/).

## Estructura del repositorio

| Ruta | Contenido |
|---|---|
| `core/` | Código del mod propio (Java, Gradle). |
| `datapack/purgatorio/` | Logros, funciones, botín y recetas de forja. Se empaqueta dentro del jar de `core`. |
| `docs/` | Diseño y documentación técnica. |
| `tools/` | Scripts para probar sin tocar el servidor real. |
| `polymer/` | Resource pack de producción. |

> **No muevas ni borres `polymer/`.** GitHub Pages sirve esta rama `main` y el servidor real descarga de aquí el resource pack. Si cambia, los jugadores no pueden entrar.

## Compilar y probar

Requiere JDK 25.

```bash
cd core && ./gradlew build        # genera core/build/libs/purgatorio-core-*.jar
tools/run-integration-tests.sh    # pruebas en un servidor aislado (ver tools/README.md)
```

## Más información

- Diseño del juego: [`docs/diseno-fase1.md`](docs/diseno-fase1.md)
- Estado actual: [`docs/informe-vertical-slice.md`](docs/informe-vertical-slice.md)
