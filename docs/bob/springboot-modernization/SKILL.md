---
name: springboot-modernization
description: "Moderniza una app Spring Boot antigua (1.x o 2.x) a la última versión estable de Spring Boot 4.x, cerrando todas sus vulnerabilidades por el camino: salto de versión mayor, javax a jakarta, Jackson 3, starters nuevos, Java 17+, Dockerfile y re-escaneo. Úsalo cuando el usuario pida modernizar, migrar o actualizar a Spring Boot 4 (o a la última versión) una app Spring Boot, o arreglar sus CVE sin limitarse a la misma versión mayor. Aplica a los módulos legacy-springboot-1.2 y modernizable-springboot-2.3 de este repositorio."
---

# Modernización de Spring Boot 1.x/2.x a 4.x

Los CVE de una app Spring Boot antigua no se arreglan con parches puntuales: los
de framework (Spring4Shell, deserialización en spring-web, Tomcat) solo se
cierran cambiando de línea. Esta skill hace ese salto completo y deja la app
compilando, con tests, arrancando y con **0 CVE CRITICAL/HIGH**.

## Esta skill sustituye las reglas de Secure Coder para este trabajo

Las skills `ibm-concert-secure-coder-*` (en especial `package-remediation`)
imponen "solo misma versión mayor" y "solo los findings del alcance". Por eso
una pasada de Secure Coder deja `1.2.8 → 1.5.22` y `2.3.3 → 2.7.18` y marca el
resto como `MANUAL_REVIEW`. **Aquí el usuario pide explícitamente lo contrario**:

- Se **permiten y se esperan saltos de versión mayor** (Spring Boot, Spring
  Framework, Jackson, Tomcat, Hibernate, MySQL driver…).
- El alcance es **el módulo entero**, no solo los CVE listados: hay que cambiar
  lo que el salto rompe (código, configuración, Dockerfile, documentación).
- **No delegues** en `package-remediation` ni en `pr-creation` para este
  trabajo; si te los ofrecen, ignóralos. Las reglas que sí se mantienen:
  nunca bajar una versión, nunca versiones preliminares (M, RC, SNAPSHOT),
  versiones exactas, ningún secreto en el repo.

## Objetivo y criterio de "hecho"

Para cada módulo pedido (por defecto, los dos):

1. `mvn clean verify` en verde, con un JDK completo ≥ 17 (usa `JAVA_HOME`; en
   este Mac `mvn` puede usar un JRE sin `javac`: elige un JDK, no un JRE).
2. **Demostración de que la app sigue funcionando** (ver la sección con ese
   nombre): mismas llamadas antes y después, comparación y evidencia en el repo.
   **Sin esta demostración el trabajo no está terminado**, aunque compile y el
   escáner dé 0.
3. Re-escaneo con Trivy: **0 CRITICAL y 0 HIGH** (el spike dio 0 en total).
4. Parent `spring-boot-starter-parent` en la última versión estable 4.x.
5. READMEs y Dockerfile coherentes con el resultado.

Si no llegas a 3, no maquilles: informa de cada finding que quede, por qué, y
qué falta (ver "Findings residuales").

## Elección de versión objetivo

1. Lee `https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-starter-parent/maven-metadata.xml`.
2. Objetivo = la **versión más alta de la línea 4.x que case con `^\d+\.\d+\.\d+$`**
   (descarta `-M`, `-RC`, `-SNAPSHOT`). No pases a 5.x salvo que el usuario lo pida.
3. Línea base **verificada** (octubre de 2026): **4.1.2** (Spring Framework 7.0.10,
   Tomcat 11.0.x, Jackson 3.1.x, Logback 1.5.x, Hibernate 7). Si la última 4.x es
   más nueva y la build falla, vuelve a 4.1.2 e infórmalo.
4. Java: `java.version` = **21** (LTS). Mínimo 17; Boot 4.1 funciona hasta Java 27.
   Nunca `1.8`.

