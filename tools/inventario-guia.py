#!/usr/bin/env python3
"""Inventario de encantamientos y efectos reales del servidor, base de la guia/inspector de objetos.

Lee los jars de mods (y los jars anidados), el servidor vanilla, los datapacks del mundo y las traducciones al
espanol del repo. Para cada encantamiento recoge nivel maximo, objetos soportados, grupo excluyente y etiquetas, y lo
clasifica:
  - jugador: el jugador puede obtenerlo (mesa, aldeanos, botin aleatorio, tesoro, maldicion) segun las etiquetas.
  - botin:   sin etiqueta, pero algun loot table / receta / trade del pack lo menciona.
  - interno: nadie lo da; solo lo usan jefes, estructuras o el propio mod (no necesita descripcion para el jugador).

Salida:  build/guia/enchantments.json, build/guia/effects.json (no se versionan) y docs/guia-inventario.md.
Uso:     python3 tools/inventario-guia.py            (SERVER_DIR=<ruta> para otro servidor)
"""
import collections, glob, io, json, os, re, zipfile

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SERVER = os.environ.get("SERVER_DIR") or glob.glob(os.path.expanduser(
    "~/umbrel/app-data/brcly-crafty/data/servers/*/"))[0].rstrip("/")
OUT = os.path.join(REPO, "build", "guia")
ES_VANILLA = os.path.join(REPO, "build", "i18n-cache", "mojang-es_es-26.3.json")

ENCH_RE = re.compile(r"^data/([^/]+)/enchantment/(.+)\.json$")
TAG_RE = re.compile(r"^data/([^/]+)/tags/enchantment/(.+)\.json$")
LANG_RE = re.compile(r"^(?:assets|data)/([^/]+)/lang/en_us\.json$")
OBTAIN_TAGS = ("in_enchanting_table", "tradeable", "treasure", "non_treasure", "on_random_loot", "curse",
               "on_traded_equipment", "on_mob_spawn_equipment")
# La parte del id que delata un encantamiento de uso interno (no se ofrece al jugador).
INTERNAL_HINT = re.compile(r"(^|/)(internal|tech|boss|place)(/|$)")


def walk_zip(z, label, depth=0):
    """Rinde (nombre, bytes, etiqueta) de cada fichero de un zip y de los jars anidados."""
    for n in z.namelist():
        if n.endswith("/"):
            continue
        if n.endswith(".jar") and depth < 2 and (n.startswith("META-INF/jars/") or n.startswith("META-INF/versions/")):
            try:
                yield from walk_zip(zipfile.ZipFile(io.BytesIO(z.read(n))), f"{label}>{os.path.basename(n)}", depth + 1)
            except zipfile.BadZipFile:
                pass
        elif n.startswith(("data/", "assets/")) and n.endswith(".json"):
            yield n, z.read(n), label


def sources():
    for jar in sorted(glob.glob(SERVER + "/mods/*.jar")) + [SERVER + "/vanilla-26.3.jar"]:
        if os.path.exists(jar):
            yield from walk_zip(zipfile.ZipFile(jar), os.path.basename(jar))
    for dp in glob.glob(SERVER + "/*/datapacks/*") + glob.glob(SERVER + "/world/datapacks/*"):
        if os.path.isdir(dp):
            for root, _, files in os.walk(dp):
                for f in files:
                    p = os.path.join(root, f)
                    rel = os.path.relpath(p, dp)
                    if rel.startswith("data" + os.sep) and f.endswith(".json"):
                        yield rel.replace(os.sep, "/"), open(p, "rb").read(), "datapack:" + os.path.basename(dp)


def tag_values(raw):
    out = []
    for v in json.loads(raw).get("values", []):
        out.append(v["id"] if isinstance(v, dict) else v)
    return out


