# Informe del vertical slice (Fase E)

Fecha: 2026-10-03. Rama: `feat/alma-vertical-slice`. Entorno: Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.161.0,
Polymer 0.18.2, Filament 1.8.4. Todo se desarrollo y probo en un servidor de pruebas aislado; el servidor real no se tocó.

## 1. Que funciono (verificado con pruebas automaticas)

Las pruebas corren dentro de un servidor real con jugadores simulados (`ServerPlayer` real, login, respawn y guardado
por el mismo camino que vanilla). 200 comprobaciones, 7 suites, mas 9 pruebas unitarias de las reglas.

| Requisito | Resultado |
|---|---|
| Alma como valor propio 0-100, en la barra de XP | OK. Paquete propio + mixin que sustituye lo que vanilla reenvia (login, respawn, dimension). |
| Las orbes / XP vanilla no dan Alma | OK. `ExperienceOrb.award*`, `giveExperiencePoints/Levels` anulados; recoger una orbe existente no cambia nada. |
| Bonus lineal 0 / 50 / 100 = +0 / +5 / +10 % | OK (tambien 25 y 75). Techo +10 % verificado con entradas absurdas. |
| Bonus a dano del jugador | OK en cuerpo a cuerpo y proyectiles (`getEntity()` = causante). No se aplica a mobs ni a dano ambiental. |
| Ganar Alma con acciones controladas | OK: descubrir un lugar (una vez), matar al Acechador (3 de Alma). |
| Morir = perder ~30 % | OK: 100→70→49→34,3 y 50→35. No depende del XP vanilla. Respawn por el paquete real del cliente. |
| Persistencia | OK: salir y entrar conserva Alma, items mejorados y logros. |
| Alma individual | OK: dos jugadores, cada uno con su barra, su bonus y su recompensa. |
| Tope 100 | OK en `set`, `add`, comando y reglas. |
| Gancho para Graves | `AlmaEvents.LOST_ON_DEATH` (cantidad perdida), sin oyente todavia. |
| Forja minima | OK: materiales + Alma, coste visible, mejora permanente, atomica (si falla no cambia nada). Gastar baja el bonus. |
| Objeto propio | OK: Colmillo de Ceniza (Polymer), rasgo Brasa con costo de hambre, el nivel viaja en el item. |
| Descubrimiento | OK: logro por ubicacion, recompensa individual y una sola vez, entrada en el Diario. |
| Enemigo | OK: Acechador (Husk con atributos, botin por datapack, aviso de 1 s y teletransporte a la espalda). |
| Menu | OK: menu server-side con Alma, Forja y Diario (sgui); la forja se opera con clics. |

Compatibilidad: con los **154 mods del servidor real** (mas el nuestro) el servidor arranca y las 7 suites pasan.

## 2. Que no funciono o no se pudo comprobar

- **Nada visual.** No hay cliente de Minecraft en la maquina. La barra de XP, el modelo del Colmillo, el aspecto de los
  menus y el aviso del resource pack solo estan verificados a nivel de paquetes y de pack generado (el zip contiene los
  modelos y texturas). Hay una lista de comprobaciones en `docs/pruebas-manuales.md`.
- **Encantar y yunque dejan de funcionar** (no probado en juego, deducido del codigo): el XP real queda en 0. Es la
  opcion segura; hay que decidirlo (pregunta abierta 3 del diseno).
- **Prueba con el pack completo: una ejecucion de tres tuvo fallos esporadicos.** Algun mod de produccion anulo al azar
  golpes de prueba a un zombi (dano 0). Las pruebas ahora reintentan; dos ejecuciones posteriores pasaron al 100 %. No
  se identifico el mod.
- **Down But Not Out cambia la muerte con 2+ jugadores:** un golpe letal deja al jugador *caido*, no muerto, asi que no
  pierde Alma (coincide con el diseno: ser revivido no cuesta Alma). No se probo el caso "se desangra y muere".
- **Filament entidades no se probaron** (la documentacion las describe como experimentales y solo con *goals* vanilla).
- **Rendimiento no medido.** Los ganchos son baratos (un mixin en `hurtServer`, uno en el envio de la barra, seguimiento
  explicito de Acechadores) pero no hay medicion con `spark`.
- **Estructuras de worldgen no probadas.** La "ruina" es una funcion de pruebas con coordenadas fijas.

## 3. Limitaciones de Minecraft / Fabric encontradas

- Fabric API 0.161 **no** tiene evento para modificar el dano: hizo falta un mixin en `LivingEntity.hurtServer`.
- El cambio de la barra de XP solo se puede hacer sustituyendo el paquete; el cliente calcula todo lo demas.
- Las funciones de recompensa de un logro **se encolan si se disparan dentro de un comando**; fuera de comandos
  (juego normal) se ejecutan al instante. Afecto al arnes de pruebas, no al juego.
- Un jugador es invulnerable hasta que el cliente confirma la carga tras entrar, reaparecer o cambiar de dimension
  (relevante para pruebas, no para jugadores reales).
- Filament sigue las reglas del cliente vanilla: el item "base" (`vanillaItem`) condiciona la interaccion.
- El `@Redirect` sobre el envio de la barra es exclusivo: otro mod que redirija el mismo punto haria fallar el arranque
  (con los 154 mods actuales no ocurre).

