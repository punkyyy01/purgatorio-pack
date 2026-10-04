# Guía del Purgatorio (inspector de objetos)

Mod **independiente** (`purgatorio_guia`, jar `purgatorio-guia-<version>.jar`). No usa nada de `purgatorio-core`, así que
se puede desplegar en el servidor real aunque allí no esté el núcleo. Es 100 % servidor: los clientes vanilla no
necesitan nada (ni resource pack: el libro es un libro de conocimiento con nombre propio).

Código en `core/src/guia/` (conjunto de fuentes propio dentro del proyecto Gradle de `core/`; su classpath **no**
incluye `sourceSets.main.output`, por lo que no puede referenciar clases del núcleo). Pruebas: `GuiaSuite` en el
mod de pruebas (`tools/run-integration-tests.sh guia`).

## Qué hace

- **El libro:** cada jugador lleva siempre exactamente uno, por defecto en el último hueco del inventario (35).
  Clic derecho o `/guia` abre el inspector. No se puede tirar ni guardar:
  - Mixin en `AbstractContainerMenu.clicked` (`GuideBookGuard`): bloquea Mayús+clic, Q/Ctrl+Q, soltarlo fuera de
    la ventana, dejarlo en un hueco que no sea del inventario y las teclas numéricas hacia contenedores ajenos.
    Moverlo **dentro** de su inventario es libre.
  - Un libro que llegue a ser objeto suelto se elimina; antes de morir se retira (no cae ni entra en tumbas).
  - Cada 10 ticks `GuideBook.ensure` repone el libro si falta y quita duplicados (creativo, comandos, otros mods).
  - Si el inventario está lleno no se desplaza nada: se reintenta (y `/guia` funciona sin libro).
- **El inspector** (cofre doble): abajo se refleja el inventario del jugador; un clic en un objeto lo elige.
  El objeto **nunca se mueve**: el inspector trabaja con una copia, así que no hay forma de perderlo.
  - Con encantamientos (puestos o guardados en un libro): una entrada por encantamiento con descripción, efecto en
    el nivel actual, nivel máximo e incompatibilidades.
  - Efectos (fase 3): pociones (normales, arrojadizas, persistentes y flechas con efecto), estofado sospechoso, botella
    ominosa y cualquier comida o consumible que aplique efectos (manzana dorada, carne podrida...). Una entrada por
    efecto con descripción, el efecto en ese nivel, nivel y duración (o "instantáneo"), la probabilidad si no es segura y
    un aviso cuando la duración real cambia (flecha 1/8, nube persistente 1/4, arrojadiza según la distancia). Las pociones
    sin efectos (agua, rara, vulgar, densa) explican para qué sirven. Si un objeto tiene encantamientos **y** efectos,
    salen los dos.
  - Equipo (fase 4): armaduras, armas y herramientas. Una entrada por **atributo del objeto** (armadura, dureza,
    daño, golpes por segundo, resistencia al empuje, vida...) con qué significa, el valor normal del jugador, lo que
    aporta cada hueco ("En el pecho: +8") y, cuando importa, el total (daño del golpe, golpes por segundo). Además:
    durabilidad actual y máxima, con qué se repara, encantabilidad y resistencia del objeto suelto (netherita);
    reglas de minado de la herramienta ("×6 en piedra, minerales y metales", "rompe al instante: bambú"); desgaste por
    golpe y si desactiva escudos; hueco de equipo, planeo (élitros), escudo y adorno de armadura. Se lee **el objeto
    concreto**, no el tipo: los atributos que otros mods añaden a un objeto (RPG Loot...) salen igual.
  - Objetos especiales (fase 5): una entrada **"Qué es"** al principio, con qué es, cómo se usa y cómo se consigue
    (`objetos.json`). Hoy: mochilas (pequeña/mediana/grande en 16 colores, Ender, Global, de Lava) y sus 5 módulos,
    piedras de viaje (y la portátil), brújula de tumba, llaves de élitros, cuerno de la visión, polvo ilusorio y los
    materiales de Illager Expansion. Además **"Al consumirlo"** para lo que no da efectos sino que los quita o hace otra
    cosa: leche, miel, fruta de chorus y la comida de Farmer's Delight (curar, apagar fuego, quitar un efecto al azar);
    se lee de los datos del objeto, sin escribir nada.
  - Sin encantamientos ni efectos: tras los datos del equipo, un separador y la lista de los que admite y puede obtener el jugador (mismo criterio que el inventario: mesa,
    aldeanos, botín, tesoro o maldición; los internos de mods no salen). Paginado (36 por página).
  - Todo se explica: no hay objetos "secretos" (decisión de diseño; si más adelante se quiere ocultar alguno, será una
    lista explícita).

