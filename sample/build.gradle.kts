import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("com.android.application")
  kotlin("android")
  id("com.github.takahirom.decomposer")
}

android {
  namespace = "land.sungbin.composeinvalidator.sample"
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
    kotlinCompilerExtensionVersion = libs.versions.compose.core.get()
  }
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    jvmTarget = "17"
    freeCompilerArgs = freeCompilerArgs + listOf(
      "-P",
      "plugin:land.sungbin.composeinvalidator.compiler:verbose=true",
    )
  }
}

// repositories {
//   mavenLocal()
// }

dependencies {
  implementation(libs.androidx.activity)
  implementation(libs.compose.activity)
  implementation(libs.compose.material)

  implementation(projects.runtime)
  kotlinCompilerPluginClasspath(projects.compiler)
  // kotlinCompilerPluginClasspath("land.sungbin.composeinvalidator:composeinvalidator-compiler:0.1.0-SNAPSHOT")
}
