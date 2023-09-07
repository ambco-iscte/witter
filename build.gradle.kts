plugins {
    kotlin("jvm") version "1.8.20"
    id("java")
    id("application")
}

group = "iscte.ambco"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.antlr:antlr4:4.12.0")
    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.25.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation(project(":strudel"))
}

tasks.test {
    useJUnitPlatform()
}