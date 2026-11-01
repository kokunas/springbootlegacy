# springbootlegacy — CVE-driven Spring Boot modernization (IBM Concert Secure Coder demo)

Two deliberately outdated Spring Boot services in one repo. The demo thesis:
**resolving the detected CVEs is the driver that modernizes Spring Boot.**
IBM Concert Secure Coder scans each service and turns the CVE backlog into a
remediation PR. Out of the box that PR stays inside the current major line; with
the [`springboot-modernization`](docs/bob) skill it goes all the way to the
latest Spring Boot 4.x and proves the app still works.

| Component | Spring Boot | Vulns (trivy) | Stock Secure Coder | With the modernization skill |
|---|---|---|---|---|
| [`modernizable-springboot-2.3`](modernizable-springboot-2.3) | **2.3.3.RELEASE** | 132 (13 CRIT / 54 HIGH) | Same-major bump to **2.7.18** | Spring Boot **4.1.2**, 0 CRITICAL/HIGH |
| [`legacy-springboot-1.2`](legacy-springboot-1.2) | **1.2.8.RELEASE** | 142 (31 CRIT / 67 HIGH) | Same-major bump to **1.5.22** plus pins of leaf libs (jackson, snakeyaml, logback, tomcat) | Spring Boot **4.1.2**, 0 CRITICAL/HIGH |

## The two stories

### 1. Stock Secure Coder stays inside the major line
Secure Coder's package remediation is deliberately conservative: *same major
version only* and *only the findings in scope*. On this repo it produced one PR
that fixed 140 of the 265 scoped findings: `modernizable-springboot-2.3` went
2.3.3 → 2.7.18 and `legacy-springboot-1.2` went 1.2.8 → 1.5.22, with the leaf
libraries pinned by hand. Both apps keep running on end-of-life lines
(Spring Boot 1.5 / 2.7, `javax.*`, Spring Framework 4/5), and the CVEs whose fix
only exists in Spring Framework 6/7 (for example Spring4Shell, CVE-2022-22965,
on the 4.1.9 line) stay open.

### 2. The skill turns remediation into modernization
[`docs/bob/springboot-modernization`](docs/bob/springboot-modernization) is a Bob
skill (plus an organization policy rule, see [`docs/bob/README.md`](docs/bob/README.md))
that approves the major jump as an explicit customer exception. It migrates both
modules straight to Spring Boot 4.1.2 (Spring Framework 7, Tomcat 11, Jackson 3,
Hibernate 7, `jakarta.*`, Java 21), removes the old leaf-library pins, and
requires **before/after behaviour evidence** in `<module>/evidence/`: the same
HTTP calls against the original and the migrated app, with every difference
explained. A Trivy rescan of the CycloneDX SBOM must show 0 CRITICAL/HIGH.

## Behaviour changes to expect after the jump
- `legacy-springboot-1.2`: `/health` becomes `/actuator/health` (now with
  `liveness`/`readiness` groups) and `/info` is no longer exposed.
- JSON key order changes (Jackson 3 sorts alphabetically); content is identical.
- Jolokia is dropped (unsupported on Spring Boot 4).

## Scans
Each component ships its trivy scan (`cve-scan.trivy.json`) as the baseline
"before" inventory. Re-scan after the remediation PR to show the delta (use a CycloneDX SBOM with
`trivy sbom`: `trivy fs` on a Spring Boot fat JAR detects nothing).

## Next steps
1. Register each component in IBM Concert (target instance TBD).
2. Run a Secure Coder remediation action per component.
3. Review the generated PR + migration plan; re-scan to prove the CVE drop.
