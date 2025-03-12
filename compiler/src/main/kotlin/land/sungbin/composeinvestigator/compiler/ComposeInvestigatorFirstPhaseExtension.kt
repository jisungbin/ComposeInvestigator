// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

import androidx.compose.compiler.plugins.kotlin.analysis.StabilityInferencer
import java.util.EnumSet
import land.sungbin.composeinvestigator.compiler.lower.InvalidationProcessTracingFirstTransformer
import land.sungbin.composeinvestigator.compiler.lower.ComposeInvestigatorInstantiateTransformer
import land.sungbin.composeinvestigator.compiler.lower.ComposeInvestigatorIntrinsicCallTransformer
import land.sungbin.composeinvestigator.compiler.lower.StateInitializerFirstTransformer
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.validateIr
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.IrVerificationMode
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

/**
 * Performs IR visiting/transformation tasks that must be executed *before* the
 * code transformations conducted by the Compose Compiler.
 *
 * This class primarily handles tasks that should not be affected by Composable
 * Group structures.
 *
 * The following IR transformations are carried out by this class:
 *
 * - [ComposeInvestigatorInstantiateTransformer]
 * - [InvalidationProcessTracingFirstTransformer]
 * - [StateInitializerFirstTransformer]
 * - [ComposeInvestigatorIntrinsicCallTransformer]
 */
public class ComposeInvestigatorFirstPhaseExtension(
  private val messageCollector: MessageCollector,
  private val verificationMode: IrVerificationMode,
  private val features: EnumSet<FeatureFlag>,
) : IrGenerationExtension {
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    messageCollector.log("Enabled first-phase features: ${features.filter { it.phase == 0 }.joinToString()}")

    // Input check. This should always pass, else something is horribly wrong upstream.
    // Necessary because oftentimes the issue is upstream. (compiler bug, prior plugin, etc.)
    validateIr(messageCollector, verificationMode) {
      performBasicIrValidation(
        moduleFragment,
        pluginContext.irBuiltIns,
        "Before ComposeInvestigator First Phase",
        ComposeInvestigatorPluginRegistrar.IrValidatorConfig,
      )
    }

    val composeInvestigatorInstantiator = ComposeInvestigatorInstantiateTransformer(pluginContext, messageCollector)
    moduleFragment.transformChildrenVoid(composeInvestigatorInstantiator)

    if (FeatureFlag.ComposeInvestigatorIntrinsicCall in features)
      moduleFragment.transformChildrenVoid(ComposeInvestigatorIntrinsicCallTransformer(pluginContext, messageCollector))

    if (FeatureFlag.InvalidationProcessTracing in features)
      moduleFragment.transformChildrenVoid(
        InvalidationProcessTracingFirstTransformer(
          pluginContext,
          messageCollector,
          StabilityInferencer(
            currentModule = moduleFragment.descriptor,
            externalStableTypeMatchers = emptySet(), // TODO Supports this feature
          ),
        ),
      )

    if (FeatureFlag.StateInitializerTracking in features)
      moduleFragment.transformChildrenVoid(StateInitializerFirstTransformer(pluginContext, messageCollector))

    // Verify that our transformations didn't break something.
    validateIr(messageCollector, verificationMode) {
      performBasicIrValidation(
        moduleFragment,
        pluginContext.irBuiltIns,
        "After ComposeInvestigator First Phase",
        ComposeInvestigatorPluginRegistrar.IrValidatorConfig,
      )
    }
  }
}
