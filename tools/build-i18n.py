#!/usr/bin/env python3
"""Genera las traducciones al espanol que van dentro de purgatorio_core.

Objetivo: que TODOS los jugadores vean el servidor en espanol sin tocar su configuracion. El cliente vanilla
arranca en en_us, asi que se sobrescribe en_us con texto en espanol (resource pack obligatorio + traducciones de
servidor de Polymer). Quien use es_es/es_mx tambien queda cubierto (es_* cae a en_us).

Entradas:  mods del servidor (MODS_DIR), i18n/es/<ns>.<assets|data>.json (nuestras traducciones) y es_es de
           Mojang (se descarga y se cachea; NO se versiona).
Salida:    build/i18n/{assets,data}/<ns>/lang/{en_us,es_es}.json  (los "assets" los empaqueta core/build.gradle: van al
           resource pack) y build/i18n/zz_purgatorio_es-1.0.0.jar (mod que va a mods/ del servidor).
Prioridad por clave: i18n/es > es_es del propio mod > cualquier es_* del mod > (vanilla) es_es de Mojang > ingles.
"""
import glob, json, os, re, sys, urllib.request, zipfile

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MODS_DIR = os.environ.get("MODS_DIR") or glob.glob(os.path.expanduser(
    "~/umbrel/app-data/brcly-crafty/data/servers/*/mods"))[0]
MC_VERSION = os.environ.get("MC_VERSION", "26.3")
OUT = os.path.join(REPO, "build", "i18n")
CACHE = os.path.join(REPO, "build", "i18n-cache")
SKIP_JARS = ("purgatorio-core", "zz_purgatorio_es")   # los nuestros: su lang no es la fuente en ingles
LANG_RE = re.compile(r"^(assets|data)/([^/]+)/lang/(en_us|es_[a-z]{2})\.json$")


def vanilla_es():
    path = os.path.join(CACHE, f"mojang-es_es-{MC_VERSION}.json")
    if not os.path.exists(path):
        os.makedirs(CACHE, exist_ok=True)
        get = lambda u: json.load(urllib.request.urlopen(u, timeout=60))
        manifest = get("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json")
        ver = next(v for v in manifest["versions"] if v["id"] == MC_VERSION)
        index = get(get(ver["url"])["assetIndex"]["url"])["objects"]
        h = index["minecraft/lang/es_es.json"]["hash"]
        data = urllib.request.urlopen(f"https://resources.download.minecraft.net/{h[:2]}/{h}", timeout=60).read()
        open(path, "wb").write(data)
    return json.load(open(path, encoding="utf-8"))


def scan_mods():
    en, es = {}, {}                       # (kind, ns) -> {clave: texto}; es: (kind, ns) -> [(prioridad, dict)]
    for jar in sorted(glob.glob(os.path.join(MODS_DIR, "*.jar"))):
        if os.path.basename(jar).startswith(SKIP_JARS):
            continue
        z = zipfile.ZipFile(jar)
        for name in z.namelist():
            m = LANG_RE.match(name)
            if not m:
                continue
            kind, ns, lang = m.groups()
            d = json.loads(z.read(name))
            if lang == "en_us":
                en.setdefault((kind, ns), {}).update(d)
            else:
                es.setdefault((kind, ns), []).append((0 if lang == "es_es" else 1, d))
    return en, es


def compile_entrypoint():
    """Compila la unica clase del mod (registra sus assets en Polymer). Necesita el JDK de ~/dev/tools."""
    import subprocess, tempfile, io
    work = os.path.join(CACHE, "javac"); os.makedirs(work, exist_ok=True)
    polymer = os.path.join(work, "polymer-resource-pack.jar")
    if not os.path.exists(polymer):
        bundled = glob.glob(os.path.join(MODS_DIR, "polymer-bundled-*.jar"))[0]
        zb = zipfile.ZipFile(bundled)
        name = next(n for n in zb.namelist() if n.startswith("META-INF/jars/polymer-resource-pack-"))
        open(polymer, "wb").write(zb.read(name))
    loader = glob.glob(os.path.join(os.path.dirname(MODS_DIR), "libraries/net/fabricmc/fabric-loader/*/fabric-loader-*.jar"))[0]
    javac = os.path.join(os.environ.get("JAVA_HOME", os.path.expanduser("~/dev/tools/jdk-25.0.4.1")), "bin", "javac")
    subprocess.run([javac, "--release", "25", "-cp", f"{polymer}:{loader}", "-d", work,
                    os.path.join(REPO, "tools", "i18n-mod", "EsMod.java")], check=True)
    return os.path.join(work, "purgatorio", "es", "EsMod.class")