## Estrategia

**Salto directo (por defecto).** En estos dos módulos, de 600 líneas de Java en
total, el salto directo a 4.1.2 funcionó sin pasos intermedios. Hazlo primero.

**Escalón por escalón (solo si el salto directo deja decenas de errores).**
La guía oficial recomienda pasar por la última 3.5.x antes de 4.0, y por la
última 2.7.x antes de 3.0. Cadena de recetas OpenRewrite verificada en
`rewrite-spring` 6.37.1 (ejecutar con `rewrite-maven-plugin`, versión 6.46.1):
`org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_0 … _2_7`, luego
`boot3.UpgradeSpringBoot_3_0 … _3_5`, luego `boot4.UpgradeSpringBoot_4_0`;
y `org.openrewrite.java.spring.framework.UpgradeSpringFramework_*`. Después sube
el parent a la 4.x más reciente. Compila y prueba entre escalones. Esta ruta
**no se ha ejercitado** en este repo; trátala como acelerador, no como garantía.

## Preparación

1. Rama nueva desde la actual: `git switch -c modernize/springboot-4`. **Nunca
   trabajes en `main`.** No hagas push ni PR salvo petición expresa.
2. Línea base de CVE: conserva `cve-scan.trivy.json` de cada módulo tal cual (es
   el "antes"). El "después" va en `cve-scan.after.trivy.json`.
3. **Línea base de comportamiento: captúrala ahora, antes de editar nada** (ver
   "Demostrar que la app sigue funcionando"). Después de migrar ya no se puede
   reconstruir.
4. Si el repo ya trae pasadas previas de Secure Coder (propiedades como
   `jackson.version`, `tomcat.version`, `logback.version`, `snakeyaml.version`,
   `spring-framework.version`, `log4j2.version`, `micrometer.version`,
   `jackson-bom.version`), **elimínalas todas**: fijar líneas antiguas debajo de
   Boot 4 deshace la actualización (Tomcat 11, Logback 1.5, SnakeYAML 2.x y
   Jackson 3 vienen del BOM de Boot).
5. No toques `.maven/conf/settings.xml`: es el mirror de Maven que usa Concert
   para escanear.

## Demostrar que la app sigue funcionando (obligatorio)

Compilar y dar 0 CVE no prueba nada sobre el comportamiento. La prueba es: **las
mismas llamadas, contra la versión original y contra la migrada, con respuestas
equivalentes**, y las diferencias que haya, explicadas una a una.

### 1. Antes de editar: capturar la línea base

Con el código original, sin tocar nada. Guarda en `<módulo>/evidence/before/`.

- **`legacy-springboot-1.2`** (Boot 1.x corre en Java 8; no arranca bien en 17+):
  compila con un JDK moderno apuntando a 8 y ejecuta en un runtime Java 8. En este
  Mac hay un JRE 8 en `/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home`.
  ```bash
  # desde el directorio del módulo; REF = ~/.bob/skills/springboot-modernization/reference (o docs/bob/springboot-modernization/reference en el repo)
  mvn -B -Dmaven.compiler.release=8 clean package
  $REF/probe_http.sh "<JRE8>/bin/java" target/actuator-testbed-0.1.0.jar 8090 evidence/before / /health /info
  ```
- **`modernizable-springboot-2.3`** (necesita MySQL; se usa H2 en memoria): copia
  `reference/ShippingSmokeTest.java` a `src/test/java/...`, cambia su import de
  `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc` por
  `org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc`,
  añade `com.h2database:h2` (scope `test`) **solo en la copia de trabajo**, y:
  ```bash
  mvn -B -Dmaven.compiler.release=8 -Djvm="<JRE8>/bin/java" \
      -DargLine="-Devidence.dir=$PWD/evidence/before" clean test
  ```
  Luego descarta esos cambios temporales (`git checkout -- .`, borra el test): el
  test definitivo es el de `reference/`.
