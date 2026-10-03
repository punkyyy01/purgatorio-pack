#!/usr/bin/env python3
"""Genera la plantilla NBT de "La Ruina de Ceniza" (sin dependencias externas, determinista).

Salida: datapack/purgatorio/data/purgatorio/structure/ruina_de_ceniza/main.nbt

Diseno (17 x 15 x 17):
  * plataforma de piedra negra con muros rotos y una torre en ruinas al NE;
  * en lo alto de la torre, una hoguera de almas sobre un fardo de heno: el humo (fuego de senal) se ve desde muy lejos;
  * una capilla caida al NO con un altar: el cofre del altar guarda el botin (tabla purgatorio:chests/ruina_de_ceniza);
  * un brasero en el patio y un barril pequeno en la base de la torre;
  * dos Acechadores de Ceniza (Husk con etiqueta propia) vigilan el lugar.
Ejecutar desde la raiz del repo:  python3 tools/gen_structures.py
"""
import gzip
import os
import random
import struct

DATA_VERSION = 5023            # DataVersion de las plantillas de Minecraft 26.3
W, H, D = 17, 15, 17
OUT = os.path.join(os.path.dirname(__file__), "..", "datapack", "purgatorio", "data", "purgatorio", "structure",
                   "ruina_de_ceniza", "main.nbt")


# ---------- escritor NBT minimo ----------
class Byte(int): pass
class Int(int): pass
class Float(float): pass
class Double(float): pass
class NList:
    def __init__(self, kind, items): self.kind, self.items = kind, items


TAGID = {"byte": 1, "int": 3, "float": 5, "double": 6, "string": 8, "list": 9, "compound": 10}


def _kind(v):
    if isinstance(v, Byte): return "byte"
    if isinstance(v, Int): return "int"
    if isinstance(v, Float): return "float"
    if isinstance(v, Double): return "double"
    if isinstance(v, bool): raise TypeError("usa Byte")
    if isinstance(v, int): return "int"
    if isinstance(v, str): return "string"
    if isinstance(v, NList): return "list"
    if isinstance(v, dict): return "compound"
    raise TypeError(type(v))


def _str(s):
    b = s.encode("utf8")
    return struct.pack(">H", len(b)) + b


def _payload(v):
    k = _kind(v)
    if k == "byte": return struct.pack(">b", int(v))
    if k == "int": return struct.pack(">i", int(v))
    if k == "float": return struct.pack(">f", float(v))
    if k == "double": return struct.pack(">d", float(v))
    if k == "string": return _str(v)
    if k == "list":
        out = struct.pack(">bi", TAGID[v.kind] if v.items else 0, len(v.items))
        return out + b"".join(_payload(i) for i in v.items)
    out = b""
    for key, val in v.items():
        out += struct.pack(">b", TAGID[_kind(val)]) + _str(key) + _payload(val)
    return out + b"\x00"


def write_nbt(path, root):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    data = struct.pack(">b", 10) + _str("") + _payload(root)
    with gzip.GzipFile(path, "wb", mtime=0) as f:      # mtime=0: salida identica en cada ejecucion
        f.write(data)


def ints(*v): return NList("int", [Int(x) for x in v])
def doubles(*v): return NList("double", [Double(x) for x in v])


# ---------- construccion ----------
rng = random.Random(20261003)
grid = {}


def put(x, y, z, name, props=None, nbt=None):
    assert 0 <= x < W and 0 <= y < H and 0 <= z < D, (x, y, z)
    grid[(x, y, z)] = (name, tuple(sorted((props or {}).items())), nbt)


def inside(x, z):
    return min(x, W - 1 - x) + min(z, D - 1 - z) >= 2


def pick(weights):
    total = sum(w for _, w in weights)
    r = rng.uniform(0, total)
    for name, w in weights:
        r -= w
        if r <= 0:
            return name
    return weights[-1][0]


WALL = [("deepslate_bricks", 45), ("cracked_deepslate_bricks", 30), ("polished_blackstone_bricks", 25)]
FLOOR = [("deepslate_bricks", 40), ("cracked_deepslate_bricks", 25), ("polished_blackstone_bricks", 20), ("soul_soil", 10),
         ("cobbled_deepslate", 5)]

