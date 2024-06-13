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

// Thanks for https://github.com/ZacSweers/redacted-compiler-plugin/blob/c866a8ae7b2ab039fee9709c990a5478ac0dc0c7/redacted-compiler-plugin-gradle/build.gradle.kts#L21-L34
sourceSets.main {
  java.srcDir(layout.buildDirectory.dir("generated/sources/version-template/kotlin/main"))
}

val copyVersionTemplateProvider =
  tasks.register<Copy>("copyVersionTemplate") {
    inputs.property("version", project.property("VERSION_NAME"))
    from(project.layout.projectDirectory.dir("version-template"))
    into(project.layout.buildDirectory.dir("generated/sources/version-template/kotlin/main"))
    expand(mapOf("projectVersion" to "${project.property("VERSION_NAME")}"))
    filteringCharset = "UTF-8"
  }

tasks
  .matching { task -> task.name == "sourcesJar" || task.name == "dokkaHtml" }
  .configureEach { dependsOn(copyVersionTemplateProvider) }

tasks.withType<KotlinCompile>().configureEach {
  dependsOn(copyVersionTemplateProvider)
}

gradlePlugin {
  plugins {
    create("composeInvestigatorPlugin") {
      id = "land.sungbin.composeinvestigator"
      implementationClass = "land.sungbin.composeinvestigator.gradle.ComposeInvestigatorGradleSubplugin"
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
