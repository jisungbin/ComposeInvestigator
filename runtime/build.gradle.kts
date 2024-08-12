/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */
@file:Suppress("UnstableApiUsage")

import org.gradle.kotlin.dsl.compileOnly
import org.gradle.kotlin.dsl.dokkaHtml
import org.gradle.kotlin.dsl.libs
import org.gradle.kotlin.dsl.testImplementation


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
