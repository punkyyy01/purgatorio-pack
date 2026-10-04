# Inventario para la guia de objetos

Generado por `tools/inventario-guia.py` a partir del servidor real (mods, vanilla y datapacks). No editar a mano: se regenera al anadir o actualizar mods.

Tipos: **jugador** = obtenible por el jugador (etiquetas de mesa, aldeanos, botin, tesoro o maldicion); **botin** = sin etiqueta pero algun loot table/receta lo menciona; **interno** = nadie lo da al jugador (jefes, estructuras, logica del mod): no necesita descripcion.

**171 encantamientos definidos**: 128 de jugador, 19 por botin, 24 internos. **Descripciones: 147 escritas de 147 necesarias, 0 por escribir.** (Las escritas viven en `core/src/guia/resources/purgatorio_guia/encantamientos.json`.)

## `dke` (13)

| id | tipo | max | nombre (es) | etiquetas | descrita | nota |
|---|---|---|---|---|---|---|
| `altitude` | botin | 1 | Altitud |  | si |  |
| `clear_skies` | botin | 1 | Cielos despejados |  | si |  |
| `deterioration_curse` | jugador | 1 | Maldición de deterioro | curse | si |  |
| `dragon_lungs` | botin | 3 | Pulmones de dragón |  | si |  |
| `dragonbane` | botin | 4 | Perdición del dragón |  | si |  |
| `dragonhearted` | botin | 3 | Corazón de dragón |  | si |  |
| `dragonsight` | botin | 1 | Visión de dragón |  | si |  |
| `dragonyield` | botin | 3 | Botín de dragón |  | si |  |
| `exhalation` | botin | 1 | Exhalación |  | si |  |
| `kickback` | botin | 1 | Retroceso |  | si |  |
| `pillaring` | botin | 3 | Pilares |  | si |  |
| `voidwalk` | botin | 1 | Caminante del vacío |  | si |  |
| `wingspan` | botin | 5 | Envergadura |  | si |  |

## `dqc.bows` (11)

| id | tipo | max | nombre (es) | etiquetas | descrita | nota |
|---|---|---|---|---|---|---|
| `guiding` | jugador | 1 | Guía | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `hydrodynamic` | jugador | 1 | Hidrodinámica | treasure | si |  |
| `phasing` | botin | 3 | Fase |  | si |  |
| `precision` | jugador | 3 | Precisión | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `repeating` | jugador | 3 | Repetición | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `ricochet` | jugador | 5 | Rebote | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `scrying` | jugador | 1 | Clarividencia | treasure | si |  |
| `seeking` | jugador | 1 | Fototrópico | treasure | si |  |
| `shulking` | jugador | 3 | Shulker | treasure | si |  |
| `subtle` | jugador | 1 | Sutileza | treasure | si |  |
| `warping` | botin | 1 | Distorsión |  | si |  |

## `enchantments` (35)

