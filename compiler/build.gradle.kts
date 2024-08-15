/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

plugins {
  kotlin("jvm")
  id(libs.plugins.gradle.publish.maven.get().pluginId)
}

sourceSets.main {
  java.srcDir("src/main/kotlin")
}

kotlin {
  explicitApi()
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
  compileOnly(kotlin("compiler", version = libs.versions.kotlin.core.get()))
  compileOnly(kotlin("compose-compiler-plugin", version = libs.versions.kotlin.core.get()))
  compileOnly(libs.jetbrains.annotation)

  testImplementation(projects.runtime)
  testImplementation(libs.compose.runtime)
  testImplementation(kotlin("compiler", version = libs.versions.kotlin.core.get()))
  testImplementation(kotlin("compose-compiler-plugin", version = libs.versions.kotlin.core.get()))
  testImplementation(kotlin("test-junit5", version = libs.versions.kotlin.core.get()))
}
