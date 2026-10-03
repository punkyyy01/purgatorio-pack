# Purgatorio — Diseño Fase 1 (borrador v2)

Estado: **solo diseño, nada implementado.** Todas las cifras y nombres son hipótesis para pensar y probar, no objetivos ni contenido definitivo.

Cambios de la v2: **Alma pasa a ser la experiencia del jugador** (sistema central, sección 8). Se eliminan las "almas equipables en ranuras". La palabra *Alma* ya solo significa este recurso.

---

## 0. Filosofía

> No queremos un juego que nos diga constantemente qué hacer. Queremos construir un mundo tan bien diseñado que el jugador quiera descubrir qué hacer.

Principios (en orden de importancia):

1. **Explorar es divertido aunque no sepas qué vas a encontrar.** Sin marcadores de "POI aquí". Una aventura puede empezar porque viste algo raro en el horizonte.
2. **Progresión sin puertas.** El poder crece por lo que haces. Ningún jefe ni requisito "abre" nada. La dificultad es la única barrera, y se puede intentar antes de tiempo.
3. **Pocos sistemas, entendibles, con profundidad.** El equipo, las builds y la exploración pesan mucho más que cualquier número.
4. **Cada cosa tiene una razón de existir.** Un material sirve para algo concreto. Un enemigo cambia cómo luchas. Un lugar tiene una idea. Si no, no entra.
5. **Menos contenido con identidad antes que mucho de relleno.**
6. **La guía orienta, no ordena.** Diario, rumores, pistas. Nada de cadena obligatoria de quests.
7. **Funciona en grupo y solo.**
8. **Tono:** aventura, descubrimiento y calma, con ruinas, espíritus y corrupción. No "Minecraft edgy".

---

## 1. Una aventura típica (ejemplo, no contenido fijo)

1. Desde una colina ves una **columna de luz tenue** entre los árboles, a lo lejos. De noche se nota más.
2. Al acercarte el terreno cambia: árboles secos, ceniza en el suelo, una **campana** lejana. Junto a un cadáver hay una **nota** con algo ambiguo sobre "lo que duerme bajo el pozo".
3. Hay un pozo. Dentro, una cripta pequeña con una mecánica propia (por ejemplo, la oscuridad te hiere y las antorchas atraen enemigos).
4. En el fondo: un material que no habías visto y un **arma con un rasgo raro**.
5. Con eso cambias cómo juegas. El pantano que te mataba en dos golpes ahora se puede cruzar.
6. Tu **Alma** subió por haber descubierto el lugar. El Diario anotó el sitio, el enemigo y una pista sobre otro rincón. Nadie te dijo que fueras.

El jugador nunca vio un marcador. Lo movió la curiosidad.

---

## 2. El mundo: regiones y tipos de peligro

El peligro no es solo "más daño". Tipos que pueden combinarse:

| Tipo | Cómo se siente | Qué pone a prueba |
|---|---|---|
| **Bruto** | Enemigos muy fuertes. | Equipo y habilidad en combate. |
| **Ambiental** | Ceniza, frío, niebla, corrupción que te desgasta. | Preparación: resistencias, comida, equipo. |
| **Emboscada** | Cuevas y bosques densos, enemigos que acechan. | Atención, luz, sigilo. |
| **Laberíntico** | Ruinas y cuevas verticales donde es fácil perderse. | Orientación, mapa, paciencia. |
| **Temporal** | La noche, procesiones de espíritus, tormentas. | Elegir cuándo ir. |
| **Colectivo** | Jefes mundiales y amenazas grandes. | Cooperación. |

**Una zona letal** es una región donde el peligro bruto o ambiental está muy por encima. El jugador puede entrar, morir y aprender que todavía no está listo. Pero tiene mejores drops y materiales raros, así que la tentación existe. Morir ahí cuesta Alma (sección 8), lo que da peso real a la decisión de entrar.

**Cómo se lee el peligro sin números:** cambia el paisaje, el sonido y los mobs. Opcional: un aviso diegético al cruzar ("el aire se vuelve pesado").

**Regiones:** sin cantidad fija. Aprovechamos las bases que ya existen:
- **Overworld** (Terralith): zonas de tono distinto, desde un inicio calmo hasta tierras hostiles.
- **Nether** (Incendium, Fortress of War) y **End** (Nullscape, Dragonkind): cada dimensión ya tiene identidad natural.
- Cada región debe tener **un tipo de peligro dominante y una sensación propia**. Se definen mirando el mundo real.

---

## 3. Exploración sin marcadores

