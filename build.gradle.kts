/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

import com.adarshr.gradle.testlogger.TestLoggerExtension
import com.adarshr.gradle.testlogger.theme.ThemeType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
  alias(libs.plugins.test.gradle.logging) apply false
  alias(libs.plugins.kotlin.ktlint) apply false
}

buildscript {
  repositories {
    google()
    mavenCentral()
  }

  dependencies {
    classpath(libs.kotlin.gradle)
    classpath(libs.gradle.android)
  }
}

subprojects {
  repositories {
    google()
    mavenCentral()
  }

  apply {
    plugin(rootProject.libs.plugins.test.gradle.logging.get().pluginId)
    plugin(rootProject.libs.plugins.kotlin.ktlint.get().pluginId)
  }

  afterEvaluate {
    extensions.configure<TestLoggerExtension> {
      theme = ThemeType.MOCHA_PARALLEL
      slowThreshold = 10_000
    }

    extensions.configure<KtlintExtension> {
      version.set(rootProject.libs.versions.kotlin.ktlint.source.get())
      android.set(true)
      verbose.set(true)
    }

    tasks.withType<KotlinCompile> {
      kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = freeCompilerArgs + listOf(
          "-opt-in=kotlin.OptIn",
          "-opt-in=kotlin.RequiresOptIn",
        )
      }
    }

    tasks.withType<Test>().configureEach {
      useJUnitPlatform()
      outputs.upToDateWhen { false }
    }
  }
}

tasks.register("cleanAll", type = Delete::class) {
  allprojects.map { project -> project.layout.buildDirectory }.forEach(::delete)
}
