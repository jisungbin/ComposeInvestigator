/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

package land.sungbin.composeinvalidator.compiler

import land.sungbin.composeinvalidator.compiler.internal.key.DurableFunctionKeyTransformer
import land.sungbin.composeinvalidator.compiler.internal.transformer.InvalidationTrackableTransformer
import land.sungbin.composeinvalidator.compiler.util.VerboseLogger
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

internal class InvalidationTrackExtension(private val logger: VerboseLogger) : IrGenerationExtension {
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    DurableFunctionKeyTransformer(pluginContext).lower(moduleFragment)
    moduleFragment.transformChildrenVoid(InvalidationTrackableTransformer(pluginContext, logger))
  }
}
