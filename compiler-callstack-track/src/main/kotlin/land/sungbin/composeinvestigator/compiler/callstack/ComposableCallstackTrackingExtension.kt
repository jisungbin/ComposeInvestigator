/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.callstack

import land.sungbin.composeinvestigator.compiler.base.VerboseLogger
import land.sungbin.composeinvestigator.compiler.callstack.internal.ComposableCallstackTrackingTransformer
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

internal class ComposableCallstackTrackingExtension(private val logger: VerboseLogger) : IrGenerationExtension {
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    moduleFragment.transformChildrenVoid(
      ComposableCallstackTrackingTransformer(
        context = pluginContext,
        logger = logger,
      ),
    )
  }
}
