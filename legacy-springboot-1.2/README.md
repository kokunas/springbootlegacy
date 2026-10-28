# springboot-1.2-cve — Spring Boot 1.2.8 CVE testbed (IBM Concert Secure Coder demo)

Deliberately outdated Spring Boot app, pinned to **Spring Boot 1.2.8.RELEASE**
(2015), used to exercise **IBM Concert Secure Coder** remediation + migration
planning against a legacy, end-of-life framework line.

Based on [`veracode-research/actuator-testbed`](https://github.com/veracode-research/actuator-testbed),
trimmed for a clean 1.2.8 build:
- Parent pinned to `spring-boot-starter-parent:1.2.8.RELEASE`.
- Removed Spring Cloud (Camden) + Eureka client — those are aligned to Boot
  1.4/1.5 and don't resolve cleanly against 1.2.x.
- Kept `spring-boot-starter-web`, `spring-boot-starter-actuator`, `jolokia-core`.

## Vulnerability inventory

Trivy scan of the resolved dependency tree (see `cve-scan.trivy.json`):

| Severity | Count |
|---|---|
| CRITICAL | 31 |
| HIGH | 67 |
| MEDIUM | 35 |
| LOW | 9 |
| **Total (unique id×pkg)** | **142** |

Top contributors:

| Package | Version | Vulns |
|---|---|---|
| com.fasterxml.jackson.core:jackson-databind | 2.4.6 | 52 |
| org.apache.tomcat.embed:tomcat-embed-core | 8.0.30 | 21 |
| org.springframework:spring-webmvc | 4.1.9.RELEASE | 12 |
| ch.qos.logback:logback-core | 1.1.3 | 9 |
| org.springframework:spring-expression | 4.1.9.RELEASE | 8 |
| org.yaml:snakeyaml | 1.14 | 8 |
| org.springframework:spring-web | 4.1.9.RELEASE | 6 |

Plus the classic framework CVEs: **Spring4Shell (CVE-2022-22965)** on
spring-beans/spring-webmvc 4.1.9, **spring-web deserialization RCE
(CVE-2016-1000027)**, **Tomcat Ghostcat (CVE-2020-1938)**, and the full
jackson-databind deserialization catalogue.

## What Secure Coder can and cannot fix here (the point of the 1.2 demo)

Secure Coder's Java remediations are dependency version bumps with a
compatibility assessment; observed behaviour keeps apps **within their major
Spring Boot line**. On a 1.2.8 base that splits the inventory in two:

- **Fixable by version pin (no framework migration):** jackson-databind
  (fixes exist in the 2.6–2.9 line), snakeyaml, logback. Expect explicit
  `<version>` overrides, like Secure Coder did on `banco-kokunas#15`.
- **NOT fixable without leaving 1.x:** Spring4Shell and the spring-web RCE
  require Spring 5.2.20+/5.3.18+ (Boot 2.x) or 6.x (Boot 3.x); embedded Tomcat
  needs 8.5/9.0. These are the findings that force the **"migrate off the EOL
  1.x line"** recommendation — which is exactly what this testbed is meant to
  surface.

## Build

Requires a JDK 8 **compiler** (only a JRE 8 is on this Mac; compile was verified
with a newer JDK targeting `--release 8`). Runtime (embedded Tomcat 8.0.30)
needs JDK 7/8.

```
mvn -Dmaven.compiler.release=8 clean package
```

## Next steps (pending environment confirmation)

1. Push to a GitHub org that Secure Coder can open PRs against.
2. Register the repo as a Component in the target IBM Concert instance.
3. Run a Secure Coder remediation action and review the generated PR + plan.
