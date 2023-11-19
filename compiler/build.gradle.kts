plugins {
  kotlin("jvm")
  alias(libs.plugins.gradle.publish.maven)
  alias(libs.plugins.kotlin.ksp)
}

sourceSets {
  getByName("main").java.srcDir("src/main/kotlin")
}

kotlin {
  explicitApi()
  compilerOptions {
    optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
    optIn.add("org.jetbrains.kotlin.utils.addToStdlib.UnsafeCastFunction")
  }
}

dependencies {
  compileOnly(libs.kotlin.compiler.embedded)
  implementation(libs.jetbrains.annotation)

  implementation(libs.fastlist)
  implementation(libs.compose.compiler) // for StabilityInferencer

  implementation(libs.autoservice.annotation)
  ksp(libs.autoservice.ksp)
}
