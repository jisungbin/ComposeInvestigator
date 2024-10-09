// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
plugins {
  kotlin("jvm")
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

kotlin {
  explicitApi()
  compilerOptions {
    optIn.addAll(
      "land.sungbin.composeinvestigator.runtime.ComposeInvestigatorCompilerApi",
      "land.sungbin.composeinvestigator.runtime.ExperimentalComposeInvestigatorApi",
    )
  }
}

dependencies {
  compileOnly(libs.compose.runtimeStubs)

  testImplementation(kotlin("test-junit5", version = libs.versions.kotlin.core.get()))
  testImplementation(libs.test.assertk)
}