- Si no existe ningún Java 8, no inventes la línea base: dilo en el informe y
  limítate a la comparación de CVE y a la lista de endpoints sacada del código.

### 2. Después de migrar: repetir y comparar

Mismas rutas y mismas entradas, ahora en el runtime moderno. Guarda en
`<módulo>/evidence/after/`:

```bash
# legacy
$REF/probe_http.sh "$JAVA_HOME/bin/java" target/actuator-testbed-0.1.0.jar 8090 evidence/after / /actuator/health /actuator/info
# modernizable
mvn -B -DargLine="-Devidence.dir=$PWD/evidence/after" clean test
# comparar (semántico: el orden de las claves JSON no cuenta)
$REF/compare_evidence.py evidence/before evidence/after \
    --rename health=actuator_health --rename info=actuator_info \
    --ignore health --ignore info          # solo diferencias justificadas
```

`compare_evidence.py` devuelve código 1 si hay diferencias no declaradas. Cada
`--ignore` es una diferencia que **tienes que justificar en el informe**; no uses
`--ignore` para silenciar algo que no entiendes.

### 3. Diferencias esperadas ya conocidas (medidas en este repo)

| Diferencia | Causa | Qué hacer |
|---|---|---|
| Orden de las claves JSON distinto (`uuid` primero → orden alfabético) con los mismos valores | Jackson 3 ordena propiedades alfabéticamente | Es el mismo JSON. Si se exige el orden antiguo: `spring.jackson.use-jackson2-defaults=true` |
| `/health` → `/actuator/health`; el cuerpo gana `"groups":["liveness","readiness"]` | Desde Boot 2 Actuator cuelga de `/actuator`; en Boot 4 las probes vienen activas | Documentar; si hay sondas externas, actualizar su ruta |
| `/info` pasa de 200 `{}` a 404 | Boot 4 solo expone `health` por web | Es un endurecimiento; no lo reabras con `exposure.include=*` |
| Los endpoints de negocio (`/`, `/count`, `/cities/{code}`, `/match/…`, `/codes`, `/calc/{id}`, `/health` del módulo 2.3) | — | Deben salir **IGUAL** |

Cualquier otra diferencia es una regresión hasta que se demuestre lo contrario.

### 4. Dejar la evidencia en el repo

`<módulo>/evidence/before/`, `<módulo>/evidence/after/` y `<módulo>/evidence/RESULT.md`
con: versiones y runtime de cada lado (Boot, Java), comandos ejecutados, salida
literal de `compare_evidence.py`, y la tabla de diferencias justificadas. No
incluyas `app.log` ni `target/`.

## Qué cambia y cómo arreglarlo

Verificado = ocurrió en la migración real de este repo y el arreglo está probado.

