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
DESCRIPCIONES = os.path.join(REPO, "core", "src", "guia", "resources", "purgatorio_guia", "encantamientos.json")
DESCRIPCIONES_ATRIBUTOS = os.path.join(REPO, "core", "src", "guia", "resources", "purgatorio_guia", "atributos.json")
DESCRIPCIONES_OBJETOS = os.path.join(REPO, "core", "src", "guia", "resources", "purgatorio_guia", "objetos.json")
DESCRIPCIONES_EFECTOS = os.path.join(REPO, "core", "src", "guia", "resources", "purgatorio_guia", "efectos.json")
# Volcado del registro REAL de efectos y pociones (lo escribe GuiaSuite en el servidor de pruebas). Para que incluya los
# mods del servidor real: FULL_PACK=1 tools/run-integration-tests.sh guia
REGISTRO = os.environ.get("REGISTRO") or os.path.expanduser("~/dev/test-server/purgatorio-guia-registro.json")

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

    # Efectos: los de los ficheros de idioma + los del registro real (si hay volcado), con su categoria.
    effects = {}
    for k, v in lang.items():
        m = re.match(r"^effect\.([a-z0-9_]+)\.([a-z0-9_]+)$", k)
        if m and m.group(1) != "duration":
            effects[f"{m.group(1)}:{m.group(2)}"] = {"nombre_en": v, "nombre_es": es.get(k)}
    for k, v in es.items():
        m = re.match(r"^effect\.([a-z0-9_]+)\.([a-z0-9_]+)$", k)
        if m and m.group(1) != "duration":
            effects.setdefault(f"{m.group(1)}:{m.group(2)}", {"nombre_en": None, "nombre_es": v})

    registry = json.load(open(REGISTRO, encoding="utf-8")) if os.path.exists(REGISTRO) else None
    potions_base = []
    if registry:
        for eid, e in registry["efectos"].items():
            effects.setdefault(eid, {"nombre_en": None, "nombre_es": None})["categoria"] = e["categoria"]
        potions_base = sorted(pid for pid, eff in registry["pociones"].items() if not eff)
    else:
        print("AVISO: no hay volcado del registro (", REGISTRO, "): los efectos salen solo de los ficheros de idioma")
    described_fx = set()
    if os.path.exists(DESCRIPCIONES_EFECTOS):
        described_fx = {k for k in json.load(open(DESCRIPCIONES_EFECTOS, encoding="utf-8")) if not k.startswith("_")}
    for eid, e in effects.items():
        e["descrita"] = eid in described_fx
    unknown_fx = sorted(k for k in described_fx if not k.startswith("potion:") and k not in effects)
    if unknown_fx:
        print("AVISO: descripciones de efectos que no existen en el servidor:", unknown_fx)

    # Atributos: los del registro real, cuantos objetos los llevan de serie y si estan descritos.
    attributes = {}
    if registry:
        used = collections.Counter()
        for arr in registry.get("objetos_con_atributos", {}).values():
            for a in {x.split()[0] for x in arr}:
                used[a] += 1
        described_at = set()
        if os.path.exists(DESCRIPCIONES_ATRIBUTOS):
            described_at = {k for k in json.load(open(DESCRIPCIONES_ATRIBUTOS, encoding="utf-8")) if not k.startswith("_")}
        for aid in registry.get("atributos", {}):
            attributes[aid] = {"objetos": used.get(aid, 0), "descrita": aid in described_at}
        unknown_at = sorted(described_at - set(attributes))
        if unknown_at:
            print("AVISO: descripciones de atributos que no existen en el servidor:", unknown_at)

    # Objetos de mods: candidatos a descripcion a mano (se descartan decoracion, huevos, bloques y lo que el inspector ya explica).
    mod_items = []
    described_items = set()
    if os.path.exists(DESCRIPCIONES_OBJETOS):
        for k, v in json.load(open(DESCRIPCIONES_OBJETOS, encoding="utf-8")).items():
            if not k.startswith("_"):
                described_items.update(v["ids"])
    if registry:
        skip_ns = {"tsa"}   # TSA Decorations: decoracion pura
        for iid, it in sorted(registry.get("objetos_de_mods", {}).items()):
            ns = iid.split(":")[0]
            cls = it["clase"].rsplit(".", 1)[-1]
            plain = ns in skip_ns or cls.endswith(("BlockItem", "SpawnEggItem", "SpawnEgg")) or "spawn_egg" in iid
            mod_items.append({"id": iid, "nombre": it["nombre"], "clase": cls, "descrito": iid in described_items,
                              "explicado": it["explicado_por_inspector"], "candidato": not plain and not it["explicado_por_inspector"]})
        unknown_items = sorted(i for i in described_items if i not in registry.get("objetos_de_mods", {})
                               and any(x.startswith(i.split(":")[0] + ":") for x in registry.get("objetos_de_mods", {})))
        if unknown_items:
            print("AVISO: descripciones de objetos que no existen en el servidor:", unknown_items)

    # Descripciones ya escritas para el inspector (las claves que empiezan por "_" son notas del formato).
    described = set()
    if os.path.exists(DESCRIPCIONES):
        described = {k for k in json.load(open(DESCRIPCIONES, encoding="utf-8")) if not k.startswith("_")}
    for r in result:
        r["descrita"] = r["id"] in described
    unknown = sorted(described - {r["id"] for r in result})
    if unknown:
        print("AVISO: descripciones de ids que no existen en el servidor:", unknown)

    os.makedirs(OUT, exist_ok=True)
    json.dump(result, open(os.path.join(OUT, "enchantments.json"), "w", encoding="utf-8"), indent=1, ensure_ascii=False)
    json.dump(effects, open(os.path.join(OUT, "effects.json"), "w", encoding="utf-8"), indent=1, ensure_ascii=False)
    write_doc(result, effects, potions_base, described_fx, registry is not None, attributes, mod_items)
    c = collections.Counter(r["tipo"] for r in result)
    pending_fx = sum(1 for e in effects.values() if not e["descrita"])
    print(f"{len(result)} encantamientos: {dict(c)}; {len(effects)} efectos ({pending_fx} sin descripcion)")