# 1) cimientos y suelo
for x in range(W):
    for z in range(D):
        if not inside(x, z):
            continue
        put(x, 0, z, "basalt" if rng.random() < 0.2 else "blackstone")
        put(x, 1, z, "cobbled_deepslate" if rng.random() < 0.7 else "blackstone")
        edge = x in (0, W - 1) or z in (0, D - 1)
        put(x, 2, z, "smooth_basalt" if edge else pick(FLOOR))

TOWER = (10, 14, 1, 5)           # x0, x1, z0, z1
CHAPEL = (1, 9, 8, 15)           # x0, x1, z0, z1


def in_box(x, z, box):
    return box[0] <= x <= box[1] and box[2] <= z <= box[3]


# 2) muro perimetral roto (el recinto exterior, salvo torre y capilla, que tienen sus propios muros)
for x in range(1, W - 1):
    for z in range(1, D - 1):
        ring = x in (1, W - 2) or z in (1, D - 2)
        if not ring or not inside(x, z) or in_box(x, z, TOWER) or in_box(x, z, CHAPEL):
            continue
        if z == 1 and 7 <= x <= 9:                       # entrada sur
            continue
        h = rng.choice([0, 1, 1, 2, 2, 3, 3, 4])
        for y in range(3, 3 + h):
            put(x, y, z, pick(WALL))

# 3) pilares de las esquinas libres, con farol de almas
for (px, pz, ph) in [(1, 1, 7), (15, 15, 6), (15, 7, 5)]:
    if in_box(px, pz, TOWER) or in_box(px, pz, CHAPEL):
        continue
    for y in range(3, 3 + ph):
        put(px, y, pz, "polished_blackstone_bricks")
    put(px, 3 + ph, pz, "chiseled_polished_blackstone")
    put(px, 4 + ph, pz, "soul_lantern", {"hanging": "false"})

# 4) torre NE hueca con escalera de mano; arriba, la hoguera de senal
x0, x1, z0, z1 = TOWER
for x in range(x0, x1 + 1):
    for z in range(z0, z1 + 1):
        wall = x in (x0, x1) or z in (z0, z1)
        for y in range(3, 14):
            if wall:
                if y >= 10 and rng.random() < 0.30:     # la parte alta esta rota
                    continue
                put(x, y, z, pick(WALL))
            elif 3 <= y <= 10:
                put(x, y, z, "air")
# puerta (oeste) y rendijas
for y in (3, 4):
    put(x0, y, 3, "air")
for y in (7, 10):
    put(x1, y, 3, "air")
put(12, 8, z0, "air")
# suelo superior (y=11) con hueco para la escalera, y escalera de mano en el muro sur interior
for x in range(x0 + 1, x1):
    for z in range(z0 + 1, z1):
        if (x, z) != (12, 4):
            put(x, 11, z, "deepslate_bricks")
        else:
            put(x, 11, z, "air")
for y in range(3, 13):
    put(12, y, 4, "ladder", {"facing": "north"})
# aseguramos el muro donde se apoya la escalera
for y in range(3, 13):
    put(12, y, 5, "deepslate_bricks")
# techo de la cubierta: aire arriba, heno y hoguera de almas (fuego de senal => humo muy visible)
put(12, 12, 3, "hay_block", {"axis": "y"})
put(12, 13, 3, "soul_campfire", {"lit": "true", "signal_fire": "true", "facing": "north"})
put(11, 12, 2, "skeleton_skull", {"rotation": "5"})
put(13, 3, 2, "barrel", {"facing": "up"}, {"id": "minecraft:barrel", "LootTable": "purgatorio:chests/ruina_de_ceniza_menor"})

# 5) capilla caida (NO): muros mas altos, pilares, altar con cofre
cx0, cx1, cz0, cz1 = CHAPEL
for x in range(cx0, cx1 + 1):
    for z in range(cz0, cz1 + 1):
        wall = x in (cx0, cx1) or z in (cz0, cz1)
        if wall:
            if z == cz0 and 4 <= x <= 6:                  # puerta de la capilla
                continue
            back = 1.0 + (z - cz0) / (cz1 - cz0)
            h = int(rng.choice([2, 3, 4, 5, 6]) * (back if z >= 12 or x == cx0 else 1.0)) or 2
            for y in range(3, 3 + min(h, 8)):
                put(x, y, z, pick(WALL))
        else:
            for y in range(3, 10):
                put(x, y, z, "air")
