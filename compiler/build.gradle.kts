// Copyright 2024 Ji Sungbin
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
    footerMessage = "ComposeInvestigator ⓒ 2024 Ji Sungbin"
  }
}

kotlin {
  explicitApi()
  jvmToolchain(libs.versions.jdk.get().toInt())

  compilerOptions {
    optIn.addAll(
      "org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi",
      "org.jetbrains.kotlin.utils.addToStdlib.UnsafeCastFunction",
      // Only works with IR phase, not FIR.
      // See https://slack-chats.kotlinlang.org/t/16073880/is-there-a-doc-writeup-somewhere-about-unsafeduringirconstru.
      "org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI",
    )
  }
}

dependencies {
  compileOnly(kotlin("compiler-embeddable", version = libs.versions.kotlin.core.get()))
  compileOnly(kotlin("compose-compiler-plugin", version = libs.versions.kotlin.core.get()))

  testImplementation(projects.runtime)
  testImplementation(libs.compose.runtime)
  testImplementation(libs.test.diffutil)
  testImplementation(libs.test.kluent) // TODO Temporary library to use until KT-53336 is resolved

  testImplementation(kotlin("test-junit5", version = libs.versions.kotlin.core.get()))
  testImplementation(kotlin("compiler-embeddable", version = libs.versions.kotlin.core.get()))
  testImplementation(kotlin("compose-compiler-plugin-embeddable", version = libs.versions.kotlin.core.get()))

  kotlinCompilerPluginClasspathTest(kotlin("compose-compiler-plugin-embeddable", version = libs.versions.kotlin.core.get()))
}