| id | tipo | max | nombre (es) | etiquetas | descrita | nota |
|---|---|---|---|---|---|---|
| `attack_speed` | jugador | 3 | Velocidad de ataque | in_enchanting_table, tradeable | si |  |
| `big_foot` | jugador | 3 | Pie grande | in_enchanting_table, tradeable | si |  |
| `block_reach` | jugador | 3 | Alcance de bloques | in_enchanting_table, tradeable | si |  |
| `burning_protection` | jugador | 1 | Protección contra el fuego | in_enchanting_table, tradeable | si |  |
| `burst` | botin | 3 | Ráfaga |  | si |  |
| `curse_of_decaying` | jugador | 1 | Maldición de descomposición | tradeable, curse | si |  |
| `curse_of_giantism` | jugador | 1 | Maldición del gigantismo | tradeable, curse | si |  |
| `curse_of_smallism` | jugador | 1 | Maldición del enanismo | tradeable, curse | si |  |
| `dolphins_grace` | jugador | 3 | Gracia del delfín | in_enchanting_table, tradeable | si |  |
| `entity_reach` | jugador | 3 | Alcance de entidades | in_enchanting_table, tradeable | si |  |
| `evoking` | jugador | 1 | Evocación | in_enchanting_table, tradeable | si |  |
| `falling_wings` | jugador | 3 | Alas de caída | in_enchanting_table, tradeable | si |  |
| `frozen` | jugador | 3 | Congelado | tradeable | si |  |
| `glow` | jugador | 3 | Resplandor | in_enchanting_table, tradeable | si |  |
| `gravity` | jugador | 1 | Gravedad | in_enchanting_table, tradeable | si |  |
| `harvesting_touch` | jugador | 1 | Toque cosechador | in_enchanting_table, tradeable | si |  |
| `haste` | jugador | 3 | Prisa | in_enchanting_table, tradeable | si |  |
| `healthy` | jugador | 3 | Saludable | in_enchanting_table, tradeable | si |  |
| `invisibility` | jugador | 3 | Invisibilidad | tradeable | si |  |
| `jump` | jugador | 5 | Salto | in_enchanting_table, tradeable | si |  |
| `knockback_protection` | jugador | 2 | Protección contra empuje | in_enchanting_table, tradeable | si |  |
| `lava_walker` | jugador | 1 | Caminante de lava | in_enchanting_table, tradeable | si |  |
| `lifesteal` | jugador | 3 | Robo de vida | in_enchanting_table, tradeable | si |  |
| `shulker` | jugador | 3 | Shulker | in_enchanting_table, tradeable | si |  |
| `smelting_touch` | jugador | 1 | Toque fundidor | in_enchanting_table, tradeable | si |  |
| `speed` | jugador | 5 | Velocidad | in_enchanting_table, tradeable | si |  |
| `spider` | jugador | 5 | Araña | in_enchanting_table, tradeable | si |  |
| `strength` | jugador | 3 | Fuerza | in_enchanting_table, tradeable | si |  |
| `telekinesis` | jugador | 1 | Telequinesis | in_enchanting_table, tradeable | si |  |
| `thunderbolt` | jugador | 1 | Rayo | tradeable | si |  |
| `vanquish` | jugador | 5 | Aniquilación | in_enchanting_table, tradeable | si |  |
| `villager_healer` | jugador | 1 | Sanador de aldeanos | tradeable | si |  |
| `vision` | jugador | 3 | Visión | tradeable | si |  |
| `void_step` | jugador | 1 | Paso del vacío | tradeable | si |  |
| `wither` | jugador | 3 | Wither | in_enchanting_table, tradeable | si |  |

## `enchantsplus` (21)

| id | tipo | max | nombre (es) | etiquetas | descrita | nota |
|---|---|---|---|---|---|---|
| `breaking_curse` | jugador | 1 | Maldición de rotura | tradeable, on_random_loot, curse, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `breeze_burst` | jugador | 1 | Ráfaga de brisa | in_enchanting_table, tradeable, treasure, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `clumsiness_curse` | jugador | 1 | Maldición de torpeza | tradeable, on_random_loot, curse, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `crabs_touch` | jugador | 3 | Toque de cangrejo | tradeable, treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `displacement_curse` | jugador | 1 | Maldición de desplazamiento | tradeable, on_random_loot, curse, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `double_edge_curse` | jugador | 1 | Maldición de doble filo | tradeable, on_random_loot, curse, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `gluttony` | jugador | 3 | Glotonería | in_enchanting_table, tradeable, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `graviole` | botin | 3 | Graviola |  | si |  |
| `ice_aspect` | jugador | 2 | Aspecto de hielo | treasure | si |  |
| `kinetic_protection` | jugador | 4 | Protección cinética | in_enchanting_table, tradeable, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `outreach` | jugador | 2 | Alcance | in_enchanting_table, tradeable, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `precision` | jugador | 2 | Precisión | in_enchanting_table, tradeable, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `pyrolysis` | jugador | 1 | Pirólisis | in_enchanting_table, tradeable, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `retrieval` | jugador | 4 | Recuperación | tradeable, on_random_loot, on_traded_equipment | si |  |
| `scorch_walker` | jugador | 2 | Caminante chamuscado | tradeable, treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `skyguard` | interno | 4 | Guardacielos |  |  |  |
| `stride` | jugador | 3 | Zancada | in_enchanting_table, tradeable, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `swift_strike` | jugador | 5 | Golpe veloz | in_enchanting_table, tradeable, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `toxic` | jugador | 1 | Tóxico | in_enchanting_table, tradeable, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `vitality` | jugador | 3 | Vitalidad | in_enchanting_table, tradeable, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `websnare` | interno | 2 | Red de telaraña |  |  |  |

