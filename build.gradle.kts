plugins {
    kotlin("jvm") version "1.8.20"
    id("java")
    id("application")
}

group = "pt.iscte.ambco"
version = "0.2.2"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.antlr:antlr4:4.13.1")
    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.25.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation(project(":strudel"))
}

tasks.test {
    useJUnitPlatform()
}