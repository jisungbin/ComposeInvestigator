/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */
@file:Suppress("UnstableApiUsage")

plugins {
  kotlin("jvm")
  alias(libs.plugins.kotlin.dokka)
  id(libs.plugins.gradle.publish.maven.get().pluginId)
}

tasks.dokkaHtml {
  moduleName.set("ComposeInvestigator Runtime API")
  moduleVersion.set(project.property("VERSION_NAME") as String)
  outputDirectory.set(rootDir.resolve("documentation/site/runtime/api"))

  dokkaSourceSets.configureEach {
    jdkVersion.set(JavaVersion.VERSION_17.majorVersion.toInt())
  }

  pluginsMapConfiguration.set(
    mapOf(
      "org.jetbrains.dokka.base.DokkaBase" to
        """{ "footerMessage": "ComposeInvestigator ⓒ 2024 Ji Sungbin" }""",
    ),
  )
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
  compileOnly(libs.compose.stableMarker)

  testImplementation(kotlin("test-junit5", version = libs.versions.kotlin.core.get()))
  testImplementation(libs.test.assertk)
}
