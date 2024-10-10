// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("UnstableApiUsage")

rootProject.name = "ComposeInvestigator"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

pluginManagement {
  repositories {
    gradlePluginPortal()
    google {
      mavenContent {
        includeGroupByRegex(".*google.*")
        includeGroupByRegex(".*android.*")
      }
    }
    mavenCentral {
      mavenContent {
        releasesOnly()
      }
    }
    mavenLocal()
  }
}

dependencyResolutionManagement {
  repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
  repositories {
    google {
      mavenContent {
        includeGroupByRegex(".*google.*")
        includeGroupByRegex(".*android.*")
        releasesOnly()
      }
    }
    mavenCentral {
      mavenContent {
        releasesOnly()
      }
    }
    mavenLocal()
  }
}

include(
  ":runtime",
  ":compiler",
  ":compiler-gradle-plugin",
  ":compiler-integration-test",
  ":samples:android",
)
