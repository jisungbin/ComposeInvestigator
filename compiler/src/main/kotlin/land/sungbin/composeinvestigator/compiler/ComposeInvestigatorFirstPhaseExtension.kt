/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler

import androidx.compose.compiler.plugins.kotlin.analysis.StabilityInferencer
import land.sungbin.composeinvestigator.compiler.lower.DurableComposableKeyTransformer
import land.sungbin.composeinvestigator.compiler.lower.InvalidationProcessTracingFirstTransformer
import land.sungbin.composeinvestigator.compiler.lower.StateInitializerFirstTransformer
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

public class ComposeInvestigatorFirstPhaseExtension(private val messageCollector: MessageCollector) : IrGenerationExtension {
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    val stabilityInferencer = StabilityInferencer(
      currentModule = moduleFragment.descriptor,
      externalStableTypeMatchers = emptySet(), // TODO supports this feature
    )

    moduleFragment.transformChildrenVoid(DurableComposableKeyTransformer(pluginContext, stabilityInferencer))
    moduleFragment.transformChildrenVoid(InvalidationProcessTracingFirstTransformer(pluginContext, messageCollector, stabilityInferencer))
    moduleFragment.transformChildrenVoid(StateInitializerFirstTransformer(pluginContext, messageCollector))
  }
}
