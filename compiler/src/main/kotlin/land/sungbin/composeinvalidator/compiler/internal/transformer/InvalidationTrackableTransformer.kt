/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

package land.sungbin.composeinvalidator.compiler.internal.transformer

import androidx.compose.compiler.plugins.kotlin.analysis.StabilityInferencer
import androidx.compose.compiler.plugins.kotlin.analysis.normalize
import land.sungbin.composeinvalidator.compiler.internal.AbstractInvalidationTrackingLower
import land.sungbin.composeinvalidator.compiler.internal.COMPOSABLE_FQN
import land.sungbin.composeinvalidator.compiler.internal.COMPOSER_FQN
import land.sungbin.composeinvalidator.compiler.internal.COMPOSER_KT_FQN
import land.sungbin.composeinvalidator.compiler.internal.IS_TRACE_IN_PROGRESS
import land.sungbin.composeinvalidator.compiler.internal.SKIP_TO_GROUP_END
import land.sungbin.composeinvalidator.compiler.internal.TRACE_EVENT_END
import land.sungbin.composeinvalidator.compiler.internal.TRACE_EVENT_START
import land.sungbin.composeinvalidator.compiler.internal.irString
import land.sungbin.composeinvalidator.compiler.internal.origin.InvalidationTrackableOrigin
import land.sungbin.composeinvalidator.compiler.internal.stability.toIrDeclarationStability
import land.sungbin.composeinvalidator.compiler.util.VerboseLogger
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrElseBranch
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrWhen
import org.jetbrains.kotlin.ir.expressions.addElement
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isTopLevel
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.toIrConst
import org.jetbrains.kotlin.load.kotlin.FacadeClassSource
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.utils.addToStdlib.cast

internal class InvalidationTrackableTransformer(
  private val context: IrPluginContext,
  private val logger: VerboseLogger,
  private val stabilityInferencer: StabilityInferencer,
) : AbstractInvalidationTrackingLower(context) {
  override fun visitFileNew(declaration: IrFile, data: IrInvalidationTrackTableClass): IrFile {
    val invalidationTrackTable = IrInvalidationTrackTableClass.create(context, declaration)
    declaration.addChild(invalidationTrackTable.get())
//    logger(declaration.dump())
//    logger(declaration.dumpKotlinLike())
    return super.visitFileNew(declaration, data)
  }

  override fun visitFunctionNew(declaration: IrFunction, data: IrInvalidationTrackTableClass): IrStatement {
    if (!declaration.hasAnnotation(COMPOSABLE_FQN)) return super.visitFunctionNew(declaration, data)
    declaration.body?.transformChildren(this, data)
    return super.visitFunctionNew(declaration, data)
  }

  // <BLOCK> {
  //   <WHEN> when { <CALL> isTraceInProgress() -> <CALL> traceEventStart() }
  //   composable()
  //   <WHEN> when { <CALL> isTraceInProgress() -> <CALL> traceEventEnd() }
  // }
  override fun visitBlock(expression: IrBlock, data: IrInvalidationTrackTableClass): IrExpression {
    // skip if it is already transformed
    if (expression.origin == InvalidationTrackableOrigin) return super.visitBlock(expression, data)

    // composable ir block is always has more than 3 statements
    if (expression.statements.size < 3) return super.visitBlock(expression, data)

    val firstStatement = expression.statements.first()
    val lastStatement = expression.statements.last()

    // composable ir block's first and last statement must be IrWhen
    if (firstStatement !is IrWhen || lastStatement !is IrWhen) return super.visitBlock(expression, data)

    val isComposerIrBlock = firstStatement.isComposerTrackBranch() && lastStatement.isComposerTrackBranch()
    if (isComposerIrBlock) {
      val println = irPrintln(context.irString("[INVALIDATION_TRACKER] <$currentFunctionName> invalidation processed"))
      val log = buildString {
        for ((index, statement) in expression.statements.withIndex()) {
          val dump = run {
            val dump = statement.dump().trimIndent()
            if (dump.length > 500) dump.substring(0, 500) + "..." else dump
          }
          if (index == 1) appendLine(">>>>>>>>>> ADD: ${println.dump()}")
          appendLine(dump)
          if (index == 3) {
            appendLine("...")
            break
          }
        }
      }

      // The last two arguments are generated by the Compose compiler ($composer, $changed)
      val validValueParamters = currentFunctionOrNull?.valueParameters?.dropLast(2).orEmpty()
      val paramInfos = IrVarargImpl(
        startOffset = UNDEFINED_OFFSET,
        endOffset = UNDEFINED_OFFSET,
        type = context.irBuiltIns.arrayClass.owner.defaultType,
        varargElementType = data.paramInfoSymbol!!.owner.defaultType,
      )
      for (param in validValueParamters) {
        val name = context.irString(param.name.asString())
        val value = irGetValue(param)
        val valueString =
          irToString(value).toIrConst(context.irBuiltIns.stringType).cast<IrConst<String>>()
        val hashCode = irHashCode(value)
        val stability = stabilityInferencer.stabilityOf(value).normalize()

        paramInfos.addElement(
          data.obtainParameterInfo(
            name = name,
            valueString = valueString,
            hashCode = hashCode,
            stability = stability.toIrDeclarationStability(context),
          ),
        )
      }
      val putParamsIfAbsent = data.irPutParamsIfAbsent(
        name = context.irString(currentFunctionName),
        paramInfos = paramInfos,
      )

      expression.statements.add(1, println)
      expression.statements.add(2, putParamsIfAbsent)
      expression.origin = InvalidationTrackableOrigin

      logger("[invalidation processed] transformed: $log")
      logger("[invalidation processed] dump: ${expression.dump()}")
      logger("[invalidation processed] dumpKotlinLike: ${expression.dumpKotlinLike()}")
    }

    return super.visitBlock(expression, data)
  }

  // <WHEN> when {
  //   ...
  //   else -> <CALL> $composer.skipToGroupEnd()
  // }
  override fun visitElseBranch(branch: IrElseBranch, data: IrInvalidationTrackTableClass): IrElseBranch {
    val call = branch.result as? IrCall ?: return super.visitElseBranch(branch, data)

    val fnName = call.symbol.owner.name
    val fnParentFqn = call.symbol.owner.parent.kotlinFqName

    // SKIP_TO_GROUP_END is declared in 'androidx.compose.runtime.Composer'
    if (fnName == SKIP_TO_GROUP_END && fnParentFqn == COMPOSER_FQN) {
      val println = irPrintln(context.irString("[INVALIDATION_TRACKER] <$currentFunctionName> invalidation skipped"))
      val block =
        IrBlockImpl(
          startOffset = UNDEFINED_OFFSET,
          endOffset = UNDEFINED_OFFSET,
          type = context.irBuiltIns.unitType,
          origin = InvalidationTrackableOrigin,
          statements = listOf(println, call),
        )
      branch.result = block
      logger("[invalidation skipped] transformed: ${call.dump()} -> ${block.dump()}")
    }

    return super.visitElseBranch(branch, data)
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
}

// TODO(multiplatform): this is jvm specific implementation
private fun IrFunction.unsafeGetTopLevelParentFqn(): FqName =
  parent.cast<IrClass>()
    .source.cast<FacadeClassSource>()
    .className.getFqNameForClassNameWithoutDollars() // JvmClassName -> FqName
