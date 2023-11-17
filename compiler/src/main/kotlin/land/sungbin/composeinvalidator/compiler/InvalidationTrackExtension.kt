/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

package land.sungbin.composeinvalidator.compiler

import androidx.compose.compiler.plugins.kotlin.analysis.StabilityInferencer
import land.sungbin.composeinvalidator.compiler.internal.transformer.InvalidationTrackableTransformer
import land.sungbin.composeinvalidator.compiler.internal.transformer.IrInvalidationTrackTableClass
import land.sungbin.composeinvalidator.compiler.internal.transformer.key.DurableFunctionKeyTransformer
import land.sungbin.composeinvalidator.compiler.util.VerboseLogger
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal class InvalidationTrackExtension(private val logger: VerboseLogger) : IrGenerationExtension {
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    val stabilityInferencer = StabilityInferencer(moduleFragment.descriptor, emptySet())

    DurableFunctionKeyTransformer(pluginContext).lower(moduleFragment)
    moduleFragment.transformChildren(
      transformer = InvalidationTrackableTransformer(
        context = pluginContext,
        logger = logger,
        stabilityInferencer = stabilityInferencer,
      ),
      data = IrInvalidationTrackTableClass.empty(),
    )
  }
}
