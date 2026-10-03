# Primera vez que un jugador entra en La Ruina de Ceniza. Recompensa individual (cuelga de un logro).
title @s times 12 80 30
title @s subtitle {"text":"Algo en este lugar te reconoce.","color":"gray","italic":true}
title @s title {"text":"La Ruina de Ceniza","color":"gold"}
playsound minecraft:block.bell.resonate player @s ~ ~ ~ 0.6 0.55
playsound minecraft:ambient.soul_sand_valley.mood player @s ~ ~ ~ 0.8 0.8
particle minecraft:soul ~ ~1 ~ 0.5 0.7 0.5 0.03 25 normal @s
purgatorio admin alma add @s 8 descubrimiento