Caja de herramientas de "ganchos de curiosidad". Se usan pocos, con intención:

- **Siluetas y verticalidad:** estructuras visibles desde lejos. El terreno oculta y revela para dar ganas de rodear.
- **Luz, partículas y humo:** columnas de luz, brasas, niebla fuera de lugar.
- **Sonido:** campanas, cánticos o ruidos lejanos que indican una dirección sin señalar un punto.
- **Rastros:** caminos desgastados, ofrendas, huellas, restos de expediciones fallidas.
- **Anomalías:** un bloque fuera de lugar, un árbol distinto, un círculo de piedras.
- **Notas y libros:** pistas **incompletas y cruzadas** (una nota en A habla de algo cerca de B).
- **NPCs o figuras:** viajeros y espíritus errantes con diálogo ambiguo, sin cadena de misión.
- **Eventos:** procesiones nocturnas, tormentas de ceniza, un jefe errante que aparece raras veces.
- **Secretos por capas:** el lugar tiene algo visible y algo escondido para quien investiga.

**Regla del pago:** curiosear siempre da algo, aunque sea pequeño (una anécdota, un material común, una nota, un poco de Alma). Un gancho que lleva a un lugar vacío castiga la curiosidad.

**El Diario** solo anota lo que **ya descubriste**. No muestra lugares que no has visto. Puede haber entradas de rumor ("???") que aparecen tras encontrar una pista.

---

## 4. Lugares: por intención, no por cantidad

Cada lugar responde a tres preguntas: **¿qué me hace querer entrar? ¿qué pasa dentro (una idea)? ¿qué me da?** Un lugar = una idea.

| Tipo | Duración | Para qué sirve |
|---|---|---|
| **Hallazgo** | Minutos | Una pista, un objeto, una anécdota. Alimenta la curiosidad. |
| **Escondrijo** | 5-10 min | Material raro o un item con historia. |
| **Cripta** | 20-40 min | Mecánica propia, enemigos del lugar, loot con propósito. |
| **Set piece / jefe** | Variable | Un hito con drops únicos que cambian la build. |
| **Secreto** | Variable | Escondido, premia investigar. |
| **Refugio** | Calma | Descanso, cocina, forja, ambiente. No todo es lucha. |

**Cantidad:** las que valga la pena.

**Reutilizar vs. construir:** las estructuras de los mods actuales se pueden **reajustar por datapack** (loot, enemigos, rareza). Los lugares verdaderamente únicos (jefes, secretos) hay que **construirlos a mano**: es probablemente la mayor carga de trabajo del proyecto. Para evitar solapamientos hay que podar mods que den estructuras genéricas.

---

## 5. Enemigos y jefes

**Regla:** un enemigo nuevo debe **cambiar cómo luchas**. Si solo cambia la vida o la textura, no entra.

Cada enemigo plantea una **pregunta al jugador**, con una respuesta que la preparación premia:

- *Emboscador:* ¿cómo me protejo de los ataques por la espalda?
- *Asedio a distancia:* ¿cómo cierro la distancia o me cubro?
- *Bloqueador:* ¿cómo lo rompo?
- *Invocador:* ¿a quién ataco primero?
- *Drenador:* ¿cómo evito que me agote en un combate largo?

Señales claras (telegraph) antes de los golpes fuertes.

**Jefes:**
- Son **hitos**, no llaves. Se pueden pelear en cualquier orden y no abren nada.
- **La recompensa es su contenido único**: armas, armaduras, materiales, objetos especiales, piezas para builds concretas. El jugador debe pensar *"quiero matarlo para conseguir SU objeto"*, no *"necesito su experiencia"*.
- **No dan Alma en cantidad.** Un jefe puede dar un pequeño bonus de Alma la primera vez (por la hazaña), pero no es su recompensa principal.
- Fases y mecánicas distintas.
- Su vida y daño **escalan con el número de jugadores cercanos**.
- Pueden estar en un lugar descubierto o aparecer como evento. Algún jefe menor puede ser **errante**.

**Ya tenemos** jefes y mobs de Dragonkind, Fortress of War, Gigantic Squid, Warden, Giant, Tom's Mobs, Illager Expansion, etc. El trabajo es decidir cuáles encajan y cuáles se podan.

---

## 6. Loot y materiales

