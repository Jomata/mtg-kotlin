import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.20"
    application
}

group = "me.jose"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    // https://mvnrepository.com/artifact/io.github.forohforerror/ScryfallAPIBinding
    implementation("io.github.forohforerror:ScryfallAPIBinding:1.11.1")
    // https://mvnrepository.com/artifact/io.jenetics/jenetics
    implementation("io.jenetics:jenetics:7.0.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}