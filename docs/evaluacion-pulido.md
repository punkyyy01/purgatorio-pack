# Evaluación del estado actual y prioridades de pulido

Base de diseño: `docs/diseno-fase1.md` (RPG de aventura, exploración, descubrimiento, progresión sin puertas, equipo como
principal fuente de poder, sin relleno). Esta evaluación **no mete contenido**: mide dónde estamos y propone en qué orden mejorar.

Fecha: 2026-10-03. Fuentes: los 58 jars del servidor (metadatos y datos), las estadísticas y logros de los 2 jugadores del mundo
`purgatorio2` (agregados, solo lectura) y la vertical slice de Alma.

## 1. Diagnóstico: por qué se siente "meh"

### Lo que dicen los datos de juego (2 jugadores, ~10 h en total)

| Dato | Valor | Lectura |
|---|---|---|
| Mobs matados | 279, **casi todos vanilla** (zombi 64, magma cube 63, esqueleto 36, piglin 20) | Los enemigos del pack apenas se han visto. Nadie ha matado a un jefe propio. |
| Uso de herramientas | picos ~3.400 usos, espadas ~940 | Se juega a **minar y bajar al Nether**, no a explorar y pelear. |
| Estructuras de mods encontradas | ~4 (una mazmorra, un pozo, algo de Incendium, ruinas) en ~15 km caminados | El contenido de exploración no está llegando al jugador. |
| Equipo propio obtenido | bastones de Ruins and Towers y algo de More Tools | El botín de las estructuras no ha cambiado cómo juegan. |
| Muertes | 13 (12 de un jugador) | Hoy morir no cuesta nada (Graves). Con Alma sí importará: es una oportunidad. |
| Cofres abiertos | 92 | La gente sí abre cofres: el botín es el gancho más fácil de mejorar. |

Conclusión: el juego real sigue siendo **vanilla con extras que casi no se cruzan**. El problema no es que falte contenido; es que lo
que hay no se *encuentra*, no se *recuerda* y no *cambia* cómo se juega.

### El problema estructural: el bucle del diseño está abierto

El diseño pide: explorar → descubrir → combatir → loot → materiales → equipo/build → nuevas posibilidades.
Hoy, con la vertical slice, existen las piezas pero **no están conectadas en el mundo real**:

- El Colmillo de Ceniza solo se consigue con un comando de administrador.
- La Esquirla de Brasa solo la suelta el Acechador, que solo aparece por comando.
- "La Ruina de Ceniza" es una función de pruebas con coordenadas fijas.
- Alma se gana (descubrir, Acechador) y se gasta (forja), pero **en una partida normal no hay nada que descubrir ni nada que forjar**.

Mientras eso no se cierre, Alma es un sistema sin razón para importar. Es la prioridad uno.

## 2. Contenido actual: genérico, redundante o desconectado

Medido en los jars (estructuras, encantamientos, logros). "A verificar" = lo juzgo por metadatos y datos, no por haberlo jugado.

### Estructuras: cantidad sin identidad

| Mod | Estructuras | Problema |
|---|---|---|
| Repurposed Structures | 119 | Variantes de estructuras vanilla (misma estructura, otra madera/bioma). Cero identidad. |
| Dungeons and Taverns | 150 | Muchas casas/tabernas/mazmorras de estilo similar. Genérico por volumen. |
| Terralith | 47 | Aquí sí: los biomas (134) son el escenario y dan siluetas. Mantener. |
| Structory, DeCubed Dungeons, Roguelike Dungeons, RPG Loot (14) | ~45 | Se solapan entre sí y con los anteriores. |
| Incendium (9), Ruins and Towers (17), Deadly Deadly Dungeon (9), Gigantic Squid, Fortress of War | ~40 | **Con identidad**: tienen jefes, mecánica o narrativa propia. |

Son ~400 variantes frente al objetivo del diseño ("30 lugares memorables antes que 300 genéricos"). Recomendación: **desactivar la
mayoría por datapack** (conjuntos de estructuras vacíos, igual que ya hace `purgatorio_tuning`) y dejar un núcleo con identidad. No requiere quitar jars.

### Encantamientos: sprawl de números

