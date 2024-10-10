// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
plugins {
  id(libs.plugins.gradle.android.application.get().pluginId)
  kotlin("android")
  kotlin("plugin.compose")
}

android {
  namespace = "land.sungbin.composeinvestigator.sample"
  compileSdk = 34

  defaultConfig {
    minSdk = 24
  }

  compileOptions {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.jdk.get().toInt())
    targetCompatibility = JavaVersion.toVersion(libs.versions.jdk.get().toInt())
  }

  sourceSets {
    getByName("main").java.srcDir("src/main/kotlin")
  }
}

kotlin {
  jvmToolchain(libs.versions.jdk.get().toInt())
  compilerOptions {
    freeCompilerArgs.addAll("-P", "plugin:land.sungbin.composeinvestigator.compiler:verbose=true")
  }
}

dependencies {
  implementation(libs.androidx.activity)

  implementation(libs.compose.activity)
  implementation(libs.compose.material)

  implementation(projects.runtime)
  kotlinCompilerPluginClasspath(projects.compiler)
}
