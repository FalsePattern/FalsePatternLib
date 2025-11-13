plugins {
    id("com.falsepattern.fpgradle-mc") version("3.1.0")
}

group = "com.falsepattern"

//bump this after ANY change to the deploader!
val deploaderVersion = 2

minecraft_fp {
    java {
        compatibility = jvmDowngrader
        jvmDowngraderShade = projectIsLgpl21PlusCompatible
        version = JavaVersion.VERSION_17
    }
    mod {
        modid = "falsepatternlib"
        name = "FalsePatternLib"
        rootPkg = "$group.lib"
    }
    api {
        packages = listOf("asm", "compat", "config", "dependencies", "mapping", "mixin", "optifine", "text", "toasts", "turboasm", "util")
        packagesNoRecurse = listOf(".")
    }

    core {
        coreModClass = "internal.asm.CoreLoadingPlugin"
    }

    tokens {
        tokenClass = "internal.Tags"
        modid = "MODID"
        name = "MODNAME"
        version = "VERSION"
        rootPkg = "GROUPNAME"
    }

    publish {
        changelog = "https://github.com/FalsePattern/FalsePatternLib/releases/tag/$version"
        maven {
            repoUrl = "https://mvn.falsepattern.com/releases/"
            repoName = "mavenpattern"
        }
        curseforge {
            projectId = "665627"
        }
        modrinth {
            projectId = "eGLBEILf"
        }
    }
}

val java8Compiler = javaToolchains.compilerFor {
    languageVersion = JavaLanguageVersion.of(8)
    vendor = JvmVendorSpec.ADOPTIUM
}

fun createSourceSet(name: String, shareDeps: Boolean): SourceSet {
    val set = sourceSets.create(name) {
        if (shareDeps) {
            compileClasspath += sourceSets["patchedMc"].output
        }
    }
    tasks.named<JavaCompile>(set.compileJavaTaskName).configure {
        javaCompiler = java8Compiler
    }
    afterEvaluate {
        tasks.named<JavaCompile>(set.compileJavaTaskName).configure {
            this.sourceCompatibility = "8"
            this.targetCompatibility = "8"
            this.options.release = null
        }
    }
    if (shareDeps) {

        configurations.named(set.compileClasspathConfigurationName) {
            extendsFrom(configurations.getByName("compileClasspath"))
            exclude("com.falsepattern", "falsepatternlib-mc1.7.10")
        }
        configurations.named(set.runtimeClasspathConfigurationName) {
            extendsFrom(configurations.getByName("runtimeClasspath"))
            exclude("com.falsepattern", "falsepatternlib-mc1.7.10")
        }
        configurations.named(set.annotationProcessorConfigurationName) {
            extendsFrom(configurations.getByName("annotationProcessor"))
            exclude("com.falsepattern", "falsepatternlib-mc1.7.10")
        }
    }

    return set
}

val depLoader = createSourceSet("deploader", true)

val depLoaderJar = tasks.register<Jar>(depLoader.jarTaskName) {
    from(depLoader.output)
    archiveBaseName = "falsepatternlib-mc1.7.10"
    archiveVersion = minecraft_fp.mod.version
    archiveClassifier = "deploader"
    manifest {
        attributes("FPLib-Deploader-Version" to deploaderVersion)
    }
}

val depLoaderStub = createSourceSet("deploaderStub", true)

val depLoaderStubJar = tasks.register<Jar>(depLoaderStub.jarTaskName) {
    from(depLoaderStub.output)
    archiveBaseName = "falsepatternlib-mc1.7.10"
    archiveVersion = minecraft_fp.mod.version
    archiveClassifier = "deploader_stub"
}

sourceSets["main"].compileClasspath += depLoader.output
sourceSets["main"].compileClasspath += depLoaderStub.output

afterEvaluate {
    for (outgoingConfig in listOf("runtimeElements", "apiElements")) {
        val outgoing = configurations.getByName(outgoingConfig)
        outgoing.outgoing.artifact(depLoaderStubJar)
        outgoing.outgoing.artifact(depLoaderJar)
    }
}

tasks.jar {
    dependsOn(depLoaderJar, depLoaderStubJar)
    from(depLoaderJar.map { it.archiveFile }) {
        rename { "fplib_deploader.jar" }
    }
    from(zipTree(depLoaderStubJar.map { it.archiveFile })) {
        exclude("META-INF/MANIFEST.MF")
    }
}

tasks.processResources {
    val ver = minecraft_fp.mod.version
    filesMatching("META-INF/deps.json") {
        expand("modVersion" to ver.get())
    }
}

repositories {
    exclusive(maven("horizon", "https://mvn.falsepattern.com/horizon/"), "com.gtnewhorizons.retrofuturabootstrap")
    exclusive(horizon()) {
        includeModule("io.github.legacymoddingmc", "unimixins")
    }
}

dependencies {
    compileOnly("com.gtnewhorizons.retrofuturabootstrap:RetroFuturaBootstrap:1.0.7")
    compileOnly("io.github.legacymoddingmc:unimixins:0.1.23:dev")
}