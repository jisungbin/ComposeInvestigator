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
import land.sungbin.composeinvestigator.compiler.frontend.ComposeInvestigatorFirExtensionRegistrar
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.messageCollector
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

public class ComposeInvestigatorPluginRegistrar : ComponentRegistrar {
  override val supportsK2: Boolean = true

  // This deprecated override is safe to use up to Kotlin 2.1.0 by KT-55300.
  // Also see: https://youtrack.jetbrains.com/issue/KT-52665/Deprecate-ComponentRegistrar#focus=Change-27-7999959.0-0
  override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
    val enabled = configuration[ComposeInvestigatorConfiguration.KEY_ENABLED] != false
    if (!enabled) return

    val verbose = configuration[ComposeInvestigatorConfiguration.KEY_VERBOSE] == true
    val messageCollector = VerboseMessageCollector(configuration.messageCollector).apply { if (verbose) verbose() }

    FirExtensionRegistrarAdapter.registerExtension(project, ComposeInvestigatorFirExtensionRegistrar(messageCollector))

    // We need to explicitly define the LAST order because ComposeInvestigator should
    // run after the Compose compiler is applied.
    project.extensionArea
      .getExtensionPoint(IrGenerationExtension.extensionPointName)
      .registerExtension(
        ComposableInvalidationTracingExtension(messageCollector),
        LoadingOrder.LAST,
        project,
      )
  }
}
