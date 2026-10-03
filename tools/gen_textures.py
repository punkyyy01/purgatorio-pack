#!/usr/bin/env python3
"""Genera las texturas 16x16 de prueba (arte provisional) sin dependencias externas.
Salida: core/src/main/resources/assets/purgatorio/textures/item/*.png"""
import os, struct, zlib

OUT = os.path.join(os.path.dirname(__file__), "..", "core", "src", "main", "resources",
                   "assets", "purgatorio", "textures", "item")

PAL = {
    ".": None,
    "k": (24, 20, 28, 255),      # contorno
    "g": (88, 84, 96, 255),      # hoja gris
    "G": (150, 146, 158, 255),   # brillo hoja
    "e": (222, 96, 32, 255),     # brasa
    "E": (255, 190, 64, 255),    # brasa clara
    "b": (92, 56, 36, 255),      # empunadura
    "B": (140, 92, 60, 255),
}

SWORD = [
    "..............kk",
    ".............kGk",
    "............kGgk",
    "...........kGgk.",
    "..........kGgk..",
    ".........kGgk...",
    "..k.....kGgk....",
    "..kk...kEgk.....",
    "...kk.kegk......",
    "....kkeek.......",
    ".....kek........",
    "....kBbk........",
    "...kBbk.k.......",
    "..kBbk..........",
    ".kbk............",
    "kk..............",
]

SHARD = [
    "................",
    "................",
    ".......kk.......",
    "......kEEk......",
    ".....kEEEek.....",
    ".....kEeeek.....",
    "....kEEeeeek....",
    "....kEeeeeek....",
    "....kEeeekek....",
    ".....keeekk.....",
    ".....kekek......",
    "......kkk.......",
    "................",
    "................",
    "................",
    "................",
]


def png(rows):
    raw = b""
    for row in rows:
        raw += b"\x00"
        for ch in row:
            c = PAL[ch]
            raw += bytes(c) if c else b"\x00\x00\x00\x00"

    def chunk(tag, data):
        body = tag + data
        return struct.pack(">I", len(data)) + body + struct.pack(">I", zlib.crc32(body) & 0xFFFFFFFF)

    return (b"\x89PNG\r\n\x1a\n"
            + chunk(b"IHDR", struct.pack(">IIBBBBB", 16, 16, 8, 6, 0, 0, 0))
            + chunk(b"IDAT", zlib.compress(raw, 9))
            + chunk(b"IEND", b""))


os.makedirs(OUT, exist_ok=True)
for name, rows in (("colmillo_de_ceniza", SWORD), ("esquirla_de_brasa", SHARD)):
    assert len(rows) == 16 and all(len(r) == 16 for r in rows), name
    with open(os.path.join(OUT, name + ".png"), "wb") as f:
        f.write(png(rows))
    print("ok", name)
