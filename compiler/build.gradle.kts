// Copyright 2025 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
plugins {
  kotlin("jvm")
  alias(libs.plugins.kotlin.dokka)
  id(libs.plugins.gradle.publish.maven.get().pluginId)
}

dokka {
  moduleName = "ComposeInvestigator Compiler API"
  moduleVersion = project.property("VERSION_NAME") as String
  basePublicationsDirectory = rootDir.resolve("documentation/site/compiler/api")

  dokkaSourceSets.configureEach {
    jdkVersion = libs.versions.jdk.get().toInt()
  }

  pluginsConfiguration.html {
    homepageLink = "https://jisungbin.github.io/ComposeInvestigator/"
    footerMessage = "ComposeInvestigator ⓒ 2025 Ji Sungbin"
  }
}

kotlin {
  explicitApi()
  jvmToolchain(libs.versions.jdk.get().toInt())

  compilerOptions {
    optIn.addAll(
      "org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi",
      "org.jetbrains.kotlin.utils.addToStdlib.UnsafeCastFunction",
      // FYI: https://slack-chats.kotlinlang.org/t/16073880/is-there-a-doc-writeup-somewhere-about-unsafeduringirconstru.
      "org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI",
    )
  }
}

val investigatorRuntimeClasspath: Configuration by configurations.creating

dependencies {
  val kotlinVersion = libs.versions.kotlin.asProvider().get()

  compileOnly(kotlin("stdlib", version = kotlinVersion))
  compileOnly(kotlin("compiler-embeddable", version = kotlinVersion))
  compileOnly(kotlin("compose-compiler-plugin", version = kotlinVersion))

  investigatorRuntimeClasspath(projects.runtime)
  investigatorRuntimeClasspath(libs.compose.runtime)
  testImplementation(kotlin("compiler", version = kotlinVersion))
  testImplementation(kotlin("compose-compiler-plugin", version = kotlinVersion))
  testImplementation(kotlin("compiler-internal-test-framework", version = kotlinVersion))
  testImplementation(kotlin("test-junit5", version = kotlinVersion))

  // Dependencies required to run the internal test framework.
  testRuntimeOnly(kotlin("annotations-jvm", version = kotlinVersion))
}

tasks.register<JavaExec>("generateTests") {
  inputs
    .dir(layout.projectDirectory.dir("src/test/data"))
    .withPropertyName("testData")
    .withPathSensitivity(PathSensitivity.RELATIVE)
  outputs
    .dir(layout.projectDirectory.dir("src/test/java"))
    .withPropertyName("generatedTests")

  classpath = sourceSets.test.get().runtimeClasspath
  mainClass.set("land.sungbin.composeinvestigator.compiler.GenerateTestsKt")
  workingDir = rootDir
}

tasks.withType<Test> {
  dependsOn(investigatorRuntimeClasspath)
  inputs
    .dir(layout.projectDirectory.dir("src/test/data"))
    .withPropertyName("testData")
    .withPathSensitivity(PathSensitivity.RELATIVE)

  workingDir = rootDir
  systemProperty("investigatorRuntime.classpath", investigatorRuntimeClasspath.asPath)

  // Properties required to run the internal test framework.
  systemProperty("idea.home.path", rootDir)
  systemProperty("idea.ignore.disabled.plugins", "true")
}
