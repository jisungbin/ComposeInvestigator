/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler

import androidx.compose.compiler.plugins.kotlin.analysis.StabilityInferencer
import land.sungbin.composeinvestigator.compiler.internal.callstack.ComposableCallstackTransformer
import land.sungbin.composeinvestigator.compiler.internal.key.DurableFunctionKeyTransformer
import land.sungbin.composeinvestigator.compiler.internal.tracker.affect.IrAffectedComposable
import land.sungbin.composeinvestigator.compiler.internal.tracker.affect.IrAffectedField
import land.sungbin.composeinvestigator.compiler.internal.tracker.logger.IrInvalidationLogger
import land.sungbin.composeinvestigator.compiler.util.VerboseLogger
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

internal class InvalidationTrackingExtension(private val logger: VerboseLogger) : IrGenerationExtension {
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
      DurableFunctionKeyTransformer(
        context = pluginContext,
        stabilityInferencer = stabilityInferencer,
      ),
    )
    moduleFragment.transformChildrenVoid(
      ComposableCallstackTransformer(
        context = pluginContext,
        logger = logger,
      ),
    )
//    moduleFragment.transformChildrenVoid(
//      InvalidationTrackableTransformer(
//        context = pluginContext,
//        logger = logger,
//        stabilityInferencer = stabilityInferencer,
//      ),
//    )
    moduleFragment.transformChildrenVoid(
      object : IrElementTransformerVoidWithContext() {
        override fun visitFileNew(declaration: IrFile): IrFile {
          if (declaration.name == "SourceForIrDump.kt") {
            logger("[IR DUMPING TEST RESULT]")
            logger(declaration.dump())
            logger(declaration.dumpKotlinLike())
          }
          return declaration
        }
      },
    )
  }
}
