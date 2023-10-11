plugins {
    kotlin("jvm") version "1.9.0"
    application
}

group = "io.github.jisungbin.composeinvalidator"
version = "0.1.0-snapshot"

repositories {
    mavenCentral()
}



kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}