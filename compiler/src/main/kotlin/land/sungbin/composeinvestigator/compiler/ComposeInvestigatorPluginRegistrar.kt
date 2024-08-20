/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("DEPRECATION", "UnstableApiUsage")

package land.sungbin.composeinvestigator.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.jvm.compiler.CompileEnvironmentException
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.extensions.LoadingOrder
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.IrVerificationMode
import org.jetbrains.kotlin.config.languageVersionSettings
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

    if (!configuration.languageVersionSettings.languageVersion.usesK2) {
      throw CompileEnvironmentException(ErrorMessages.SUPPORTS_K2_ONLY)
    }

    configuration.messageCollector = messageCollector
    val verificationMode = configuration.get(CommonConfigurationKeys.VERIFY_IR, IrVerificationMode.WARNING)

    FirExtensionRegistrarAdapter.registerExtension(project, ComposeInvestigatorFirExtensionRegistrar())
    project.extensionArea
      .getExtensionPoint(IrGenerationExtension.extensionPointName)
      .run {
        registerExtension(ComposeInvestigatorFirstPhaseExtension(messageCollector, verificationMode), LoadingOrder.FIRST, project)
        registerExtension(ComposeInvestigatorLastPhaseExtension(messageCollector, verificationMode), LoadingOrder.LAST, project)
      }
  }
}
