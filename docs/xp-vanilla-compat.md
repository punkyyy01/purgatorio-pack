# P0 · XP vanilla: qué necesita cada sistema y cómo se mantiene sin que dé Alma

Decisión de diseño de partida: el Alma es la experiencia del jugador; el XP vanilla **no** da Alma ni niveles reales. Esto describe
qué sistemas dependían de él, qué necesita cada uno (leído en el código de 26.3, no supuesto) y cómo se resuelve.

## Qué necesita cada sistema

| Sistema | Qué necesita realmente | Dónde se comprueba |
|---|---|---|
| **Mending** (efecto `repair_with_xp`) | Que la **orbe de XP exista y se recoja**. Repara con `ExperienceOrb.repairPlayerItems` y *después* da el sobrante como XP. No necesita nivel. | Servidor (`ExperienceOrb.playerTouch`) |
| **Mesa de encantamientos** | Lapis (1–3) + nivel del jugador ≥ coste (1–3) y ≥ requisito (1–30). Consume el coste en niveles. | **Servidor y cliente**: `EnchantmentScreen.mouseClicked` ejecuta `clickMenuButton` con el nivel del cliente y solo envía el clic si pasa |
| **Yunque** | Nivel ≥ coste y coste > 0. Gasta niveles al tomar el resultado. Rechaza costes ≥ 40 (trabajo previo acumulado). | **Servidor y cliente** (`Slot.mayPickup`) |
| Piedra de afilar, hornos, comercio, pesca, cría, botellas | Solo **producen** orbes. No consumen. | — |

**Hallazgo clave:** el nivel que muestra el cliente es el Alma (la barra de XP). Como el cliente decide por sí mismo si envía el clic de
encantar o permite tomar el resultado del yunque, **un arreglo solo de servidor no es suficiente**.

## Solución

1. **Mending: las orbes solo existen si alguien cerca las puede usar** (`MendingGate`). Se crean únicamente si hay un jugador a ≤16 bloques con un
   objeto dañado con `repair_with_xp`. Recoger la orbe repara y el sobrante se descarta (`giveExperiencePoints` sigue anulado). Así Mending funciona
   exactamente como en vanilla, no hay orbes inútiles ni lag en granjas, y el XP **nunca** produce Alma.
2. **Encantar y yunque: nivel temporal mientras el menú está abierto** (`MenuLevelBridge`). Con una mesa o un yunque abiertos el jugador tiene
   nivel 39 (suficiente para cualquier requisito legal) tanto en el servidor como en lo que ve el cliente. Al cerrar, el nivel real vuelve a 0 y la
   barra a su Alma. Consecuencias deliberadas:
   - **Encantar cuesta solo lapis** (más la altura de estanterías que ya fija el requisito).
   - **El yunque no cuesta niveles.** Se mantiene el límite vanilla de trabajo previo (coste ≥ 40 = "demasiado caro").
   - El nivel temporal no se guarda: salir con el menú abierto no deja XP (probado).
3. La barra de XP mostrará brevemente "39" mientras uses la mesa o el yunque. Es la única vez que no muestra Alma.

**Nota de balance:** sin el coste en niveles, encantar es más barato que en vanilla. Si resulta demasiado fácil se puede subir el coste en lapis
sin tocar nada de lo anterior.

## Mods del pack que tocan el XP (escaneo de bytecode de los 58 jars)

| Mod | Qué hace con el XP | Estado |
|---|---|---|
| Universal Graves | Guarda el XP de la tumba (100 %). Costes de abrir/teleportar: gratis / creativo. | Compatible: con XP real 0 no guarda nada. Sin conflicto. |
| sswaystones | `cross_dimension_xp_cost: 1` (viajar entre dimensiones cuesta 1 nivel). | **Cambio de configuración necesario**: ponerlo en `0`. Si no, ese viaje queda bloqueado (nivel real 0). |
| Illager Expansion — Mesa de imbuición | Exige y gasta `experienceLevel` directamente en su propia pantalla. | **No funciona** con XP real 0 (no está puenteada a propósito: sería imbuición gratis). Queda para el rediseño de forja. |
| Collective | Mixin de eventos de yunque + utilidades de XP (biblioteca de otros mods). | Compatible (prueba con el pack completo). |
| ServerCore | Optimiza orbes (activación/fusión). | Compatible; arranque y pruebas con el pack completo OK. |
| Farmer's Delight, Filament, Flower Mimics | Solo generan orbes. | Funcionan: las orbes ahora aparecen si alguien las necesita para Mending. |

## Qué está probado

`xp-compat` (35 comprobaciones, también con los 154 mods): sin objeto dañado no hay orbes; Mending sin dañar o dañado sin Mending no atrae; con un pico dañado con Mending la orbe
existe, repara y no da Alma ni XP; orbes lejos no se crean; mesa: el servidor **rechaza** sin el puente (control), el cliente recibe nivel 39, 1 lapis no
basta para la opción de 3, con 3 lapis encanta, se consumen los lapis, el Alma no cambia y al cerrar todo vuelve; yunque: resultado no recogible sin puente,
sí con él, no cobra niveles ni Alma; salir con el menú abierto no guarda XP.

## Qué NO está probado (requiere cliente real)
Que el cliente envíe realmente el clic de encantar y permita tomar el resultado del yunque con el nivel temporal (se basa en el código del cliente
leído, pero hay que verlo). Ver `docs/pruebas-manuales.md`, sección 8.
