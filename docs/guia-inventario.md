# Inventario para la guia de objetos

Generado por `tools/inventario-guia.py` a partir del servidor real (mods, vanilla y datapacks). No editar a mano: se regenera al anadir o actualizar mods.

Tipos: **jugador** = obtenible por el jugador (etiquetas de mesa, aldeanos, botin, tesoro o maldicion); **botin** = sin etiqueta pero algun loot table/receta lo menciona; **interno** = nadie lo da al jugador (jefes, estructuras, logica del mod): no necesita descripcion.

**171 encantamientos definidos**: 128 de jugador, 19 por botin, 24 internos. **Descripciones: 4 escritas de 147 necesarias, 143 por escribir.** (Las escritas viven en `core/src/guia/resources/purgatorio_guia/encantamientos.json`.)

## `dke` (13)

| id | tipo | max | nombre (es) | etiquetas | descrita | nota |
|---|---|---|---|---|---|---|
| `altitude` | botin | 1 | Altitud |  | NO |  |
| `clear_skies` | botin | 1 | Cielos despejados |  | NO |  |
| `deterioration_curse` | jugador | 1 | Maldición de deterioro | curse | NO |  |
| `dragon_lungs` | botin | 3 | Pulmones de dragón |  | NO |  |
| `dragonbane` | botin | 4 | Perdición del dragón |  | NO |  |
| `dragonhearted` | botin | 3 | Corazón de dragón |  | NO |  |
| `dragonsight` | botin | 1 | Visión de dragón |  | NO |  |
| `dragonyield` | botin | 3 | Botín de dragón |  | NO |  |
| `exhalation` | botin | 1 | Exhalación |  | NO |  |
| `kickback` | botin | 1 | Retroceso |  | NO |  |
| `pillaring` | botin | 3 | Pilares |  | NO |  |
| `voidwalk` | botin | 1 | Caminante del vacío |  | NO |  |
| `wingspan` | botin | 5 | Envergadura |  | NO |  |

## `dqc.bows` (11)

| id | tipo | max | nombre (es) | etiquetas | descrita | nota |
|---|---|---|---|---|---|---|
| `guiding` | jugador | 1 | Guía | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `hydrodynamic` | jugador | 1 | Hidrodinámica | treasure | NO |  |
| `phasing` | botin | 3 | Fase |  | NO |  |
| `precision` | jugador | 3 | Precisión | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `repeating` | jugador | 3 | Repetición | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `ricochet` | jugador | 5 | Rebote | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `scrying` | jugador | 1 | Clarividencia | treasure | NO |  |
| `seeking` | jugador | 1 | Fototrópico | treasure | NO |  |
| `shulking` | jugador | 3 | Shulker | treasure | NO |  |
| `subtle` | jugador | 1 | Sutileza | treasure | NO |  |
| `warping` | botin | 1 | Distorsión |  | NO |  |

## `enchantments` (35)

