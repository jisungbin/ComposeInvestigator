import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("com.android.application")
  kotlin("android")
  id("com.github.takahirom.decomposer")
}

android {
  namespace = "land.sungbin.composeinvestigator.sample"
  compileSdk = 34

  defaultConfig {
    minSdk = 24
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  sourceSets {
    getByName("main").java.srcDir("src/main/kotlin")
  }

  buildFeatures {
    compose = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
  }
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = freeCompilerArgs + listOf("-P", "plugin:land.sungbin.composeinvestigator.compiler.callstack:verbose=true")
    freeCompilerArgs = freeCompilerArgs + listOf("-P", "plugin:land.sungbin.composeinvestigator.compiler.invalidation:verbose=true")
  }
}

dependencies {
  implementation(libs.androidx.activity)
  implementation(libs.compose.activity)
  implementation(libs.compose.material)

  implementation(projects.runtime)
  kotlinCompilerPluginClasspath(projects.compilerCallstackTrack)
  kotlinCompilerPluginClasspath(projects.compilerInvalidationTrack)
}
