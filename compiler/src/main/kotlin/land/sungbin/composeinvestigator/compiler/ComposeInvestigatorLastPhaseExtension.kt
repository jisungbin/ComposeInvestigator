// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

import java.util.EnumSet
import land.sungbin.composeinvestigator.compiler.lower.InvalidationSkipTracingLastTransformer
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.validateIr
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.IrVerificationMode
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

/**
 * Performs IR visiting/transformation tasks that must be executed *after* the
 * code transformations conducted by the Compose Compiler.
 *
 * This class primarily handles tasks that depend on Composable Group structures.
 *
 * The following IR transformations are carried out by this class:
 *
 * - [ComposeInvestigatorInstantiateTransformer]
 * - [InvalidationSkipTracingLastTransformer]
 */
public class ComposeInvestigatorLastPhaseExtension(
  private val messageCollector: MessageCollector,
  private val verificationMode: IrVerificationMode,
  private val features: EnumSet<FeatureFlag>,
) : IrGenerationExtension {
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    messageCollector.log("Enabled last-phase features: ${features.filter { it.phase == 1 }.joinToString()}")

    // Input check. This should always pass, else something is horribly wrong upstream.
    // Necessary because oftentimes the issue is upstream. (compiler bug, prior plugin, etc)
    validateIr(messageCollector, verificationMode) {
      performBasicIrValidation(
        moduleFragment,
        pluginContext.irBuiltIns,
        "Before ComposeInvestigator Last Phase",
        ComposeInvestigatorPluginRegistrar.IrValidatorConfig,
      )
    }

    if (FeatureFlag.InvalidationSkipTracing in features)
      moduleFragment.transformChildrenVoid(InvalidationSkipTracingLastTransformer(pluginContext, messageCollector))

    // Verify that our transformations didn't break something.
    validateIr(messageCollector, verificationMode) {
      performBasicIrValidation(
        moduleFragment,
        pluginContext.irBuiltIns,
        "After ComposeInvestigator Last Phase",
        ComposeInvestigatorPluginRegistrar.IrValidatorConfig,
      )
    }
  }
}
