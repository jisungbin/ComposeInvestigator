/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("UnstableApiUsage")

plugins {
  id("com.android.library")
  kotlin("android")
}

android {
  namespace = "land.sungbin.composeinvestigator.compiler.test"
  compileSdk = 34

  defaultConfig {
    minSdk = 21
  }

  sourceSets {
    getByName("main").java.srcDir("src/main/kotlin")
    getByName("test").java.srcDir("src/test/kotlin")
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  lint {
    disable.add("MissingClass")
  }

  testOptions {
    unitTests {
      isIncludeAndroidResources = true
      isReturnDefaultValues = true

      all { test ->
        test.useJUnitPlatform()
      }
    }
  }

  buildFeatures {
    compose = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    useLiveLiterals = true
  }
}

kotlin {
  compilerOptions {
    optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
    optIn.add("org.jetbrains.kotlin.utils.addToStdlib.UnsafeCastFunction")
    optIn.add("land.sungbin.composeinvestigator.runtime.ComposeInvestigatorCompilerApi")
    optIn.add("land.sungbin.composeinvestigator.runtime.ExperimentalComposeInvestigatorApi")
    freeCompilerArgs.addAll("-P", "plugin:land.sungbin.composeinvestigator.compiler:verbose=false")
  }
}

afterEvaluate {
  tasks.withType<Test> {
    dependsOn(":compiler:embeddedPlugin")
  }
}

dependencies {
  implementation(projects.runtime)
  implementation(libs.compose.material)
  implementation(libs.test.kotest.assertion)

  testImplementation(projects.compilerHosted)
  testImplementation(libs.compose.compiler)
  testImplementation(libs.kotlin.compiler) // must be 'implementation' (not 'compileOnly')

  testImplementation(libs.test.kotlin.coroutines) {
    because("https://github.com/Kotlin/kotlinx.coroutines/issues/3673")
  }

  testImplementation(libs.test.mockk)
  testImplementation(libs.test.kotest.junit5)
  testImplementation(libs.test.robolectric) {
    because("https://stackoverflow.com/a/64287388/14299073")
  }

  testImplementation(libs.test.junit.core)
  testRuntimeOnly(libs.test.junit.enigne)
  testImplementation(libs.test.junit.compose)

  kotlinCompilerPluginClasspath(projects.compiler)
}
