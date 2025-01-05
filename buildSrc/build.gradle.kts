plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.wagyourtail.xyz/releases") {
        name = "PaperMC Repository"
    }
    maven("https://maven.wagyourtail.xyz/snapshots") {
        name = "PaperMC Repository"
    }
}

dependencies {
    implementation("com.gradleup.shadow:shadow-gradle-plugin:8.3.5")
    implementation("gradle.plugin.org.cadixdev.gradle:licenser:0.6.1")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.25.0")
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:6.0.27")
    implementation("net.kyori:indra-git:3.1.3")
    implementation("io.github.patrick.remapper:io.github.patrick.remapper.gradle.plugin:1.4.2")
    implementation("io.freefair.gradle:lombok-plugin:8.11")
    implementation("xyz.wagyourtail.jvmdowngrader:xyz.wagyourtail.jvmdowngrader.gradle.plugin:1.2.1")

    implementation("commons-io:commons-io:2.18.0")
    implementation("org.apache.ant:ant:1.10.15")
    implementation("org.codehaus.plexus:plexus-utils:4.0.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)
}