def build_mod_jar():
    """Mod minimo `zz_purgatorio_es`: Fabric aplica los idiomas de los mods en orden alfabetico de id, asi que
    el nuestro tiene que ser el ULTIMO para ganar a los en_us de los demas (traducciones del lado servidor de
    Polymer: sswaystones, serverbackpacks... y el idioma de la consola). El resource pack sale de purgatorio_core."""
    meta = {"schemaVersion": 1, "id": "zz_purgatorio_es", "version": "1.1.0", "name": "Purgatorio: espanol",
            "description": "Traduce al espanol los textos de los mods. Generado por tools/build-i18n.py.",
            "authors": ["Purgatorio"], "license": "MIT", "environment": "server",
            "entrypoints": {"main": ["purgatorio.es.EsMod"]},
            "depends": {"fabricloader": ">=0.19.5", "polymer-resource-pack": "*"}}
    jar = os.path.join(OUT, "zz_purgatorio_es-1.1.0.jar")
    cls = compile_entrypoint()
    with zipfile.ZipFile(jar, "w", zipfile.ZIP_DEFLATED) as z:
        z.writestr("fabric.mod.json", json.dumps(meta, indent=2))
        z.write(cls, "purgatorio/es/EsMod.class")
        for root, _, files in os.walk(OUT):
            for f in files:
                if f.endswith(".json"):
                    full = os.path.join(root, f)
                    z.write(full, os.path.relpath(full, OUT))


def main():
    en, es = scan_mods()
    mine = {}
    for f in glob.glob(os.path.join(REPO, "i18n", "es", "*.json")):
        ns, kind = os.path.basename(f)[:-5].rsplit(".", 1)
        mine[(kind, ns)] = json.load(open(f, encoding="utf-8"))
    van = vanilla_es()
    en.setdefault(("assets", "minecraft"), {})
    stats = []
    for (kind, ns), base in sorted(en.items()):
        out = {}
        other = "data" if kind == "assets" else "assets"      # el mismo mod puede traer su es en la otra carpeta
        layers = [mine.get((kind, ns), {}), mine.get((other, ns), {})]
        mod_es = [d for _, d in sorted(es.get((kind, ns), []) + es.get((other, ns), []), key=lambda x: x[0])]
        layers += ([van] + mod_es) if ns == "minecraft" else (mod_es + [])
        for k in base:
            for layer in layers:
                if k in layer and layer[k] != "":
                    out[k] = layer[k]
                    break
            else:
                out[k] = base[k]            # sin traduccion: se deja el ingles (se lista abajo)
        missing = [k for k in base if base[k].strip() and not any(l.get(k) for l in layers)]
        if ns == "minecraft":               # en_us completo = todo el vanilla en espanol + claves de mods
            full = dict(van); full.update(out)
            en_us, es_es = full, out
        else:
            en_us, es_es = out, out
        for lang, d in (("en_us", en_us), ("es_es", es_es)):
            p = os.path.join(OUT, kind, ns, "lang", f"{lang}.json")
            os.makedirs(os.path.dirname(p), exist_ok=True)
            with open(p, "w", encoding="utf-8") as f:
                json.dump(d, f, ensure_ascii=False, indent=1)
        stats.append((kind, ns, len(base), len(missing)))
    build_mod_jar()
    print(f"i18n: {len(stats)} espacios de nombres -> {os.path.relpath(OUT, REPO)}")
    for kind, ns, n, miss in stats:
        if miss:
            print(f"  sin traducir: {kind}/{ns}: {miss} de {n} claves (se muestran en ingles)")


if __name__ == "__main__":
    sys.exit(main())