| id | tipo | max | nombre (es) | etiquetas | descrita | nota |
|---|---|---|---|---|---|---|
| `attack_speed` | jugador | 3 | Velocidad de ataque | in_enchanting_table, tradeable | NO |  |
| `big_foot` | jugador | 3 | Pie grande | in_enchanting_table, tradeable | NO |  |
| `block_reach` | jugador | 3 | Alcance de bloques | in_enchanting_table, tradeable | NO |  |
| `burning_protection` | jugador | 1 | Protección contra el fuego | in_enchanting_table, tradeable | NO |  |
| `burst` | botin | 3 | Ráfaga |  | NO |  |
| `curse_of_decaying` | jugador | 1 | Maldición de descomposición | tradeable, curse | NO |  |
| `curse_of_giantism` | jugador | 1 | Maldición del gigantismo | tradeable, curse | NO |  |
| `curse_of_smallism` | jugador | 1 | Maldición del enanismo | tradeable, curse | NO |  |
| `dolphins_grace` | jugador | 3 | Gracia del delfín | in_enchanting_table, tradeable | NO |  |
| `entity_reach` | jugador | 3 | Alcance de entidades | in_enchanting_table, tradeable | NO |  |
| `evoking` | jugador | 1 | Evocación | in_enchanting_table, tradeable | NO |  |
| `falling_wings` | jugador | 3 | Alas de caída | in_enchanting_table, tradeable | NO |  |
| `frozen` | jugador | 3 | Congelado | tradeable | NO |  |
| `glow` | jugador | 3 | Resplandor | in_enchanting_table, tradeable | NO |  |
| `gravity` | jugador | 1 | Gravedad | in_enchanting_table, tradeable | NO |  |
| `harvesting_touch` | jugador | 1 | Toque cosechador | in_enchanting_table, tradeable | NO |  |
| `haste` | jugador | 3 | Prisa | in_enchanting_table, tradeable | NO |  |
| `healthy` | jugador | 3 | Saludable | in_enchanting_table, tradeable | NO |  |
| `invisibility` | jugador | 3 | Invisibilidad | tradeable | NO |  |
| `jump` | jugador | 5 | Salto | in_enchanting_table, tradeable | NO |  |
| `knockback_protection` | jugador | 2 | Protección contra empuje | in_enchanting_table, tradeable | NO |  |
| `lava_walker` | jugador | 1 | Caminante de lava | in_enchanting_table, tradeable | NO |  |
| `lifesteal` | jugador | 3 | Robo de vida | in_enchanting_table, tradeable | NO |  |
| `shulker` | jugador | 3 | Shulker | in_enchanting_table, tradeable | NO |  |
| `smelting_touch` | jugador | 1 | Toque fundidor | in_enchanting_table, tradeable | NO |  |
| `speed` | jugador | 5 | Velocidad | in_enchanting_table, tradeable | NO |  |
| `spider` | jugador | 5 | Araña | in_enchanting_table, tradeable | NO |  |
| `strength` | jugador | 3 | Fuerza | in_enchanting_table, tradeable | NO |  |
| `telekinesis` | jugador | 1 | Telequinesis | in_enchanting_table, tradeable | NO |  |
| `thunderbolt` | jugador | 1 | Rayo | tradeable | NO |  |
| `vanquish` | jugador | 5 | Aniquilación | in_enchanting_table, tradeable | NO |  |
| `villager_healer` | jugador | 1 | Sanador de aldeanos | tradeable | NO |  |
| `vision` | jugador | 3 | Visión | tradeable | NO |  |
| `void_step` | jugador | 1 | Paso del vacío | tradeable | NO |  |
| `wither` | jugador | 3 | Wither | in_enchanting_table, tradeable | NO |  |

## `enchantsplus` (21)

| id | tipo | max | nombre (es) | etiquetas | descrita | nota |
|---|---|---|---|---|---|---|
| `breaking_curse` | jugador | 1 | Maldición de rotura | tradeable, on_random_loot, curse, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `breeze_burst` | jugador | 1 | Ráfaga de brisa | in_enchanting_table, tradeable, treasure, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `clumsiness_curse` | jugador | 1 | Maldición de torpeza | tradeable, on_random_loot, curse, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `crabs_touch` | jugador | 3 | Toque de cangrejo | tradeable, treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `displacement_curse` | jugador | 1 | Maldición de desplazamiento | tradeable, on_random_loot, curse, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `double_edge_curse` | jugador | 1 | Maldición de doble filo | tradeable, on_random_loot, curse, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `gluttony` | jugador | 3 | Glotonería | in_enchanting_table, tradeable, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `graviole` | botin | 3 | Graviola |  | NO |  |
| `ice_aspect` | jugador | 2 | Aspecto de hielo | treasure | NO |  |
| `kinetic_protection` | jugador | 4 | Protección cinética | in_enchanting_table, tradeable, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `outreach` | jugador | 2 | Alcance | in_enchanting_table, tradeable, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `precision` | jugador | 2 | Precisión | in_enchanting_table, tradeable, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `pyrolysis` | jugador | 1 | Pirólisis | in_enchanting_table, tradeable, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `retrieval` | jugador | 4 | Recuperación | tradeable, on_random_loot, on_traded_equipment | NO |  |
| `scorch_walker` | jugador | 2 | Caminante chamuscado | tradeable, treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `skyguard` | interno | 4 | Guardacielos |  |  |  |
| `stride` | jugador | 3 | Zancada | in_enchanting_table, tradeable, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `swift_strike` | jugador | 5 | Golpe veloz | in_enchanting_table, tradeable, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `toxic` | jugador | 1 | Tóxico | in_enchanting_table, tradeable, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `vitality` | jugador | 3 | Vitalidad | in_enchanting_table, tradeable, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `websnare` | interno | 2 | Red de telaraña |  |  |  |

## `farmersdelight` (1)

| id | tipo | max | nombre (es) | etiquetas | descrita | nota |
|---|---|---|---|---|---|---|
| `backstabbing` | jugador | 3 | Puñaladas por la espalda | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO | sin nombre en espanol |

## `incendium` (1)

