# Comprobaciones manuales con un cliente real

Las pruebas automaticas verifican **servidor y paquetes**, pero en la maquina de desarrollo no hay cliente de
Minecraft. Estas cosas solo se pueden confirmar con un cliente vanilla 26.3 conectado a un servidor de pruebas
(nunca al real):

1. **Barra de XP = Alma.** Al entrar muestra el Alma (0 en un jugador nuevo). `/purgatorio admin alma set <tu> 42.5`
   → la barra muestra nivel 42 y va a la mitad. Recoger orbes de XP o minar mena no la mueve.
2. **Resource pack.** Aparece el aviso de aceptar el pack; tras aceptar, el Colmillo de Ceniza se ve como una espada
   propia (no como la de hierro) y la Esquirla de Brasa como un fragmento naranja, no como polvo de blaze.
   `/purgatorio admin dar colmillo @s 2`.
3. **Menu.** `/purgatorio` abre un cofre de 3 filas con Alma, Forja y Diario. Los textos se leen bien (colores,
   acentos). Los iconos no se pueden coger ni mover.
4. **Forja.** Con el Colmillo en la mano: `/purgatorio forja` muestra coste de Alma y materiales (rojo/verde). Dar 4
   lingotes de hierro y 50 de Alma → el boton Mejorar funciona y baja la barra.
5. **Descubrimiento.** `/function purgatorio:pruebas/construir_ruina` (como op) levanta la ruina en x,z 36..44; al
   entrar sale el aviso del logro, +8 de Alma y la entrada en el Diario (tecla L, pestaña "Diario de Purgatorio").
6. **Acechador.** `/purgatorio admin invocar acechador`: se ve con nombre; si le das la espalda y esta lejos, humo azul
   y sonido, y aparece detras ~1 s despues. Al matarlo suelta Esquirlas.
7. **Muerte.** Con 100 de Alma, morir deja 70 y aparece el mensaje "Tu Alma se derrama...".
8. **Encantar / yunque.** Comprobar que **no** se pueden usar (el XP real es 0). Es un efecto conocido pendiente de
   decision de diseno.
9. **Rendimiento.** Con varios jugadores, mirar `spark` mientras pelean (no medido todavia).
