@file:Suppress("UnstableApiUsage", "PropertyName")

import org.polyfrost.gradle.util.noServerRunConfigs

plugins {
    id("org.polyfrost.multi-version")
    id("org.polyfrost.defaults.repo")
    id("org.polyfrost.defaults.java")
    id("org.polyfrost.defaults.loom")
    java
}

val mod_name: String by project
val mod_version: String by project
val mod_id: String by project
val mod_archives_name: String by project


version = mod_version
group = "dev.movi"

base {
    archivesName.set("$mod_archives_name-$platform")
}

loom {
    noServerRunConfigs()
    if (project.platform.isLegacyForge) {
        runConfigs {
            "client" {
                programArgs("--tweakClass", "cc.polyfrost.oneconfig.loader.stage0.LaunchWrapperTweaker")
                property("mixin.debug.export", "true")
            }
        }
    }
    if (project.platform.isForge) {
        forge {
            mixinConfig("mixins.${mod_id}.json")
        }
    }
    mixin.defaultRefmapName.set("mixins.${mod_id}.refmap.json")
}

val shade: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

java { withSourcesJar() }

val generatedVersionDirectory = layout.buildDirectory.dir("generated/pikastatsVersion")
val generateVersion by tasks.registering {
    inputs.property("version", mod_version)
    outputs.dir(generatedVersionDirectory)
    val output = generatedVersionDirectory.map { it.file("dev/movi/pikastats/BuildInfo.java") }
    val versionText = mod_version
    doLast {
        output.get().asFile.apply {
            parentFile.mkdirs()
            writeText("package dev.movi.pikastats;\npublic final class BuildInfo { public static final String VERSION = \"$versionText\"; private BuildInfo() {} }\n")
        }
    }
}

sourceSets {
    main {
        java.srcDir(generateVersion)
        output.setResourcesDir(java.classesDirectory)
    }
}

repositories {
    mavenCentral()
    maven("https://repo.polyfrost.org/releases")
    maven("https://repo.spongepowered.org/maven")
}

dependencies {
    modCompileOnly("cc.polyfrost:oneconfig-$platform:0.2.2-alpha228")
    modRuntimeOnly("me.djtheredstoner:DevAuth-forge-legacy:1.2.0") { isTransitive = false }
    if (platform.isLegacyForge) {
        compileOnly("org.spongepowered:mixin:0.7.11-SNAPSHOT")
        shade("cc.polyfrost:oneconfig-wrapper-launchwrapper:1.0.0-beta17")
    }
}

tasks {
    processResources {
        val resourceProperties = mapOf(
            "id" to mod_id,
            "name" to mod_name,
            "version" to mod_version,
            "mcVersionStr" to project.platform.mcVersionStr
        )
        inputs.properties(resourceProperties)
        filesMatching(listOf("mcmod.info", "mixins.${mod_id}.json")) {
            expand(resourceProperties)
        }
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    val bundledJar by registering(Jar::class) {
        archiveClassifier.set("dev")
        from(sourceSets.main.get().output)
        from(provider { shade.files.map { zipTree(it) } })
        exclude("META-INF/*.SF", "META-INF/*.RSA", "META-INF/*.DSA")
        manifest.attributes(mapOf("ModSide" to "CLIENT", "ForceLoadAsMod" to true,
            "TweakOrder" to "0", "MixinConfigs" to "mixins.${mod_id}.json",
            "TweakClass" to "cc.polyfrost.oneconfig.loader.stage0.LaunchWrapperTweaker"))
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    remapJar {
        inputFile.set(bundledJar.get().archiveFile)
        archiveClassifier.set("")
    }

    jar {
        dependsOn(bundledJar)
        enabled = false
    }
}

// These checks use a standalone main rather than a JUnit engine.
val regression by sourceSets.creating {
    java.setSrcDirs(listOf(rootProject.file("src/regression/java")))
    compileClasspath += sourceSets.main.get().output + sourceSets.main.get().compileClasspath
    runtimeClasspath += output + compileClasspath
}
val regressionChecks by tasks.registering(JavaExec::class) {
    dependsOn(tasks.named(regression.classesTaskName))
    classpath = regression.runtimeClasspath
    mainClass.set("dev.movi.pikastats.RegressionChecks")
    jvmArgs("-Djava.awt.headless=true")
}
tasks.check { dependsOn(regressionChecks) }

// Opt-in real OpenGL validation; the regular build remains headless.
tasks.register<JavaExec>("renderStateSmoke") {
    dependsOn(tasks.named(regression.classesTaskName))
    classpath = regression.runtimeClasspath
    mainClass.set("dev.movi.pikastats.RenderStateSmoke")
    providers.gradleProperty("lwjglNatives").orNull?.let {
        systemProperty("org.lwjgl.librarypath", it)
    }
}
