owapi-reborn
==

[![Gradle](https://github.com/home-climate-control/owapi-reborn/actions/workflows/gradle.yml/badge.svg)](https://github.com/home-climate-control/owapi-reborn/actions/workflows/gradle.yml)
[![CodeQL](https://github.com/home-climate-control/owapi-reborn/actions/workflows/codeql.yml/badge.svg)](https://github.com/home-climate-control/owapi-reborn/actions/workflows/codeql.yml)
[![SonarCloud](https://github.com/home-climate-control/owapi-reborn/actions/workflows/sonarcloud.yml/badge.svg)](https://github.com/home-climate-control/owapi-reborn/actions/workflows/sonarcloud.yml)  
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=home-climate-control_owapi-reborn&metric=alert_status)](https://sonarcloud.io/dashboard?id=home-climate-control_owapi-reborn)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=home-climate-control_owapi-reborn&metric=bugs)](https://sonarcloud.io/dashboard?id=home-climate-control_owapi-reborn)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=home-climate-control_owapi-reborn&metric=code_smells)](https://sonarcloud.io/dashboard?id=home-climate-control_owapi-reborn)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=home-climate-control_owapi-reborn&metric=coverage)](https://sonarcloud.io/dashboard?id=home-climate-control_owapi-reborn)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=home-climate-control_owapi-reborn&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=home-climate-control_owapi-reborn)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=home-climate-control_owapi-reborn&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=home-climate-control_owapi-reborn)

### Dallas Semiconductor 1-Wire API, on its own again

This code is based on the original Dallas Semiconductor `owapi` library version - when it was released (about 20 years ago).
At that point, Java dependency management mechanism was in its infancy, and the most sensible way to retain a potentially disappearing artifact
was to integrate it in its entirety.

Since then, it has been heavily reworked with stability and memory efficiency being the primary focus. Now that the project it was integrated with
([Home Climate Control](https://github.com/home-climate-control/dz)) is undergoing yet another transformation, it's time for this library
to be separated so that it can continue life on its own - surprisingly, there's still demand for 1-Wire operations from Java.

### Build

`./gradlew build`. Done.

### About those code quality badges...

It is what it is. `owapi` code was never a focus for independent improvement, the quality level was "good enough" (and it is, for its primary use case).
Now that the project is independent again, hopefully, that will change.

## BIG FAT WARNING: This is a subset.

This library contains just a limited subset of containers, deciding factor for retaining or dropping being usefulness for the [Home Climate Control](https://github.com/home-climate-control/dz) project. This subset is being maintained, others were dropped long time ago.

There are other copies of the original 1-Wire library code on the Internet, the most complete at the time being the one at https://github.com/onewire/onewire/tree/master/owapi - go there if you need a full set, but there are no guarantees about authenticity or heritage of that code. Fair warning, at the moment of writing it doesn't even build - bit rot is merciless. Good luck.

## Next Steps
* [Release Notes](./docs/release-notes.md)
