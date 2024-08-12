/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.lower

import androidx.compose.compiler.plugins.kotlin.analysis.StabilityInferencer
import androidx.compose.compiler.plugins.kotlin.hasComposableAnnotation
import androidx.compose.compiler.plugins.kotlin.lower.includeFileNameInExceptionTrace
import land.sungbin.composeinvestigator.compiler.COMPOSER_FQN
import land.sungbin.composeinvestigator.compiler.Composer_SKIPPING
import land.sungbin.composeinvestigator.compiler.Composer_SKIP_TO_GROUP_END
import land.sungbin.composeinvestigator.compiler.Composer_START_RESTART_GROUP
import land.sungbin.composeinvestigator.compiler.EMPTY_LIST_FQN
import land.sungbin.composeinvestigator.compiler.HASH_CODE_FQN
import land.sungbin.composeinvestigator.compiler.NO_INVESTIGATION_FQN
import land.sungbin.composeinvestigator.compiler.SCOPE_UPDATE_SCOPE_FQN
import land.sungbin.composeinvestigator.compiler.ScopeUpdateScope_UPDATE_SCOPE
import land.sungbin.composeinvestigator.compiler.VerboseMessageCollector
import land.sungbin.composeinvestigator.compiler.fromFqName
import land.sungbin.composeinvestigator.compiler.origin.ComposableInvalidationTracerOrigin
import land.sungbin.composeinvestigator.compiler.struct.IrAffectedComposable
import land.sungbin.composeinvestigator.compiler.struct.IrInvalidationTraceTable
import land.sungbin.fastlist.fastLastOrNull
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrSymbolOwner
import org.jetbrains.kotlin.ir.declarations.IrValueDeclaration
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrElseBranch
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrStatementContainer
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.types.isNullableAny
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.ir.util.setDeclarationsParent
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.utils.addToStdlib.cast
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