~100 encantamientos de jugador repartidos en More Enchants (35), Enchants Plus (23), Dungeons and Taverns (18), Dragonkind (13), Advanced Archery (11),
Fortress of War (~6). Muchos son "+X a una estadística" (alcance, velocidad, resistencia...). Contradicen el principio *"verbo con costo, no +10 %"* y
compiten con los rasgos propios. Además, el Alma ya deja inutilizados la mesa de encantamientos y el yunque. Es una **decisión de diseño pendiente**
(sección 4, P0).

### Otros solapes y ruido

- **Dificultad por tiempo** (Hostile Mobs Improve Over Time): el peligro crece con las horas jugadas, no con el lugar. Choca con "peligro por región" y con
  la idea de que se puede entrar a una zona letal pronto. A verificar si conviene sustituirlo por peligro por zona.
- **Pestañas de logros**: 5 de vanilla + 6 de mods (Farmer's Delight, Incendium, Lootr, Repurposed, Hostile Mobs, Dragonkind) + la nuestra: el Diario sería 1 de 12.
- **Mods de ajuste suelto** (Spiders 2.0, Flower Mimics, Naturally Charged Creepers, Giant Spawn, Harder Wardens, More Mobs): dificultad "porque sí", sin conexión.
  Algunos pueden ser ingredientes de peligro con sentido; otros sobran.
- **Estética/QoL que sí aportan al ambiente**: Farmer's Delight (momentos tranquilos), Lootr (cofres por jugador, ya pensado para grupo),
  Universal Graves, Down But Not Out, sswaystones, Better Healthbar, TSA Decorations (base propia). Mantener.
- **Rendimiento** (C2ME, Lithium, ScalableLux, FerriteCore, Krypton, ServerCore, spark): se quedan.

### Nuestro núcleo

| Pieza | Estado | Carencia |
|---|---|---|
| Alma (barra, bonus, muerte, gasto) | Funciona y validado | Sin fuentes reales, sin derrame en tumba, sin fatiga; sin sonido ni feedback propio |
| Forja | Funciona | Un solo objeto mejorable |
| Colmillo de Ceniza | Funciona, ya con nombre y tooltip | No se consigue jugando |
| Acechador | Funciona | No aparece en el mundo; un solo enemigo |
| Menús/mensajes | Pulidos al mínimo | Sin sonidos, sin títulos, sin momentos |

## 3. Qué haría que Purgatorio "se sienta" como un juego

Tres ideas, de las que salen las prioridades:

1. **Cerrar el bucle una vez, con calidad, antes de ampliarlo.** Un solo rincón completo vale más que diez estructuras sueltas.
2. **Dar identidad por la *selección*, no por la adición.** Quitar el ruido hace que lo que queda se note.
3. **Feedback.** Hoy casi todo sucede en silencio. Sonidos, títulos y pequeños momentos son baratos y cambian mucho la sensación.

## 4. Prioridades (por impacto en la experiencia)

Cada una responde a las tres preguntas del diseño. Esfuerzo: S (horas), M (un día), L (varios días).

### P0 · Decidir qué pasa con el XP vanilla (bloquea el despliegue) · S–M
Con Alma como XP real, quedan sin uso encantar, yunque y Mending, y el XP de hornos/comercio/pesca. Opciones:
- **A (recomendada para empezar):** encantar solo con lapis (sin niveles), yunque sin coste de niveles, y que Mending siga reparando con orbes que **no** dan Alma. Mantiene el juego conocido y el diseño de Alma intacto.
- **B:** deshabilitarlos y sustituirlos por rasgos de forja. Más fiel al diseño, pero rompe ~100 encantamientos de 6 mods y es un cambio fuerte para los jugadores.
Aporta: que el pack se pueda desplegar sin quitar funciones básicas. Existe porque hoy es una regresión.

### P1 · Cerrar el bucle en un primer rincón: "La Ruina de Ceniza" · M–L
Convertir lo que ya existe en un lugar real y memorable (detalle en la sección 5). Aporta: la primera razón para ganar y gastar Alma, y el primer "vi algo raro en el horizonte".
Existe porque sin esto lo demás es un tech demo. Se recuerda por: silueta, sonido, un enemigo con aviso y un objeto que cambia cómo juegas.

### P2 · Capa de feedback · S–M
Sonidos y títulos en los momentos clave: descubrir un lugar, ganar/perder Alma, rasgo activado, aviso del Acechador, mejora de forja, muerte.
Aporta mucho "feel" por poco coste. Existe porque hoy el juego es mudo. Se recuerda por la sensación, no por un número.

### P3 · Curar el contenido genérico · M
Desactivar por datapack ~70–80 % de las estructuras genéricas (Repurposed, buena parte de D&T y los solapes), recortar las pestañas de logros ajenas y
decidir el destino de la dificultad por tiempo. Aporta: que lo que quede se encuentre y se note, y que los 30 lugares memorables del diseño sean visibles.
Existe porque el volumen diluye la identidad. Se hace con **datapack reversible**, sin borrar jars, y con la lista revisada contigo.

### P4 · Botín y objetos con identidad · M
Un pequeño catálogo de **objetos firmados** (6–8, no 80): cada uno con nombre, texto de ambiente, rasgo (verbo + costo) y un lugar donde se consigue. Reutilizar equipo
existente de los mods como base con un rasgo propio. Re-temar el botín de los pocos lugares que se queden. Aporta builds con sentido; existe porque el equipo es la fuente de poder.

### P5 · Alma más profunda · M
Dentro del diseño ya aprobado: Alma derramada en la tumba (recuperable), fatiga anti-granja y filtro de origen, fuentes de Alma por descubrimiento
con más variedad (pequeñas por notas, secretos), y feedback. Aporta riesgo y decisión reales. Sin nuevas mecánicas fuera del documento.

### P6 · Ganchos de exploración · M
Que un lugar llame la atención sin marcador: columna de luz o brasas visibles de lejos, sonido de campana a distancia, notas y libros con pistas cruzadas,
un viajero o espíritu errante. Aporta "qué es eso?"; existe porque el principio nº 1 es explorar aunque no sepas qué hay.

### P7 · Más enemigos con una pregunta · M
Dos o tres más (no veinte), cada uno con su "¿cómo me protejo de...?" y su aviso, reutilizando mobs de los mods con comportamiento Java. Aporta variedad de combate.

### P8 · Menús y Diario · S–M
Pulido visual del menú, pestaña del Diario con entradas ocultas que se revelan al descubrir, y un fondo propio vía resource pack. Es lo último: sin contenido que mostrar, un menú bonito no ayuda.

## 5. Ejemplo: el primer rincón, "La Ruina de Ceniza"

Responde por qué existe, qué aporta y qué se recuerda:

- **Qué ves desde lejos:** una columna de brasas y un cielo ceniciento sobre una colina (siluetas y partículas, sin marcador).
- **Qué encuentras:** una ruina pequeña con una capilla caída y una linterna de almas que aún arde. Una nota ambigua en un cadáver.
- **Qué pasa dentro:** el Acechador de Ceniza (ya construido): avisa y aparece a tu espalda. Se juega mejor con otra persona vigilando (cooperativo sin obligarlo).
- **Qué sacas:** Esquirlas de Brasa (material con uso real en la forja) y, en un cofre por jugador (Lootr), el Colmillo de Ceniza o su equivalente. +8 de Alma por descubrirla.
- **Cómo cambia tu juego:** con el Colmillo y la forja ya puedes enfrentarte a lo siguiente que antes te mataba; la nota apunta a otro sitio.
- **Por qué se recuerda:** es el primer lugar con silueta, sonido, un enemigo con aviso y un objeto con rasgo, todo conectado.

Todo con piezas ya validadas. Lo nuevo sería: la estructura real de worldgen, su aparición en el mundo, el tabla de botín del cofre y el feedback.

## 6. Decisiones que necesito de ti

1. **P0:** ¿opción A (lapis + yunque sin niveles + Mending), opción B, u otra?
2. ¿De acuerdo con empezar por **P1 + P2** (rincón completo con feedback) y dejar P3 para después de verlo jugando?
3. Para **P3**: ¿me dejas proponer una lista concreta de estructuras/encantamientos a desactivar por datapack (reversible), para que la revises antes de aplicar nada?
4. ¿Mantenemos o sustituimos la dificultad por tiempo (Hostile Mobs Improve Over Time)?

## 7. Cómo sabremos que deja de ser "meh"

No por cantidad. Tres señales de una sesión de juego real: (1) alguien se desvía de su ruta por algo que vio en el horizonte; (2) alguien decide gastar o guardar Alma
con una razón concreta; (3) alguien cuenta a otro "lo que encontré" con detalle. Las medimos jugando, igual que se hizo con la vertical slice.
