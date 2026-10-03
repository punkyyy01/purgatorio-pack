# P3 · Propuesta de poda (para revisar; **no se ha desactivado nada**)

Criterio: la poda se decide **por función, no por cantidad**. Un lugar o encantamiento se queda si cumple al menos una:
(1) tiene identidad propia (jefe, mecánica o narrativa reconocibles), (2) alimenta el bucle de Purgatorio (materiales o equipo que importan),
(3) no se solapa con otro que ya lo hace mejor. Se propone quitar lo que es **la misma idea con otra piel**, lo que solo rellena el mapa y
lo que choca con el diseño. Lo no visto en juego se marca "Revisar jugando": se decide después de verlo, no antes.

## Mecanismo (probado con los 154 mods reales)

Un datapack-mod aparte (`zz_purgatorio_poda`, **reversible**: quitar el jar lo deshace) que sustituye por conjuntos vacíos los `structure_set` elegidos.
Experimento hecho el 2026-10-03 en el servidor de pruebas con los mods reales:

| Prueba | Resultado |
|---|---|
| Mod con id `purgatorio_poda_exp` (ordena *antes* que `repurposed_structures` y `terralith`) | Solo vació el conjunto de Dungeons and Taverns; los otros dos **no** (sus datapacks ganaban) |
| Mod con id `zz_purgatorio_poda` (ordena el último) | Vació los tres conjuntos de prueba; los demás quedaron intactos |

Lección: los datapacks de mods se aplican **en orden alfabético por id de mod**; el de poda debe llamarse de forma que quede el último. Para
quitar encantamientos de las tablas (`in_enchanting_table`, `tradeable`, botín) la API de tags de Fabric admite la clave `remove` en los archivos de etiqueta
(confirmado en el código; **pendiente de probar en juego** al implementar).

## Resumen de estructuras (conjuntos de generación)

| | Conjuntos | Estructuras (variantes) |
|---|---|---|
| Se queda (identidad) | 29 | 66 |
| Neutral (se deja) | 4 | 18 |
| Revisar jugando | 23 | 50 |
| Candidato a desactivar | 65 | 253 |
| **Total** | 121 | 387 |

Si se aplica la fase 1 (solo los "candidatos", ver abajo) el mundo pasa de **387 a 134 variantes** de lugares (66 con identidad + 50 por revisar + 18 neutrales),
más La Ruina de Ceniza y los rincones que vayamos construyendo.

## Estructuras por mod

### Repurposed Structures
Casi todo son **versiones de estructuras vanilla con otra piel** (aldeas, avanzadillas, pirámides, mansiones, templos, ruinas, barcos...) repetidas por bioma y dimensión. Aportan cantidad sin identidad ni función. Se dejan solo las variantes de minas (reemplazan a las vanilla, no son lugares nuevos).

- **Neutral (se deja):** `mineshafts_end` (1), `mineshafts_nether` (5), `mineshafts_ocean` (1), `mineshafts_overworld` (11)
- **Revisar jugando:** `ancient_cities_end` (1), `ancient_cities_nether` (1), `ancient_cities_overworld` (1)
- **Candidato a desactivar:** `abandoned_camp_nether` (5), `bastions_overworld` (1), `cities_nether` (1), `cities_overworld` (1), `fortresses_overworld` (1), `igloos_overworld` (4), `mansions_dappled` (1), `mansions_mangrove` (1), `mansions_overworld` (7), `monuments_nether` (1), `monuments_overworld` (3), `outposts_end` (1), `outposts_nether` (5), `outposts_ocean` (1), `outposts_overworld` (12), `pyramids_end` (1), `pyramids_mushroom` (1), `pyramids_nether` (1), `pyramids_overworld` (8), `ruined_portals_end` (1), `ruins_nether` (1), `ruins_overworld` (4), `shipwrecks_end` (1), `shipwrecks_nether` (3), `strongholds_end` (1), `strongholds_nether` (1), `temples_nether` (5), `temples_overworld` (2), `villages_dappled` (1), `villages_mushroom` (1), `villages_nether` (2), `villages_overworld` (11), `villages_pale_garden` (1), `witch_huts_overworld` (7)

### Dungeons and Taverns
Dungeons and Taverns: 22 conjuntos de **pequeñas casas, pozos, tabernas, campamentos y mazmorras casi idénticas** (por ejemplo 27 variantes de "remnants", 13 pozos, 13 tabernas). Es relleno que compite por atención. Se quedan criptas y santuarios (con ambiente y botín); varios solapan con Illager Expansion o Nullscape y se revisan.

