/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

@file:Suppress("UnstableApiUsage")

rootProject.name = "ComposeInvalidator"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
}

buildCache {
  local {
    removeUnusedEntriesAfterDays = 7
  }
}

include(
  ":compiler",
  ":compiler-integration-test",
)
