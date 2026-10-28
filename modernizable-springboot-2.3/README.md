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

## Expected Secure Coder remediation
A single bump of the parent to `spring-boot-starter-parent:2.7.18` (and the
MySQL coordinate `mysql:mysql-connector-java` → `com.mysql:mysql-connector-j`,
BOM-managed), staying within the 2.x line with no `javax`→`jakarta` migration.
Clears the bulk of the 132 findings. This is the change merged upstream as
[robot-shop#1](https://github.com/niklaushirt/robot-shop/pull/1).

## Build
```
mvn -Dmaven.compiler.release=8 clean package
```
