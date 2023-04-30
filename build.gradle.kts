plugins {
    id("java")
    // ShadowJar (https://github.com/johnrengelman/shadow/releases)
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "me.zax71"
version = "1.1.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.panda-lang.org/releases")
}

dependencies {
    // Minestom
    implementation("com.github.Minestom.Minestom:Minestom:8ad2c7701f")

    // Kyori stuff (Adventure)
    implementation("net.kyori:adventure-text-serializer-plain:4.13.1")
    implementation("net.kyori:adventure-text-minimessage:4.13.0")
    implementation("org.spongepowered:configurate-hocon:4.1.2")

    // LiteCommands (command framework)
    implementation("dev.rollczi.litecommands:core:2.8.7")
    implementation("dev.rollczi.litecommands:minestom:2.8.7")

    // SQLite
    implementation("org.xerial:sqlite-jdbc:3.41.2.1")

}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "me.zax71.stomKor.Main"
        }
    }
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        mergeServiceFiles()
        archiveClassifier.set("") // Prevent the -all suffix
    }
}