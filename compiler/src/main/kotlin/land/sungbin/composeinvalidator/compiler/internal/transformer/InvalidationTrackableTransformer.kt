/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

package land.sungbin.composeinvalidator.compiler.internal.transformer

import land.sungbin.composeinvalidator.compiler.internal.COMPOSABLE_FQN
import land.sungbin.composeinvalidator.compiler.internal.COMPOSER_FQN
import land.sungbin.composeinvalidator.compiler.internal.COMPOSER_KT_FQN
import land.sungbin.composeinvalidator.compiler.internal.IS_TRACE_IN_PROGRESS
import land.sungbin.composeinvalidator.compiler.internal.SKIP_TO_GROUP_END
import land.sungbin.composeinvalidator.compiler.internal.TRACE_EVENT_END
import land.sungbin.composeinvalidator.compiler.internal.TRACE_EVENT_START
import land.sungbin.composeinvalidator.compiler.internal.origin.InvalidationTrackableOrigin
import land.sungbin.composeinvalidator.compiler.util.VerboseLogger
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrElseBranch
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrWhen
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.isNullableAny
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isTopLevel
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.load.kotlin.FacadeClassSource
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addToStdlib.cast

internal class InvalidationTrackableTransformer(
  private val context: IrPluginContext,
  private val logger: VerboseLogger,
) : IrElementTransformerVoidWithContext() {
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

  override fun visitFunctionNew(declaration: IrFunction): IrStatement {
    if (!declaration.hasAnnotation(COMPOSABLE_FQN)) return super.visitFunctionNew(declaration)
    declaration.body?.transformChildrenVoid()
    return super.visitFunctionNew(declaration)
  }

  // <BLOCK> {
  //   <WHEN> when { <CALL> isTraceInProgress() -> <CALL> traceEventStart() }
  //   composable()
  //   <WHEN> when { <CALL> isTraceInProgress() -> <CALL> traceEventEnd() }
  // }
  override fun visitBlock(expression: IrBlock): IrExpression {
    // skip if it is already transformed
    if (expression.origin == InvalidationTrackableOrigin) return super.visitBlock(expression)

    // composable ir block is always has more than 3 statements
    if (expression.statements.size < 3) return super.visitBlock(expression)

    val firstStatement = expression.statements.first()
    val lastStatement = expression.statements.last()

    // composable ir block's first and last statement must be IrWhen
    if (firstStatement !is IrWhen || lastStatement !is IrWhen) return super.visitBlock(expression)

    val isComposerIrBlock = firstStatement.isComposerTrackBranch() && lastStatement.isComposerTrackBranch()
    if (isComposerIrBlock) {
      val printerCall = irPrintlnCall(irString("[INVALIDATION_TRACKER] <$currentFunctionName> invalidation processed"))
      val log = buildString {
        for ((index, statement) in expression.statements.withIndex()) {
          val dump = run {
            val dump = statement.dump().trimIndent()
            if (dump.length > 500) dump.substring(0, 500) + "..." else dump
          }
          if (index == 1) appendLine(">>>>>>>>>> Add: ${printerCall.dump()}")
          appendLine(dump)
          if (index == 3) {
            appendLine("..")
            break
          }
        }
      }
      logger("[invalidation processed] transformed: $log")
      expression.statements.add(1, printerCall)
      expression.origin = InvalidationTrackableOrigin
    }

    return super.visitBlock(expression)
  }

  // <WHEN> when {
  //   ...
  //   else -> <CALL> $composer.skipToGroupEnd()
  // }
  override fun visitElseBranch(branch: IrElseBranch): IrElseBranch {
    val call = branch.result as? IrCall ?: return super.visitElseBranch(branch)

    val fnName = call.symbol.owner.name
    val fnParentFqn = call.symbol.owner.parent.kotlinFqName

    // SKIP_TO_GROUP_END is declared in 'androidx.compose.runtime.Composer'
    if (fnName == SKIP_TO_GROUP_END && fnParentFqn == COMPOSER_FQN) {
      val printerCall = irPrintlnCall(irString("[INVALIDATION_TRACKER] <$currentFunctionName> invalidation skipped"))
      val block =
        IrBlockImpl(
          startOffset = UNDEFINED_OFFSET,
          endOffset = UNDEFINED_OFFSET,
          type = context.irBuiltIns.unitType,
          origin = InvalidationTrackableOrigin,
          statements = listOf(printerCall, call),
        )
      branch.result = block
      logger("[invalidation skipped] transformed: ${call.dump()} -> ${block.dump()}")
    }

    return super.visitElseBranch(branch)
  }

  private fun IrWhen.isComposerTrackBranch(): Boolean {
    val branch = branches.singleOrNull() ?: return false
    val `if` = (branch.condition as? IrCall ?: return false).symbol.owner
    val then = (branch.result as? IrCall ?: return false).symbol.owner

    // IS_TRACE_IN_PROGRESS, TRACE_EVENT_START, TRACE_EVENT_END are declared in 'androidx.compose.runtime.ComposerKt' (top-level)
    if (!`if`.isTopLevel || !then.isTopLevel) return false

    val thenName = then.name
    val thenParentFqn = then.unsafeGetTopLevelParentFqn()

    val validIf = `if`.name == IS_TRACE_IN_PROGRESS && `if`.unsafeGetTopLevelParentFqn() == COMPOSER_KT_FQN
    val validThen = (thenName == TRACE_EVENT_START || thenName == TRACE_EVENT_END) && thenParentFqn == COMPOSER_KT_FQN

    return validIf && validThen
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

  private val currentFunctionName: String
    get() {
      return (currentFunction ?: return "unknown").scope.scopeOwnerSymbol.cast<IrFunctionSymbol>().owner.name.asString()
    }
}

// TODO(multiplatform): this is jvm specific implementation
private fun IrFunction.unsafeGetTopLevelParentFqn(): FqName =
  parent.cast<IrClass>()
    .source.cast<FacadeClassSource>()
    .className.getFqNameForClassNameWithoutDollars() // JvmClassName -> FqName
