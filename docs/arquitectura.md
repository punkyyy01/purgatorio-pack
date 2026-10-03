# Arquitectura del nucleo (Fase B)

Principio: **Alma es un sistema aislado**. Las reglas son funciones puras sin dependencia de Minecraft (testeables con JUnit); lo demas son adaptadores finos.

## Capas

```
reglas puras  ->  servicio  ->  adaptadores de Minecraft
AlmaRules         AlmaService   AlmaStore (persistencia)   AlmaDisplay (barra XP)
(sin MC)                        mixins (XP, dano)          eventos (muerte, join)
```

Dependencias solo hacia la izquierda: `AlmaRules` no conoce a nadie; `AlmaService` usa `AlmaRules` y habla con la persistencia y la pantalla por interfaces pequenas; los mixins y eventos solo llaman al servicio.

## Paquetes (`com.purgatorio.core`)

| Paquete | Responsabilidad |
|---|---|
| `PurgatorioCore` | Entrada. Solo registra modulos, sin logica. |
| `alma` | `AlmaRules` (puro), `AlmaService`, `AlmaStore`, `AlmaDisplay`, `AlmaEvents` (muerte/join/respawn) |
| `alma.sources` | De donde viene el Alma (descubrimientos, kills controlados). Separado para crecer sin tocar el nucleo. |
| `forge` | `ForgeRecipe` (record), `ForgeRecipeLoader` (recarga de datapack), `ForgeService`, `UpgradeData` |
| `item` | Registro de items propios y sus rasgos |
| `enemy` | Seguimiento de encuentros y comportamientos |
| `menu` | Menu principal y pantalla de forja (sgui) |
| `command` | `/purgatorio ...` (jugador) y `/purgatorio admin ...` (op) |
| `mixin` | Mixins minimos; delegan en el servicio |

## Alma: modelo

- **Unidad interna:** centesimas de Alma (`int` 0..10000). Evita errores de coma flotante y permite ganancias fraccionarias.
- **API publica (`AlmaService`):** `get`, `set`, `add`, `remove`, `spend`, `loseOnDeath`, `damageMultiplier`.
- **Reglas (`AlmaRules`)**:
  - rango 0..100, siempre saturado;
  - `bonus = Alma * 0,1%` (maximo +10%, nunca mas);
  - muerte: pierde 30% del Alma actual (redondeo hacia abajo de lo que se conserva solo en centesimas);
  - `spend` es atomico: o hay Alma suficiente y se descuenta, o no pasa nada.
- **Persistencia:** `AttachmentRegistry.createPersistent` sobre el jugador, `copyOnDeath`. Se guarda con los datos normales del jugador. Es individual por definicion.
- **Barra:** `AlmaDisplay` envia `ClientboundSetExperiencePacket` (nivel = parte entera, progreso = fraccion; a 100 la barra va llena). Un mixin en `ServerPlayer.tick` sustituye los valores que enviaria vanilla.
- **XP vanilla:** las orbes (`ExperienceOrb.award*`) y `giveExperiencePoints/Levels` se anulan. El XP real se queda en 0.
- **Bonus de dano:** un mixin en `LivingEntity.hurtServer` multiplica el dano cuando el causante (`DamageSource.getEntity()`) es un `ServerPlayer` distinto de la victima. Cubre cuerpo a cuerpo y proyectiles.
- **Gancho para Graves:** `AlmaService.loseOnDeath` devuelve la cantidad perdida (`long` centesimas) para que un modulo futuro la deposite en la tumba. No se implementa la recuperacion todavia.

## Forja (prueba minima)

- Receta data-driven: `data/<ns>/forja/<item>.json` (JSON propio, recargable con `/reload`, verificado).
- `ForgeService.tryUpgrade(player, stack)` valida: item mejorable, nivel siguiente, materiales en inventario, Alma suficiente. Si todo es valido, consume materiales, llama a `AlmaService.spend` y sube el nivel. Si algo falla no cambia nada.
- El nivel de mejora vive **en el propio item** (componente `custom_data`), asi que es permanente y viaja con el objeto.
- Interfaz: pantalla sgui abierta desde el menu o `/purgatorio forja`. (Estacion fisica = trabajo futuro.)

## Descubrimientos (prueba minima)

- Un logro de datapack con predicado de ubicacion marca el lugar. Los logros son **individuales** por jugador y forman el Diario.
- La recompensa del logro ejecuta una funcion que llama a un comando admin de Java para dar Alma. Al ser un logro, solo se concede una vez por jugador.

## Enemigo (prueba minima)

- Un mob vanilla con atributos, equipo y tabla de botin propios (datapack) mas un comportamiento en Java (`enemy`): el **Acechador** se teletransporta a la espalda del jugador tras un aviso visible y sonoro de 1 s (telegraph).
- Seguimiento por etiqueta de entidad; sin escaneos globales por tick.

## Contenido y despliegue

- El datapack vive en `datapack/purgatorio/` y Gradle lo incluye dentro del jar (`processResources`). Un solo artefacto: `purgatorio-core-<version>.jar`.
- Assets propios: `core/src/main/resources/assets/purgatorio_core/` (Polymer los registra con `addModAssets`).
- **El resource pack de produccion cambia de hash** al anadir assets. Para llevarlo al servidor real hay que publicar el nuevo zip en `polymer/` de `main` *antes* de reiniciar el servidor (ver informe).

## Entorno de pruebas

- Directorio fuera del repo (`/home/umbrel/dev/test-server`), `127.0.0.1` solamente, puerto propio, heap 2 GB, `nice`.
- Mundo nuevo (nunca una copia del mundo real).
- Se reconstruye con `tools/` desde el repo.

## Estado de implementacion (vertical slice)

Implementado y probado: `alma` (reglas, servicio, persistencia, barra, mixins de XP y dano, perdida por muerte),
`forge` (recetas, servicio, GUI), `item` (Colmillo de Ceniza), `enemy` (Acechador), `menu`, `command`, datapack
(Diario, descubrimiento, botin, forja, esquirla en Filament). Sin implementar: Alma derramada en la tumba, fatiga
anti-granja, escalado por region, estacion fisica de forja, constantes en archivo de configuracion.

## Pruebas

| Nivel | Herramienta | Cubre |
|---|---|---|
| Reglas | JUnit | rangos, tope 100, bonus <= 10%, perdida 30%, gasto |
| Integracion en el servidor | Mod de pruebas `purgatorio_core_testmod` (comando `/purgatorio_test`) con jugadores simulados reales (ServerPlayer + canal de red simulado) | 200 comprobaciones en 7 suites: Alma, forja, item, descubrimiento, enemigo, menu, comandos |
| Compatibilidad | Mismas suites con los 154 mods del servidor real (`FULL_PACK=1`) | el mod arranca y funciona junto al resto de mods |
| Manual (pendiente) | Cliente real, ver `docs/pruebas-manuales.md` | barra de XP, modelos, GUIs, resource pack |