| id | tipo | max | nombre (es) | etiquetas | descrita | nota |
|---|---|---|---|---|---|---|
| `poison_protection` | botin | 1 | Protección contra veneno |  | NO |  |

## `minecraft` (43)

| id | tipo | max | nombre (es) | etiquetas | descrita | nota |
|---|---|---|---|---|---|---|
| `aqua_affinity` | jugador | 1 | Afinidad acuática | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `bane_of_arthropods` | jugador | 5 | Perdición de los artrópodos | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `binding_curse` | jugador | 1 | Maldición de ligamiento | tradeable, treasure, on_random_loot, curse | NO |  |
| `blast_protection` | jugador | 4 | Protección contra explosiones | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `breach` | jugador | 4 | Fisura | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `channeling` | jugador | 1 | Conductividad | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `density` | jugador | 5 | Densidad | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `depth_strider` | jugador | 3 | Agilidad acuática | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `efficiency` | jugador | 5 | Eficiencia | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `feather_falling` | jugador | 4 | Caída de pluma | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `fire_aspect` | jugador | 2 | Aspecto ígneo | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO | sobrescribe vanilla: enchants-plus-2.0.jar, vanilla-26.3.jar>server-26.3.jar |
| `fire_protection` | jugador | 4 | Protección contra el fuego | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `flame` | jugador | 1 | Fuego | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `fortune` | jugador | 3 | Fortuna | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `frost_walker` | jugador | 2 | Paso helado | tradeable, treasure, on_random_loot | NO |  |
| `impaling` | jugador | 5 | Empalamiento | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `infinity` | jugador | 1 | Infinidad | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `knockback` | jugador | 2 | Empuje | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `looting` | jugador | 3 | Botín | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `loyalty` | jugador | 3 | Lealtad | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `luck_of_the_sea` | jugador | 3 | Suerte marina | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `lunge` | jugador | 3 | Estocada | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `lure` | jugador | 3 | Atracción | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `mending` | jugador | 1 | Reparación | tradeable, treasure, on_random_loot | si |  |
| `multishot` | jugador | 1 | Multidisparo | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `piercing` | jugador | 4 | Perforación | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `power` | jugador | 5 | Poder | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO | sobrescribe vanilla: enchants-plus-2.0.jar, vanilla-26.3.jar>server-26.3.jar |
| `projectile_protection` | jugador | 4 | Protección contra proyectiles | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `protection` | jugador | 4 | Protección | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `punch` | jugador | 2 | Retroceso | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `quick_charge` | jugador | 3 | Carga rápida | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `respiration` | jugador | 3 | Respiración | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `riptide` | jugador | 3 | Propulsión acuática | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `sharpness` | jugador | 5 | Filo | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `silk_touch` | jugador | 1 | Toque de seda | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `smite` | jugador | 5 | Golpeo | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `soul_speed` | jugador | 3 | Velocidad del alma | treasure | NO |  |
| `sweeping_edge` | jugador | 3 | Barrido | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `swift_sneak` | jugador | 3 | Sigilo veloz | treasure | NO |  |
| `thorns` | jugador | 3 | Espinas | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `unbreaking` | jugador | 3 | Irrompibilidad | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | NO |  |
| `vanishing_curse` | jugador | 1 | Maldición de desaparición | tradeable, treasure, on_random_loot, curse | NO |  |
| `wind_burst` | jugador | 3 | Aeroimpulso | treasure | NO |  |

## `nova_structures` (18)

| id | tipo | max | nombre (es) | etiquetas | descrita | nota |
|---|---|---|---|---|---|---|
| `aerials_bane` | jugador | 5 | Perdición Aérea | treasure | NO | sin nombre en espanol |
| `antidote` | jugador | 1 | Antídoto | treasure | NO | sin nombre en espanol |
| `conductivity_curse` | jugador | 1 | Maldición de Conductividad | treasure, curse | NO | sin nombre en espanol |
| `function_only` | jugador | 5 | Encantamiento Bugueado | treasure | NO | sin nombre en espanol |
| `ghasted` | jugador | 3 | Ghastificado | treasure | NO | sin nombre en espanol |
| `gravity` | jugador | 3 | Gravedad | treasure | NO | sin nombre en espanol |
| `hydro_veil` | jugador | 4 | Velo Acuático | treasure | NO | sin nombre en espanol |
| `illagers_bane` | jugador | 5 | Perdición de los Saqueadores | treasure | NO | sin nombre en espanol |
| `multishot` | jugador | 1 | Disparo Triple | treasure | NO | sin nombre en espanol |
| `outreach` | jugador | 4 | Alcance | treasure | NO | sin nombre en espanol |
| `photosynthesis` | jugador | 1 | Fotosíntesis | treasure | NO | sin nombre en espanol |
| `piercing` | jugador | 4 | Atravesar | treasure | NO | sin nombre en espanol |
| `power` | jugador | 5 | Poderío | treasure | NO | sin nombre en espanol |
| `spiteful` | jugador | 3 | Rencor | treasure | NO | sin nombre en espanol |
| `swift_soar` | jugador | 3 | Vuelo Veloz | treasure | NO | sin nombre en espanol |
| `traveler` | jugador | 3 | Viajero | treasure | NO | sin nombre en espanol |
| `wax_wings` | jugador | 1 | Alas de Cera | treasure | NO | sin nombre en espanol |
| `wither_coated` | jugador | 3 | Recubrimiento de Descomposición | treasure | NO | sin nombre en espanol |