- **Loot por lugar y peligro:** lo que cae depende de dónde estás y qué tan peligroso es. Cofres individuales por jugador (Lootr, ya instalado).
- **Los materiales no pertenecen a regiones por obligación.** Valen porque **sirven para algo concreto**. Tres clases:
  - **Comunes:** los de siempre.
  - **Raros:** de enemigos, lugares o fenómenos específicos.
  - **De condición:** solo caen bajo ciertas circunstancias (por ejemplo, un enemigo durante una tormenta de ceniza).
- **Regla:** un material no se crea hasta que tenga **al menos un uso concreto** (receta, mejora, ritual).
- **Recetas:** pueden descubrirse probando o encontrándolas. Los planos son opcionales y no deben ser requisito para lo básico (si no, serían una puerta).

---

## 7. Equipo y builds: la fuente principal de poder

**El jugador solo debe aprender cuatro ideas:**

1. **Equipo** (con 0-2 rasgos).
2. **Alma** (una barra, un número).
3. **Materiales** (cada uno con su uso).
4. **Diario** (qué descubrió).

**Equipo:** item base + materiales + Alma para mejorarlo (sección 8) + 0-2 rasgos con **compensación** (no multiplicadores planos). Cada rasgo es un verbo ("te deja hacer X") con un costo. Una sola estación de forja y una forma sencilla de reciclar lo que sobra.

**Habilidades y reliquias:** si más adelante encontramos una forma adecuada de implementarlas, serán **objetos o rasgos**, no un sistema aparte. Se deja pendiente. Para no confundir con Alma, se llamarían *reliquias*.

**Regla de proporción:** el salto entre dos tiers de equipo debe pesar **mucho más** que el 10% máximo que da el Alma.

---

## 8. Alma: sistema central

### 8.1 Qué es

**Alma = experiencia + una pequeña fuente de poder + riesgo + recurso para mejorar equipo.**

Es literalmente la experiencia del jugador, y se muestra en la barra verde de Minecraft. En una frase: **tener Alma te hace un poco más fuerte, pero te arriesga; gastarla te debilita un poco hoy y te fortalece de forma permanente**.

### 8.2 Reglas base

| Regla | Valor (hipótesis) |
|---|---|
| Rango | 0 a 100 (máximo absoluto) |
| Bonus de daño | Hasta **+10%** con 100 de Alma (0,1% por punto) |
| Al morir | Pierdes **~30% de tu Alma actual** (100→70→49→34) |
| Gasto | Mejorar y forjar equipo cuesta Alma |
| Visibilidad | Privada: no se muestra a otros jugadores |

La pérdida porcentual evita la espiral: cuanta menos Alma tienes, menos pierdes.

### 8.3 La curva del bonus

| Curva | Efecto | Veredicto |
|---|---|---|
| **Lineal** (0,1% por punto) | Simple de entender: la barra es el bonus. | **Recomendada** para empezar |
| **Cóncava** (sube rápido al inicio) | Los primeros puntos pesan más. Morir duele menos arriba. | Opción si queremos que el inicio se sienta bien |
| **Convexa** (premia estar cerca de 100) | Más tensión, más riesgo al llegar arriba. | Riesgo de frustrar |
| **Escalonada** (saltos a 25/50/75/100) | Se entiende por hitos. | Pierde sutileza |

Se prueba con la lineal y se ajusta tras jugar. Pendiente de decidir qué daño cuenta: lo ideal es **todo el daño que inflige el jugador** (cuerpo a cuerpo, distancia y habilidades). Técnicamente, el daño a distancia puede requerir más trabajo que el cuerpo a cuerpo (ver 8.10).

### 8.4 Cómo se gana Alma (no solo matando)

| Fuente | Peso | Se puede repetir |
|---|---|---|
| **Descubrir un lugar por primera vez** | Medio-alto | No (una vez por jugador) |
| **Completar un lugar por primera vez** | Medio | No |
| **Primera victoria sobre un jefe** | Medio-bajo | No |
| **Restaurar o liberar algo** (Redención) | Medio | Según el evento |
| **Notas, lore y secretos** | Bajo | No |
| **Eventos y encargos** (bounties) | Medio | Sí, con rotación |
| **Matar enemigos** | Bajo | Sí, con fatiga (8.5) |

**Problema que hay que resolver:** las fuentes de una sola vez son finitas, pero morir y gastar Alma la drenan. Hace falta **ingreso renovable**, pequeño pero constante: matar con fatiga, eventos, bounties y lugares que se pueden repetir.

**Primera hipótesis de ritmo (a probar):** una hora de exploración normal debería rellenar algo así como 15-30 de Alma.

### 8.5 Cómo evitar que farmear sea lo óptimo

