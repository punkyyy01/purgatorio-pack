# Recompensa individual por descubrir la Ruina de Ceniza. Se ejecuta una sola vez por jugador
# porque cuelga de un logro. Alma pequena, no la recompensa principal del lugar.
tellraw @s {"text":"Algo en este lugar te reconforta. Tu Alma crece.","color":"gray","italic":true}
purgatorio admin alma add @s 8
