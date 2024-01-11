@file:Suppress("UnstableApiUsage")

plugins {
  id("com.android.library")
  kotlin("android")
  id(libs.plugins.gradle.publish.maven.get().pluginId)
}

android {
  namespace = "land.sungbin.composeinvestigator.runtime"
  compileSdk = 34

  defaultConfig {
    minSdk = 21
  }

  sourceSets {
    getByName("main").java.srcDir("src/main/kotlin")
    getByName("test").java.srcDir("src/main/kotlin")
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  buildFeatures {
    compose = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
  }

  testOptions.unitTests {
    isReturnDefaultValues = true
    isIncludeAndroidResources = true
  }
}

kotlin {
  explicitApi()
  compilerOptions {
    optIn.add("androidx.compose.runtime.InternalComposeApi")
    optIn.add("land.sungbin.composeinvestigator.runtime.ComposeInvestigatorCompilerApi")
    optIn.add("land.sungbin.composeinvestigator.runtime.ExperimentalComposeInvestigatorApi")
  }
}

dependencies {
  implementation(libs.compose.runtime)
  testImplementation(libs.test.mockk)
  testImplementation(libs.test.kotest.junit5)
  testImplementation(libs.test.kotlin.coroutines)
}
