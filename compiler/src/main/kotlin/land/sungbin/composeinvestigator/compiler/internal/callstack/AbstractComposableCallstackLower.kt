/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.internal.callstack

import androidx.compose.compiler.plugins.kotlin.hasComposableAnnotation
import androidx.compose.compiler.plugins.kotlin.irTrace
import land.sungbin.composeinvestigator.compiler.internal.key.DurableWritableSlices
import land.sungbin.composeinvestigator.compiler.util.HandledMap
import land.sungbin.composeinvestigator.compiler.util.VerboseLogger
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.impl.IrTryImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.util.OperatorNameConventions

@Suppress("unused")
internal abstract class AbstractComposableCallstackLower(
  private val context: IrPluginContext,
  private val logger: VerboseLogger,
) : IrElementTransformerVoid() {
  private val handledFunction = HandledMap()
  private val handledCall = HandledMap()

  @Suppress("UnnecessaryVariable", "NAME_SHADOWING")
  override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
    val parent = declaration

    if (!parent.hasComposableAnnotation()) return super.visitSimpleFunction(parent)
    if (!handledFunction.handle(parent.sourceKey())) return super.visitSimpleFunction(parent)

    parent.transformChildren(
      object : IrElementTransformer<Name> {
        override fun visitCall(expression: IrCall, data: Name): IrElement {
          val parent = data
          val function = expression.symbol.owner
          val current = function.name

          if (!handledCall.handle(function.sourceKey(), parent.asString())) return super.visitCall(expression, parent)

          if (expression.isComposableInvoke()) return transformComposableCall(parent = parent, expression = expression)
          if (function.hasComposableAnnotation()) {
            for (index in 0 until function.valueParameters.size) {
              val param = function.valueParameters[index]

              when {
                // vararg contents: @Composable () -> Unit
                // (parent is current)
                param.varargElementType?.hasComposableAnnotation() == true -> {
                  val composableVararg = expression.getValueArgument(index) as? IrVararg ?: continue
                  val newComposableVararg = IrVarargImpl(
                    startOffset = composableVararg.startOffset,
                    endOffset = composableVararg.endOffset,
                    type = composableVararg.type,
                    varargElementType = composableVararg.varargElementType,
                    elements = composableVararg.elements,
                  )

                  for (varargIndex in 0 until composableVararg.elements.size) {
                    val nameForComposableVararg = Name.identifier("${current.asString()}$${param.name.asString()}_$varargIndex")

                    val composableLambda = composableVararg.elements[varargIndex] as? IrFunctionExpression ?: continue

                    val transformed = composableLambda.function.body?.transform(this, nameForComposableVararg)
                    composableLambda.function.body = transformed

                    newComposableVararg.elements[varargIndex] = composableLambda
                  }

                  expression.putValueArgument(index, newComposableVararg)
                }
                // content: @Composable () -> Unit
                // (parent is current)
                param.type.hasComposableAnnotation() -> {
                  val nameForComposableLambda = Name.identifier("${current.asString()}$${param.name.asString()}")

                  val composableLambda = expression.getValueArgument(index) as? IrFunctionExpression ?: continue
                  composableLambda.function.body?.transformChildren(this, nameForComposableLambda)

                  expression.putValueArgument(index, composableLambda)
                }
              }
            }

            return transformComposableCall(parent = parent, expression = expression)
          }

          return super.visitCall(expression, parent)
        }
      },
      parent.name,
    )

    return super.visitSimpleFunction(parent)
  }

  private fun IrCall.isComposableInvoke(): Boolean {
    val isInvoke = run {
      if (origin == IrStatementOrigin.INVOKE) return@run true
      symbol.owner.name == OperatorNameConventions.INVOKE &&
        symbol.owner.parentClassOrNull?.defaultType?.isFunction() == true
    }
    return isInvoke && dispatchReceiver?.type?.hasComposableAnnotation() == true
  }

  private fun IrSimpleFunction.sourceKey(): String =
    context.irTrace[DurableWritableSlices.DURABLE_FUNCTION_KEY, this]?.keyName
      ?: (kotlinFqName.asString() + valueParameters.joinToString { it.type.classFqName.toString() })

  abstract fun transformComposableCall(parent: Name, expression: IrCall): IrExpression

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
