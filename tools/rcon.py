#!/usr/bin/env python3
"""Cliente RCON minimo (solo stdlib) para el servidor de PRUEBAS."""
import os, re, socket, struct, sys

DEST = os.environ.get("TEST_SERVER_DIR", os.path.expanduser("~/dev/test-server"))


def _props():
    out = {}
    with open(os.path.join(DEST, "server.properties")) as f:
        for line in f:
            if "=" in line and not line.startswith("#"):
                k, v = line.rstrip("\n").split("=", 1)
                out[k] = v
    return out


def _pkt(sock, rid, ptype, body):
    data = struct.pack("<ii", rid, ptype) + body.encode() + b"\x00\x00"
    sock.sendall(struct.pack("<i", len(data)) + data)


def _read(sock):
    raw = b""
    while len(raw) < 4:
        raw += sock.recv(4 - len(raw))
    (n,) = struct.unpack("<i", raw)
    body = b""
    while len(body) < n:
        body += sock.recv(n - len(body))
    rid, ptype = struct.unpack("<ii", body[:8])
    return rid, ptype, body[8:-2].decode(errors="replace")


def command(cmd, host="127.0.0.1"):
    p = _props()
    s = socket.create_connection((host, int(p["rcon.port"])), timeout=10)
    _pkt(s, 1, 3, p["rcon.password"])
    rid, _, _ = _read(s)
    if rid == -1:
        raise SystemExit("rcon: autenticacion fallida")
    _pkt(s, 2, 2, cmd)
    _, _, out = _read(s)
    s.close()
    return re.sub("§.", "", out)


if __name__ == "__main__":
    print(command(" ".join(sys.argv[1:])))
