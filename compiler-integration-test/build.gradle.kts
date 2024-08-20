/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  alias(libs.plugins.kotlin.compose)
}

kotlin {
  compilerOptions {
    optIn.addAll(
      "land.sungbin.composeinvestigator.runtime.ComposeInvestigatorCompilerApi",
      "land.sungbin.composeinvestigator.runtime.ExperimentalComposeInvestigatorApi",
    )
    freeCompilerArgs.addAll("-P", "plugin:land.sungbin.composeinvestigator.compiler:verbose=true")
  }
}

tasks.withType<KotlinCompile> {
  dependsOn(":compiler-embeddable:embeddedPlugin")
}

dependencies {
  implementation(projects.runtime)
  implementation(libs.compose.runtime)

  testImplementation(kotlin("test-junit5", version = libs.versions.kotlin.core.get()))
  testImplementation(projects.compiler)
  testImplementation(libs.test.assertk)

  kotlinCompilerPluginClasspath(projects.compilerEmbeddable)
}
