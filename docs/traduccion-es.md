# Traduccion al espanol

Objetivo: que **todos** los jugadores vean el servidor en espanol sin tocar su configuracion (clientes vanilla, que
arrancan en ingles).

## Como funciona

1. **El resource pack obligatorio sobrescribe `en_us`** con texto en espanol (vanilla + todos los mods). Un cliente en
   ingles lee `en_us`, asi que ve espanol; un cliente en `es_es`/`es_mx` tambien (las claves que falten caen a `en_us`).
   - `assets/minecraft/lang/en_us.json` = `es_es` de Mojang (se descarga al construir; no se versiona) + claves de mods.
   - Un `assets/<mod>/lang/en_us.json` por mod, ya en espanol. Los empaqueta `purgatorio_core` (`core/build.gradle`)
     y Polymer los mete en el pack con `addModAssets`.
2. **El mod `zz_purgatorio_es`** (solo recursos) hace lo mismo para las traducciones **del lado servidor** de Polymer
   (`data/<mod>/lang`: waystones, mochilas, tumbas...) y para el idioma de la consola. Fabric aplica los idiomas en
   orden alfabetico de id de mod, por eso se llama `zz_`: tiene que ir el ultimo para ganar a los `en_us` de los demas.

## Donde se editan los textos

`i18n/es/<mod>.<assets|data>.json`: nuestras traducciones (tienen prioridad sobre todo). Prioridad por clave:
`i18n/es` > `es_es` del propio mod > cualquier `es_*` del mod > (vanilla) `es_es` de Mojang > ingles.

## Construir

```bash
python3 tools/build-i18n.py     # lee los mods de MODS_DIR (por defecto, los del servidor real) -> build/i18n/
```

Lista al final las claves que siguen en ingles. `tools/build-resource-pack.sh` lo ejecuta solo y
`tools/setup-test-server.sh` copia `zz_purgatorio_es-1.0.0.jar` al servidor de pruebas.

**Al anadir o actualizar un mod:** volver a ejecutarlo; si aparecen claves sin traducir, anadirlas en `i18n/es/`.

## Despliegue en produccion (no se hace solo)

1. `FULL_PACK=1 tools/build-resource-pack.sh` y subir el nuevo `resources+<sha1>.zip` a `polymer/` por PR a `main`.
2. Esperar a que GitHub Pages lo publique.
3. Copiar `purgatorio-core-*.jar` y `build/i18n/zz_purgatorio_es-1.0.0.jar` a `mods/` y reiniciar.

## Limites conocidos

- Solo cubre textos que viven en archivos de idioma. Lo escrito directamente en codigo o en datapacks (nombres
  puestos con `custom_name`/`item_name` literal, p. ej. Incendium, Nullscape, Structory, Enchants Plus, RPG Loot)
  hay que traducirlo en esos datos.
- `collective` incluye textos de otros mods de Serilum que no estan instalados; solo se tradujeron los compartidos.
- Los nombres de la interfaz del cliente que no pasan por `en_us` (p. ej. el nombre de idioma "English") no cambian.
