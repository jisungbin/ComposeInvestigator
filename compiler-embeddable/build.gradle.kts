/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("UnstableApiUsage")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
  java
  id(libs.plugins.gradle.publish.maven.get().pluginId)
  alias(libs.plugins.gradle.shadow)
}

dependencies {
  compileOnly(projects.compiler)
}

tasks.register<ShadowJar>("embeddedPlugin") {
  configurations = listOf(project.configurations.compileClasspath.get())
  relocate("com.intellij", "org.jetbrains.kotlin.com.intellij")
  archiveBaseName.set("composeinvestigator-compiler")
  archiveClassifier.set("shadow")
  destinationDirectory.set(File(layout.buildDirectory.asFile.get(), "repackaged"))
}

