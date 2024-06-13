/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

plugins {
  id("com.android.application")
  kotlin("android")
  id("com.github.takahirom.decomposer")
  alias(libs.plugins.kotlin.compose)
}

android {
  namespace = "land.sungbin.composeinvestigator.sample"
  compileSdk = 34

  defaultConfig {
    minSdk = 24
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  sourceSets {
    getByName("main").java.srcDir("src/main/kotlin")
  }
}

kotlin {
  compilerOptions {
    freeCompilerArgs.addAll("-P", "plugin:land.sungbin.composeinvestigator.compiler:verbose=true")
  }
}

dependencies {
  implementation(libs.compose.activity)
  implementation(libs.compose.material)
  implementation(libs.androidx.activity)

  implementation(projects.runtime)
  kotlinCompilerPluginClasspath(projects.compiler)
}
