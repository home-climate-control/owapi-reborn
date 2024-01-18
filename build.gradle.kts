plugins {
    java
    `java-library`
    jacoco
    alias(libs.plugins.errorprone)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.git.properties) apply false
    alias(libs.plugins.gradle.versions)
}

group = "com.homeclimatecontrol"
version = "2.0.2-SNAPSHOT"

apply(plugin = "java")
apply(plugin = "java-library")
apply(plugin = "maven-publish")
apply(plugin = "jacoco")
apply(plugin = "net.ltgt.errorprone")

if (project.parent == null) {
    // If this project is included as a submodule, this plugin chokes on non-existing ./.git
    // and produces very annoying unsuppressable output
    // See https://github.com/n0mer/gradle-git-properties/issues/175
    apply(plugin = libs.plugins.git.properties.get().pluginId)
}

tasks.compileJava {
    options.release = 11
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
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
