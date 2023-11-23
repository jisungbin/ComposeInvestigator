plugins {
  id("com.android.library")
  kotlin("android")
  // alias(libs.plugins.gradle.publish.maven)
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
    kotlinCompilerExtensionVersion = libs.versions.compose.core.get()
  }
}

kotlin {
  explicitApi()
  compilerOptions {
    optIn.add("land.sungbin.composeinvestigator.runtime.ComposeInvestigatorCompilerApi")
  }
}

dependencies {
  implementation(libs.compose.runtime)
  testImplementation(libs.test.kotest)
}
