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
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrAttributeContainer
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
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

    // TODO IrBlockImpl
    return object : IrBlock() {
      override var origin: IrStatementOrigin? = null
      override var type = context.irBuiltIns.unitType

      override val startOffset = expression.startOffset
      override val endOffset = UNDEFINED_OFFSET // TODO

      override var attributeOwnerId: IrAttributeContainer = this
      override var originalBeforeInline: IrAttributeContainer? = null

      override val statements = mutableListOf<IrStatement>(logger, expression)
    }
      .also {
        messageCollector.log(
          "Transform skipToGroupEnd call succeed: ${composable.name}",
          expression.getCompilerMessageLocation(composable.file),
        )
      }
  }
}
