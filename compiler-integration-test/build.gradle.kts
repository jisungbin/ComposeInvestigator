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
  alias(libs.plugins.kotlin.compose)
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
}

kotlin {
  compilerOptions {
    optIn.addAll(
      "org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi",
      "org.jetbrains.kotlin.utils.addToStdlib.UnsafeCastFunction",
      "land.sungbin.composeinvestigator.runtime.ComposeInvestigatorCompilerApi",
      "land.sungbin.composeinvestigator.runtime.ExperimentalComposeInvestigatorApi",
    )
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
  implementation(libs.jetbrains.annotation)
  implementation(libs.test.assertk)

  testImplementation(projects.compilerHosted)
  testImplementation(libs.kotlin.compiler.core)
  testImplementation(libs.kotlin.compiler.compose) // must be 'implementation' (not 'compileOnly')

  testImplementation(libs.test.kotlin.coroutines) {
    because("https://github.com/Kotlin/kotlinx.coroutines/issues/3673")
  }

  testImplementation(libs.test.mockk)
  testImplementation(libs.test.robolectric) {
    because("https://stackoverflow.com/a/64287388/14299073")
  }

  testImplementation(kotlin("test-junit5"))
  testImplementation(libs.test.junit.compose)

  kotlinCompilerPluginClasspath(projects.compiler)
}
