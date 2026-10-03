# Primera vez que un jugador derrota a un Acechador de Ceniza (lo otorga el codigo al matarlo).
title @s times 8 60 25
title @s subtitle {"text":"Ya no vigila a nadie.","color":"gray","italic":true}
title @s title {"text":"Ceniza al viento","color":"gold"}
playsound minecraft:entity.wither.break_block player @s ~ ~ ~ 0.5 1.4
particle minecraft:ash ~ ~1 ~ 0.6 0.8 0.6 0.05 40 normal @s
