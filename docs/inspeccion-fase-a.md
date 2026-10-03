# Inspeccion tecnica (Fase A)

Fecha: 2026-10-03. Todo lo de abajo fue **verificado leyendo jars, configuracion o el codigo decompilado**, salvo lo marcado como *pendiente de probar*.

## Versiones del entorno

| Componente | Version | Como se verifico |
|---|---|---|
| Minecraft | 26.3 (sin ofuscar) | `vanilla-26.3.jar`, `loom` |
| Fabric Loader | 0.19.5 | log de arranque |
| Fabric API | 0.161.0+26.3 | jar en `mods/` |
| Polymer (bundled) | 0.18.2+26.3 | jar; modulos core, resource-pack, virtual-entity, autohost, blocks |
| Filament | 1.8.4+26.3 | jar; trae su documentacion en `docs/` |
| sgui | 2.2.0+26.3 | ya empaquetado dentro de Filament, Graves, Server Backpacks |
| Universal Graves | 3.13.0+26.3 | jar |
| Down But Not Out | 0.5.2+26.3 | jar |
| Java | 25.0.4.1 | el servidor real corre en contenedor; en el host no hay Java |
| Gradle / Loom | 9.7.1 / 1.18.2 | plantilla oficial `fabric-example-mod` rama `26.3` |

## Lo que ya existe de Purgatorio

- **`purgatorio_tuning-1.0.0.jar`**: mod "vacio" que solo empaqueta un datapack (frecuencia de estructuras y jefes, correcciones de formato 26.3). No tiene codigo Java. No se toca.
- **`datapacks/` de los mundos**: vacios.
- **Repo `purgatorio-pack`** (publico): solo contenia `README.md`, `.nojekyll` y `polymer/resources+<sha1>.zip`.
- **El servidor real usa el repo como host del resource pack**: `config/polymer/auto-host.json` apunta a `https://punkyyy01.github.io/purgatorio-pack` (`polymer:external`, `required: true`). GitHub Pages sirve `main` en la raiz. **Mover o borrar `polymer/` rompe la entrada de los jugadores.**

## Restricciones del entorno

- El repo es **publico**: todo lo que se sube es visible. No se versionan secretos, mundos ni datos de jugadores (ver `.gitignore`).
- El servidor real usa ~7 GB de RAM de una maquina de 62 GB con poco margen libre y ya muestra avisos "Can't keep up". El entorno de pruebas debe ser pequeno (heap 2 GB), con `nice`, escuchando solo en `127.0.0.1`.
- No hay clientes de Minecraft reales disponibles en la maquina. Lo que dependa de **renderizado del cliente** (barra de XP, modelos, GUIs) solo se puede verificar a nivel de paquetes/servidor y debe confirmarse a mano con un cliente.

## API de Minecraft 26.3 relevante (leida del codigo)

- `ServerPlayer.tick` envia la barra de XP con `ClientboundSetExperiencePacket(progress, totalExperience, level)` cuando `totalExperience != lastSentExp`.
- Todo el XP orbital pasa por `ExperienceOrb.award(...)` / `awardWithDirection(...)` (mobs, minado, hornos, botellas, piedra de afilar, dragon). El XP directo pasa por `Player.giveExperiencePoints` / `giveExperienceLevels`.
- Encantar y yunque **leen y gastan** `experienceLevel` (`onEnchantmentPerformed`, `giveExperienceLevels(-n)`).
- `LivingEntity.hurtServer(ServerLevel, DamageSource, float)` es el punto unico de dano. `DamageSource.getEntity()` es el *causante* (el jugador aunque el dano sea de una flecha).
- Fabric API 0.161 **no** tiene evento para *modificar* el dano (solo `ALLOW_DAMAGE`/`AFTER_DAMAGE`): hace falta un mixin.
- Persistencia por jugador: `AttachmentRegistry.createPersistent(Identifier, Codec)` + `copyOnDeath()`.
- Las API usan `Identifier` (ya no `ResourceLocation`).

## Conflicto XP vanilla vs. Alma (resultado de la inspeccion)

La barra se puede alimentar con datos propios **sin tocar el XP real**: se sustituyen los argumentos del paquete en `ServerPlayer.tick` y se fuerza el reenvio cuando cambia el Alma. Las orbes y `giveExperience*` se anulan. Consecuencia, **a decidir por diseno** (pregunta abierta 3 del documento):

- El XP vanilla real del jugador se queda en 0, asi que **la mesa de encantamiento y el yunque dejan de funcionar** para jugadores no creativos (no se puede pagar niveles que no existen). Es la opcion segura: no regala encantamientos ni drena Alma.
- La alternativa (hacer que los niveles reales sean el Alma) permitiria encantar gratis mientras haya Alma suficiente y obligaria a bloquear el gasto: contradice el diseno, por eso **no se adopta**.

## Que va en datapack y que requiere Java

| Funcion | Donde |
|---|---|
| Alma (estado, barra, perdida, gasto) | **Java** (mixins + servicio) |
| Bonus de dano (melee y distancia) | **Java** (mixin en `hurtServer`) |
| Diario y descubrimientos | **Datapack** (logros con predicado de ubicacion) + comando minimo en Java para la recompensa |
| Recetas de forja | **Datapack** (JSON propio) leido por Java |
| Item con rasgo | **Java/Polymer** (el rasgo reacciona a eventos) |
| Material con uso | **Filament** (JSON) — *pendiente de probar* |
| Enemigo base y botin | **Datapack** (loot table, atributos) |
| Comportamiento especial del enemigo | **Java** (Filament solo soporta goals vanilla) |
| Menu | **Java** (sgui) |
