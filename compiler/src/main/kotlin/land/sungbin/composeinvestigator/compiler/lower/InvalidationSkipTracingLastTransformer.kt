/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.lower

import androidx.compose.compiler.plugins.kotlin.irTrace
import land.sungbin.composeinvestigator.compiler.HandledMap
import land.sungbin.composeinvestigator.compiler.analysis.DurationWritableSlices
import land.sungbin.composeinvestigator.compiler.log
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.getCompilerMessageLocation
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrAttributeContainer
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.file

internal class InvalidationSkipTracingLastTransformer(context: IrPluginContext) : ComposeInvestigatorBaseLower(context) {
  private val handled = HandledMap()

  override fun lastTransformSkipToGroupEndCall(composable: IrSimpleFunction, expression: IrCall): IrExpression {
    messageCollector.log(
      "Visit skipToGroupEnd call: ${composable.name}",
      expression.getCompilerMessageLocation(expression.symbol.owner.file),
    )

    val currentKey = context.irTrace[DurationWritableSlices.DURABLE_FUNCTION_KEY, composable] ?: return expression
    if (!handled.handle(currentKey.keyName)) return expression

    val invalidationTypeSkipped = invalidationLogger.irInvalidationTypeSkipped().apply {
      type = invalidationLogger.irInvalidationTypeSymbol.defaultType
    }
    val logger = invalidationLogger.irLog(currentKey.composable, type = invalidationTypeSkipped)

    // TODO IrBlockImpl
    return object : IrBlock() {
      override var origin: IrStatementOrigin? = null
      override var type: IrType = context.irBuiltIns.unitType

      override val startOffset: Int = expression.startOffset
      override val endOffset: Int = UNDEFINED_OFFSET // TODO

      override var attributeOwnerId: IrAttributeContainer = this
      override var originalBeforeInline: IrAttributeContainer? = null

      override val statements: MutableList<IrStatement> = mutableListOf(logger, expression)
    }
      .also {
        messageCollector.log(
          "Transform skipToGroupEnd call succeed: ${composable.name}",
          expression.getCompilerMessageLocation(expression.symbol.owner.file),
        )
      }
  }
}
