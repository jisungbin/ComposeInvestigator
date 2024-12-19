// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

import java.util.EnumSet
import land.sungbin.composeinvestigator.compiler.ComposeInvestigatorPluginRegistrar.Companion.DefaultIrValidatorConfig
import land.sungbin.composeinvestigator.compiler.lower.InvalidationSkipTracingLastTransformer
import land.sungbin.composeinvestigator.compiler.lower.InvalidationTraceTableInstantiateTransformer
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
 * The following two IR transformations are carried out by this class:
 *
 * - [InvalidationTraceTableInstantiateTransformer]
 * - [InvalidationSkipTracingLastTransformer]
 */
public class ComposeInvestigatorLastPhaseExtension(
  private val messageCollector: MessageCollector,
  private val verificationMode: IrVerificationMode,
  // TODO Consider accepting this value from an CommandLineProcessor.
  private val features: EnumSet<FeatureFlag> = ComposeInvestigatorPluginRegistrar.DefaultEnabledFeatureFlags,
) : IrGenerationExtension {
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    val tables = InvalidationTraceTableInstantiateTransformer(pluginContext, messageCollector)

    messageCollector.log("Enabled last-phase features: ${features.filter { it.phase == 1 }.joinToString()}")

    if (features.count { it.phase == 1 } == 0) return

    // Input check. This should always pass, else something is horribly wrong upstream.
    // Necessary because oftentimes the issue is upstream. (compiler bug, prior plugin, etc)
    validateIr(messageCollector, verificationMode) {
      performBasicIrValidation(
        moduleFragment,
        pluginContext.irBuiltIns,
        "Before ComposeInvestigator Last Phase",
        DefaultIrValidatorConfig,
      )
    }

    moduleFragment.transformChildrenVoid(tables)

    if (FeatureFlag.InvalidationSkipTracing in features)
      moduleFragment.transformChildrenVoid(InvalidationSkipTracingLastTransformer(pluginContext, messageCollector, tables))

    // Verify that our transformations didn't break something.
    validateIr(messageCollector, verificationMode) {
      performBasicIrValidation(
        moduleFragment,
        pluginContext.irBuiltIns,
        "After ComposeInvestigator Last Phase",
        DefaultIrValidatorConfig,
      )
    }
  }
}
