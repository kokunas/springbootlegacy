# Behavioral Verification Result — modernizable-springboot-2.3

## Environment & Versions

| Property | Baseline (Before) | Modernized (After) |
|---|---|---|
| Spring Boot | `2.3.3.RELEASE` | `4.1.2` |
| Spring Framework | `5.2.8.RELEASE` | `7.0.10` |
| Java Runtime | Java 8 (`1.8.461`) | Java 21+ (`26.0.1`) |
| Hibernate ORM | `5.4.20.Final` | `7.4.13.Final` |
| Embedded Tomcat | `9.0.37` | `11.0.x` |

## Execution Commands

```bash
# Capture Baseline (Before)
cd modernizable-springboot-2.3 && mvn -B -Dmaven.compiler.release=8 \
  -Djvm="/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java" \
  -DargLine="-Devidence.dir=$PWD/evidence/before" clean test

# Capture Modernized (After)
cd modernizable-springboot-2.3 && mvn -B \
  -DargLine="-Devidence.dir=$PWD/evidence/after" clean test

# Semantic Comparison
python3 docs/bob/springboot-modernization/reference/compare_evidence.py \
  modernizable-springboot-2.3/evidence/before modernizable-springboot-2.3/evidence/after \
  --ignore actuator-health
```

## Comparison Output

```text
=== Comparing modernizable-springboot-2.3/evidence/before vs modernizable-springboot-2.3/evidence/after ===
  ESPERADA  actuator-health: ('200', {'status': 'UP'}) -> ('200', {'groups': ['liveness', 'readiness'], 'status': 'UP'})
  IGUAL     calc
  IGUAL     cities
  IGUAL     codes
  IGUAL     count
  IGUAL     health
  IGUAL     match

Result: SUCCESS (0 unexpected differences)
```

## Endpoint Comparison Matrix

| Endpoint | Original Behavior | Modernized Behavior | Verdict |
|---|---|---|---|
| `/health` | `200 OK` ("OK") | `200 OK` ("OK") | **IDENTICAL** |
| `/count` | `200 OK` ("1") | `200 OK` ("1") | **IDENTICAL** |
| `/cities/DE` | `200 OK` (`[{"uuid":1,"code":"DE","city":"Dusseldorf",...}]`) | `200 OK` (`[{"uuid":1,"code":"DE","city":"Dusseldorf",...}]`) | **IDENTICAL** |
| `/match/DE/Dus` | `200 OK` (`[{"uuid":1,"code":"DE","city":"Dusseldorf",...}]`) | `200 OK` (`[{"uuid":1,"code":"DE","city":"Dusseldorf",...}]`) | **IDENTICAL** |
| `/codes` | `200 OK` | `200 OK` | **IDENTICAL** |
| `/calc/{id}` | `200 OK` (`{"distance":30,"cost":1.5}`) | `200 OK` (`{"distance":30,"cost":1.5}`) | **IDENTICAL** |
| `/actuator/health` | `200 OK` `{"status":"UP"}` | `200 OK` `{"status":"UP","groups":["liveness","readiness"]}` | **EXPECTED** (Spring Boot 4.x probe groups) |
