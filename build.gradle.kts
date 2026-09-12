plugins {
    kotlin("jvm") version "2.1.0"
    // Shadow removed — manual fatJar used to avoid ASM 65 issue (shadow 8.1.1 can't read Java 21).
    // If you want relocation/minimize, add org.gradle.shadow 8.3.x + re-enable the shadow block below.
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

// Manual fatJar — bundles runtimeClasspath (kotlin stdlib) without shadow ASM.
// No relocation/minimize (add shadow 8.3.x if you need them).
val shadowJar by tasks.registering(Jar::class) {
    archiveBaseName.set("PurrCollections")
    archiveClassifier.set("")
    archiveVersion.set("1.0.0")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })
}

tasks {
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