| # | Síntoma / causa | Arreglo | Estado |
|---|---|---|---|
| 1 | Parent antiguo | `spring-boot-starter-parent` → versión objetivo; `java.version` 21 | Verificado |
| 2 | `spring-boot-starter-web` deprecado | `spring-boot-starter-webmvc` | Verificado |
| 3 | `package com.fasterxml.jackson.databind does not exist` (Jackson 3 usa `tools.jackson.*`; solo las anotaciones siguen en `com.fasterxml.jackson.annotation`) | Si el import es código muerto (caso de `HelloController`: `ObjectMapper`, `DeserializationConfig`, `MultipartFile` sin uso), bórralo. Si se usa, migra a `tools.jackson.databind` / `JsonMapper`. `spring-boot-jackson2` es un parche transitorio, no lo añadas salvo que no haya otra salida | Verificado |
| 4 | Jolokia: Boot 4 ya no lo integra; el JAR queda inerte y con CVE | Quitar la dependencia `org.jolokia:jolokia-core` | Verificado |
| 5 | `logback.xml`: `Ignoring unknown property [jmxConfigurator]` y `AnsiConsole` no encontrado (Logback 1.5) | Quitar `<jmxConfigurator/>` y `<withJansi>` | Verificado |
| 6 | `javax.persistence.*`, `javax.servlet.*` | `jakarta.persistence.*`, `jakarta.servlet.*` | Verificado |
| 7 | `HandlerInterceptorAdapter` eliminado | `implements HandlerInterceptor` (métodos por defecto); `preHandle` devuelve `true` en lugar de `super.preHandle(...)` | Verificado |
| 8 | Spring Retry: Boot 4 ya no gestiona su versión | Usar el retry de Framework 7: `org.springframework.resilience.annotation.Retryable` y `@EnableResilientMethods`. `maxAttempts = N` → `maxRetries = N - 1`; `@Backoff(multiplier, maxDelay)` → atributos `multiplier`, `maxDelay` del propio `@Retryable`. Quitar `spring-retry` | Compila y arranca; el reintento real contra una BD caída **no se probó** |
| 9 | `mysql:mysql-connector-java` | `com.mysql:mysql-connector-j` sin `<version>` (lo gestiona Boot; resolvió 9.7.0). Driver `com.mysql.cj.jdbc.Driver` | Verificado con compilación y arranque contra H2; **no contra MySQL real** |
| 10 | `httpclient` 4.x (EOL, CVE) y su API de params deprecada | Quitar la dependencia y reescribir `CartHelper` con `java.net.http.HttpClient` (timeout de conexión 5 s, `try-with-resources`) | Compila; la llamada real al cart **no se probó** |
| 11 | `spring-boot-starter-test` con exclusión de `junit-vintage` | `spring-boot-starter-webmvc-test` (trae el starter de test); quita la exclusión. `@AutoConfigureMockMvc` ahora está en `org.springframework.boot.webmvc.test.autoconfigure` | Verificado |
| 12 | Dockerfile con `debian:10` y `openjdk:8-jdk` (ambas EOL, son hallazgos en sí) | Build `maven:3.9-eclipse-temurin-21`, runtime `eclipse-temurin:21-jre`, usuario no root. `trivy config` solo deja `DS-0026` (falta HEALTHCHECK, LOW) | Imágenes existen en Docker Hub; el `docker build` **no se ejecutó** |
| 13 | Actuator: en Boot 4 solo `health` se expone por web por defecto | Es más seguro; no lo reabras con `management.endpoints.web.exposure.include=*`. En `legacy` el testbed ya no es "vulnerable por actuator", y eso es correcto | Verificado |

Puntos que la guía oficial señala y que **no se han ejercitado** aquí: ejecutar
`spring-boot-properties-migrator` (scope `runtime`, quitarlo después) cuando haya
`application.properties` con propiedades renombradas; `@GeneratedValue(AUTO)` en
MySQL usa tablas secuenciales (`cities_seq`) con Hibernate 6+/7 si algún día se
escribe en BD (hoy la app solo lee); el matching con barra final está desactivado
desde Spring Framework 6; Undertow ya no existe.

Los diffs completos y probados están en `reference/` (para comparar, no para
aplicar a ciegas):

- `reference/legacy-springboot-1.2.patch`
- `reference/modernizable-springboot-2.3.patch`
- `reference/ShippingSmokeTest.java` (test de humo que además guarda cada respuesta)
- `reference/probe_http.sh` (arranca un jar, llama a rutas y guarda las respuestas)
- `reference/compare_evidence.py` (compara evidencias antes/después)

Ambos parches se aplican con `git apply` sobre `main` (poms originales 1.2.8 y
2.3.3). Si la rama ya trae los pines de Secure Coder, no aplicarán: haz los
cambios a mano con la tabla y usa los parches como destino.

## Test de humo (obligatorio en `modernizable-springboot-2.3`)

