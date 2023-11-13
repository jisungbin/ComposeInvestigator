plugins {
  kotlin("jvm")
  alias(libs.plugins.gradle.publish.maven)
  alias(libs.plugins.kotlin.ksp)
}



dependencies {
  compileOnly(libs.kotlin.compiler.embedded)
  implementation(libs.jetbrains.annotation)

  implementation(libs.autoservice.annotation)
  ksp(libs.autoservice.ksp)

  testImplementation(libs.test.kotest)
  testImplementation(libs.test.kotlin.compilation)
  testImplementation(libs.test.junit.core)
  testRuntimeOnly(libs.test.junit.enigne)
}
