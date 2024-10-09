// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.lower

import androidx.compose.compiler.plugins.kotlin.irTrace
import land.sungbin.composeinvestigator.compiler.COMPOSER_FQN
import land.sungbin.composeinvestigator.compiler.analysis.DurationWritableSlices
import land.sungbin.composeinvestigator.compiler.log
import land.sungbin.composeinvestigator.compiler.struct.IrComposableInformation
import land.sungbin.composeinvestigator.compiler.struct.IrInvalidationTraceTable
import land.sungbin.composeinvestigator.compiler.struct.IrInvalidationTraceTableHolder
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.getCompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.fromSymbolOwner
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.file

/**
 * Generates code that reports whenever a Composable function is skipped during
 * recomposition due to smart recomposition.
 *
 * ### Original
 *
 * ```
 * @Composable fun DisplayPlusResult(a: Int, b: Int) {
 *   Text((a + b).toString())
 * }
 * ```
 *
 * ### Transformed
 *
 * ```
 * @Composable fun DisplayPlusResult(a: Int, b: Int) {
 *   if (!currentComposer.skipping) {
 *     Text((a + b).toString())
 *   } else {
 *     val affectedComposable = ComposableInformation(
 *       name = "DisplayPlusResult",
 *       packageName = "land.sungbin.composeinvestigator.sample",
 *       fileName = "DisplayPlusResult.kt",
 *       compoundKey = androidx.compose.runtime.currentCompositeKeyHash,
 *     )
 *     ComposeInvestigator.logger.log(affectedComposable, InvalidationReason.Skipped)
 *     currentComposer.skipToGroupEnd()
 *   }
 * }
 * ```
 */
public class InvalidationSkipTracingLastTransformer(
  context: IrPluginContext,
  messageCollector: MessageCollector,
  table: IrInvalidationTraceTableHolder,
) : ComposeInvestigatorBaseLower(context, messageCollector, table) {
  override fun lastTransformSkipToGroupEndCall(
    composable: IrSimpleFunction,
    expression: IrCall,
    table: IrInvalidationTraceTable,
  ): IrExpression {
    messageCollector.log(
      "Visit skipToGroupEnd call: ${composable.name}",
      expression.getCompilerMessageLocation(composable.file),
    )

    val currentKey = context.irTrace[DurationWritableSlices.DURABLE_FUNCTION_KEY, composable] ?: return expression
    val composer = composable.valueParameters
      .last { param -> param.type.classFqName == COMPOSER_FQN }
      .let(::irGetValue)

    val compoundKeyHashCall = IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = composerCompoundKeyHashSymbol,
    ).also { fn ->
      fn.dispatchReceiver = composer
    }

    val affectedComposable = IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = IrComposableInformation.withCompoundKeySymbol(context),
    ).apply {
      dispatchReceiver = currentKey.composable
      putValueArgument(0, compoundKeyHashCall)
    }

    val invalidationResultSkipped = invalidationLogger.irInvalidationResultSkipped()
      .apply { type = invalidationLogger.irInvalidationResultSymbol.defaultType }
    val logger = invalidationLogger.irLog(affectedComposable, result = invalidationResultSkipped)

    return IrBlockImpl(
      startOffset = expression.startOffset,
      endOffset = expression.endOffset,
      type = context.irBuiltIns.unitType,
      statements = listOf(logger, expression),
    )
      .also {
        messageCollector.log(
          "Transform skipToGroupEnd call succeed: ${composable.name}",
          expression.getCompilerMessageLocation(composable.file),
        )
      }
  }
}
