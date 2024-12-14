// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet.Companion.COMMON_MAIN_SOURCE_SET_NAME
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetContainer
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.plugin.kotlinToolingVersion

public class ComposeInvestigatorGradleSubplugin : KotlinCompilerPluginSupportPlugin {
  override fun apply(target: Project) {
    target.extensions.create<ComposeInvestigatorPluginExtension>("composeInvestigator")
  }

  override fun getCompilerPluginId(): String = "land.sungbin.composeinvestigator.compiler"

  override fun getPluginArtifact(): SubpluginArtifact =
    SubpluginArtifact(
      groupId = "in.sungb.composeinvestigator",
      artifactId = "composeinvestigator-compiler",
      version = VERSION,
    )

  // TODO test this
  override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
    val project = kotlinCompilation.target.project
    val kotlinVersion = project.kotlinToolingVersion
    return kotlinVersion.major == 2
  }

  override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
    val project = kotlinCompilation.target.project
    val extension = project.extensions.getByType<ComposeInvestigatorPluginExtension>()
    val runtimeDependency = project.dependencies.create("in.sungb.composeinvestigator:composeinvestigator-runtime:$VERSION")

    if (project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
      val kotlin = project.extensions.getByType<KotlinSourceSetContainer>()
      kotlin.sourceSets.getByName(COMMON_MAIN_SOURCE_SET_NAME) {
        kotlin {
          dependencies {
            implementation(runtimeDependency)
          }
        }
      }
    } else {
      project.dependencies.add(kotlinCompilation.implementationConfigurationName, runtimeDependency)
    }

    return project.provider {
      listOf(
        SubpluginOption(key = "enabled", value = extension.enabled.get().toString()),
        SubpluginOption(key = "verbose", value = extension.verbose.get().toString()),
      )
    }
  }
}
