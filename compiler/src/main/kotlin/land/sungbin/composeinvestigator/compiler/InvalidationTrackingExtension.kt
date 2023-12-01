/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler

import androidx.compose.compiler.plugins.kotlin.analysis.StabilityInferencer
import land.sungbin.composeinvestigator.compiler.internal.tracker.InvalidationTrackableTransformer
import land.sungbin.composeinvestigator.compiler.internal.tracker.affect.IrAffectedField
import land.sungbin.composeinvestigator.compiler.internal.tracker.affect.IrAffectedComposable
import land.sungbin.composeinvestigator.compiler.internal.tracker.key.DurableFunctionKeyTransformer
import land.sungbin.composeinvestigator.compiler.internal.tracker.logger.InvalidationLoggerVisitor
import land.sungbin.composeinvestigator.compiler.internal.tracker.logger.IrInvalidationLogger
import land.sungbin.composeinvestigator.compiler.util.VerboseLogger
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

internal class InvalidationTrackingExtension(private val logger: VerboseLogger) : IrGenerationExtension {
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    try {
      IrInvalidationLogger.init(pluginContext)
      IrAffectedField.init(pluginContext)
      IrAffectedComposable.init(pluginContext)

      moduleFragment.transformChildrenVoid(InvalidationLoggerVisitor(pluginContext, logger))

      if (IrInvalidationLogger.getCurrentLoggerSymbolOrNull() == null) {
        IrInvalidationLogger.useDefaultLogger(pluginContext)
      }
    } finally {
      moduleFragment.transformChildrenVoid(DurableFunctionKeyTransformer(pluginContext))
      moduleFragment.transformChildrenVoid(
        InvalidationTrackableTransformer(
          context = pluginContext,
          logger = logger,
          stabilityInferencer = StabilityInferencer(
            currentModule = moduleFragment.descriptor,
            // TODO: support this field
            externalStableTypeMatchers = emptySet(),
          ),
        ),
      )
    }
  }
}
