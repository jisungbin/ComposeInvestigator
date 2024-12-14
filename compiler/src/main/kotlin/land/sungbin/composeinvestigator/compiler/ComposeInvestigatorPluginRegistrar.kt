// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("DEPRECATION", "UnstableApiUsage")

package land.sungbin.composeinvestigator.compiler

import java.util.EnumSet
import org.jetbrains.kotlin.backend.common.IrValidatorConfig
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

/**
 * Registers the [IrGenerationExtension]'s of ComposeInvestigator into the Kotlin Compiler Plugin.
 *
 * This class registers the [IrGenerationExtension] in two [LoadingOrder] phases:
 *
 * 1. [LoadingOrder.FIRST] - Adds the [IrGenerationExtension] of ComposeInvestigator that
 * must run *before* the Compose Compiler operates. This corresponds to [ComposeInvestigatorFirstPhaseExtension].
 *
 * 2. [LoadingOrder.LAST] - Adds the [IrGenerationExtension] of ComposeInvestigator
 * that must run *after* the Compose Compiler has completed. This corresponds to [ComposeInvestigatorLastPhaseExtension].
 *
 * [ComposeInvestigatorPluginRegistrar] only supports the K2 Kotlin compiler.
 */
public class ComposeInvestigatorPluginRegistrar : ComponentRegistrar {
  override val supportsK2: Boolean get() = true

  // TODO This deprecated override is safe to use up to Kotlin 2.1.0 by KT-55300.
  //  See also: https://youtrack.jetbrains.com/issue/KT-52665/Deprecate-ComponentRegistrar#focus=Change-27-7999959.0-0
  override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
    val enabled = configuration.get(ComposeInvestigatorConfiguration.KEY_ENABLED, /* defaultValue = */ true)
    if (!enabled) return

    val verbose = configuration.get(ComposeInvestigatorConfiguration.KEY_VERBOSE, /* defaultValue = */ false)
    val messageCollector = VerboseMessageCollector(configuration.messageCollector).apply { if (verbose) verbose() }
    val verificationMode = configuration.get(CommonConfigurationKeys.VERIFY_IR, /* defaultValue = */ IrVerificationMode.WARNING)

    configuration.messageCollector = messageCollector

    if (!configuration.languageVersionSettings.languageVersion.usesK2)
      throw CompileEnvironmentException(ErrorMessages.SUPPORTS_K2_ONLY)

    FirExtensionRegistrarAdapter.registerExtension(project, ComposeInvestigatorFirExtensionRegistrar())
    project.extensionArea
      .getExtensionPoint(IrGenerationExtension.extensionPointName)
      .run {
        registerExtension(ComposeInvestigatorFirstPhaseExtension(messageCollector, verificationMode), LoadingOrder.FIRST, project)
        registerExtension(ComposeInvestigatorLastPhaseExtension(messageCollector, verificationMode), LoadingOrder.LAST, project)
      }
  }

  public companion object {
    public val DefaultEnabledFeatureFlags: EnumSet<FeatureFlag> =
      EnumSet.allOf(FeatureFlag::class.java).apply { remove(FeatureFlag.StateInitializerTracking) }

    public val DefaultIrValidatorConfig: IrValidatorConfig =
      IrValidatorConfig(
        checkProperties = true,
        checkTypes = false, // TODO KT-68663
      )
  }
}