## 4. Que hubo que resolver con Java

Estado y servicio de Alma, barra de XP, anulacion de XP vanilla, bonus de dano, perdida por muerte, rasgo del Colmillo,
forja (validacion y cobro atomico), comportamiento del Acechador, menu (sgui), comandos, y la recarga de recetas.

## 5. Que se resolvio con datapack

Diario y descubrimiento (logros con predicado de ubicacion), recompensa (funcion), tabla de botin del Acechador, recetas
de forja (JSON propio), y la Esquirla de Brasa (item de Filament).

## 6. Polymer y Filament en 26.3 (lo que funciona de verdad)

- **Polymer:** item propio con `SimplePolymerItem`; un cliente vanilla recibe una espada de hierro con `item_model`
  `purgatorio:colmillo_de_ceniza`; el resource pack generado contiene sus modelos y texturas.
- **Filament:** item definido solo con JSON de datapack; queda registrado, genera su propio modelo en el pack, se puede
  usar en tablas de botin y como material de forja. Los assets de la textura los aporta el mod.
- **sgui:** menus de cofre con clics, via la copia que ya llevan Filament y Graves.
- **Fabric Data Attachments:** persistencia por jugador, con `copyOnDeath`.

## 7. Que seria dificil de implementar

- Redisenar **encantamientos/yunque** (afecta a Enchants Plus y More Enchants).
- **Alma derramada en la tumba:** requiere leer la API de Universal Graves y depositar un objeto/valor en su tumba.
- **Fatiga anti-granja y filtro de origen** (spawners, granjas): hay que marcar el origen de cada mob.
- **Escalado de mobs por region** y enemigos con comportamiento profundo: Filament solo da *goals* vanilla; lo demas es Java.
- **Lugares memorables:** estructuras de worldgen con NBT propio y construccion a mano.
- **HUD/animaciones:** un servidor vanilla solo puede usar chat, barra de acciones, titulos, bossbar y menus de cofre.

## 8. Arquitectura propuesta para continuar

La del documento `docs/arquitectura.md`, que se mantuvo: reglas puras testeables (`AlmaRules`) → servicio → adaptadores
de Minecraft; paquetes `alma`, `forge`, `item`, `enemy`, `menu`, `command`, `mixin`; contenido en datapack dentro del jar.
Siguientes modulos: `alma.sources` (fatiga, origen), `alma.grave` (integracion Graves), `region` (peligro por zona),
configuracion en archivo para las constantes (+10 %, 30 %, costes). Cada uno con su suite en el mod de pruebas.

## 9. Cambios necesarios antes de llevarlo al servidor real

1. **Decidir encantamientos y yunque** (ver 2). Hoy quedarian inutilizados.
2. **Jugadores existentes:** su XP actual pasa a estar oculto y su Alma empieza en 0. Decidir si se migra algo.
3. **Resource pack:** al anadir assets cambia el hash. Hay que **regenerar el pack en el servidor real** (con todos los
   mods), subir el nuevo `polymer/resources+<hash>.zip` a `main` y esperar a que GitHub Pages lo publique **antes** de
   reiniciar. Si no, los jugadores (pack obligatorio) no podran entrar. No se probo este flujo.
4. Instalar solo `purgatorio-core-<v>.jar` en `mods/` (**no** el `...-testmod`). No hacen falta mods nuevos.
5. Quitar del datapack la funcion de pruebas `pruebas/construir_ruina` y sustituir "La Ruina de Ceniza" por un lugar real.
6. Ejecutar la lista de `docs/pruebas-manuales.md` con un cliente real en el servidor de pruebas.
7. Medir rendimiento con `spark` con varios jugadores.
8. Revisar si quieren que el bonus de dano aplique tambien en PvP y a explosiones causadas por jugadores (hoy si).
9. Probar el caso "caido que se desangra y muere" con Down But Not Out.
10. Hacer backup del servidor y de `world/` antes del primer arranque con el mod.

## 10. Que quedo versionado

Repositorio `punkyyy01/purgatorio-pack`, rama **`feat/alma-vertical-slice`** (no se toco `main`, porque GitHub Pages
sirve `main` y el servidor real descarga de ahi el resource pack). Commits, en orden:

| Commit | Contenido |
|---|---|
| `eaa8d0a` | Estructura del repo, README, `.gitignore`, diseno v2 |
| `e681807` | Proyecto Gradle Fabric 26.3 solo-servidor |
| `d1e20b2` | Inspeccion tecnica (Fase A) y arquitectura (Fase B) |
| `7c357de` | Reglas puras de Alma + 9 pruebas JUnit |
| `eb2825a` | Servicio de Alma, persistencia, barra de XP, mixins de XP y dano |
| `e367ec2` | Mod de pruebas + suite de Alma + scripts del entorno |
| `1ae100c` | Forja, Colmillo de Ceniza (Polymer), Esquirla (Filament), datapack |
| `c145edb` | Acechador de Ceniza |
| `64cc8bc` | Menu, Diario, descubrimiento y comandos |
| `964b285` | Suites de integracion (forja, item, descubrimiento, enemigo, menu, comandos) |
| `02ed102` | Prueba de cambio de dimension |
| `daa3947` | Modo de compatibilidad con el pack completo, pruebas tolerantes |
| (siguiente) | Documentacion final: este informe y comprobaciones manuales |
