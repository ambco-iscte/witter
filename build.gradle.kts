plugins {
    kotlin("jvm") version "1.8.20"
    id("java")
    id("application")
}

group = "pt.iscte.ambco"
version = "0.5.6"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testApi("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testApi("org.junit.platform:junit-platform-suite:1.9.2")
    implementation("org.antlr:antlr4:4.13.1")
    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.25.8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-html:0.8.0")
    api(project(":strudel"))
}

tasks.test {
    useJUnitPlatform()
}