def write_doc(result, effects, potions_base, described_fx, has_registry, attributes, mod_items):
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
    needed = [r for r in result if r["tipo"] != "interno"]
    done = sum(1 for r in needed if r["descrita"])
    L += [f"**{len(result)} encantamientos definidos**: {c['jugador']} de jugador, {c['botin']} por botin, "
          f"{c['interno']} internos. **Descripciones: {done} escritas de {len(needed)} necesarias, "
          f"{len(needed) - done} por escribir.** (Las escritas viven en `core/src/guia/resources/purgatorio_guia/encantamientos.json`.)", ""]
    for ns, rs in sorted(by_ns.items()):
        L += [f"## `{ns}` ({len(rs)})", "", "| id | tipo | max | nombre (es) | etiquetas | descrita | nota |", "|---|---|---|---|---|---|---|"]
        for r in rs:
            note = []
            if r["sobrescribe_vanilla"]:
                note.append("sobrescribe vanilla: " + ", ".join(r["fuentes"]))
            if not r["nombre_es"] and r["tipo"] != "interno":
                note.append("sin nombre en espanol")
            L.append(f"| `{r['id'].split(':', 1)[1]}` | {r['tipo']} | {r['max_level']} | {r['nombre_es'] or r['nombre_en'] or ''} "
                     f"| {', '.join(r['etiquetas'])} | {'si' if r['descrita'] else ('' if r['tipo'] == 'interno' else 'NO')} | {'; '.join(note)} |")
        L.append("")
    done_fx = sum(1 for e in effects.values() if e["descrita"])
    L += ["## Efectos", "",
          f"**{len(effects)} efectos**{'' if has_registry else ' (solo los que traen los ficheros de idioma: falta el volcado del registro)'}. "
          f"**Descripciones: {done_fx} escritas, {len(effects) - done_fx} por escribir.** "
          "(Las escritas viven en `core/src/guia/resources/purgatorio_guia/efectos.json`.) Las cifras de atributos "
          "(velocidad, daño, vida...) las lee el inspector del propio juego: no se escriben.", "",
          "El volcado del registro se genera con `FULL_PACK=1 tools/run-integration-tests.sh guia` "
          "(incluye los efectos de todos los mods del servidor real).", "",
          "| id | categoria | descrita |", "|---|---|---|"]
    for eid, e in sorted(effects.items()):
        L.append(f"| `{eid}` | {e.get('categoria', '')} | {'si' if e['descrita'] else 'NO'} |")
    L.append("")
    if potions_base:
        L += ["### Pociones sin efectos (base)", "", "| pocion | descrita |", "|---|---|"]
        for pid in potions_base:
            L.append(f"| `{pid}` | {'si' if 'potion:' + pid in described_fx else 'NO'} |")
        L.append("")
    if attributes:
        done_at = sum(1 for a in attributes.values() if a["descrita"])
        L += ["## Atributos", "",
              f"**{len(attributes)} atributos**. **Descripciones: {done_at} escritas, {len(attributes) - done_at} por escribir.** "
              "(Viven en `core/src/guia/resources/purgatorio_guia/atributos.json`.) Un objeto puede llevar cualquiera "
              "(p. ej. los que añade RPG Loot a objetos concretos), por eso se describen todos. La columna *objetos* es "
              "cuántos objetos los llevan de serie.", "",
              "| id | objetos | descrita |", "|---|---|---|"]
        for aid, a in sorted(attributes.items(), key=lambda kv: (-kv[1]["objetos"], kv[0])):
            L.append(f"| `{aid}` | {a['objetos']} | {'si' if a['descrita'] else 'NO'} |")
        L.append("")
    if mod_items:
        total = len(mod_items)
        done = sum(1 for i in mod_items if i["descrito"])
        explained = sum(1 for i in mod_items if i["explicado"] and not i["descrito"])
        pending = [i for i in mod_items if i["candidato"] and not i["descrito"]]
        L += ["## Objetos de mods", "",
              f"**{total} objetos de mods**: {done} con descripción a mano (`objetos.json`), {explained} que el inspector ya explica "
              f"solo (efectos, equipo, consumo) y **{len(pending)} candidatos por describir**. No se cuentan como candidatos la "
              "decoración (TSA), los huevos generadores ni los bloques.", "",
              "| id | nombre | clase |", "|---|---|---|"]
        for i in pending:
            L.append(f"| `{i['id']}` | {i['nombre']} | {i['clase']} |")
        L.append("")
    open(os.path.join(REPO, "docs", "guia-inventario.md"), "w", encoding="utf-8").write("\n".join(L))


if __name__ == "__main__":
    main()
