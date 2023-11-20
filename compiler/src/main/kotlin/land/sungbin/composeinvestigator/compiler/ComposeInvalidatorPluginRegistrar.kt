/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("unused", "DEPRECATION", "UnstableApiUsage")

package land.sungbin.composeinvestigator.compiler

import com.google.auto.service.AutoService
import land.sungbin.composeinvestigator.compiler.ComposeInvestigatorConfiguration.KEY_VERBOSE
import land.sungbin.composeinvestigator.compiler.util.VerboseLogger
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.extensions.LoadingOrder
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(ComponentRegistrar::class)
public class ComposeInvestigatorPluginRegistrar : ComponentRegistrar {
  override fun registerProjectComponents(
    project: MockProject,
    configuration: CompilerConfiguration,
  ) {
    val verbose = configuration[KEY_VERBOSE]?.toBooleanStrictOrNull() ?: false
    val logger = VerboseLogger(configuration).apply { if (verbose) verbose() }

    project.extensionArea
      .getExtensionPoint(IrGenerationExtension.extensionPointName)
      .registerExtension(
        InvalidationTrackExtension(logger = logger),
        // TODO: LoadingOrder("after ${ComposeCommandLineProcessor.PLUGIN_ID}"),
        // https://github.com/JetBrains/intellij-community/blob/17ca1d0afb43f824cda948fc3ea4467ebe55b346/platform/extensions/src/com/intellij/openapi/extensions/LoadingOrder.kt#L24
        LoadingOrder.LAST,
        project,
      )
  }
}