- **Se queda (identidad):** `crypts` (3), `shrine_tower` (1), `shrines` (11)
- **Revisar jugando:** `end_castle` (1), `end_lighthouses` (1), `end_ship` (1), `illager_hideout` (1), `illager_manor` (1), `pale_residence` (1), `ruin_town` (1), `stray_big_structures` (3), `swamp_structure` (2), `trident_trial_monument` (1)
- **Candidato a desactivar:** `badlands_miner_outpost` (1), `badlands_miner_outpost_mineshafts` (1), `bunker` (1), `common_remnants` (2), `conduit_ruin` (2), `desert_structures` (1), `firewatch_towers` (11), `illager_barracks` (3), `illager_camps` (2), `jungle_ruins` (1), `mangrove_witch_hut` (1), `nether_encampments` (10), `nether_structures` (6), `ocean_structures` (1), `remnants` (27), `small_cave_structures` (8), `small_overworld_dungeons` (13), `strongholds_bunker` (1), `taverns` (13), `villages` (5), `wells` (13), `wild_ruin` (1)

### Terralith
Los **biomas** son el escenario y se quedan todos. De sus estructuras: se quedan torres de mago, la aguja y las aldeas fortificadas; los escombros y refugios genéricos son candidatos.

- **Se queda (identidad):** `mage` (6), `rare_dungeon` (1), `rare_village` (2), `underground_dungeon` (1)
- **Revisar jugando:** `underground` (5)
- **Candidato a desactivar:** `abandoned_camps` (1), `regular` (5), `rubble` (6)

### Structory
Ruinas pequeñas y una capilla/cementerio; solapa con RPG Loot y Dungeons and Taverns. Las ruinas genéricas son candidatas; la capilla y el cementerio se revisan (encajan con el tono).

- **Revisar jugando:** `old_manor` (2), `outcast_villager` (8)
- **Candidato a desactivar:** `mid_rare_ruin` (3), `ruin` (1), `ruin_quiet` (1)

### RPG Loot (modern port)
Mazmorras temáticas y estructuras; **revisar jugando** (puede tener identidad, pero solapa con Deadly Deadly Dungeon).

- **Revisar jugando:** `overworld_dungeons` (10), `overworld_structures` (4)

### DeCubed Dungeons
Mazmorras por bioma muy parecidas a las de otros mods; candidatas.

- **Candidato a desactivar:** `dungeons` (12), `ruins` (1)

### Ruins and Towers
Tiene jefes y bastones propios: se queda lo ritual y los jefes (antiguos, torres de desafío, obeliscos, ilusionista, prehistórico, sculk, jardín pálido). Revisar las cabañas y piezas más "gag".

- **Se queda (identidad):** `ancient` (2), `challenge_towers` (3), `illusionist` (1), `obelisks` (2), `pale_garden` (1), `prehistoric` (1), `sculk` (1)
- **Revisar jugando:** `badlands` (1), `cages` (1), `scott` (1), `trenches` (1), `triton` (1)

### Deadly Deadly Dungeon
Mazmorras de varios niveles con jefes y torres: identidad clara. Se quedan.

- **Se queda (identidad):** `dddtowers` (2), `dddungeons` (7)

### Illager Expansion
Amplía el lore de los illagers con mobs y estructuras propias. Se quedan.

- **Se queda (identidad):** `firecaller_hut` (1), `illager_fort` (1), `illusioner_tower` (1), `labyrinth` (1), `sorcerer_hut` (1)

### Incendium
Es la identidad del Nether. Se quedan.

- **Se queda (identidad):** `greater_structures` (3), `lesser_structures` (6)

### Nullscape
Es la identidad del End. Se quedan.

- **Se queda (identidad):** `crashed_ship` (1), `dragon_skeleton` (1), `rift` (1)

### Gigantic Squid
Jefe oceánico con mecánica propia. Se queda.

- **Se queda (identidad):** `gigantic_squid` (1)

### More Enchants
Un campamento de brujas del mod de encantamientos; candidato (no aporta función).

- **Candidato a desactivar:** `witch_camp` (1)

### Vanilla (Nether/End)
Estructuras vanilla de Nether y End que se mantienen.

- **Se queda (identidad):** `end_cities` (1), `nether_complexes` (2)

## Encantamientos

~100 encantamientos de jugador de 6 mods. Con la mesa funcionando solo con lapis (P0), una mesa que ofrece 100 encantamientos al azar diluye todo.
Criterio: se queda lo que **cambia cómo juegas (verbo)** o está ligado a un jefe; se quita lo que es **+X a una estadística** o duplica uno de vanilla.

