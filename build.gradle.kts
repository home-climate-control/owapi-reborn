plugins {
    java
    `java-library`
    jacoco
    id("net.ltgt.errorprone")
    id("org.sonarqube")
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
    toolVersion = "0.8.11"
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

    api("org.apache.logging.log4j:log4j-api:2.20.0")
    api("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("org.rxtx:rxtx:2.1.7")

    testImplementation("org.mockito:mockito-core:3.12.4")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.assertj:assertj-core:3.21.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")

    errorprone("com.google.errorprone:error_prone_core:2.14.0")
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
