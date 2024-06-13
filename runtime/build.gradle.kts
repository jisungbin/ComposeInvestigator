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
  alias(libs.plugins.kotlin.compose)
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

  testOptions.unitTests {
    isReturnDefaultValues = true
    isIncludeAndroidResources = true
  }
}

kotlin {
  explicitApi()
  compilerOptions {
    optIn.addAll(
      "androidx.compose.runtime.InternalComposeApi",
      "land.sungbin.composeinvestigator.runtime.ComposeInvestigatorCompilerApi",
      "land.sungbin.composeinvestigator.runtime.ExperimentalComposeInvestigatorApi",
    )
  }
}

dependencies {
  implementation(libs.compose.runtime)
  implementation(libs.compose.animation)
  implementation(kotlin("reflect")) // TODO remove kotlin-reflect usage. This is large dependency.

  testImplementation(kotlin("test-junit5"))
  testImplementation(libs.test.assertk)
  testImplementation(libs.test.kotlin.coroutines)

  // noinspection UseTomlInstead
  testImplementation("androidx.compose.runtime:runtime-test-utils:1.8.0-SNAPSHOT") {
    isTransitive = false

    // Why snapshot?
    because("https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/runtime/runtime-test-utils/build.gradle;l=68;drc=aa3aa01c08fc9d9e7c13260b4f2fe89dfa2a58f1")
  }
}
