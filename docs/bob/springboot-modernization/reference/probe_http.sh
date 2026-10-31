#!/usr/bin/env bash
# Arranca un jar de Spring Boot, llama a las rutas indicadas y guarda cada
# respuesta (linea 1 = codigo HTTP, resto = cuerpo) en <dir_salida>/<ruta>.txt.
# Uso: probe_http.sh <java> <jar> <puerto> <dir_salida> <ruta> [<ruta> ...]
set -u
JAVA=$1; JAR=$2; PORT=$3; OUT=$4; shift 4
mkdir -p "$OUT"
"$JAVA" -jar "$JAR" > "$OUT/app.log" 2>&1 &
PID=$!
trap 'kill $PID 2>/dev/null' EXIT
for _ in $(seq 1 60); do
  curl -s -o /dev/null "http://127.0.0.1:$PORT/" && break
  kill -0 $PID 2>/dev/null || { echo "la app no arranco, ver $OUT/app.log"; exit 1; }
  sleep 1
done
for path in "$@"; do
  name=$(echo "$path" | sed 's#^/##; s#/#_#g'); name=${name:-root}
  { curl -s -o "$OUT/.body" -w '%{http_code}\n' "http://127.0.0.1:$PORT$path"; cat "$OUT/.body"; echo; } > "$OUT/$name.txt"
  echo "$path -> $(head -1 "$OUT/$name.txt")"
done
rm -f "$OUT/.body"
