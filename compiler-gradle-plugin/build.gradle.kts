/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

plugins {
  kotlin("jvm")
  `kotlin-dsl`
  id(libs.plugins.gradle.publish.maven.get().pluginId)
}

// val updateVersion = tasks.register<UpdatePluginVersionTask>("updateVersion") {
//  version.set(project.property("VERSION_NAME") as String)
//  destination.set(projectDir.walk().first { path -> path.endsWith("VERSION.kt") })
// }
//
// tasks.matching { task ->
//  task.name == "sourcesJar" || task.name == "spotlessKotlin" || task.name == "dokkaHtml"
// }
//  .configureEach { dependsOn(updateVersion) }
//
// tasks.withType<KotlinCompile>().configureEach {
//  dependsOn(updateVersion)
// }

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
  compileOnly(kotlin("gradle-plugin", version = libs.versions.kotlin.core.get()))
}

// abstract class UpdatePluginVersionTask : DefaultTask() {
//  @get:Input abstract val version: Property<String>
//  @get:InputFile abstract val destination: RegularFileProperty
//
//  @TaskAction fun run() {
//    var packageLine: String? = null
//    var currentVersion: String? = null
//
//    destination.get().asFile.forEachLine { line ->
//      if (line.startsWith("package")) packageLine = line
//      if (line.contains("const val VERSION")) currentVersion = line.split("\"")[1]
//    }
//    if (currentVersion == version.get()) return
//
//    destination.get().asFile.writeText(
//      """
//      $packageLine
//
//      internal const val VERSION = "${version.get()}"
//      """.trimIndent(),
//    )
//  }
// }
