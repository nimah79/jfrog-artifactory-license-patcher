# [JFrog Artifactory](https://jfrog.com/artifactory/) License Patcher

A javaagent that returns a hardcoded license for [JFrog Artifactory](https://jfrog.com/artifactory/).

Latest tested version: [7.161.26](https://docs.jfrog.com/releases/docs/artifactory-self-managed-releases#artifactory-716126-self-managed) (also compatible with 7.133.x and 7.173.x+)

**⚠️ YOU SHOULD NEVER USE THIS PROJECT TO CRACK/PATCH/ ILLEGAL USE JFROG ARTIFACTORY. THIS PROJECT IS FOR EDUCATIONAL PURPOSES! THE CONSEQUENCES CAUSED BY THE USE OF THIS SOFTWARE SHALL BE BORNE BY THE USER.**

## Fixed Issues

This patcher addresses all known issues from:
- [nimah79/jfrog-artifactory-license-patcher](https://github.com/nimah79/jfrog-artifactory-license-patcher/issues)
- [Lama3L9R/ArtifactoryKeygen](https://github.com/Lama3L9R/ArtifactoryKeygen/issues)

Specifically fixed:
- **Enterprise Plus license type** (not just Enterprise) — enables evidence collection, HA, all E+ features
- **"Invalid license: last block incomplete in decryption"** errors in jfac (Access service) — Keys Management, Vault, Integrations, System Import now work
- **Xray product in license** — `/artifactory/api/xray/license` returns valid license
- **LDAP group sync** — `isPro()` returns true with Enterprise Plus license
- **HA mode support** — license type satisfies HA requirements
- **Distribution/JFConnect/Pipelines/Metadata/Evidence** product IDs included in hardcoded license
- **Version robustness** — works across 7.133.x, 7.161.x, and 7.173.x+ (tested on 7.133.15 and 7.161.26)

## Installation

1. Start the services using [Docker Compose](https://docs.docker.com/compose/):

```shell
docker compose up
```

2. In the JFrog Artifactory licenses page, write any random text and click "Save".

![JFrog Artifactory licenses page](images/jfrog-artifactory-licenses-page.jpg)

## Building the javaagent

The prebuilt jar file of the javaagent is available in `jfrog-artifactory-license-patcher-1.0.jar`. However, if you have modified the source code, you can build the jar file using the following command:

```shell
mvn clean package
```

## Notes

- The agent uses `LoaderClassPath` to access webapp classes during bytecode transformation — this is essential for patching to work correctly.
- All patches are fail-soft: if a class/method doesn't exist in a specific version, it's logged and skipped without breaking startup.
- Online license validation is disabled (`validateOnline=false`) to avoid external JFrog entitlement service calls.
- Xray itself is a native application (Go) and requires separate binary patching for full functionality — this agent only fixes the Artifactory-side `/artifactory/api/xray/license` endpoint.