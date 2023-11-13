plugins {
  kotlin("jvm")
  alias(libs.plugins.gradle.publish.maven)
  alias(libs.plugins.kotlin.ksp)
}

sourceSets {
  getByName("main").java.srcDir("src/main/kotlin")
}

kotlin {
  jvmToolchain(17)
  explicitApi()
}

dependencies {
  compileOnly(libs.kotlin.compiler.embedded)
  implementation(libs.jetbrains.annotation)

  implementation(libs.autoservice.annotation)
  ksp(libs.autoservice.ksp)
}
