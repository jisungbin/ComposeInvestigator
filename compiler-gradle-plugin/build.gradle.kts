/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  `java-gradle-plugin`
  `kotlin-dsl`
  id(libs.plugins.gradle.publish.maven.get().pluginId)
}

gradlePlugin {
  plugins {
    create("composeInvestigatorPlugin") {
      id = "land.sungbin.composeinvestigator"
      implementationClass = "land.sungbin.composeinvestigator.gradleplugin.ComposeInvestigatorGradleSubplugin"
    }
  }
}

kotlin {
  explicitApi()
}

dependencies {
  compileOnly(libs.kotlin.gradle.core)
  compileOnly(libs.kotlin.gradle.api)
}

// configuration required to produce unique META-INF/*.kotlin_module file names
tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions { moduleName = project.property("POM_ARTIFACT_ID") as String }
}
