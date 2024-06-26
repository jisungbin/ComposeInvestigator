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
      // See https://kotlinlang.slack.com/archives/C7L3JB43G/p1700429910462239.
      "org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI",
    )
  }
}

dependencies {
  compileOnly(libs.kotlin.compiler.core)
  implementation(libs.kotlin.compiler.compose)
  implementation(libs.jetbrains.annotation)
}
