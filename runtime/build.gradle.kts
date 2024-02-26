/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */
@file:Suppress("UnstableApiUsage")

import org.jetbrains.dokka.DokkaConfiguration.Visibility

plugins {
  id("com.android.library")
  kotlin("android")
  alias(libs.plugins.kotlin.dokka)
  id(libs.plugins.gradle.publish.maven.get().pluginId)
}

tasks.dokkaHtml {
  moduleName.set("ComposeInvestigator Runtime API")
  moduleVersion.set(project.property("VERSION_NAME") as String)
  outputDirectory.set(rootDir.resolve("documentation/site/runtime/api"))

  dokkaSourceSets.configureEach {
    jdkVersion.set(17)
    documentedVisibilities.set(setOf(Visibility.PUBLIC, Visibility.PROTECTED))
  }

  pluginsMapConfiguration.set(
    mapOf(
      "org.jetbrains.dokka.base.DokkaBase" to
        """{ "footerMessage": "ComposeInvestigator ⓒ 2024 Ji Sungbin" }""",
    )
  )
}

android {
  namespace = "land.sungbin.composeinvestigator.runtime"
  compileSdk = 34

  defaultConfig {
    minSdk = 21
  }

  sourceSets {
    getByName("main").java.srcDir("src/main/kotlin")
    getByName("test").java.srcDir("src/main/kotlin")
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  buildFeatures {
    compose = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
  }

  testOptions.unitTests {
    isReturnDefaultValues = true
    isIncludeAndroidResources = true
  }
}

kotlin {
  explicitApi()
  compilerOptions {
    optIn.add("androidx.compose.runtime.InternalComposeApi")
    optIn.add("land.sungbin.composeinvestigator.runtime.ComposeInvestigatorCompilerApi")
    optIn.add("land.sungbin.composeinvestigator.runtime.ExperimentalComposeInvestigatorApi")
  }
}

dependencies {
  implementation(libs.compose.runtime)
  implementation(libs.compose.animation)
  implementation(embeddedKotlin("reflect"))

  testImplementation(libs.test.mockk)
  testImplementation(libs.test.kotest.junit5)
  testImplementation(libs.test.kotlin.coroutines)
}
