# Comprobaciones manuales con un cliente real (Minecraft 26.3)

Las pruebas automaticas verifican servidor y paquetes. Lo de abajo solo se puede confirmar con un cliente vanilla 26.3.
**Siempre contra el servidor de pruebas, nunca el real.**

## Conexion

Servidor de pruebas (solo tailnet): `tools/manual-test-server.sh start` en el servidor. Direccion `100.97.25.10:25690`.
Online-mode activado (cuenta real de Mojang). El resource pack es obligatorio y llega por el mismo puerto. Para darte
operador de prueba, el usuario con el que entres se ejecuta con `tools/manual-test-server.sh op <usuario>`.

## Guia (cada punto: OK / problema + captura si algo se ve mal)

**1. Resource pack (obligatorio).** Al conectar aparece el aviso del pack; al aceptar entras. Si lo rechazas te echa.

**2. Barra de XP = Alma.**
- Entrando por primera vez la barra esta vacia (Alma 0).
- `/purgatorio admin alma set @s 42.5` → nivel 42 y barra a la mitad.
- `/xp add @s 50 levels` y `/give @s experience_bottle 3` + lanzarlas, minar mena de carbon, fundir algo: la barra NO cambia.

**3. Menu.** `/purgatorio` abre un cofre de 3 filas: botella (tu Alma y bonus), yunque (Forja), libro (Diario). Textos
legibles (acentos, colores). No se pueden coger los iconos.

**4. Colmillo de Ceniza y forja.**
- `/purgatorio admin dar colmillo @s` → la espada se ve distinta a la de hierro (modelo propio) y tiene el texto del rasgo.
- `/purgatorio admin alma set @s 50`, `/give @s iron_ingot 4`, con el Colmillo en la mano `/purgatorio forja`: coste de
  Alma y materiales visibles; pulsa Mejorar → la barra baja a 40, la espada pasa a Mejora 1/3 y hace mas dano.
- Con un zombi (`/summon zombie ~ ~ ~`): 3 golpes seguidos lo prenden y cuesta hambre.
- Esquirla: `/give @s purgatorio:esquirla_de_brasa 2` → se ve como fragmento naranja, no como polvo de blaze.

**5. Descubrimiento y Diario.**
- `/function purgatorio:pruebas/construir_ruina`, `/tp @s 30 -60 40`, camina hacia (40, 40).
- Al entrar: aviso del logro "La Ruina de Ceniza", mensaje y +8 de Alma. Tecla L → pestaña "Diario de Purgatorio".
- Sal y vuelve a entrar: no da mas Alma. `/purgatorio` → el Diario lista el descubrimiento.

**6. Acechador.** `/purgatorio admin invocar acechador`. Se ve con nombre. Con el jugador de espaldas y a >4 bloques: humo
azul + sonido y ~1 s despues aparece detras. Matarlo suelta Esquirlas y suma 3 de Alma.

**7. Muerte.** `/purgatorio admin alma set @s 100`, `/kill @s`, reaparece: barra en 70 y mensaje "Tu Alma se derrama...".
Un segundo `/kill @s` → 49.

**8. Encantar y yunque (estado actual conocido).** Pon una mesa de encantamientos con lapis y un yunque. Anota lo que
ves: se espera que NO se puedan usar (el XP real es 0). Es la decision de diseno pendiente; solo hay que confirmarlo.

**9. Extra.** Cambiar de dimension (portal) y volver: la barra sigue mostrando el Alma. Salir y entrar: se conserva.