## Añadir descripciones

**Efectos** (`efectos.json`, mismos campos): las cifras de atributos (velocidad +20 %, daño +3, vida +4...) **no se
escriben**: el inspector las lee del propio juego (`MobEffect.createModifiers`), así que nunca quedan desfasadas. Solo se
escribe `desc` y, para lo que no es un atributo, el efecto por nivel. Los `valores` admiten `tipo`: `lineal` (por
defecto), `der` (`base >> (nivel-1)`, mínimo 1: intervalos en ticks como Regeneración, Veneno y Wither), `izq`
(`base << (nivel-1)`: curas que se duplican), más `tope` y `div` (20 = ticks a segundos). `"atributos": false` oculta las
líneas automáticas cuando un atributo interno no dice nada útil (p. ej. Invisibilidad). Las pociones base sin efectos
van como `potion:<id>`. **Las cifras de intervalos y curas las verifica `GuiaSuite` contra el código del juego**
(`shouldApplyEffectTickThisTick` y aplicando el efecto a un jugador): si se cambia una fórmula mal, la prueba falla.
Un efecto nuevo de un mod sin descripción también hace fallar la suite hasta que se describa.

**Objetos especiales** (`objetos.json`): `{ids: [...], desc, uso: [...], consigue: [...]}`; `ids` son todos los objetos a
los que aplica (variantes de color, de piedra...). **Regla: solo se escribe lo verificado**, con una de estas fuentes:
datos del mod (recetas, loot tables), la configuración REAL del servidor (`config/`) o el objeto usado de verdad. Esto
último lo hace `GuiaSuite.observeBehavior` (escribe `purgatorio-guia-comportamiento.txt` en el servidor de pruebas) y la
suite comprueba que el texto coincide con lo observado (tamaños de mochila, duraciones del polvo ilusorio, si un módulo
se usa suelto...). Las cifras que dependen de la configuración (costes de las piedras de viaje, tamaños) se sacaron de
`config/` el 2026-10-04: si cambia la configuración, revisarlas. El informe `docs/guia-inventario.md` lista los candidatos
por describir (descartando decoración, huevos y bloques).

**Atributos** (`atributos.json`): solo `desc` (qué significa). Las cifras (valor normal, valor del objeto, total) las
calcula el inspector leyendo el juego. Hay entradas para los 40 atributos porque un objeto puede llevar cualquiera; un
atributo nuevo sin descripción hace fallar la suite.

**Cobertura permanente:** `GuiaSuite` falla si un encantamiento que el jugador puede conseguir (mesa, aldeanos, botín,
tesoro o maldición) no tiene descripción; igual que con efectos y atributos. Al añadir un mod con encantamientos, la
suite avisa de cuáles faltan.

**Cómo se verifican los números de los encantamientos (`GuiaObserve`):** las cifras de los textos no se calculan a mano
ni se copian de la wiki; se **miden con el juego** y la suite comprueba que coinciden:
- *Atributos* (velocidad, vida, alcance, escalón...): se aplican los modificadores del objeto y del encantamiento
  (`EnchantmentHelper.forEachModifier`) al atributo real de un jugador y se lee el valor con el cálculo del juego. Así se
  detectan multiplicadores sobre una base 0 (que no hacen nada) o valores desproporcionados.
- *Daño* (Filo, Perdición, Smite, Dragonbane...): `EnchantmentHelper.modifyDamage` con un golpe de 10 de daño base.
- *Efectos al golpear*: 300 golpes por nivel con `doPostAttackEffects`; se anotan efecto, amplificador, duración y
  probabilidad reales (`purgatorio-guia-encantamientos.txt` en el servidor de pruebas).
- *Funciones de datapack* (los de `advanced-archery`, `dke`, `warft`...): se leen las funciones; no se pueden medir con el
  mismo método. Los textos de esos 24 encantamientos describen lo que dicen las funciones. Dudas: los **niveles** de
  Pillaring (las funciones no distinguen niveles) y Phasing (cuántos bloques de grosor atraviesa).

**Encantamientos:** editar `core/src/guia/resources/purgatorio_guia/encantamientos.json` (el formato está en la clave `_formato`):

```json
"minecraft:sharpness": {
  "desc": "Aumenta el daño que haces cuerpo a cuerpo con espadas y hachas.",
  "efecto": "Daño extra: +{v} (2 puntos = 1 corazón)",
  "valores": { "v": { "base": 1.0, "paso": 0.5 } }
}
```