## `farmersdelight` (1)

| id | tipo | max | nombre (es) | etiquetas | descrita | nota |
|---|---|---|---|---|---|---|
| `backstabbing` | jugador | 3 | Puñaladas por la espalda | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si | sin nombre en espanol |

## `incendium` (1)

| id | tipo | max | nombre (es) | etiquetas | descrita | nota |
|---|---|---|---|---|---|---|
| `poison_protection` | botin | 1 | Protección contra veneno |  | si |  |

## `minecraft` (43)

| id | tipo | max | nombre (es) | etiquetas | descrita | nota |
|---|---|---|---|---|---|---|
| `aqua_affinity` | jugador | 1 | Afinidad acuática | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `bane_of_arthropods` | jugador | 5 | Perdición de los artrópodos | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `binding_curse` | jugador | 1 | Maldición de ligamiento | tradeable, treasure, on_random_loot, curse | si |  |
| `blast_protection` | jugador | 4 | Protección contra explosiones | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `breach` | jugador | 4 | Fisura | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `channeling` | jugador | 1 | Conductividad | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `density` | jugador | 5 | Densidad | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `depth_strider` | jugador | 3 | Agilidad acuática | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `efficiency` | jugador | 5 | Eficiencia | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `feather_falling` | jugador | 4 | Caída de pluma | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `fire_aspect` | jugador | 2 | Aspecto ígneo | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si | sobrescribe vanilla: enchants-plus-2.0.jar, vanilla-26.3.jar>server-26.3.jar |
| `fire_protection` | jugador | 4 | Protección contra el fuego | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `flame` | jugador | 1 | Fuego | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `fortune` | jugador | 3 | Fortuna | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `frost_walker` | jugador | 2 | Paso helado | tradeable, treasure, on_random_loot | si |  |
| `impaling` | jugador | 5 | Empalamiento | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `infinity` | jugador | 1 | Infinidad | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `knockback` | jugador | 2 | Empuje | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `looting` | jugador | 3 | Botín | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `loyalty` | jugador | 3 | Lealtad | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `luck_of_the_sea` | jugador | 3 | Suerte marina | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `lunge` | jugador | 3 | Estocada | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `lure` | jugador | 3 | Atracción | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `mending` | jugador | 1 | Reparación | tradeable, treasure, on_random_loot | si |  |
| `multishot` | jugador | 1 | Multidisparo | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `piercing` | jugador | 4 | Perforación | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `power` | jugador | 5 | Poder | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si | sobrescribe vanilla: enchants-plus-2.0.jar, vanilla-26.3.jar>server-26.3.jar |
| `projectile_protection` | jugador | 4 | Protección contra proyectiles | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `protection` | jugador | 4 | Protección | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `punch` | jugador | 2 | Retroceso | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `quick_charge` | jugador | 3 | Carga rápida | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `respiration` | jugador | 3 | Respiración | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `riptide` | jugador | 3 | Propulsión acuática | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `sharpness` | jugador | 5 | Filo | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `silk_touch` | jugador | 1 | Toque de seda | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `smite` | jugador | 5 | Golpeo | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `soul_speed` | jugador | 3 | Velocidad del alma | treasure | si |  |
| `sweeping_edge` | jugador | 3 | Barrido | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `swift_sneak` | jugador | 3 | Sigilo veloz | treasure | si |  |
| `thorns` | jugador | 3 | Espinas | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `unbreaking` | jugador | 3 | Irrompibilidad | in_enchanting_table, tradeable, non_treasure, on_random_loot, on_traded_equipment, on_mob_spawn_equipment | si |  |
| `vanishing_curse` | jugador | 1 | Maldición de desaparición | tradeable, treasure, on_random_loot, curse | si |  |
| `wind_burst` | jugador | 3 | Aeroimpulso | treasure | si |  |

## `nova_structures` (18)

