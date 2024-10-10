// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
import org.gradle.kotlin.dsl.dokka
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  kotlin("multiplatform")
  alias(libs.plugins.kotlin.dokka)
  id(libs.plugins.gradle.publish.maven.get().pluginId)
}

dokka {
  moduleName = "ComposeInvestigator Runtime API"
  moduleVersion = project.property("VERSION_NAME") as String
  dokkaPublicationDirectory = rootDir.resolve("documentation/site/runtime/api")

  dokkaSourceSets.configureEach {
    jdkVersion = JavaVersion.VERSION_22.majorVersion.toInt()
  }

  pluginsConfiguration.html {
    homepageLink = "https://jisungbin.github.io/ComposeInvestigator/"
    footerMessage = "ComposeInvestigator ⓒ 2024 Ji Sungbin"
  }
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
  explicitApi()
  jvmToolchain(libs.versions.jdk.get().toInt())

  compilerOptions {
    optIn.addAll(
      "land.sungbin.composeinvestigator.runtime.ComposeInvestigatorCompilerApi",
      "land.sungbin.composeinvestigator.runtime.ExperimentalComposeInvestigatorApi",
    )
  }

  jvm()

  iosArm64()
  iosX64()
  iosSimulatorArm64()

  sourceSets {
    commonMain {
      dependencies {
        compileOnly(libs.compose.runtime)
        implementation(libs.androidx.annotation)
      }
    }

    commonTest {
      dependencies {
        implementation(kotlin("test", version = libs.versions.kotlin.core.get()))
        implementation(libs.test.assertk)
      }
    }

    jvmTest {
      dependencies {
        implementation(kotlin("reflect", version = libs.versions.kotlin.core.get())) // Used by assertk
      }
    }
  }
}
