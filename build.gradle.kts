plugins {
    kotlin("jvm") version "2.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "gay.nyaa"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly(files("../PurrCore/build/libs/purrcore-1.0.0.jar"))
    compileOnly(files("../PurrSkills/build/libs/purrskills-1.0.0.jar"))
    compileOnly(files("../PurrItems/build/libs/purritems-1.0.0.jar"))
    compileOnly(files("../PurrEconomy/build/libs/purreconomy-1.0.0.jar"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    testImplementation(files("../PurrCore/build/libs/purrcore-1.0.0.jar"))
    testImplementation(files("../PurrSkills/build/libs/purrskills-1.0.0.jar"))
    testImplementation(files("../PurrItems/build/libs/purritems-1.0.0.jar"))
    testImplementation(files("../PurrEconomy/build/libs/purreconomy-1.0.0.jar"))
}

tasks {
    shadowJar {
        archiveBaseName.set("PurrCollections")
        archiveClassifier.set("")
        archiveVersion.set("1.0.0")

        minimize()
    }

    build {
        dependsOn(shadowJar)
    }

    test {
        useJUnitPlatform()
    }
}

kotlin {
    jvmToolchain(21)
}
