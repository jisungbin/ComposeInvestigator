import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("com.android.library")
  kotlin("android")
}

android {
  namespace = "land.sungbin.composeinvalidator.compiler.test"
  compileSdk = 34

  sourceSets {
    getByName("test").java.srcDir("src/main/kotlin")
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    jvmTarget = "17"
  }
}

dependencies {
  testImplementation(projects.compiler)

  testImplementation(libs.compose.compiler)
  testImplementation(libs.compose.material)

  testImplementation(libs.test.kotlin.compilation)

  testImplementation(libs.test.junit.core)
  testRuntimeOnly(libs.test.junit.enigne)
}