| id | tipo | max | nombre (es) | etiquetas | descrita | nota |
|---|---|---|---|---|---|---|
| `aerials_bane` | jugador | 5 | Perdición Aérea | treasure | si | sin nombre en espanol |
| `antidote` | jugador | 1 | Antídoto | treasure | si | sin nombre en espanol |
| `conductivity_curse` | jugador | 1 | Maldición de Conductividad | treasure, curse | si | sin nombre en espanol |
| `function_only` | jugador | 5 | Encantamiento Bugueado | treasure | si | sin nombre en espanol |
| `ghasted` | jugador | 3 | Ghastificado | treasure | si | sin nombre en espanol |
| `gravity` | jugador | 3 | Gravedad | treasure | si | sin nombre en espanol |
| `hydro_veil` | jugador | 4 | Velo Acuático | treasure | si | sin nombre en espanol |
| `illagers_bane` | jugador | 5 | Perdición de los Saqueadores | treasure | si | sin nombre en espanol |
| `multishot` | jugador | 1 | Disparo Triple | treasure | si | sin nombre en espanol |
| `outreach` | jugador | 4 | Alcance | treasure | si | sin nombre en espanol |
| `photosynthesis` | jugador | 1 | Fotosíntesis | treasure | si | sin nombre en espanol |
| `piercing` | jugador | 4 | Atravesar | treasure | si | sin nombre en espanol |
| `power` | jugador | 5 | Poderío | treasure | si | sin nombre en espanol |
| `spiteful` | jugador | 3 | Rencor | treasure | si | sin nombre en espanol |
| `swift_soar` | jugador | 3 | Vuelo Veloz | treasure | si | sin nombre en espanol |
| `traveler` | jugador | 3 | Viajero | treasure | si | sin nombre en espanol |
| `wax_wings` | jugador | 1 | Alas de Cera | treasure | si | sin nombre en espanol |
| `wither_coated` | jugador | 3 | Recubrimiento de Descomposición | treasure | si | sin nombre en espanol |

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
| `capacity` | jugador | 3 | Capacidad | in_enchanting_table, treasure, on_random_loot | si |  |
| `link_scroller` | interno | 3 | Almacenamiento entrelazado |  |  |  |

## `warft` (13)

| id | tipo | max | nombre (es) | etiquetas | descrita | nota |
|---|---|---|---|---|---|---|
| `blazing` | botin | 1 | Llameante |  | si |  |
| `core_strength` | botin | 1 | Fuerza del núcleo |  | si |  |
| `dominator` | jugador | 3 | Dominador | on_random_loot | si |  |
| `dominion` | jugador | 5 | Dominio | on_random_loot | si |  |
| `magma_walker` | jugador | 1 | Caminante del magma | on_random_loot | si |  |
| `tech/immunity/explosions` | interno | 1 | Encantamiento técnico: inmunidad a explosiones |  |  |  |
| `tech/immunity/magic` | interno | 1 | Encantamiento técnico: inmunidad a la magia |  |  |  |
| `tech/immunity/projectiles` | interno | 1 | Encantamiento técnico: inmunidad a proyectiles |  |  |  |
| `tech/immunity/suffocation` | interno | 1 | Encantamiento técnico: inmunidad a la asfixia |  |  |  |
| `tech/impulse` | interno | 1 | Encantamiento técnico: impulso |  |  |  |
| `tech/resistance/magic` | interno | 1 | Encantamiento técnico: resistencia a ataques |  |  |  |
| `tech/resistance/melee` | interno | 1 | Encantamiento técnico: resistencia a ataques |  |  |  |
| `tech/resistance/projectiles` | interno | 1 | Encantamiento técnico: resistencia a ataques |  |  |  |

## Efectos

**42 efectos**. **Descripciones: 42 escritas, 0 por escribir.** (Las escritas viven en `core/src/guia/resources/purgatorio_guia/efectos.json`.) Las cifras de atributos (velocidad, daño, vida...) las lee el inspector del propio juego: no se escriben.

El volcado del registro se genera con `FULL_PACK=1 tools/run-integration-tests.sh guia` (incluye los efectos de todos los mods del servidor real).