public abstract class AbstractComposableInvalidationTraceLower(
  private val context: IrPluginContext,
  private val logger: VerboseMessageCollector,
  private val stabilityInferencer: StabilityInferencer,
  private val affectedComposable: IrAffectedComposable,
) : IrElementTransformerVoidWithContext() {
  private class IrSymbolOwnerWithData<D>(private val owner: IrSymbolOwner, val data: D) : IrSymbolOwner by owner

  private val composerSymbol = context.referenceClass(ClassId.topLevel(COMPOSER_FQN))!!
  private val nullableComposerType = composerSymbol.defaultType.makeNullable()
  private val composerStartRestartGroupSymbol = composerSymbol.getSimpleFunction(Composer_START_RESTART_GROUP.asString())!!
  private val composerSkippingGetterSymbol = composerSymbol.getPropertyGetter(Composer_SKIPPING.asString())!!
  private val composerSkipToGroupEndSymbol = composerSymbol.getSimpleFunction(Composer_SKIP_TO_GROUP_END.asString())!!

  private val scopeUpdateScopeSymbol = context.referenceClass(ClassId.topLevel(SCOPE_UPDATE_SCOPE_FQN))!!
  private val scopeUpdateScopeUpdateScopeSymbol = scopeUpdateScopeSymbol.getSimpleFunction(ScopeUpdateScope_UPDATE_SCOPE.asString())!!
  private val scopeUpdateScopeUpdateScopeBlockType =
    context.irBuiltIns.functionN(2).typeWith(
      nullableComposerType,
      context.irBuiltIns.intType,
      context.irBuiltIns.unitType,
    )

  private val emptyListSymbol =
    context
      .referenceFunctions(CallableId.fromFqName(EMPTY_LIST_FQN))
      .single { symbol -> symbol.owner.valueParameters.isEmpty() && symbol.owner.typeParameters.size == 1 }
  private val stringEmptyListCall =
    IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = emptyListSymbol,
    ).apply {
      putTypeArgument(0, context.irBuiltIns.stringType)
    }

  private val hashCodeSymbol =
    context
      .referenceFunctions(CallableId.fromFqName(HASH_CODE_FQN))
      .single { symbol ->
        val isNullableAnyExtension = with(symbol.owner.extensionReceiverParameter) {
          this != null && type.isNullableAny()
        }
        val isIntReturn = symbol.owner.returnType.isInt()

        isNullableAnyExtension && isIntReturn
      }

  private fun lastReachedComposable(): IrSimpleFunction? =
    allScopes
      .fastLastOrNull { scope -> scope.irElement.safeAs<IrSimpleFunction>()?.hasComposableAnnotation() == true }
      ?.irElement?.safeAs<IrSimpleFunction>()

  protected val currentInvalidationTraceTable: IrInvalidationTraceTable?
    get() =
      allScopes
        .fastLastOrNull { scope ->
          val element = scope.irElement
          element is IrSymbolOwnerWithData<*> && element.data is IrInvalidationTraceTable
        }
        ?.irElement?.cast<IrSymbolOwnerWithData<IrInvalidationTraceTable>>()?.data

  final override fun visitFileNew(declaration: IrFile): IrFile =
    includeFileNameInExceptionTrace(declaration) {
      // If the file is @NoInvestigation, skip all processing.
      if (declaration.hasAnnotation(NO_INVESTIGATION_FQN)) return declaration

      val table = IrInvalidationTraceTable.create(context, declaration)
      val tableCallTransformer = InvalidationTraceTableIntrinsicTransformer(
        context = context,
        table = table,
        logger = logger,
        affectedComposable = affectedComposable,
      )
      declaration.declarations.add(0, table.prop.also { prop -> prop.setDeclarationsParent(declaration) })
      declaration.transformChildrenVoid(tableCallTransformer)

      withinScope(IrSymbolOwnerWithData(owner = declaration, data = table)) {
        super.visitFileNew(declaration)
      }
    }

  final override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
    // If the function itself is @NoInvestigation, all elements contained in this function
    // will be excluded from investigation.
    if (declaration.hasAnnotation(NO_INVESTIGATION_FQN)) return declaration

    // Since some of the elements inside the function may be composable, we continue inspection.
    if (!declaration.hasComposableAnnotation()) return super.visitSimpleFunction(declaration)

    withinScope(declaration) { declaration.body?.transformChildrenVoid() }
    return super.visitSimpleFunction(declaration)
  }

  // ScopeUpdateScope?.updateScope(block = local fun <anonymous>($composer: Composer?, $force: Int) {
  //   <ENTER HERE>
  //   return Composable($composer = $composer, $changed = updateChangedFlags(flags = $changed.or(other = 1)))
  // })
  final override fun visitCall(expression: IrCall): IrExpression {
    if (
      expression.dispatchReceiver?.type == scopeUpdateScopeSymbol.defaultType.makeNullable() &&
      expression.symbol.owner.kotlinFqName == scopeUpdateScopeUpdateScopeSymbol.owner.kotlinFqName &&
      expression.getValueArgument(0) is IrFunctionExpression
    ) {
      val block = expression.getValueArgument(0) as IrFunctionExpression
      check(block.origin == IrStatementOrigin.LAMBDA && block.type == scopeUpdateScopeUpdateScopeBlockType) {
        "The block of ScopeUpdateScope.updateScope must be a lambda with the following signature: (Composer?, Int) -> Unit" +
          "\nCurrent signature: ${block.render()}"
      }
      block.function.transformChildrenVoid(
        object : IrElementTransformerVoidWithContext() {
          override fun visitBlockBody(body: IrBlockBody): IrBody {
            // TODO should be 'lastOrNull { it is IrReturn }'?
            val returnCall = body.statements.singleOrNull()?.safeAs<IrReturn>() ?: return body
            val returnTarget = returnCall.value

            if (returnTarget !is IrCall || !returnTarget.symbol.owner.hasComposableAnnotation()) return body

            val transformed = transformUpdateScopeBlock(target = returnTarget.symbol.owner, initializer = returnCall)
            body.statements.clear()
            body.statements.addAll(transformed.statements)

            return body
          }
        },
      )
    }
    return super.visitCall(expression)
  }

  // TODO we can transform this on visitCall.
  // when {
  //   ...
  //   else -> $composer.skipToGroupEnd() <ENTER HERE>
  // }
  final override fun visitElseBranch(branch: IrElseBranch): IrElseBranch {
    val composable = lastReachedComposable() ?: return super.visitElseBranch(branch)
    val call = branch.result as? IrCall ?: return super.visitElseBranch(branch)

    if (call.symbol.owner.kotlinFqName != composerSkipToGroupEndSymbol.owner.kotlinFqName)
      return super.visitElseBranch(branch)

    val transformed = transformSkipToGroupEndCall(composable = composable, initializer = call)
    branch.result = IrBlockImpl(
      startOffset = transformed.startOffset,
      endOffset = transformed.endOffset,
      type = context.irBuiltIns.unitType,
      origin = ComposableInvalidationTracerOrigin,
      statements = transformed.statements,
    )

    return super.visitElseBranch(branch)
  }

  protected abstract fun transformUpdateScopeBlock(target: IrSimpleFunction, initializer: IrReturn): IrStatementContainer
  protected abstract fun transformSkipToGroupEndCall(composable: IrSimpleFunction, initializer: IrCall): IrStatementContainer

  protected fun irGetValue(value: IrValueDeclaration): IrGetValue =
    IrGetValueImpl(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = value.symbol,
    )

  protected fun irHashCode(value: IrExpression): IrCall =
    IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = hashCodeSymbol,
    ).apply {
      extensionReceiver = value
    }

  protected fun irToString(value: IrExpression): IrCall =
    IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = context.irBuiltIns.extensionToString,
    ).apply {
      extensionReceiver = value
    }

  protected fun irTmpVariableInCurrentFun(expression: IrExpression, nameHint: String? = null): IrVariable =
    currentFunction!!.scope.createTemporaryVariable(expression, nameHint = nameHint)
}
