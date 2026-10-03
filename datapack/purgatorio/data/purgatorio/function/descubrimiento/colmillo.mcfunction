# Primera vez que un jugador tiene el Colmillo de Ceniza en el inventario.
title @s actionbar {"text":"Algo en este colmillo todavía arde.","color":"gold","italic":true}
playsound minecraft:item.firecharge.use player @s ~ ~ ~ 0.8 0.6
particle minecraft:flame ~ ~1 ~ 0.3 0.4 0.3 0.02 12 normal @s
