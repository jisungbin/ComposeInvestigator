/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

plugins {
  kotlin("jvm")
  alias(libs.plugins.kotlin.ksp)
  id(libs.plugins.gradle.publish.maven.get().pluginId)
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
  implementation(libs.compose.compiler)

  implementation(libs.autoservice.annotation)
  ksp(libs.autoservice.ksp)
}
