/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

plugins {
  kotlin("jvm")
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
  compileOnly(libs.kotlin.compiler)

  implementation(libs.compose.compiler)
  implementation(libs.jetbrains.annotation)
}
