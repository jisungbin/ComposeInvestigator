/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.invalidation

import androidx.compose.compiler.plugins.kotlin.analysis.StabilityInferencer
import land.sungbin.composeinvestigator.compiler.base.VerboseLogger
import land.sungbin.composeinvestigator.compiler.invalidation.internal.key.DurableComposableKeyTransformer
import land.sungbin.composeinvestigator.compiler.invalidation.internal.tracker.ComposableInvalidationTrackingTransformer
import land.sungbin.composeinvestigator.compiler.invalidation.internal.tracker.affect.IrAffectedComposable
import land.sungbin.composeinvestigator.compiler.invalidation.internal.tracker.affect.IrAffectedField
import land.sungbin.composeinvestigator.compiler.invalidation.internal.tracker.logger.IrInvalidationLogger
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

internal class ComposableInvalidationTrackingExtension(private val logger: VerboseLogger) : IrGenerationExtension {
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    IrInvalidationLogger.init(pluginContext)
    IrAffectedField.init(pluginContext)
    IrAffectedComposable.init(pluginContext)

    val stabilityInferencer = StabilityInferencer(
      currentModule = moduleFragment.descriptor,
      // TODO: support this field
      externalStableTypeMatchers = emptySet(),
    )

    moduleFragment.transformChildrenVoid(
      DurableComposableKeyTransformer(
        context = pluginContext,
        stabilityInferencer = stabilityInferencer,
      ),
    )
    moduleFragment.transformChildrenVoid(
      ComposableInvalidationTrackingTransformer(
        context = pluginContext,
        logger = logger,
        stabilityInferencer = stabilityInferencer,
      ),
    )
  }
}
