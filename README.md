owapi-reborn
==

[![Gradle](https://github.com/home-climate-control/owapi-reborn/actions/workflows/gradle.yml/badge.svg)](https://github.com/home-climate-control/owapi-reborn/actions/workflows/gradle.yml)
[![CodeQL](https://github.com/home-climate-control/owapi-reborn/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/home-climate-control/owapi-reborn/actions/workflows/codeql-analysis.yml)
[![SonarCloud](https://github.com/home-climate-control/owapi-reborn/actions/workflows/sonarcloud.yml/badge.svg)](https://github.com/home-climate-control/owapi-reborn/actions/workflows/sonarcloud.yml)

### Dallas Semiconductor 1-Wire API, on its own again

This code is based on the original Dallas Semicondictor `owapi` library version - when it was released (about 20 years ago).
At that point, Java dependency management mechanism was in its infancy, and the most sensible way to retain a potentially disappearing artifact
was to integrate it in its entirety.

Since then, it has been heavily reworked with stability and memory efficiency being the primary focus. Now that the project it was integrated with
([Home Climate Control](https://github.com/home-climate-control/dz)) is undergoing yet another transformation, it's time for this library
to be separated so that it can continue life on its own - surprisingly, there's still demand for 1-Wire operations from Java.

### Status

Just split out. Cutting out hanging pieces, setting up CI. Come back tomorrow.

### About those code quality badges...

(coming soon) It is what it is. `owapi` code was never a focus for independent improvement, the quality level was "good enough" (and it is, for its primary use case).
Now that the project is independent again, hopefully, that will change.
