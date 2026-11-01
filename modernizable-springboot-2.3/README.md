# modernizable-springboot-2.3 — robot-shop `shipping` (Spring Boot 2.3.3)

The Java/Spring Boot microservice from
[niklaushirt/robot-shop](https://github.com/niklaushirt/robot-shop), pinned to
`spring-boot-starter-parent:2.3.3.RELEASE` (Java 8). Used unchanged as the
**CVE-driven-modernization success path**.

## Vulnerability inventory (trivy, `cve-scan.trivy.json`)

| Severity | Count |
|---|---|
| CRITICAL | 13 |
| HIGH | 54 |
| MEDIUM | 54 |
| LOW | 11 |
| **Total** | **132** |

Top contributors: tomcat-embed-core 9.0.37 (42), jackson-databind 2.11.2 (15),
spring-webmvc 5.2.8 (12), logback-core 1.2.3 (8), snakeyaml 1.26 (7).

## Secure Coder remediation
- **Stock Secure Coder** (same-major rule): parent bump to
  `spring-boot-starter-parent:2.7.18` plus the BOM-managed MySQL coordinate
  `com.mysql:mysql-connector-j`. Closes part of the 132 findings; the app stays on
  Spring Framework 5 / `javax.*`.
- **With the `springboot-modernization` skill** (see [`docs/bob`](../docs/bob)):
  Spring Boot **4.1.2**, `javax.*` → `jakarta.*`, Hibernate 7, Tomcat 11, with
  before/after behaviour evidence in `evidence/`.

## Build
```
mvn -Dmaven.compiler.release=8 clean package
```
