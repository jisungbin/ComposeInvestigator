import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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

tasks.withType<KotlinCompile> {
  kotlinOptions {
    jvmTarget = "17"
  }
}

dependencies {
  compileOnly(libs.kotlin.compiler.embedded)
  implementation(libs.jetbrains.annotation)

  implementation("land.sungbin.fastlist:fastlist:0.1.0")

  implementation(libs.autoservice.annotation)
  ksp(libs.autoservice.ksp)
}