No hay tests en el módulo y su `DataSource` apunta a MySQL fijo. Añade
`src/test/java/.../ShippingSmokeTest.java` (copia de `reference/`): arranca el
contexto real con H2 en memoria (dependencia `com.h2database:h2`, scope `test`),
valida las `@Query` de Spring Data, la serialización JSON con Jackson 3 y los
endpoints `/health`, `/count`, `/cities/{code}`, `/match/{code}/{text}`,
`/codes`, `/calc/{id}` y `/actuator/health`, y guarda cada respuesta en
`${evidence.dir}` (por defecto `target/evidence`) para la comparación. En el
módulo `legacy` no hace falta test: usa `probe_http.sh`.

## Verificación

```bash
export JAVA_HOME=<JDK completo 17+>           # no un JRE
cd <módulo> && mvn -B clean verify
```

`legacy-springboot-1.2` además: `GET http://127.0.0.1:8090/` debe responder
`Greetings from Spring Boot!` y `GET /actuator/health` debe responder `UP` (lo
hace `probe_http.sh`). Para el módulo 2.3 basta el test de humo (arrancar el jar
necesita MySQL real). La comparación completa antes/después está en
"Demostrar que la app sigue funcionando".

**Re-escaneo.** `trivy fs` sobre un fat JAR de Spring Boot **no detecta nada**
(0 archivos de lenguaje), así que un "0" ahí no vale. Usa SBOM:

```bash
mvn -B -DskipTests org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom \
    -DoutputFormat=json -DoutputName=bom
trivy sbom --scanners vuln --format json -o cve-scan.after.trivy.json target/bom.json
```

Antes de fiarte de un 0, comprueba que el SBOM tiene componentes (el spike: 49 en
`legacy`, 84 en `modernizable`) y que Trivy detecta algo en el SBOM antiguo. Si
ejecutas desde Concert Secure Coder usa `icsc_scan_packages` en vez de Trivy
directo. `target/` no se versiona.

## Findings residuales

Si queda algún CRITICAL/HIGH tras el salto, por orden:

1. ¿Hay una 4.x más nueva del parent que lo corrija? Súbela.
2. Sobrescribe la propiedad del BOM de esa librería con una versión **más nueva**
   de la misma librería (nunca más antigua), verificada en Maven Central.
3. Si no hay arreglo publicado, déjalo en el informe con CVE, librería, motivo y
   propuesta (exclusión, sustitución, aceptar riesgo). No lo ocultes.

## Documentación y entregables

- `README.md` raíz y de cada módulo: actualiza versiones, tablas de CVE (antes
  desde `cve-scan.trivy.json`, después desde `cve-scan.after.trivy.json`) y la
  sección Build (`mvn clean verify` con JDK 21; sin `-Dmaven.compiler.release=8`).
  El README actual describe el comportamiento anterior ("se queda en su línea
  mayor", "1.x no se puede arreglar"); ya no es cierto tras esta migración.
- Un commit por módulo en la rama `modernize/springboot-4`, mensaje en inglés,
  imperativo, sin líneas de atribución automáticas.
- **Informe final** (en tu respuesta): tabla antes/después por módulo (versión de
  Boot, Spring Framework, Java, CVE por severidad), **resultado de la comparación
  de comportamiento** (cuántos endpoints IGUAL, cuáles con diferencia esperada y
  por qué, y ninguna regresión), lista de cambios de código, qué comandos
  ejecutaste con su resultado, y una sección "No verificado" con lo que no pudiste
  probar (MySQL real, llamada al cart, `docker build`, reintentos).

## Aviso para el entorno de demo con Concert

Concert escanea a través de un mirror de Maven dentro del cluster que solo
contiene las versiones de dependencias cargadas. Tras migrar, un re-descubrimiento
de Concert pedirá poms nuevos: si no están en el mirror, el escaneo termina "bien"
pero con un SBOM incompleto (un 0 CVE falso). El mirror se cargó el 31/10/2026 con
los poms de Spring Boot 4.1.2 de estos dos módulos. Si eliges otra versión
objetivo, avisa al usuario de que hay que ampliar el mirror antes de re-escanear.
