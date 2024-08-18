/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.lower

import androidx.compose.compiler.plugins.kotlin.irTrace
import land.sungbin.composeinvestigator.compiler.analysis.DurationWritableSlices
import land.sungbin.composeinvestigator.compiler.log
import land.sungbin.composeinvestigator.compiler.struct.IrInvalidationTraceTable
import land.sungbin.composeinvestigator.compiler.struct.IrInvalidationTraceTableHolder
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.getCompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.file

internal class InvalidationSkipTracingLastTransformer(
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

    val invalidationTypeSkipped = invalidationLogger.irInvalidationTypeSkipped().apply {
      type = invalidationLogger.irInvalidationTypeSymbol.defaultType
    }
    val logger = invalidationLogger.irLog(currentKey.composable, type = invalidationTypeSkipped)

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
