// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
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
    freeCompilerArgs.add("-Xlambdas=class")
    sourceSets.all {
      languageSettings.enableLanguageFeature("ExplicitBackingFields")
    }
  }
}

dependencies {
  implementation(projects.runtime)
  implementation(libs.compose.runtime)

  implementation("androidx.compose.runtime:runtime-test-utils:1.8.0-SNAPSHOT") {
    because("Why SNAPSHOT? See https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/runtime/runtime-test-utils/build.gradle;l=71;drc=214c6abe4e624304956276717a0163fad3858be9")
  }
  kotlinCompilerPluginClasspath(projects.compiler)

  testImplementation(kotlin("test-junit5", version = libs.versions.kotlin.core.get()))
  testImplementation(kotlin("reflect")) // Used by assertk
  testImplementation(libs.test.kotlin.coroutines)
  testImplementation(libs.test.assertk)
}
