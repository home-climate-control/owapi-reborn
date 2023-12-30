plugins {
    java
    `java-library`
    jacoco
    alias(libs.plugins.errorprone)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.git.properties)
    alias(libs.plugins.gradle.versions)
}

group = "com.homeclimatecontrol"
version = "2.0.0-SNAPSHOT"

apply(plugin = "java")
apply(plugin = "java-library")
apply(plugin = "maven-publish")
apply(plugin = "jacoco")
apply(plugin = "net.ltgt.errorprone")


tasks.compileJava {
    options.release = 11
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
        html.required = true
    }
}

tasks.test {
    useJUnitPlatform()
}

repositories {
    mavenCentral()
}

dependencies {

    api(libs.log4j.api)
    api(libs.log4j.core)
    implementation(libs.rxtx)

    testImplementation(libs.mockito)
    testImplementation(libs.junit5.api)
    testImplementation(libs.assertj.core)
    testRuntimeOnly(libs.junit5.engine)

    errorprone(libs.errorprone)
}

java {
    // Original Javadoc from DalSemi is too broken to fix
    //withJavadocJar()
    withSourcesJar()
}

artifacts {
    val sourcesJar by tasks.named("sourcesJar")
    add("archives", sourcesJar)
}

sonarqube {
    properties {
        property("sonar.projectKey", "home-climate-control_owapi-reborn")
        property("sonar.organization", "home-climate-control")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}
