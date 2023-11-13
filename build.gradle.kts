plugins {
    kotlin("jvm") version "1.8.20"
    id("java")
    id("application")
}

group = "pt.iscte.ambco"
version = "0.3.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.antlr:antlr4:4.13.1")
    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.25.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation(files("libs/strudel-0.8.1.jar"))
}

tasks.test {
    useJUnitPlatform()
}