`valor(nivel) = base + paso * (nivel - 1)`. Para fórmulas no lineales (p. ej. Eficiencia = nivel² + 1) se usa
`"niveles": ["...", "..."]`, un texto por nivel. **Comprobar la cifra contra el JSON del encantamiento** (en el jar del
mod o de vanilla) en vez de fiarse de la wiki; la suite valida que cada id existe y que `niveles` tiene un texto por nivel.

Lo que no esté descrito se muestra como "Sin descripción todavía" con su id. `python3 tools/inventario-guia.py`
regenera `docs/guia-inventario.md` con lo que falta (encantamientos: 147 de 147; efectos: 42 de 42). Para que el
inventario de efectos incluya los de todos los mods, antes: `FULL_PACK=1 tools/run-integration-tests.sh guia` (vuelca el
registro real a `~/dev/test-server/purgatorio-guia-registro.json`).

## Desplegar en el servidor real

1. `cd core && ./gradlew guiaJar` (necesita JDK 25: `~/dev/tools/jdk-25.0.4.1`).
2. Copiar `core/build/libs/purgatorio-guia-<version>.jar` a la carpeta `mods/` del servidor y reiniciar.
3. No requiere cambios en el resource pack.

Los jugadores que ya estén dentro reciben el libro en menos de medio segundo; los nuevos, al entrar.

## Pendiente (fases siguientes)

Pendiente tras la fase 5:
- **Trims Overhaul** (datapack del servidor con 17 patrones con habilidades, 9 materiales con atributos, plantillas con
  propietario y misiones; sus textos están en los `es_es` del propio mod, pero son frases de ambientación, no
  explicaciones mecánicas: hay que leer sus funciones). Hoy el inspector solo dice que hay un adorno.
- Las armas de lanza (componentes `piercing_weapon`/`kinetic_weapon`) siguen sin explicar.
- Lo que está descrito con menos certeza, a revisar si se puede comprobar más: la mochila **de Lava** (lo que guardas se
  quema: leído del código, no probado con el paso del tiempo), la **Global** (solo se sabe su tamaño y su receta, no si
  comparte contenido) y la **brújula de tumba** (el uso sin tumba no hace nada; el menú con tumba se leyó de la config).
- Unos 84 candidatos en `docs/guia-inventario.md` (casi todo Farmer's Delight: comida y utensilios de cocina).
- ~~Las 143 descripciones de encantamientos~~ (hechas), porque cualquier armadura, arma o herramienta
  sin encantar lista los que admite y casi todos salen como "Sin descripción todavía".
- Cómo se elabora cada poción.

## Hallazgos al describir los encantamientos (a revisar por quien administra el servidor)

Al medir con el juego salieron cosas que probablemente no son lo que se pretendía; el inspector las describe tal cual:

- **`moreenchantments` está sin equilibrar** (sus ids internos se llaman `example_*`): Velocidad de ataque +50 % / +650 % /
  +1250 %; Prisa (haste) minado ×3 / ×5 / ×7; Pie grande, escalones de 1,2 / 2,1 / 3 bloques; Fuerza hasta +100 % del
  daño total; Protección contra el fuego quema a 0; Velocidad y Salto +60 % en el nivel V. Se pueden obtener en la mesa
  de encantamientos y con aldeanos (están en `in_enchanting_table` y `tradeable`).
- **No hacen nada:** `knockback_protection` (multiplica sobre una base 0; con netherita suma una milésima),
  `wingspan` (de Dragonkind: multiplica el barrido sobre 0, solo pone sonido y partículas) y `serverbackpacks:capacity`
  (no cambia los huecos de la mochila; comprobado con pequeña, mediana y grande). `deterioration_curse` solo es
  incompatible con Reparación.
- **Cuidado al jugar:** `warping` (Advanced Archery) cuesta 10 de daño de caída (5 corazones); `void_step` gasta la mitad de la
  durabilidad máxima del peto cada vez; `lifesteal` nivel I no llega a curar nada (Regeneración I dura 1 s).
- **Mods que cambian vanilla:** `enchants-plus` sobrescribe `power` (también en ballesta) y `fire_aspect` (exclusivo con
  `#sword_buffs`). En el servidor `minecraft:spider` ya no cuenta para la Perdición de los artrópodos pero sí
  `spiderstpo:spider`, que es la araña que realmente aparece.
- **Perdición de los artrópodos y similares** miden como los datos; Fuego/Explosión/Proyectil protección vanilla coinciden
  con lo escrito.
