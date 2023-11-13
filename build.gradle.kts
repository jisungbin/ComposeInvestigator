/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
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
    plugin(rootProject.libs.plugins.kotlin.ktlint.get().pluginId)
  }

  afterEvaluate {
    extensions.configure<KtlintExtension> {
      version.set(rootProject.libs.versions.kotlin.ktlint.source.get())
      android.set(true)
      verbose.set(true)
    }

    extensions.configure<SourceSetContainer> {
      getByName("main").java.srcDir("src/main/kotlin")
      getByName("test").java.srcDir("src/main/kotlin")
    }

    tasks.withType<KotlinCompile> {
      kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + listOf(
          "-opt-in=kotlin.OptIn",
          "-opt-in=kotlin.RequiresOptIn",
        )
      }
    }
  }
}

tasks.register("cleanAll", type = Delete::class) {
  allprojects.map { project -> project.layout.buildDirectory }.forEach(::delete)
}
