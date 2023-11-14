/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

package land.sungbin.composeinvalidator.compiler.internal.transformer

import land.sungbin.composeinvalidator.compiler.internal.COMPOSABLE_FQN
import land.sungbin.composeinvalidator.compiler.internal.COMPOSER_FQN
import land.sungbin.composeinvalidator.compiler.internal.SKIP_TO_GROUP_END
import land.sungbin.composeinvalidator.compiler.internal.origin.InvalidationTrackableOrigin
import land.sungbin.composeinvalidator.compiler.util.VerboseLogger
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrElseBranch
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.isNullableAny
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal class InvalidationTrackableTransformer(
  private val context: IrPluginContext,
  private val logger: VerboseLogger,
) : IrElementTransformerVoid() {
  private val printlnSymbol: IrSimpleFunctionSymbol =
    context
      .referenceFunctions(
        CallableId(
          packageName = FqName("kotlin.io"),
          callableName = Name.identifier("println"),
        ),
      )
      .single { symbol ->
        symbol.owner.valueParameters.size == 1 &&
          symbol.owner.valueParameters.single().type.isNullableAny()
      }

  override fun visitFunction(declaration: IrFunction): IrStatement {
    if (!declaration.hasAnnotation(COMPOSABLE_FQN)) return super.visitFunction(declaration)
    declaration.body?.transformChildrenVoid()
    return super.visitFunction(declaration)
  }

  override fun visitBody(body: IrBody): IrBody {
    for (statement in body.statements) {
      statement.transformChildrenVoid()
    }
    return super.visitBody(body)
  }

  override fun visitBlock(expression: IrBlock): IrExpression {
//    expression.statements.findIsInstanceAnd<IrCall> { call ->
//      logger("visitBlock - call: ${call.dump()}")
//
//      val fnName = call.symbol.owner.name
//      val fnParentFqn = call.symbol.owner.parent.kotlinFqName
//
//      (fnName == TRACE_EVENT_START && fnParentFqn == COMPOSER_FQN).also {
//        logger("visitBlock is $TRACE_EVENT_START: $it")
//      }
//    }
    logger(expression.statements.joinToString(separator = "\n\n\n") { it.dump() })
    return super.visitBlock(expression)
  }

  override fun visitElseBranch(branch: IrElseBranch): IrElseBranch {
    val call = branch.result as? IrCall ?: return super.visitElseBranch(branch)

    val fnName = call.symbol.owner.name
    val fnParentFqn = call.symbol.owner.parent.kotlinFqName

    if (fnName == SKIP_TO_GROUP_END && fnParentFqn == COMPOSER_FQN) {
      val printerCall = irPrintlnCall(irString("[INVALIDATION_TRACKER] invalidation skipped"))
      val block =
        IrBlockImpl(
          startOffset = UNDEFINED_OFFSET,
          endOffset = UNDEFINED_OFFSET,
          type = context.irBuiltIns.unitType,
          origin = InvalidationTrackableOrigin,
          statements = listOf(printerCall, call),
        )
      branch.result = block
      logger("transformed: ${call.dump()} -> ${block.dump()}")
    }

    return super.visitElseBranch(branch)
  }

  private fun irPrintlnCall(value: IrExpression): IrCall =
    IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = printlnSymbol,
    ).apply {
      origin = InvalidationTrackableOrigin
      putValueArgument(0, value)
    }

  private fun irString(value: String): IrConst<String> =
    IrConstImpl.string(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      type = context.irBuiltIns.stringType,
      value = value,
    )
}