| Mod | Se quedan | Candidatos a quitar de las tablas |
|---|---|---|
| Advanced Archery (11) | todos: guiding, phasing, ricochet, seeking, scrying, warping, shulking, repeating, hydrodynamic, subtle, precision | — |
| Dragonkind Evolved (13) | todos (se consiguen con el jefe del End): dragonbane, voidwalk, wingspan, pillaring, dragonhearted... | — |
| Fortress of War (5) | todos (botín del jefe de la fortaleza) | — |
| More Enchants (35) | verbos: telekinesis, smelting_touch, harvesting_touch, lifesteal, thunderbolt, evoking, wither, frozen, burst, vanquish, spider, lava_walker, void_step, villager_healer | estadísticas y gags: attack_speed, speed, jump, haste, strength, healthy, entity_reach, block_reach, vision, glow, big_foot, invisibility, knockback_protection, burning_protection, dolphins_grace, falling_wings, curse_of_giantism, curse_of_smallism, gravity (duplicado) |
| Enchants Plus (23) | verbos: ice_aspect, pyrolysis, websnare, retrieval, scorch_walker, toxic, breeze_burst | estadísticas y duplicados de vanilla: vitality, stride, kinetic_protection, swift_strike, outreach, precision, power, fire_aspect; **revisar** las 4 maldiciones (breaking, clumsiness, displacement, double_edge) |
| Dungeons and Taverns (18) | situacionales con identidad: wither_coated, spiteful, ghasted, wax_wings, photosynthesis | duplicados de vanilla (power, piercing, multishot) y estadísticas (traveler, swift_soar, hydro_veil, outreach) |

Los encantamientos que ya existan en objetos de jugadores **no se tocan**: solo se retiran de la mesa, el comercio y el botín.

## Pestañas de logros

Hoy: 5 de vanilla + 6 de mods + la nuestra. Propuesta: se queda **Incendium** (contenido del Nether) y **Dragonkind** (jefe del End) y **Farmer's Delight** (cocina, tono tranquilo);
se ocultan **Lootr** (21 logros triviales), **Repurposed Structures** (sin sentido al podar sus estructuras) y la de **Hostile Mobs** (desaparece con el mod). Se ocultan quitando el `display` de la raíz
con un datapack (reversible). Queda el Diario de Purgatorio como protagonista.

## Dificultad: Hostile Mobs Improve Over Time

Decisión ya tomada: **deja de ser el eje de dificultad**. Los mobs que mejoran con el tiempo hacen que *todo el mundo* se endurezca por horas jugadas, justo lo contrario a
"el peligro es del lugar, y se puede entrar a una zona letal desde el minuto uno".

- **Cómo:** retirar `hostile-mobs-improve-over-time-2.3.jar` en el despliegue (reversible: devolver el jar). Su pestaña de logros y sus funciones desaparecen; sus marcadores en el mundo (puntuaciones) quedan inertes sin el mod (a verificar con una copia del mundo antes de aplicarlo).
- **Qué se conserva a propósito:** nada de su lógica. Si más adelante queremos **mecánicas temporales concretas** (una noche de ceniza, un evento), se diseñan como parte de un lugar o fenómeno, no como un contador global.
- **Peligro por lugar:** ya existe un ejemplo (el Acechador de la Ruina). El siguiente paso es un servicio de peligro por región, fuera del alcance de esta poda.

## Otros mods con ruido (se mantienen, a revisar)

Spiders 2.0, Flower Mimics, Naturally Charged Creepers, Giant Spawn, Harder Wardens y More Mobs aplican dificultad "porque sí". No se tocan ahora: se decide si alguno puede ser
ingrediente de un lugar con sentido. La **mesa de imbuición de Illager Expansion** ya no puede pagarse con XP (ver `docs/xp-vanilla-compat.md`); se decide si se rediseña dentro de la forja.

## Propuesta de fases

1. **Fase 1 (conservadora):** desactivar los "candidatos" de estructuras de Repurposed Structures y Dungeons and Taverns, y retirar Hostile Mobs. Es lo de mayor confianza y mayor efecto.
2. **Fase 2 (tras jugar con La Ruina):** decidir los "Revisar jugando", y aplicar la lista de encantamientos.
3. **Fase 3:** pestañas de logros.

Cada fase es un único jar/datapack reversible, con prueba automática que verifica que los conjuntos quedan vacíos y que los de identidad siguen intactos.

## Qué necesito de ti

Marca lo que no estés de acuerdo en las listas de arriba (cualquier conjunto o encantamiento puede pasar de "candidato" a "se queda", o al revés). Con eso implemento la fase 1.
