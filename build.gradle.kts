/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

import com.adarshr.gradle.testlogger.TestLoggerExtension
import com.adarshr.gradle.testlogger.theme.ThemeType
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.HasUnitTestBuilder
import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.spotless) apply false
  alias(libs.plugins.gradle.test.logging) apply false
  alias(libs.plugins.gradle.publish.maven) apply false
  idea
}

idea {
  module {
    excludeDirs = excludeDirs + listOf(file("runtime/api"), file("documentation"))
  }
}

buildscript {
  repositories {
    google {
      content {
        includeGroupByRegex(".*google.*")
        includeGroupByRegex(".*android.*")
      }
    }
    mavenCentral()
  }

  dependencies {
    classpath(libs.kotlin.gradle.core)
    classpath(libs.gradle.android)
  }
}

subprojects {
  group = project.property("GROUP") as String
  version = project.property("VERSION_NAME") as String

  repositories {
    google {
      content {
        includeGroupByRegex(".*google.*")
        includeGroupByRegex(".*android.*")
      }
    }
    mavenCentral()
  }

  apply {
    plugin(rootProject.libs.plugins.spotless.get().pluginId)
    plugin(rootProject.libs.plugins.gradle.test.logging.get().pluginId)
  }

  // https://github.com/chrisbanes/tivi/blob/0865be537f2859d267efb59dac7d6358eb47effc/gradle/build-logic/convention/src/main/kotlin/app/tivi/gradle/Android.kt#L28-L34
  extensions.findByType<AndroidComponentsExtension<*, *, *>>()?.run {
    beforeVariants(selector().withBuildType("release")) { variantBuilder ->
      (variantBuilder as? HasUnitTestBuilder)?.apply {
        enableUnitTest = false
      }
    }
  }

  extensions.configure<SpotlessExtension> {
    kotlin {
      target("**/*.kt")
      targetExclude("**/build/**/*.kt")
      ktlint(rootProject.libs.versions.ktlint.get()).editorConfigOverride(
        mapOf(
          "indent_size" to "2",
          "ktlint_standard_filename" to "disabled",
          "ktlint_standard_package-name" to "disabled",
          "ktlint_standard_function-naming" to "disabled",
          "ktlint_standard_property-naming" to "disabled",
          "ktlint_standard_backing-property-naming" to "disabled",
          "ktlint_standard_import-ordering" to "disabled",
          "ktlint_standard_max-line-length" to "disabled",
          "ktlint_standard_annotation" to "disabled",
          "ktlint_standard_multiline-if-else" to "disabled",
          "ktlint_standard_value-argument-comment" to "disabled",
          "ktlint_standard_value-parameter-comment" to "disabled",
          "ktlint_standard_comment-wrapping" to "disabled",
        )
      )
      licenseHeaderFile(rootProject.file("spotless/copyright.kt"))
    }
    format("kts") {
      target("**/*.kts")
      targetExclude("**/build/**/*.kts")
      // Look for the first line that doesn't have a block comment (assumed to be the license)
      licenseHeaderFile(rootProject.file("spotless/copyright.kts"), "(^(?![\\/ ]\\*).*$)")
    }
    format("xml") {
      target("**/*.xml")
      targetExclude("**/build/**/*.xml")
      // Look for the first XML tag that isn't a comment (<!--) or the xml declaration (<?xml)
      licenseHeaderFile(rootProject.file("spotless/copyright.xml"), "(<[^!?])")
    }
  }

  extensions.configure<TestLoggerExtension> {
    theme = ThemeType.MOCHA_PARALLEL
    slowThreshold = 10_000
  }

  tasks.withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = "17"
      freeCompilerArgs = freeCompilerArgs + listOf(
        "-opt-in=kotlin.OptIn",
        "-opt-in=kotlin.RequiresOptIn",
      )

      // https://github.com/ZacSweers/redacted-compiler-plugin/blob/c866a8ae7b2ab039fee9709c990a5478ac0dc0c7/redacted-compiler-plugin-gradle/build.gradle.kts#L91-L94
      if (project.hasProperty("POM_ARTIFACT_ID")) {
        moduleName = project.property("POM_ARTIFACT_ID") as String
      }
    }
  }

  tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    outputs.upToDateWhen { false }
  }
}

tasks.register<Delete>("cleanAll") {
  allprojects.map { project -> project.layout.buildDirectory }.forEach(::delete)
}
