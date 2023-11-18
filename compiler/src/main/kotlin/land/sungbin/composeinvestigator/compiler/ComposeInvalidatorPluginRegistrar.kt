/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("unused", "DEPRECATION", "UnstableApiUsage")

package land.sungbin.composeinvestigator.compiler

import com.google.auto.service.AutoService
import land.sungbin.composeinvestigator.compiler.util.VerboseLogger
import org.jetbrains.annotations.VisibleForTesting
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.extensions.LoadingOrder
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

@VisibleForTesting
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
      .registerExtension(InvalidationTrackExtension(logger = logger), LoadingOrder.LAST, project)
  }
}
