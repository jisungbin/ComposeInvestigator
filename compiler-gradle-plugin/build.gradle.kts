// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
import java.time.Year
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  `kotlin-dsl`
  id(libs.plugins.gradle.publish.maven.get().pluginId)
}

val updateVersion by tasks.registering(UpdatePluginVersionTask::class) {
  val copyright by lazy {
    rootDir.resolve("spotless/copyright.kt")
      .readText()
      .replace("\$YEAR", Year.now().toString())
  }

  version.set(project.property("VERSION_NAME") as String)
  this.copyright.set(provider { copyright })
  destination.set(projectDir.walk().first { path -> path.endsWith("VERSION.kt") })
}

tasks.matching { task ->
  task.name == "sourcesJar" || task.name == "spotlessKotlin" || task.name == "dokkaHtml"
}
  .configureEach { dependsOn(updateVersion) }

tasks.withType<KotlinCompile> {
  dependsOn(updateVersion)
}

gradlePlugin {
  plugins {
    create("composeInvestigatorPlugin") {
      id = "in.sungb.composeinvestigator"
      implementationClass = "land.sungbin.composeinvestigator.gradle.ComposeInvestigatorGradleSubplugin"
    }
  }
}

kotlin {
  explicitApi()
}

dependencies {
  compileOnly(kotlin("gradle-plugin", version = libs.versions.kotlin.core.get()))
}

abstract class UpdatePluginVersionTask : DefaultTask() {
  @get:Input abstract val version: Property<String>
  @get:Input abstract val copyright: Property<String>
  @get:InputFile abstract val destination: RegularFileProperty

  @TaskAction fun run() {
    var packageLine: String? = null
    var currentVersion: String? = null

    destination.get().asFile.forEachLine { line ->
      if (line.startsWith("package")) packageLine = line
      if (line.contains("const val VERSION")) currentVersion = line.split("\"")[1]
    }
    if (currentVersion == version.get()) return

    destination.get().asFile.writeText(
      """
      |${copyright.get().replace("\n", "\n|")}
      |$packageLine
      |
      |internal const val VERSION = "${version.get()}"
      |
      """.trimMargin(),
    )
  }
}
