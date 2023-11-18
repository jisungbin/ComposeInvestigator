plugins {
  id("com.android.library")
  kotlin("android")
}

android {
  namespace = "land.sungbin.composeinvestigator.compiler.test"
  compileSdk = 34

  sourceSets {
    getByName("test").java.srcDir("src/main/kotlin")
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}

kotlin {
  compilerOptions {
    optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
    optIn.add("org.jetbrains.kotlin.utils.addToStdlib.UnsafeCastFunction")
  }
}

dependencies {
  testImplementation(projects.runtime)
  testImplementation(projects.compiler)

  testImplementation(libs.compose.compiler)
  testImplementation(libs.compose.material)

  testImplementation(libs.kotlin.compiler.embedded)
  testImplementation(libs.test.kotlin.compilation)

  testImplementation(libs.test.mockk)
  testImplementation(libs.test.kotest)
  testImplementation(libs.test.junit.core)
  testRuntimeOnly(libs.test.junit.enigne)
}