| id | categoria | descrita |
|---|---|---|
| `farmersdelight:comfort` | BENEFICIAL | si |
| `farmersdelight:nourishment` | BENEFICIAL | si |
| `minecraft:absorption` | BENEFICIAL | si |
| `minecraft:bad_omen` | NEUTRAL | si |
| `minecraft:blindness` | HARMFUL | si |
| `minecraft:breath_of_the_nautilus` | BENEFICIAL | si |
| `minecraft:conduit_power` | BENEFICIAL | si |
| `minecraft:darkness` | HARMFUL | si |
| `minecraft:dolphins_grace` | BENEFICIAL | si |
| `minecraft:fire_resistance` | BENEFICIAL | si |
| `minecraft:glowing` | NEUTRAL | si |
| `minecraft:haste` | BENEFICIAL | si |
| `minecraft:health_boost` | BENEFICIAL | si |
| `minecraft:hero_of_the_village` | BENEFICIAL | si |
| `minecraft:hunger` | HARMFUL | si |
| `minecraft:infested` | HARMFUL | si |
| `minecraft:instant_damage` | HARMFUL | si |
| `minecraft:instant_health` | BENEFICIAL | si |
| `minecraft:invisibility` | BENEFICIAL | si |
| `minecraft:jump_boost` | BENEFICIAL | si |
| `minecraft:levitation` | HARMFUL | si |
| `minecraft:luck` | BENEFICIAL | si |
| `minecraft:mining_fatigue` | HARMFUL | si |
| `minecraft:nausea` | HARMFUL | si |
| `minecraft:night_vision` | BENEFICIAL | si |
| `minecraft:oozing` | HARMFUL | si |
| `minecraft:poison` | HARMFUL | si |
| `minecraft:raid_omen` | NEUTRAL | si |
| `minecraft:regeneration` | BENEFICIAL | si |
| `minecraft:resistance` | BENEFICIAL | si |
| `minecraft:saturation` | BENEFICIAL | si |
| `minecraft:slow_falling` | BENEFICIAL | si |
| `minecraft:slowness` | HARMFUL | si |
| `minecraft:speed` | BENEFICIAL | si |
| `minecraft:strength` | BENEFICIAL | si |
| `minecraft:trial_omen` | NEUTRAL | si |
| `minecraft:unluck` | HARMFUL | si |
| `minecraft:water_breathing` | BENEFICIAL | si |
| `minecraft:weakness` | HARMFUL | si |
| `minecraft:weaving` | HARMFUL | si |
| `minecraft:wind_charged` | HARMFUL | si |
| `minecraft:wither` | HARMFUL | si |

### Pociones sin efectos (base)

| pocion | descrita |
|---|---|
| `minecraft:awkward` | si |
| `minecraft:mundane` | si |
| `minecraft:thick` | si |
| `minecraft:water` | si |

## Atributos

**40 atributos**. **Descripciones: 40 escritas, 0 por escribir.** (Viven en `core/src/guia/resources/purgatorio_guia/atributos.json`.) Un objeto puede llevar cualquiera (p. ej. los que añade RPG Loot a objetos concretos), por eso se describen todos. La columna *objetos* es cuántos objetos los llevan de serie.

| id | objetos | descrita |
|---|---|---|
| `minecraft:attack_damage` | 94 | si |
| `minecraft:attack_speed` | 94 | si |
| `minecraft:armor` | 45 | si |
| `minecraft:armor_toughness` | 45 | si |
| `minecraft:knockback_resistance` | 10 | si |
| `minecraft:waypoint_transmit_range` | 8 | si |
| `minecraft:attack_knockback` | 1 | si |
| `minecraft:air_drag_modifier` | 0 | si |
| `minecraft:below_name_distance` | 0 | si |
| `minecraft:block_break_speed` | 0 | si |
| `minecraft:block_interaction_range` | 0 | si |
| `minecraft:bounciness` | 0 | si |
| `minecraft:burning_time` | 0 | si |
| `minecraft:camera_distance` | 0 | si |
| `minecraft:entity_interaction_range` | 0 | si |
| `minecraft:explosion_knockback_resistance` | 0 | si |
| `minecraft:fall_damage_multiplier` | 0 | si |
| `minecraft:flying_speed` | 0 | si |
| `minecraft:follow_range` | 0 | si |
| `minecraft:friction_modifier` | 0 | si |
| `minecraft:gravity` | 0 | si |
| `minecraft:jump_strength` | 0 | si |
| `minecraft:luck` | 0 | si |
| `minecraft:max_absorption` | 0 | si |
| `minecraft:max_health` | 0 | si |
| `minecraft:mining_efficiency` | 0 | si |
| `minecraft:movement_efficiency` | 0 | si |
| `minecraft:movement_speed` | 0 | si |
| `minecraft:name_tag_distance` | 0 | si |
| `minecraft:oxygen_bonus` | 0 | si |
| `minecraft:safe_fall_distance` | 0 | si |
| `minecraft:scale` | 0 | si |
| `minecraft:sneaking_speed` | 0 | si |
| `minecraft:spawn_reinforcements` | 0 | si |
| `minecraft:step_height` | 0 | si |
| `minecraft:submerged_mining_speed` | 0 | si |
| `minecraft:sweeping_damage_ratio` | 0 | si |
| `minecraft:tempt_range` | 0 | si |
| `minecraft:water_movement_efficiency` | 0 | si |
| `minecraft:waypoint_receive_range` | 0 | si |