for (px, pz, ph) in [(3, 11, 6), (7, 11, 5), (3, 13, 5), (7, 13, 6)]:
    for y in range(3, 3 + ph):
        put(px, y, pz, "polished_blackstone_bricks")
# altar al fondo: el cofre del altar entre dos pilares bajos con velas
put(5, 3, 14, "chest", {"facing": "north", "type": "single"},
    {"id": "minecraft:chest", "LootTable": "purgatorio:chests/ruina_de_ceniza"})
put(4, 3, 14, "chiseled_polished_blackstone")
put(6, 3, 14, "chiseled_polished_blackstone")
put(4, 4, 14, "candle", {"candles": "3", "lit": "true"})
put(6, 4, 14, "candle", {"candles": "2", "lit": "true"})
put(3, 3, 14, "chiseled_polished_blackstone")
put(3, 4, 14, "soul_lantern", {"hanging": "false"})
put(7, 3, 14, "chiseled_polished_blackstone")
put(7, 4, 14, "soul_lantern", {"hanging": "false"})
put(5, 3, 10, "skeleton_skull", {"rotation": "6"})
put(6, 3, 10, "bone_block", {"axis": "x"})
for _ in range(7):
    put(rng.randint(2, 8), rng.randint(5, 7), rng.randint(9, 14), "cobweb")

# 6) patio: brasero de almas rodeado de piedra
put(8, 3, 4, "soul_campfire", {"lit": "true", "signal_fire": "false", "facing": "north"})
for dx, dz in ((-1, 0), (1, 0), (0, -1), (0, 1)):
    put(8 + dx, 3, 4 + dz, "polished_blackstone_brick_slab", {"type": "bottom"})

# 7) ceniza: alfombras grises sobre el suelo libre
for x in range(1, W - 1):
    for z in range(1, D - 1):
        if inside(x, z) and (x, 3, z) not in grid and rng.random() < 0.22:
            put(x, 3, z, "gray_carpet" if rng.random() < 0.65 else "light_gray_carpet")

# ---------- entidades: los dos Acechadores ----------
def acechador(x, y, z, yaw):
    return {
        "blockPos": ints(int(x), int(y), int(z)),
        "pos": doubles(x, y, z),
        "nbt": {
            "id": "minecraft:husk",
            "Tags": NList("string", ["purgatorio.acechador"]),
            "CustomName": "Acechador de Ceniza",
            "CustomNameVisible": Byte(1),
            "PersistenceRequired": Byte(1),
            "DeathLootTable": "purgatorio:entities/acechador",
            "Health": Float(36.0),
            "Rotation": NList("float", [Float(yaw), Float(0.0)]),
            "Motion": doubles(0.0, 0.0, 0.0),
            "attributes": NList("compound", [
                {"id": "minecraft:max_health", "base": Double(36.0)},
                {"id": "minecraft:movement_speed", "base": Double(0.30)},
                {"id": "minecraft:attack_damage", "base": Double(4.0)},
            ]),
        },
    }


entities = [acechador(5.5, 3.0, 11.5, 180.0), acechador(8.5, 3.0, 6.5, 90.0)]

# ---------- serializar ----------
palette, index = [], {}
blocks = []
for (x, y, z), (name, props, nbt) in sorted(grid.items(), key=lambda kv: (kv[0][1], kv[0][2], kv[0][0])):
    key = (name, props)
    if key not in index:
        index[key] = len(palette)
        entry = {"id": "minecraft:" + name}
        if props:
            entry["properties"] = dict(props)
        palette.append(entry)
    b = {"pos": ints(x, y, z), "state": Int(index[key])}
    if nbt:
        b["nbt"] = nbt
    blocks.append(b)

root = {
    "DataVersion": Int(DATA_VERSION),
    "size": ints(W, H, D),
    "palette": NList("compound", palette),
    "blocks": NList("compound", blocks),
    "entities": NList("compound", entities),
}
write_nbt(OUT, root)
print("ok: %d bloques, %d estados, %d entidades -> %s" % (len(blocks), len(palette), len(entities), os.path.relpath(OUT)))
