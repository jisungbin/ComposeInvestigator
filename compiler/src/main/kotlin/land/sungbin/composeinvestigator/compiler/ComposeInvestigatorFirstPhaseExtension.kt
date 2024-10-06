// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

import androidx.compose.compiler.plugins.kotlin.analysis.StabilityInferencer
import java.util.EnumSet
import land.sungbin.composeinvestigator.compiler.analysis.DurableComposableKeyAnalyzer
import land.sungbin.composeinvestigator.compiler.lower.InvalidationProcessTracingFirstTransformer
import land.sungbin.composeinvestigator.compiler.lower.InvalidationTraceTableInstanceTransformer
import land.sungbin.composeinvestigator.compiler.lower.InvalidationTraceTableIntrinsicTransformer
import land.sungbin.composeinvestigator.compiler.lower.StateInitializerFirstTransformer
import land.sungbin.composeinvestigator.compiler.struct.IrComposableInformation
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.validateIr
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.IrVerificationMode
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

public class ComposeInvestigatorFirstPhaseExtension(
  private val messageCollector: MessageCollector,
  private val verificationMode: IrVerificationMode,
  private val features: EnumSet<FeatureFlag> = EnumSet.allOf(FeatureFlag::class.java),
) : IrGenerationExtension {
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    val stabilityInferencer = StabilityInferencer(
      currentModule = moduleFragment.descriptor,
      externalStableTypeMatchers = emptySet(), // TODO Supports this feature
    )
    val tables = InvalidationTraceTableInstanceTransformer(pluginContext, messageCollector)

    messageCollector.log("Enabled first-phase features: ${features.filter { it.phase == 0 }.joinToString()}")

    if (features.isNotEmpty()) {
      moduleFragment.transformChildrenVoid(DurableComposableKeyAnalyzer(pluginContext, stabilityInferencer))
      moduleFragment.transformChildrenVoid(tables)
    }

    if (features.count { it.phase == 0 } == 0) return

    // Input check. This should always pass, else something is horribly wrong upstream.
    // Necessary because oftentimes the issue is upstream. (compiler bug, prior plugin, etc)
    validateIr(messageCollector, verificationMode) {
      performBasicIrValidation(
        moduleFragment,
        pluginContext.irBuiltIns,
        phaseName = "Before ComposeInvestigator First Phase",
        checkProperties = true,
        checkTypes = false, // TODO KT-68663
      )
    }

    if (FeatureFlag.InvalidationProcessTracing in features)
      moduleFragment.transformChildrenVoid(InvalidationProcessTracingFirstTransformer(pluginContext, messageCollector, tables, stabilityInferencer))

    if (FeatureFlag.StateInitializerTracking in features)
      moduleFragment.transformChildrenVoid(StateInitializerFirstTransformer(pluginContext, messageCollector, tables))

    if (FeatureFlag.InvalidationTraceTableIntrinsicCall in features)
      moduleFragment.transformChildrenVoid(InvalidationTraceTableIntrinsicTransformer(pluginContext, IrComposableInformation(pluginContext), tables))

    // Verify that our transformations didn't break something.
    validateIr(messageCollector, verificationMode) {
      performBasicIrValidation(
        moduleFragment,
        pluginContext.irBuiltIns,
        phaseName = "After ComposeInvestigator First Phase",
        checkProperties = true,
        checkTypes = false, // TODO KT-68663
      )
    }
  }
}
