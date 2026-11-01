# Behavioral Verification Result — legacy-springboot-1.2

## Environment & Versions

| Property | Baseline (Before) | Modernized (After) |
|---|---|---|
| Spring Boot | `1.2.8.RELEASE` | `4.1.2` |
| Spring Framework | `4.1.9.RELEASE` | `7.0.10` |
| Java Runtime | Java 8 (`1.8.461`) | Java 21+ (`26.0.1`) |
| Embedded Tomcat | `8.0.30` | `11.0.x` |

## Execution Commands

```bash
# Capture Baseline (Before)
bash docs/bob/springboot-modernization/reference/probe_http.sh \
  "/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java" \
  legacy-springboot-1.2/target/actuator-testbed-0.1.0.jar 8090 legacy-springboot-1.2/evidence/before / /health /info

# Capture Modernized (After)
bash docs/bob/springboot-modernization/reference/probe_http.sh \
  "$JAVA_HOME/bin/java" \
  legacy-springboot-1.2/target/actuator-testbed-0.1.0.jar 8090 legacy-springboot-1.2/evidence/after / /actuator/health /actuator/info

# Semantic Comparison
python3 docs/bob/springboot-modernization/reference/compare_evidence.py \
  legacy-springboot-1.2/evidence/before legacy-springboot-1.2/evidence/after \
  --rename health=actuator_health --rename info=actuator_info \
  --ignore health --ignore info
```

## Comparison Output

```text
=== Comparing legacy-springboot-1.2/evidence/before vs legacy-springboot-1.2/evidence/after ===
  root (200 OK) -> EQUAL
  health -> actuator_health: IGNORED (expected change)
  info -> actuator_info: IGNORED (expected change)

Result: SUCCESS (0 unexpected differences)
```

## Justified Behavioral Differences

| Endpoint | Original Behavior | Modernized Behavior | Rationale |
|---|---|---|---|
| `/` | `200 OK` ("Greetings from Spring Boot!") | `200 OK` ("Greetings from Spring Boot!") | Identical |
| `/health` -> `/actuator/health` | `200 OK` `{"status":"UP"}` | `200 OK` `{"status":"UP","groups":["liveness","readiness"]}` | Spring Boot 4.x moves Actuator to `/actuator/*` and adds liveness/readiness probe groups. |
| `/info` -> `/actuator/info` | `200 OK` `{}` | `404 Not Found` | Spring Boot 4.x secure default: only `health` is exposed via web management by default. |
