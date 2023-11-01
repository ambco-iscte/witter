import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

plugins {
    kotlin("jvm") version "1.8.20"
    id("java")
    id("application")
}

group = "pt.iscte.ambco"
version = "0.2.5-demo"

repositories {
    mavenCentral()
}

val mac = System.getProperty("os.name").toLowerCase().contains("mac")
val win = System.getProperty("os.name").toLowerCase().contains("windows")
val linux = System.getProperty("os.name").toLowerCase().contains("linux")

val os = if (mac)
    "macos"
else if (win)
    "windows"
else if (linux)
    "linux"
else
    "TODO"

fun resolutionSwt(
    dependencyResolveDetails: DependencyResolveDetails,
    buildGradle: Build_gradle
) {
    if (dependencyResolveDetails.requested.name.contains("\${osgi.platform}")) {
        val platform = if (buildGradle.mac) "cocoa.macosx.x86_64"
        else if (buildGradle.win) "win32.win32.x86_64"
        else if (buildGradle.linux) "gtk.linux.x86_64"
        else "TODO"
        dependencyResolveDetails.useTarget(
            dependencyResolveDetails.requested.toString()
                .replace("\${osgi.platform}", platform)
        )
    }
}

configurations.all {
    resolutionStrategy {
        eachDependency {
            resolutionSwt(this, this@Build_gradle)
        }
    }
}

dependencies {
    if (mac)
        api("org.eclipse.platform:org.eclipse.swt.cocoa.macosx.x86_64:3.124.0")
    else if (win)
        api("org.eclipse.platform:org.eclipse.swt.win32.win32.x86_64:3.124.0")
    else if (linux)
        api("org.eclipse.platform:org.eclipse.swt.gtk.linux.x86_64:3.124.0")
    //else TODO

    testImplementation(kotlin("test"))
    implementation("org.antlr:antlr4:4.13.1")
    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.25.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation(files("libs/strudel-0.8.1.jar"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}