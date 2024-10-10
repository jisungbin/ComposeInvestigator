// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  kotlin("multiplatform")
  kotlin("plugin.compose")
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
  jvmToolchain(libs.versions.jdk.get().toInt())

  compilerOptions {
    optIn.addAll(
      "land.sungbin.composeinvestigator.runtime.ComposeInvestigatorCompilerApi",
      "land.sungbin.composeinvestigator.runtime.ExperimentalComposeInvestigatorApi",
    )
    freeCompilerArgs.addAll("-P", "plugin:land.sungbin.composeinvestigator.compiler:verbose=true")
    sourceSets.all {
      languageSettings.enableLanguageFeature("ExplicitBackingFields")
    }
  }

  jvm {
    compilations.all {
      compileTaskProvider.configure {
        compilerOptions {
          freeCompilerArgs.add("-Xlambdas=class")
        }
      }
    }
  }

  iosArm64()
  iosX64()
  iosSimulatorArm64()

  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.runtime)
        implementation(libs.compose.runtime)
        implementation(libs.kotlin.coroutines)
        implementation(libs.test.assertk)
      }
    }

    commonTest {
      dependencies {
        implementation(kotlin("test", version = libs.versions.kotlin.core.get()))
        implementation(libs.test.kotlin.coroutines)
      }
    }

    jvmTest {
      dependencies {
        implementation(kotlin("reflect", version = libs.versions.kotlin.core.get())) // Used by assertk
      }
    }
  }
}

dependencies {
  configurations
    .filter { conf -> conf.name.contains("kotlinCompilerPluginClasspath") }
    .forEach { conf -> conf(projects.compiler) }
}
