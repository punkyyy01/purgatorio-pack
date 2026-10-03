# ¿Qué tendría que vivir un jugador en sus primeras 2–3 horas para notar que esto no es Minecraft con 154 mods?

Pregunta guía de la etapa de pulido. Esto es un **guion de la experiencia objetivo** y un balance honesto de qué parte existe hoy y qué falta.
No es una lista de misiones: es lo que el jugador *debería encontrarse* si explora con curiosidad.

## El guion

| Momento | Qué vive el jugador | ¿Existe hoy? |
|---|---|---|
| **0:00–0:20** Primeros pasos | Juega a Minecraft de siempre (madera, herramientas, refugio). La barra de XP ya es su Alma (vacía). Una frase en el Diario lo orienta sin dirigirlo. | Alma y barra: sí. Frase inicial del Diario: no |
| **0:20–0:45** Ver algo extraño | Desde una colina ve **una columna de humo gris** sobre un cerro lejano, y a veces oye una campana que viene de esa dirección. Nadie le dijo que fuera. | **Sí**: hoguera de señal en la torre + campana lejana (solo cerca de la ruina) |
| **0:45–1:15** La Ruina de Ceniza | Cae ceniza. Entra: título "La Ruina de Ceniza", Alma +8. Un **Acechador** lo avisa ("presencia a tu espalda") y aparece detrás. Pelea con miedo, sube la torre, abre el cofre del altar (el suyo, aunque vaya acompañado). | **Sí** (probado de extremo a extremo) |
| **1:15–1:30** Lo que cambia | Encuentra el **Colmillo de Ceniza**, **Esquirlas de Brasa** y una **nota** que habla de otro sitio. El Diario anota "Un colmillo aún caliente" y "Fragmentos de brasa". Si muere, pierde ~30 % del Alma: aprende que el Alma pesa. | **Sí** |
| **1:30–2:00** Volver y forjar | De vuelta en casa **forja** el Colmillo: gasta Alma y materiales, ve el "antes → después" y nota que su bonus baja pero el arma es permanente. | Forja: sí, pero **solo por comando/menú**; falta una **estación física** |
| **2:00–2:30** Probar el arma | Con Brasa (cada 3.er golpe prende, y le cuesta hambre) afronta un enemigo que antes lo mataba. Empieza a **decidir** cuándo gastar Alma y cuándo guardarla. | Rasgo y coste: sí. Falta un enemigo "siguiente" más duro con razón de ser |
| **2:30–3:00** La pista | La nota lo empuja hacia **otro lugar distinto** (otro tipo de peligro, otro material). Se da cuenta de que el mundo no es una lista: es un sitio para descubrir. | **No**: falta el segundo rincón y su identidad |

## Qué falta, ordenado por impacto en esas 3 horas

1. **Que haya una ruina cerca.** Hoy una ruina cada ~20 chunks (320 bloques) en biomas de bosque/colina/llanura. Probable pero no seguro; en un mundo ya explorado **no aparecen en chunks viejos** (ver riesgo abajo).
2. **Estación de forja física.** Teclear `/purgatorio forja` rompe la inmersión. Un bloque propio (con Polymer) que abra la forja es lo que cierra el bucle "volver a casa y mejorar".
3. **Un segundo rincón con otra identidad** (peligro ambiental en vez de emboscada, otro material). Es lo que convierte "una ruina" en "un mundo con lugares". Debe usar la misma plantilla que La Ruina.
4. **Poda (P3):** que a la hora de caminar se encuentren *estos* lugares y no 400 variantes de lo mismo; y que la dificultad sea del lugar y no de las horas.
5. **Alma con más fuentes y la fatiga** (para que no sea óptimo repetir lo mismo) y **derrame en la tumba**.
6. **Frase inicial y entradas ocultas del Diario**, para orientar sin marcar.

## Riesgo importante para el servidor actual

Las estructuras solo se generan en **chunks nuevos**. En el mundo `purgatorio2` ya se han caminado ~15 km: **la ruina no aparecerá en lo ya explorado**, solo al ir más allá de lo generado.
Opciones (a decidir): (a) esperar a que exploren fuera; (b) colocar a mano una ruina cerca del spawn con `/place` (no registra el logro de descubrimiento, que va por estructura); (c) iniciar una zona/mundo nuevo para la experiencia completa.
Hay que decidirlo antes del despliegue.

## Criterio de éxito (no por cantidad)

Tres señales en una sesión real: (1) alguien se desvía de su ruta por algo que vio en el horizonte; (2) alguien decide gastar o guardar Alma por una razón concreta; (3) alguien le cuenta a otro lo que encontró con detalle.
