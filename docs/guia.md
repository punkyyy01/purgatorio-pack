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
  - Sin encantamientos: lista los que admite y puede obtener el jugador (mismo criterio que el inventario: mesa,
    aldeanos, botín, tesoro o maldición; los internos de mods no salen). Paginado (36 por página).
  - Todo se explica: no hay objetos "secretos" (decisión de diseño; si más adelante se quiere ocultar alguno, será una
    lista explícita).

## Añadir descripciones

Editar `core/src/guia/resources/purgatorio_guia/encantamientos.json` (el formato está en la clave `_formato`):

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
regenera `docs/guia-inventario.md` con lo que falta (ahora 4 de 147).

## Desplegar en el servidor real

1. `cd core && ./gradlew guiaJar` (necesita JDK 25: `~/dev/tools/jdk-25.0.4.1`).
2. Copiar `core/build/libs/purgatorio-guia-<version>.jar` a la carpeta `mods/` del servidor y reiniciar.
3. No requiere cambios en el resource pack.

Los jugadores que ya estén dentro reciben el libro en menos de medio segundo; los nuevos, al entrar.

## Pendiente (fases siguientes)

Pociones y efectos (cruzar con el registro real: solo hay 42 efectos con nombre en los ficheros de idioma),
armaduras/armas por atributos, ítems especiales a mano, y las 143 descripciones de encantamientos.
