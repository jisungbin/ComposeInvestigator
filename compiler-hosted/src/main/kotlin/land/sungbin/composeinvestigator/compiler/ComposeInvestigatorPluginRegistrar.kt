/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("DEPRECATION", "UnstableApiUsage")

package land.sungbin.composeinvestigator.compiler

import com.intellij.mock.MockProject
import com.intellij.openapi.extensions.LoadingOrder
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

public class ComposeInvestigatorPluginRegistrar : ComponentRegistrar {
  // TODO: https://github.com/jisungbin/ComposeInvestigator/issues/91
  override val supportsK2: Boolean = false

  // This deprecated override is safe to use up to Kotlin 2.1.0 by KT-55300.
  // Also see: https://youtrack.jetbrains.com/issue/KT-52665/Deprecate-ComponentRegistrar#focus=Change-27-7999959.0-0
  override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
    val enabled = configuration[ComposeInvestigatorConfiguration.KEY_ENABLED] ?: true
    if (!enabled) return

    val verbose = configuration[ComposeInvestigatorConfiguration.KEY_VERBOSE] ?: false
    val logger = VerboseLogger(configuration).apply { if (verbose) verbose() }

    project.extensionArea
      .getExtensionPoint(IrGenerationExtension.extensionPointName)
      .registerExtension(
        ComposableCallstackTrackingExtension(logger = logger),
        LoadingOrder.FIRST,
        project,
      )

    project.extensionArea
      .getExtensionPoint(IrGenerationExtension.extensionPointName)
      .registerExtension(
        ComposableInvalidationTrackingExtension(logger = logger),
        LoadingOrder.LAST,
        project,
      )
  }
}
