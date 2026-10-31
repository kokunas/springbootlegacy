# Bob skills for this repo

`springboot-modernization/` is the source of the Bob skill that migrates the Spring Boot modules to the
latest stable Spring Boot 4.x (see its `SKILL.md`). It lives here, **not** in `.bob/`, because the IBM
Concert Secure Coder PR step deletes `.bob/` from the workspace before committing, which would show the
skill as removed in its pull request.

## Install

```bash
mkdir -p ~/.bob/skills && cp -R docs/bob/springboot-modernization ~/.bob/skills/
```

To make the Secure Coder wizard apply it, also install an org policy rule in `~/.bob/rules/` (customer-added
rules are read by the plan, pre-check and execute steps). Without it, only a normal Bob chat that names the
skill will use it.
