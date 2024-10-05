// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

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

  override fun isApplicable(compilation: KotlinCompilation<*>): Boolean = true

  override fun applyToCompilation(compilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
    val project = compilation.target.project
    val extension = project.extensions.getByType<ComposeInvestigatorPluginExtension>()

    project.dependencies.add(
      compilation.implementationConfigurationName,
      "in.sungb.composeinvestigator:composeinvestigator-runtime:$VERSION",
    )

    return project.provider {
      listOf(
        SubpluginOption(key = "enabled", value = extension.enabled.get().toString()),
        SubpluginOption(key = "verbose", value = extension.verbose.get().toString()),
      )
    }
  }
}
