/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("UnstableApiUsage")

rootProject.name = "ComposeInvestigator"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
// enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

pluginManagement {
  repositories {
    gradlePluginPortal()
    google {
      content {
        includeGroupByRegex(".*google.*")
        includeGroupByRegex(".*android.*")
      }
    }
    mavenCentral()
    maven("https://jitpack.io") {
      content {
        includeGroup("com.github.takahirom")
      }
    }
  }

  resolutionStrategy.eachPlugin {
    if (requested.id.id == "com.github.takahirom.decomposer") {
      useModule("com.github.takahirom:decomposer:main-SNAPSHOT")
    }
  }
}

buildCache {
  local {
    removeUnusedEntriesAfterDays = 7
  }
}

include(
  ":runtime",
  ":compiler",
  ":compiler-integration-test",
  ":sample",
)