El tope de 100 ya limita el bonus, pero el riesgo real es **farmear Alma para gastarla en la forja**. Medidas:

1. **El Alma nunca es lo único que cuesta mejorar.** La forja también pide materiales, que hay que encontrar. El Alma es un costo, no la llave.
2. **Filtro de origen:** los mobs de spawners, granjas, nombrados, atrapados o muertos por trampa no dan Alma. Solo cuenta lo que mata el jugador.
3. **Fatiga por tipo:** matar el mismo tipo de enemigo repetidamente baja su valor y se recupera con el tiempo.
4. **El mejor ingreso está en lo nuevo y peligroso**, no en repetir lo seguro.
5. **Desbordamiento:** a 100 no se gana más. Eso empuja a gastar antes de seguir.
6. **Opción más ambiciosa** (a evaluar si hace falta): cada zona tiene una "reserva de almas" que se agota al matar y se rellena despacio.

### 8.6 La muerte: importante, pero no frustrante

- Pierdes ~30% de tu Alma actual. El inventario **no** es el castigo: Graves lo guarda.
- **Alma derramada:** una parte de lo perdido queda en tu tumba y se puede recuperar. Opciones:

| Opción | Efecto |
|---|---|
| **A. Sin recuperación** | Más simple y crudo. Máximo castigo. |
| **B. Recuperas la mitad de lo perdido** | Pérdida neta ~15%. Hay objetivo claro tras morir. **Recomendada para empezar.** |
| **C. Recuperas todo si llegas a tiempo** | Estilo souls. Más tensión; puede frustrar en zonas lejanas o letales. |

- Como Graves ya obliga a volver a la tumba por los items, el Alma derramada **va en el mismo viaje**: una sola vuelta, no dos.
- **Revivir (Down But Not Out):** si un compañero te levanta, **no cuenta como muerte** y no pierdes Alma. Es un incentivo cooperativo directo.
- **Protecciones suaves:** con poca Alma casi no se pierde nada. Un refugio puede ser zona segura. Mensajes de muerte claros ("tu alma se derrama...").

### 8.7 Alma y forja

Mejorar equipo cuesta **Alma + materiales**. La idea es que gastar sea una decisión real:

- **Equivalencia visible:** 10 de Alma = 1% de daño temporal. Gastar 30 significa renunciar a un 3% hoy a cambio de una mejora permanente. **La mejora debe valer más que ese 3%**, o nadie gastará.
- **Ejemplo de escala (hipótesis):**

| Mejora | Alma | Materiales |
|---|---|---|
| I | 10 | Comunes |
| II | 20 | Raros |
| III | 30 | Un material único (por ejemplo, de un jefe) |

- **Tope por item (60 en el ejemplo):** el Alma invertida está acotada.
- **Invertir protege del riesgo:** el Alma gastada ya no se pierde al morir. Es una decisión estratégica interesante: gastar antes de entrar a una zona letal.
- Las mejoras son permanentes. Se puede decidir más adelante si hay una manera de rehacerlas.

### 8.8 Comunicación visual

- **La barra verde de Minecraft y su número = tu Alma (0-100).** Un solo número, sin pantalla extra.
- **Ganancias:** sonido suave, una línea breve sobre la barra con "+Alma" y partículas discretas.
- **Pérdidas:** un efecto en pantalla y mensaje al morir.
- **Costos:** el texto de los objetos y de la forja muestra el costo en Alma.
- **Primer rato:** un par de pistas al principio explican "esta barra es tu Alma".
- **Privacidad:** no se muestra el Alma de otros jugadores (ni en la lista de jugadores), para evitar el "yo tengo 57 y tú 42".
- **Opcional:** el resource pack automático puede retocar el aspecto de la barra. Se deja el verde por defecto, para mantener la conexión con la barra original.

### 8.9 Guardarraíles (para no caer en un MMO)

- Máximo **100** y máximo **+10%**. Nunca más.
- **Un solo número.** Sin atributos que repartir y sin árbol de habilidades.
- **El poder real viene del equipo, las builds y lo descubierto.**
- **Ninguna puerta basada en Alma.** Nada exige tener X de Alma para entrar o equipar.
- **Casi todos los jugadores estarán entre 40 y 100**, así que la diferencia entre ellos es pequeña. El Alma es más una "reserva a administrar" que un nivel que ostentar.

### 8.10 Cuestiones técnicas y conflictos (a validar, nada verificado)

