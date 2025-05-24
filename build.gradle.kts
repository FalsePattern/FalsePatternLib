plugins {
    id("com.falsepattern.fpgradle-mc") version("0.15.1")
}

group = "com.falsepattern"

minecraft_fp {
    java {
        compatibility = jabel
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

repositories {
    exclusive(maven("horizon", "https://mvn.falsepattern.com/horizon/"), "com.gtnewhorizons.retrofuturabootstrap")
    exclusive(jitpack(), "com.github.LegacyModdingMC.UniMixins")
}

dependencies {
    compileOnly("com.gtnewhorizons.retrofuturabootstrap:RetroFuturaBootstrap:1.0.7")
    compileOnly("com.github.LegacyModdingMC.UniMixins:unimixins-all-1.7.10:0.1.19:dev")
}