## Objetos de mods

**628 objetos de mods**: 83 con descripción a mano (`objetos.json`), 97 que el inspector ya explica solo (efectos, equipo, consumo) y **84 candidatos por describir**. No se cuentan como candidatos la decoración (TSA), los huevos generadores ni los bloques.

| id | nombre | clase |
|---|---|---|
| `babyfat:ranchu_bucket` | Cubo de ranchu | RanchuBucketItem |
| `danse:player_statue` | Estatua de jugador | StatuePlayerModelItem |
| `farmersdelight:apple_pie` | Tarta de manzana | PlaceableItem |
| `farmersdelight:bacon` | Tocino crudo | Item |
| `farmersdelight:bacon_sandwich` | Sándwich de tocino | ConsumableItem |
| `farmersdelight:barbecue_stick` | Brocheta de carne asada | ConsumableItem |
| `farmersdelight:beef_patty` | Hamburguesa | Item |
| `farmersdelight:black_hanging_canvas_sign` | Cartel colgante de tela negra | HangingSignItem |
| `farmersdelight:blue_hanging_canvas_sign` | Cartel colgante de tela azul | HangingSignItem |
| `farmersdelight:brown_hanging_canvas_sign` | Cartel colgante de tela marrón | HangingSignItem |
| `farmersdelight:brown_mushroom_colony` | Colonia de hongos marrones | MushroomColonyItem |
| `farmersdelight:cabbage` | Col | Item |
| `farmersdelight:cabbage_leaf` | Hoja de col | Item |
| `farmersdelight:cabbage_rolls` | Rollos de col | ConsumableItem |
| `farmersdelight:canvas` | Tela | FuelItem |
| `farmersdelight:chicken_sandwich` | Sándwich de pollo | ConsumableItem |
| `farmersdelight:chocolate_pie` | Pastel de chocolate | PlaceableItem |
| `farmersdelight:cod_roll` | Rollos de bacalao | ConsumableItem |
| `farmersdelight:cod_slice` | Rebanada de bacalao crudo | Item |
| `farmersdelight:cooked_bacon` | Tocino cocido | Item |
| `farmersdelight:cooked_chicken_cuts` | Cortes de pollo cocidos | Item |
| `farmersdelight:cooked_cod_slice` | Rebanada de bacalao cocido | Item |
| `farmersdelight:cooked_mutton_chops` | Chuletas de cordero cocidas | Item |
| `farmersdelight:cooked_salmon_slice` | Rebanada de salmón cocido | Item |
| `farmersdelight:cooking_pot` | Olla | CookingPotItem |
| `farmersdelight:cyan_hanging_canvas_sign` | Cartel colgante de tela cian | HangingSignItem |
| `farmersdelight:debug_pumpkin_pie` | item.farmersdelight.debug_pumpkin_pie | ModItems$2 |
| `farmersdelight:dog_food` | Comida para perros | DogFoodItem |
| `farmersdelight:dumplings` | Albóndigas | ConsumableItem |
| `farmersdelight:egg_sandwich` | Sándwich de huevo | ConsumableItem |
| `farmersdelight:fried_egg` | Huevo frito | Item |
| `farmersdelight:gleaming_salad_block` | Ensalada brillante | PlaceableItem |
| `farmersdelight:gray_hanging_canvas_sign` | Cartel colgante de tela gris | HangingSignItem |
| `farmersdelight:green_hanging_canvas_sign` | Cartel colgante de tela verde | HangingSignItem |
| `farmersdelight:ham` | Jamón | Item |
| `farmersdelight:hamburger` | Hamburguesa | ConsumableItem |
| `farmersdelight:hanging_canvas_sign` | Cartel colgante de tela | HangingSignItem |
| `farmersdelight:honey_cookie` | Galleta de miel | Item |
| `farmersdelight:honey_glazed_ham_block` | Jamón glaseado con miel | PlaceableItem |
| `farmersdelight:horse_feed` | Comida para caballos | HorseFeedItem |
| `farmersdelight:hot_cocoa` | Chocolate caliente | ConsumableItem |
| `farmersdelight:kelp_roll` | Rollos de sargazo | ConsumableItem |
| `farmersdelight:kelp_roll_slice` | Rebanada de rollo de sargazo | ConsumableItem |
| `farmersdelight:light_blue_hanging_canvas_sign` | Cartel colgante de tela azul claro | HangingSignItem |
| `farmersdelight:light_gray_hanging_canvas_sign` | Cartel colgante de tela gris claro | HangingSignItem |
| `farmersdelight:lime_hanging_canvas_sign` | Cartel colgante de tela verde lima | HangingSignItem |
| `farmersdelight:magenta_hanging_canvas_sign` | Cartel colgante de tela magenta | HangingSignItem |
| `farmersdelight:melon_juice` | Zumo de melón | ConsumableItem |
| `farmersdelight:melon_popsicle` | Palito de melón | ConsumableItem |
| `farmersdelight:milk_bottle` | Botella de leche | ConsumableItem |
| `farmersdelight:minced_beef` | Carne picada | Item |
| `farmersdelight:mutton_chops` | Chuletas de cordero crudas | Item |
| `farmersdelight:mutton_wrap` | Wrap de cordero | ConsumableItem |
| `farmersdelight:orange_hanging_canvas_sign` | Cartel colgante de tela naranja | HangingSignItem |
| `farmersdelight:pie_crust` | Corteza de tarta | Item |
| `farmersdelight:pink_hanging_canvas_sign` | Cartel colgante de tela rosa | HangingSignItem |
| `farmersdelight:pumpkin_slice` | Rodaja de calabaza | Item |
| `farmersdelight:purple_hanging_canvas_sign` | Cartel colgante de tela morada | HangingSignItem |
| `farmersdelight:red_hanging_canvas_sign` | Cartel colgante de tela roja | HangingSignItem |
| `farmersdelight:red_mushroom_colony` | Colonia de hongos rojos | MushroomColonyItem |
| `farmersdelight:rice` | Arroz | RiceItem |
| `farmersdelight:rice_panicle` | Panícula de arroz | Item |
| `farmersdelight:rice_roll_medley_block` | Rollos de arroz | PlaceableItem |
| `farmersdelight:roast_chicken_block` | Pollo asado | PlaceableItem |
| `farmersdelight:rope` | Cuerda | RopeItem |
| `farmersdelight:rotten_tomato` | Tomate podrido | RottenTomatoItem |
| `farmersdelight:salmon_roll` | Rollos de salmon | ConsumableItem |
| `farmersdelight:salmon_slice` | Rebanada de salmón crudo | Item |
| `farmersdelight:shepherds_pie_block` | Pastel de pastor | PlaceableItem |
| `farmersdelight:smoked_ham` | Jamón cocido | Item |
| `farmersdelight:straw` | Paja | FuelItem |
| `farmersdelight:stuffed_potato` | Patata rellena | ConsumableItem |
| `farmersdelight:stuffed_pumpkin_block` | Calabaza rellena | PlaceableItem |
| `farmersdelight:sweet_berry_cheesecake` | Cheesecake con bayas dulces | PlaceableItem |
| `farmersdelight:sweet_berry_cookie` | Galleta de bayas dulces | Item |
| `farmersdelight:tomato` | Tomate | Item |
| `farmersdelight:tomato_sauce` | Salsa de tomate | ConsumableItem |
| `farmersdelight:tomato_seeds` | Semillas de tomate | ModItems$1 |
| `farmersdelight:tree_bark` | Corteza de árbol | FuelItem |
| `farmersdelight:white_hanging_canvas_sign` | Cartel colgante de tela blanca | HangingSignItem |
| `farmersdelight:yellow_hanging_canvas_sign` | Cartel colgante de tela amarilla | HangingSignItem |
| `purgatorio:esquirla_de_brasa` | Esquirla de Brasa | SimpleItem |
| `toms_mobs:emperor_wing_pattern` | Patrón de Alas de Emperadora | TexturedPolymerItem |
| `universal_graves:icon` | item.universal_graves.icon | IconItem |