- **Alma vs. XP vanilla.** La XP de vanilla se usa en mesas de encantamiento, yunque, mending y botellas, y se gana de muchas fuentes farmeables (minar, fundir, criar, pescar, comerciar). Propongo que **el Alma sea un valor propio, mostrado en la barra de XP**, y que las orbes vanilla no den niveles.
- **Consecuencia:** hay que decidir qué pasa con **encantamientos y yunque**, que ahora gastarían Alma. Opciones: que usen solo materiales (lapis, etc.), o integrarlos en la forja. Esto afecta a mods como Enchants Plus y More Enchants, que dependen de la mesa de encantamientos.
- **Muerte vanilla:** hay que reemplazar el comportamiento normal (soltar XP) por la regla del 30%.
- **Bonus de daño:** un modificador de atributo cubre bien el cuerpo a cuerpo. El daño a distancia y de habilidades probablemente requiera un gancho propio.
- **Alma en la tumba:** hay que ver cómo integrar el Alma derramada con Universal Graves.
- **Todo esto se prueba antes de comprometerlo**, en un servidor de test.

---

## 9. Progresión: capas, sin puertas

El jugador progresa en cuatro capas, ninguna bloquea a otra:

1. **Conocimiento:** saber qué evitar, qué buscar y cómo luchar. Es la más importante y no se puede bloquear.
2. **Equipo:** materiales, drops únicos, forja. Es la principal fuente de poder.
3. **Alma:** una pequeña ventaja por conservarla y un recurso para mejorar equipo.
4. **Mundo restaurado (Redención):** lugares que el jugador recupera y que cambian (un refugio con forja, un puesto con comerciante). Es una consecuencia, no un requisito.

La curva de poder del jugador se mide contra la curva de peligro de las zonas. El jugador elige a dónde ir.

**Redención y lore:** los habitantes del Purgatorio (espíritus, penitentes...) tienen nombre propio por definir, distinto de "Alma", para no tener dos significados.

---

## 10. Guía mínima

- **Primeros 30 minutos:** pocas pistas fuertes (un refugio cercano, una nota inicial, una explicación de la barra de Alma). Después, libertad.
- **Diario:** descubrimientos, bestiario, lore. Sin porcentajes ni desbloqueos.
- **Rumores:** NPCs, notas y libros que insinúan, no ordenan.
- **Mapas:** el mapa se llena a medida que exploran.
- **Quests ligeras (opcionales):** bounties y encargos ocasionales, no encadenadas.

---

## 11. Cooperativo

- Cofres por jugador (Lootr) y revivir (Down But Not Out): ya instalados. **Ser revivido evita perder Alma.**
- Escalado de jefes por número de jugadores.
- Algunas actividades **mucho más interesantes en grupo** (un jefe mundial, una cripta con roles). Siempre viable en solitario, con más dificultad.

---

## 12. Sistemas técnicos necesarios (resumen)

| Sistema | Notas |
|---|---|
| **Alma** (valor propio, barra de XP, pérdida al morir, gasto en forja, bonus de daño) | Probablemente núcleo propio. Es la pieza más importante. |
| Peligro por región y escalado de mobs | Datapack o núcleo propio. Hay datapacks de escalado por distancia, sin verificar en 26.3. |
| Rasgos y forja | Items con Filament/Polymer + núcleo. |
| Diario | Logros por datapack. Barato. |
| Comportamiento de enemigos y jefes | Mods actuales y datapacks. **Probar la profundidad real.** |
| Menú | Núcleo propio. |
| Escalado cooperativo | Núcleo propio. |

**Incertidumbres a validar en un servidor de test:**
1. Mostrar el Alma en la barra de XP y anular la XP vanilla sin romper otros mods.
2. Aplicar el bonus de daño (cuerpo a cuerpo y distancia).
3. Perder el 30% al morir e integrar el Alma derramada en la tumba (Graves).
4. Escalado de mobs por región.
5. Comportamiento de mobs custom (Filament, Tom's Mobs).
6. NPCs, eventos y figuras errantes solo-servidor.

---

## 13. Preguntas abiertas

1. **Recuperación de Alma:** ¿opción A (sin), B (la mitad) o C (todo si llegas)?
2. **Curva del bonus:** ¿lineal para empezar?
3. **Encantamientos y yunque:** ¿usan solo materiales, o los absorbe la forja?
4. **PvP:** ¿sí o no?
5. **Dificultad:** ¿tipo souls (castiga) o más amable?
6. **NPCs o figuras con diálogo:** dan carácter, pero cuestan trabajo.
7. **Nombre de los habitantes del Purgatorio** (para no confundirlos con Alma).
