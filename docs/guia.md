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

**Atributos** (`atributos.json`): solo `desc` (qué significa). Las cifras (valor normal, valor del objeto, total) las
calcula el inspector leyendo el juego. Hay entradas para los 40 atributos porque un objeto puede llevar cualquiera; un
atributo nuevo sin descripción hace fallar la suite.

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
regenera `docs/guia-inventario.md` con lo que falta (encantamientos: 4 de 147; efectos: 42 de 42). Para que el
inventario de efectos incluya los de todos los mods, antes: `FULL_PACK=1 tools/run-integration-tests.sh guia` (vuelca el
registro real a `~/dev/test-server/purgatorio-guia-registro.json`).

## Desplegar en el servidor real

1. `cd core && ./gradlew guiaJar` (necesita JDK 25: `~/dev/tools/jdk-25.0.4.1`).
2. Copiar `core/build/libs/purgatorio-guia-<version>.jar` a la carpeta `mods/` del servidor y reiniciar.
3. No requiere cambios en el resource pack.

Los jugadores que ya estén dentro reciben el libro en menos de medio segundo; los nuevos, al entrar.

## Pendiente (fases siguientes)

Ítems especiales a mano (fase 5): **Trims Overhaul** (datapack del servidor con 17 patrones con habilidades, 9
materiales con atributos, plantillas con propietario y misiones; sus textos están en los `es_es` del propio mod, así que
se puede reutilizar sin reescribir; hoy el inspector solo dice que hay un adorno), la miel que quita el veneno (quita
efectos en vez de darlos) y las armas de lanza (componentes `piercing_weapon`/`kinetic_weapon`, aún sin explicar).
Y las **143 descripciones de encantamientos**: ahora son lo más visible, porque cualquier armadura, arma o herramienta
sin encantar lista los que admite y casi todos salen como "Sin descripción todavía".
Tampoco se explica aún cómo se elabora cada poción.