def main():
    ench, tags, lang, refs = {}, collections.defaultdict(set), {}, collections.defaultdict(set)
    blobs = []   # texto de loot tables/recetas/trades para buscar menciones
    for name, raw, label in sources():
        m = ENCH_RE.match(name)
        if m:
            eid = f"{m.group(1)}:{m.group(2)}"
            try:
                d = json.loads(raw)
            except ValueError:
                continue
            entry = ench.setdefault(eid, {"id": eid, "sources": [], "def": None})
            entry["sources"].append(label)
            entry["def"] = d       # el ultimo gana (los mods van despues de vanilla)
            continue
        m = TAG_RE.match(name)
        if m:
            tid = f"{m.group(1)}:{m.group(2)}"
            try:
                tags[tid].update(tag_values(raw))
            except ValueError:
                pass
            continue
        m = LANG_RE.match(name)
        if m:
            try:
                lang.update(json.loads(raw))
            except ValueError:
                pass
            continue
        if re.match(r"^data/[^/]+/(loot_table|trade|villager_trade|recipe|enchanting_recipe)s?/", name):
            blobs.append((name, raw.decode("utf-8", "ignore")))

    # Traducciones propias (espanol) y vanilla de Mojang
    es = {}
    if os.path.exists(ES_VANILLA):
        es.update(json.load(open(ES_VANILLA, encoding="utf-8")))
    for f in glob.glob(REPO + "/i18n/es/*.json"):
        es.update(json.load(open(f, encoding="utf-8")))

    def resolve(tag, seen=()):
        """Ids de un tag, expandiendo #referencias."""
        out = set()
        for v in tags.get(tag, ()):
            if v.startswith("#"):
                if v[1:] not in seen:
                    out |= resolve(v[1:], seen + (tag,))
            else:
                out.add(v)
        return out

    membership = {t: resolve(f"minecraft:{t}") | resolve(f"{t}") for t in OBTAIN_TAGS}
    for tid in list(tags):                       # tags de otros espacios con el mismo nombre final
        short = tid.split(":", 1)[1]
        if short in OBTAIN_TAGS:
            membership[short] |= resolve(tid)

    text = "\n".join(t for _, t in blobs)
    result = []
    for eid, e in sorted(ench.items()):
        ns, path = eid.split(":", 1)
        d = e["def"]
        desc = d.get("description")
        key = desc.get("translate") if isinstance(desc, dict) else None   # cada mod usa su propia clave
        fallback = desc.get("fallback") if isinstance(desc, dict) else None
        key = key or f"enchantment.{ns}.{path.replace('/', '.')}"
        in_tags = [t for t in OBTAIN_TAGS if eid in membership[t]]
        mentioned = bool(re.search(re.escape(f'"{eid}"'), text)) or (ns == "minecraft" and f'"{path}"' in text)
        if INTERNAL_HINT.search(path) or d.get("supported_items") == []:
            kind = "interno"           # una lista de objetos vacia gana a cualquier etiqueta
        elif in_tags:
            kind = "jugador"
        elif mentioned:
            kind = "botin"
        else:
            kind = "interno"
        exclusive = d.get("exclusive_set")
        result.append({
            "id": eid, "tipo": kind, "fuentes": sorted(set(e["sources"])),
            "sobrescribe_vanilla": ns == "minecraft" and any("vanilla" not in s and "server-" not in s for s in e["sources"]),
            "nombre_en": lang.get(key) or fallback,
            "nombre_es": es.get(key),
            "max_level": d.get("max_level"), "weight": d.get("weight"), "anvil_cost": d.get("anvil_cost"),
            "supported_items": d.get("supported_items"), "primary_items": d.get("primary_items"),
            "exclusive_set": exclusive, "slots": d.get("slots"),
            "efectos": sorted((d.get("effects") or {}).keys()),
            "etiquetas": in_tags, "descripcion_json": d.get("description"),
        })

    # Efectos: solo lo que traen los lang; el registro real se cruza luego con el servidor de pruebas.
    effects = {}
    for k, v in lang.items():
        m = re.match(r"^effect\.([a-z0-9_]+)\.([a-z0-9_]+)$", k)
        if m and m.group(1) != "duration":
            effects[f"{m.group(1)}:{m.group(2)}"] = {"nombre_en": v, "nombre_es": es.get(k)}
    for k, v in es.items():
        m = re.match(r"^effect\.([a-z0-9_]+)\.([a-z0-9_]+)$", k)
        if m and m.group(1) != "duration":
            effects.setdefault(f"{m.group(1)}:{m.group(2)}", {"nombre_en": None, "nombre_es": v})

    os.makedirs(OUT, exist_ok=True)
    json.dump(result, open(os.path.join(OUT, "enchantments.json"), "w", encoding="utf-8"), indent=1, ensure_ascii=False)
    json.dump(effects, open(os.path.join(OUT, "effects.json"), "w", encoding="utf-8"), indent=1, ensure_ascii=False)
    write_doc(result, effects)
    c = collections.Counter(r["tipo"] for r in result)
    print(f"{len(result)} encantamientos: {dict(c)}; {len(effects)} efectos con nombre")


def write_doc(result, effects):
    by_ns = collections.defaultdict(list)
    for r in result:
        by_ns[r["id"].split(":")[0]].append(r)
    L = ["# Inventario para la guia de objetos", "",
         "Generado por `tools/inventario-guia.py` a partir del servidor real (mods, vanilla y datapacks). No editar a mano: "
         "se regenera al anadir o actualizar mods.", "",
         "Tipos: **jugador** = obtenible por el jugador (etiquetas de mesa, aldeanos, botin, tesoro o maldicion); "
         "**botin** = sin etiqueta pero algun loot table/receta lo menciona; **interno** = nadie lo da al jugador "
         "(jefes, estructuras, logica del mod): no necesita descripcion.", ""]
    c = collections.Counter(r["tipo"] for r in result)
    L += [f"**{len(result)} encantamientos definidos**: {c['jugador']} de jugador, {c['botin']} por botin, "
          f"{c['interno']} internos. **Descripciones por escribir: {c['jugador'] + c['botin']}.**", ""]
    for ns, rs in sorted(by_ns.items()):
        L += [f"## `{ns}` ({len(rs)})", "", "| id | tipo | max | nombre (es) | etiquetas | nota |", "|---|---|---|---|---|---|"]
        for r in rs:
            note = []
            if r["sobrescribe_vanilla"]:
                note.append("sobrescribe vanilla: " + ", ".join(r["fuentes"]))
            if not r["nombre_es"] and r["tipo"] != "interno":
                note.append("sin nombre en espanol")
            L.append(f"| `{r['id'].split(':', 1)[1]}` | {r['tipo']} | {r['max_level']} | {r['nombre_es'] or r['nombre_en'] or ''} "
                     f"| {', '.join(r['etiquetas'])} | {'; '.join(note)} |")
        L.append("")
    L += ["## Efectos con nombre conocido", "",
          f"{len(effects)} efectos aparecen en los ficheros de idioma (vanilla + mods). El registro real, que incluye "
          "efectos sin lang, se cruza despues con el servidor de pruebas (fase de pociones).", ""]
    ns_count = collections.Counter(k.split(":")[0] for k in effects)
    L += [f"- `{ns}`: {n}" for ns, n in sorted(ns_count.items())]
    L.append("")
    open(os.path.join(REPO, "docs", "guia-inventario.md"), "w", encoding="utf-8").write("\n".join(L))


if __name__ == "__main__":
    main()
