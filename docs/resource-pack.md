# Resource pack: como se reproduce

El pack lo genera **Polymer** al vuelo a partir de dos fuentes versionadas en este repo; no se edita a mano.

| Fuente | Ruta |
|---|---|
| Modelos, items y texturas del Colmillo de Ceniza (Java/Polymer) | `core/src/main/resources/assets/purgatorio/` |
| Texturas de arte provisional (regenerables) | `tools/gen_textures.py` |
| Esquirla de Brasa (Filament: JSON de datapack, genera su propio modelo) | `datapack/purgatorio/data/purgatorio/filament/` |

## Reconstruir

```bash
tools/build-resource-pack.sh                # pack de desarrollo (Polymer + Filament + purgatorio_core)
FULL_PACK=1 tools/build-resource-pack.sh    # pack completo con todos los mods del servidor real
```

Compila el mod, arranca el servidor de pruebas aislado, ejecuta `polymer generate-pack` y deja
`build/resource-pack/resources+<sha1>.zip` (ignorado por git). El nombre lleva el hash, como espera Polymer.

## Produccion

El servidor real descarga el pack desde GitHub Pages (`main`, carpeta `polymer/`). Cada vez que cambian los assets
cambia el hash, asi que el despliegue es:

1. Generar el pack con `FULL_PACK=1` (o con `/polymer generate-pack` en el propio servidor real).
2. Subir `resources+<sha1>.zip` a `polymer/` **por Pull Request** a `main` y esperar a que GitHub Pages lo publique.
3. Solo despues, reiniciar el servidor con el mod nuevo. Si el pack no esta publicado, los jugadores (pack obligatorio) no pueden entrar.

Los `.zip` de `polymer/` en `main` son el unico binario versionado a proposito: es lo que sirve Pages.
