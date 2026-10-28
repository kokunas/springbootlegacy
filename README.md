# springbootlegacy — CVE-driven Spring Boot modernization (IBM Concert Secure Coder demo)

Two deliberately outdated Spring Boot services in one repo. The demo thesis:
**resolving the detected CVEs is the driver that modernizes Spring Boot.**
IBM Concert Secure Coder scans each service, and its remediation turns the CVE
backlog into a framework upgrade — either applied automatically, or forced as a
migration decision when the old line is beyond auto-repair.

| Component | Spring Boot | Vulns (trivy) | What Secure Coder does | Demo role |
|---|---|---|---|---|
| [`modernizable-springboot-2.3`](modernizable-springboot-2.3) | **2.3.3.RELEASE** | 132 (13 CRIT / 54 HIGH) | Bumps the Spring Boot parent to **2.7.18** in one PR, clearing the CVEs | **Success path** — CVE fix *is* the modernization |
| [`legacy-springboot-1.2`](legacy-springboot-1.2) | **1.2.8.RELEASE** | 142 (31 CRIT / 67 HIGH) | Pins leaf libs (jackson, snakeyaml, logback); framework CVEs can't be pinned | **Forcing function** — remediation demands a migration off EOL 1.x |

## The two stories

### 1. CVE resolution modernizes the framework (2.3 → 2.7)
`modernizable-springboot-2.3` is the robot-shop `shipping` service. Its 132
vulnerabilities (Tomcat 9.0.37, jackson-databind 2.11.2, Spring 5.2.8, …) all
have fixes **inside the Spring Boot 2.x line**. Secure Coder's remediation is a
single parent bump to `spring-boot-starter-parent:2.7.18`, which carries the app
forward ~2 years of framework releases while closing the CVEs. This is the exact
change Secure Coder shipped on the upstream repo
([niklaushirt/robot-shop#1](https://github.com/niklaushirt/robot-shop/pull/1),
"Fix 22 CVEs by upgrading Spring Boot to 2.7.18"). **Remediation = modernization,
applied automatically.**

### 2. CVEs force a migration the old line can't absorb (1.2)
`legacy-springboot-1.2` is pinned to Spring Boot 1.2.8 (2015). Its leaf-library
CVEs (jackson-databind 2.4.6, snakeyaml 1.14, logback 1.1.3) can be pinned, but
the **framework** CVEs — Spring4Shell (CVE-2022-22965) on spring-beans/webmvc
4.1.9, the spring-web deserialization RCE (CVE-2016-1000027), Tomcat Ghostcat
(CVE-2020-1938) — are only fixed in Spring 5.2.20+/6.x, i.e. Spring Boot 2/3.
There is no in-line patch. Secure Coder pins what it can and surfaces the rest as
a **"migrate off the end-of-life 1.x line"** decision. **Remediation becomes the
business case for modernization.**

## Scans
Each component ships its trivy scan (`cve-scan.trivy.json`) as the baseline
"before" inventory. Re-scan after Secure Coder's PR to show the delta.

## Next steps
1. Register each component in IBM Concert (target instance TBD).
2. Run a Secure Coder remediation action per component.
3. Review the generated PR + migration plan; re-scan to prove the CVE drop.
