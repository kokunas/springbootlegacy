#!/usr/bin/env python3
"""Compara dos directorios de evidencias (antes/despues de la migracion).

Cada fichero: linea 1 = codigo HTTP, resto = cuerpo. Si el cuerpo es JSON se
compara como JSON (el orden de las claves no cuenta: Jackson 3 las ordena
alfabeticamente). Las diferencias esperadas se declaran con --ignore NOMBRE
(p. ej. el health de Actuator, que ahora incluye "groups") y se muestran aparte.

Uso: compare_evidence.py ANTES DESPUES [--ignore nombre ...] [--rename antes=despues ...]
Sale con codigo 1 si hay alguna diferencia no declarada.
"""
import argparse, json, pathlib, sys

def load(p):
    status, _, body = p.read_text().partition("\n")
    body = body.strip()
    try:
        return status.strip(), json.loads(body)
    except ValueError:
        return status.strip(), body

ap = argparse.ArgumentParser()
ap.add_argument("before"); ap.add_argument("after")
ap.add_argument("--ignore", action="append", default=[])
ap.add_argument("--rename", action="append", default=[], help="antes=despues")
a = ap.parse_args()
ren = dict(r.split("=", 1) for r in a.rename)
bad = 0
for f in sorted(pathlib.Path(a.before).glob("*.txt")):
    name = f.stem
    g = pathlib.Path(a.after) / (ren.get(name, name) + ".txt")
    if not g.exists():
        print(f"FALTA     {name}"); bad += 1; continue
    b, c = load(f), load(g)
    if b == c:
        print(f"IGUAL     {name}")
    elif name in a.ignore:
        print(f"ESPERADA  {name}: {b} -> {c}")
    else:
        print(f"DIFIERE   {name}: {b} -> {c}"); bad += 1
sys.exit(1 if bad else 0)
