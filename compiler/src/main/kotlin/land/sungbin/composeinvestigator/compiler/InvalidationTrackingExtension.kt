/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler

import androidx.compose.compiler.plugins.kotlin.analysis.StabilityInferencer
import land.sungbin.composeinvestigator.compiler.internal.tracker.InvalidationTrackableTransformer
import land.sungbin.composeinvestigator.compiler.internal.tracker.key.DurableFunctionKeyTransformer
import land.sungbin.composeinvestigator.compiler.internal.tracker.logger.InvalidationLogger
import land.sungbin.composeinvestigator.compiler.internal.tracker.logger.InvalidationLoggerVisitor
import land.sungbin.composeinvestigator.compiler.util.VerboseLogger
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

internal class InvalidationTrackingExtension(private val logger: VerboseLogger) : IrGenerationExtension {
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    // TODO: Supports externalStableTypeMatchers.
    val stabilityInferencer = StabilityInferencer(moduleFragment.descriptor, emptySet())

    InvalidationLogger.init(pluginContext)

    moduleFragment.transformChildrenVoid(DurableFunctionKeyTransformer(pluginContext))
    moduleFragment.transformChildrenVoid(InvalidationLoggerVisitor(pluginContext, logger))

    if (InvalidationLogger.getCurrentLoggerSymbolOrNull() == null) {
      InvalidationLogger.useDefaultLogger(pluginContext)
    }

    moduleFragment.transformChildrenVoid(
      InvalidationTrackableTransformer(
        context = pluginContext,
        logger = logger,
        stabilityInferencer = stabilityInferencer,
      ),
    )

    logger("[TRANSFORM RESULT]")
    for (file in moduleFragment.files) {
      logger("\n\n")
      logger(file.dump())
      logger("\n")
      logger(file.dumpKotlinLike())
    }
  }
}
