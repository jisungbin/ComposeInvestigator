/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.internal.callstack

import androidx.compose.compiler.plugins.kotlin.hasComposableAnnotation
import land.sungbin.composeinvestigator.compiler.internal.COMPOSER_FQN
import land.sungbin.composeinvestigator.compiler.util.HandledMap
import land.sungbin.composeinvestigator.compiler.util.VerboseLogger
import land.sungbin.fastlist.fastAny
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrTryImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.Name

// Once issue #77 is resolved, continue development...
@Suppress("unused")
internal abstract class AbstractComposableCallstackLower(
  private val context: IrPluginContext,
  private val logger: VerboseLogger,
) : IrElementTransformerVoid() {
  private val handledFunction = HandledMap()
  private val handledCall = HandledMap()

  override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
    @Suppress("UnnecessaryVariable", "RedundantSuppression") // false-positive "RedundantSuppression"
    val parent = declaration

    if (!parent.hasComposableAnnotation() && !parent.hasComposerParam()) return super.visitSimpleFunction(parent)
    if (!handledFunction.handle(parent)) return super.visitSimpleFunction(parent)

    parent.transformChildren(
      object : IrElementTransformer<IrSimpleFunction> {
        override fun visitCall(expression: IrCall, data: IrSimpleFunction): IrElement {
          @Suppress("NAME_SHADOWING")
          val parent = data

          val call = expression.symbol.owner as? IrSimpleFunction ?: return super.visitCall(expression, parent)
          if (!handledCall.handle(call)) return super.visitCall(expression, parent)

          if (call.hasComposableAnnotation() || call.hasComposerParam()) {
            for (index in 0 until expression.valueArgumentsCount) {
              val param = expression.getValueArgument(index) ?: continue // if user no provide value, it will be null (use default value)

              val composableLambda = param as? IrFunctionExpression ?: continue // maybe
              val composable = composableLambda.function // maybe

              if (composable.hasComposableAnnotation() || composable.hasComposerParam()) { // assert composableLambda is real composableLambda
                val originalName = composable.name
                val nameForComposableLambda = Name.identifier("${parent.name.asString()}_${index}_arg")
                try {
                  composable.transformChildren(this, composable.apply { name = nameForComposableLambda })
                } finally {
                  composable.name = originalName
                }
                val transformed = transformComposableLambdaValueArgument(
                  parent = nameForComposableLambda,
                  expression = composableLambda,
                )
                expression.putValueArgument(index, transformed)
              }
            }
            return transformComposableCall(parent = parent.name, expression = expression)
          }

          return super.visitCall(expression, parent)
        }
      },
      parent,
    )

    return super.visitSimpleFunction(parent)
  }

  abstract fun transformComposableCall(parent: Name, expression: IrCall): IrExpression
  abstract fun transformComposableLambdaValueArgument(parent: Name, expression: IrFunctionExpression): IrExpression

  private fun IrSimpleFunction.hasComposerParam() =
    valueParameters.fastAny { param -> param.type.classFqName == COMPOSER_FQN }

  protected fun IrExpression.wrapTryFinally(
    startOffset: Int = this.startOffset,
    endOffset: Int = this.endOffset,
    type: IrType = this.type,
    tryResult: IrExpression,
    finallyBlock: IrExpression?,
  ): IrTryImpl = IrTryImpl(
    startOffset = startOffset,
    endOffset = endOffset,
    type = type,
    tryResult = tryResult,
    catches = emptyList(),
    finallyExpression = finallyBlock,
  )
}
