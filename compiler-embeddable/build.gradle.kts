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

// https://github.com/johnrengelman/shadow/issues/448#issuecomment-562939439
project.configurations.implementation.get().isCanBeResolved = true

val shadowJar = tasks.register<ShadowJar>("embeddedPlugin") {
  configurations = listOf(
    project.configurations.implementation.get(),
    project.configurations.compileClasspath.get(),
  )
  relocate("com.intellij", /* destination = */ "org.jetbrains.kotlin.com.intellij")
  archiveBaseName.set("embedded")
  archiveVersion.set("")
  destinationDirectory.set(File(layout.buildDirectory.asFile.get(), "repackaged"))
}

// replace the standard jar with the one built by 'shadowJar' in both api and runtime variants
configurations {
  apiElements.get().outgoing.artifacts.clear()
  apiElements.get().outgoing.artifact(shadowJar.flatMap(AbstractArchiveTask::getArchiveFile))
  runtimeElements.get().outgoing.artifacts.clear()
  runtimeElements.get().outgoing.artifact(shadowJar.flatMap(AbstractArchiveTask::getArchiveFile))
}
