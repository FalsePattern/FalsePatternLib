plugins {
    id("fpgradle-minecraft") version("0.1.3")
}

group = "com.falsepattern"

minecraft_fp {
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
        changelog = "https://github.com/FalsePattern/FalsePatternLib/releases/tag/{version}"
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
    maven {
        name = "horizon"
        url = uri("https://mvn.falsepattern.com/horizon/")
        content {
            includeGroup("com.gtnewhorizons.retrofuturabootstrap")
        }
    }
    maven {
        name = "jitpack"
        url = uri("https://mvn.falsepattern.com/jitpack/")
        content {
            includeGroup("com.github.LegacyModdingMC.UniMixins")
        }
    }
}

dependencies {
    compileOnly("com.gtnewhorizons.retrofuturabootstrap:RetroFuturaBootstrap:1.0.2")
    compileOnly("com.github.LegacyModdingMC.UniMixins:unimixins-all-1.7.10:0.1.17:dev")
}