## `rnt` (13)

| id | tipo | max | nombre (es) | etiquetas | descrita | nota |
|---|---|---|---|---|---|---|
| `internal/boss/borealis_archmage` | interno | 1 | Archimago boreal |  |  |  |
| `internal/boss/krah-yan_aura` | interno | 1 | Aura de Krah-Yan |  |  |  |
| `internal/boss/ominous_borealis_archmage` | interno | 1 | Archimago boreal |  |  |  |
| `internal/boss/ominous_krah-yan_aura` | interno | 1 | Aura de Krah-Yan |  |  |  |
| `internal/boss/ominous_slime_wizard` | interno | 1 | Mago de slime |  |  |  |
| `internal/boss/slime_wizard` | interno | 1 | Mago de slime |  |  |  |
| `internal/ice_bound` | interno | 1 | Nacido del hielo |  |  |  |
| `internal/krah-yan_blood` | interno | 1 | Sangre de Krah-Yan | curse |  |  |
| `internal/place/sculk_chimney` | interno | 1 | ¡¿Cómo has conseguido esto?! |  |  |  |
| `internal/slime_poisoning` | interno | 3 | Envenenamiento de slime | curse |  |  |
| `internal/staff_stun` | interno | 5 | Aturdimiento de cetro | curse |  |  |
| `internal/type_ice` | interno | 16 | Tipo hielo |  |  |  |
| `internal/yan_soldier` | interno | 1 | Soldado Yan |  |  |  |

## `serverbackpacks` (2)

| id | tipo | max | nombre (es) | etiquetas | descrita | nota |
|---|---|---|---|---|---|---|
| `capacity` | jugador | 3 | Capacidad | in_enchanting_table, treasure, on_random_loot | NO |  |
| `link_scroller` | interno | 3 | Almacenamiento entrelazado |  |  |  |

## `warft` (13)

| id | tipo | max | nombre (es) | etiquetas | descrita | nota |
|---|---|---|---|---|---|---|
| `blazing` | botin | 1 | Llameante |  | NO |  |
| `core_strength` | botin | 1 | Fuerza del núcleo |  | NO |  |
| `dominator` | jugador | 3 | Dominador | on_random_loot | NO |  |
| `dominion` | jugador | 5 | Dominio | on_random_loot | NO |  |
| `magma_walker` | jugador | 1 | Caminante del magma | on_random_loot | NO |  |
| `tech/immunity/explosions` | interno | 1 | Encantamiento técnico: inmunidad a explosiones |  |  |  |
| `tech/immunity/magic` | interno | 1 | Encantamiento técnico: inmunidad a la magia |  |  |  |
| `tech/immunity/projectiles` | interno | 1 | Encantamiento técnico: inmunidad a proyectiles |  |  |  |
| `tech/immunity/suffocation` | interno | 1 | Encantamiento técnico: inmunidad a la asfixia |  |  |  |
| `tech/impulse` | interno | 1 | Encantamiento técnico: impulso |  |  |  |
| `tech/resistance/magic` | interno | 1 | Encantamiento técnico: resistencia a ataques |  |  |  |
| `tech/resistance/melee` | interno | 1 | Encantamiento técnico: resistencia a ataques |  |  |  |
| `tech/resistance/projectiles` | interno | 1 | Encantamiento técnico: resistencia a ataques |  |  |  |

## Efectos con nombre conocido

42 efectos aparecen en los ficheros de idioma (vanilla + mods). El registro real, que incluye efectos sin lang, se cruza despues con el servidor de pruebas (fase de pociones).

- `farmersdelight`: 2
- `minecraft`: 40
