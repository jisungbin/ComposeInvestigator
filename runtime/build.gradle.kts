// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
plugins {
  kotlin("multiplatform")
  alias(libs.plugins.kotlin.dokka)
  id(libs.plugins.gradle.publish.maven.get().pluginId)
}

dokka {
  moduleName = "ComposeInvestigator Runtime API"
  moduleVersion = project.property("VERSION_NAME") as String
  basePublicationsDirectory = rootDir.resolve("documentation/site/runtime/api")

  dokkaSourceSets.configureEach {
    jdkVersion = libs.versions.jdk.get().toInt()
  }

  pluginsConfiguration.html {
    homepageLink = "https://jisungbin.github.io/ComposeInvestigator/"
    footerMessage = "ComposeInvestigator ⓒ 2024 Ji Sungbin"
  }
}

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

  // FIXME Task :runtime:linkDebugTestIosSimulatorArm64 FAILED
  //  e: java.lang.NullPointerException
  // iosArm64()
  // iosX64()
  // iosSimulatorArm64()

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
