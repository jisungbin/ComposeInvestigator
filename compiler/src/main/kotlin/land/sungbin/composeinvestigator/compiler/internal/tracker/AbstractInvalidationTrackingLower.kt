/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.internal.tracker

import androidx.compose.compiler.plugins.kotlin.hasComposableAnnotation
import land.sungbin.composeinvestigator.compiler.internal.ANIMATABLE_FQN
import land.sungbin.composeinvestigator.compiler.internal.COMPOSER_FQN
import land.sungbin.composeinvestigator.compiler.internal.Composer_SKIPPING
import land.sungbin.composeinvestigator.compiler.internal.Composer_SKIP_TO_GROUP_END
import land.sungbin.composeinvestigator.compiler.internal.HASH_CODE_FQN
import land.sungbin.composeinvestigator.compiler.internal.SCOPE_UPDATE_SCOPE_FQN
import land.sungbin.composeinvestigator.compiler.internal.STATE_FQN
import land.sungbin.composeinvestigator.compiler.internal.ScopeUpdateScope_UPDATE_SCOPE
import land.sungbin.composeinvestigator.compiler.internal.fromFqName
import land.sungbin.composeinvestigator.compiler.internal.tracker.table.InvalidationTrackTableIntrinsicTransformer
import land.sungbin.composeinvestigator.compiler.internal.tracker.table.IrInvalidationTrackTable
import land.sungbin.composeinvestigator.compiler.origin.InvalidationTrackerOrigin
import land.sungbin.composeinvestigator.compiler.util.VerboseLogger
import land.sungbin.fastlist.fastLastOrNull
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrLocalDelegatedProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrSymbolOwner
import org.jetbrains.kotlin.ir.declarations.IrValueDeclaration
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrElseBranch
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrStatementContainer
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrWhen
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.types.isNullableAny
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.setDeclarationsParent
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addToStdlib.cast
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

internal abstract class AbstractInvalidationTrackingLower(
  private val context: IrPluginContext,
  private val logger: VerboseLogger,
) : IrElementTransformerVoidWithContext() {
  private class IrSymbolOwnerWithData<D>(private val owner: IrSymbolOwner, val data: D) : IrSymbolOwner by owner

  private val composerSymbol = context.referenceClass(ClassId.topLevel(COMPOSER_FQN))!!
  private val nullableComposerType = composerSymbol.defaultType.makeNullable()
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

  private val stateSymbol = context.referenceClass(ClassId.topLevel(STATE_FQN))!!
  private val animatableSymbol = context.referenceClass(ClassId.topLevel(ANIMATABLE_FQN))

  private val hashCodeSymbol =
    context
      .referenceFunctions(CallableId.fromFqName(HASH_CODE_FQN))
      .single { symbol ->
        val extensionReceiver = symbol.owner.extensionReceiverParameter

        val isValidExtensionReceiver = extensionReceiver != null && extensionReceiver.type.isNullableAny()
        val isValidReturnType = symbol.owner.returnType.isInt()

        isValidExtensionReceiver && isValidReturnType
      }

  private fun lastReachedComposable(): IrSimpleFunction? =
    allScopes
      .fastLastOrNull { scope -> scope.irElement.safeAs<IrSimpleFunction>()?.hasComposableAnnotation() == true }
      ?.irElement?.safeAs<IrSimpleFunction>()

  protected val currentInvalidationTrackTable: IrInvalidationTrackTable?
    get() = allScopes
      .fastLastOrNull { scope ->
        val element = scope.irElement
        element is IrSymbolOwnerWithData<*> && element.data is IrInvalidationTrackTable
      }
      ?.irElement?.cast<IrSymbolOwnerWithData<IrInvalidationTrackTable>>()?.data

  final override fun visitFileNew(declaration: IrFile): IrFile {
    val table = IrInvalidationTrackTable.create(context, declaration)
    val tableCallTransformer = InvalidationTrackTableIntrinsicTransformer(context = context, table = table, logger = logger)
    declaration.declarations.add(0, table.prop.also { prop -> prop.setDeclarationsParent(declaration) })
    declaration.transformChildrenVoid(tableCallTransformer)
    return withinScope(IrSymbolOwnerWithData(owner = declaration, data = table)) {
      super.visitFileNew(declaration)
    }
  }

  final override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
    if (!declaration.hasComposableAnnotation()) return super.visitSimpleFunction(declaration)
    withinScope(declaration) { declaration.body?.transformChildrenVoid() }
    return super.visitSimpleFunction(declaration)
  }

  // val state = remember { mutableStateOf(T) }
  override fun visitVariable(declaration: IrVariable): IrStatement {
    val composable = lastReachedComposable() ?: return super.visitVariable(declaration)
    if (declaration.origin == IrDeclarationOrigin.PROPERTY_DELEGATE) return super.visitVariable(declaration)
    if (declaration.isValidStateDeclaration()) {
      logger("visitVariable: ${declaration.dump()}")
      logger("visitVariable: ${declaration.dumpKotlinLike()}")

      declaration.initializer = transformStateInitializer(
        composable = composable,
        stateName = declaration.name,
        initializer = declaration.initializer!!,
      )
    }
    return super.visitVariable(declaration)
  }

  // var state by remember { mutableStateOf(T) }
  override fun visitLocalDelegatedProperty(declaration: IrLocalDelegatedProperty): IrStatement {
    logger("visitLocalDelegatedProperty: ${declaration.dump()}")
    logger("visitLocalDelegatedProperty: ${declaration.dumpKotlinLike()}")

    val composable = lastReachedComposable() ?: return super.visitLocalDelegatedProperty(declaration)
    if (declaration.delegate.isValidStateDeclaration()) {
      declaration.delegate.initializer = transformStateInitializer(
        composable = composable,
        stateName = declaration.name,
        initializer = declaration.delegate.initializer!!,
      )
    }
    return super.visitLocalDelegatedProperty(declaration)
  }

  // when {
  //   when {
  //     EQEQ(arg0 = $dirty.and(other = 11), arg1 = 2).not() -> true
  //     else -> $composer.<get-skipping>().not()
  //   } -> {
  //     (composable content) <ENTER HERE>
  //   }
  //   else -> $composer.skipToGroupEnd()
  // }
  final override fun visitWhen(expression: IrWhen): IrExpression {
    val composable = lastReachedComposable() ?: return super.visitWhen(expression)
    if (expression.branches.size == 2) {
      val firstBranch = expression.branches.first()
      val firstCondition = firstBranch.condition
      val firstResult = firstBranch.result
      if (firstResult !is IrBlock) return super.visitWhen(expression)
      val assertFirstCondition = run {
        if (
          firstCondition is IrWhen &&
          firstCondition.type == context.irBuiltIns.booleanType &&
          firstCondition.origin == IrStatementOrigin.OROR &&
          firstCondition.branches.size == 2
        ) {
          val nestedBranch = firstCondition.branches

          val assertNestedFirstCondition = run {
            val nestedFirstCondition = nestedBranch.first().condition
            // assert 'EQEQ(...).not()'
            if (
              nestedFirstCondition is IrCall &&
              nestedFirstCondition.symbol.owner.kotlinFqName ==
              context.irBuiltIns.booleanNotSymbol.owner.kotlinFqName &&
              nestedFirstCondition.dispatchReceiver?.safeAs<IrCall>()?.symbol?.owner?.kotlinFqName ==
              context.irBuiltIns.eqeqSymbol.owner.kotlinFqName
            ) {
              val nestedFirstResult = nestedBranch.first().result
              // assert '-> true'
              nestedFirstResult is IrConst<*> && nestedFirstResult.value == true
            } else false
          }
          val aasertNestedSecondCondition = run {
            val nestedSecondCondition = nestedBranch.last().condition
            // assert 'else'
            if (nestedSecondCondition is IrConst<*> && nestedSecondCondition.value == true) {
              val nestedSecondResult = nestedBranch.last().result
              // assert '-> $composer.<get-skipping>().not()'
              nestedSecondResult is IrCall &&
                nestedSecondResult.symbol.owner.kotlinFqName ==
                context.irBuiltIns.booleanNotSymbol.owner.kotlinFqName &&
                nestedSecondResult.dispatchReceiver?.safeAs<IrCall>()?.symbol?.owner?.kotlinFqName ==
                composerSkippingGetterSymbol.owner.kotlinFqName &&
                nestedSecondResult.dispatchReceiver?.safeAs<IrCall>()
                  ?.dispatchReceiver?.safeAs<IrGetValue>()?.type == nullableComposerType
            } else false
          }

          assertNestedFirstCondition && aasertNestedSecondCondition
        } else false
      }
      if (assertFirstCondition) {
        expression.branches[0].result = transformComposableBody(function = composable, block = firstResult)
      }
    }
    return super.visitWhen(expression)
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
      if (block.origin == IrStatementOrigin.LAMBDA && block.type == scopeUpdateScopeUpdateScopeBlockType) {
        block.function.transformChildrenVoid(
          object : IrElementTransformerVoidWithContext() {
            override fun visitBlockBody(body: IrBlockBody): IrBody {
              val returnCall = body.statements.singleOrNull()?.safeAs<IrReturn>() ?: return super.visitBlockBody(body)
              val returnTarget = returnCall.value

              if (returnTarget !is IrCall || !returnTarget.symbol.owner.hasComposableAnnotation())
                return super.visitBlockBody(body)

              val transformed = transformUpdateScopeBlock(target = returnTarget.symbol.owner, initializer = returnCall)
              body.statements.clear()
              body.statements.addAll(transformed.statements)
              return super.visitBlockBody(body)
            }
          },
        )
      }
    }
    return super.visitCall(expression)
  }

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
      origin = InvalidationTrackerOrigin,
      statements = transformed.statements,
    )

    return super.visitElseBranch(branch)
  }

  private fun IrVariable.isValidStateDeclaration(): Boolean {
    val hasStateObject = run {
      val isState = type.classOrNull?.isSubtypeOfClass(stateSymbol.defaultType.classOrFail) ?: false
      val isAnimatable = animatableSymbol?.let { animatable ->
        type.classOrNull?.isSubtypeOfClass(animatable.defaultType.classOrFail)
      } ?: false

      isState || isAnimatable
    }
    val isTempVariable = origin == IrDeclarationOrigin.IR_TEMPORARY_VARIABLE
    val hasInitializer = initializer != null
    return hasStateObject && !isTempVariable && hasInitializer
  }

  protected abstract fun transformStateInitializer(composable: IrSimpleFunction, stateName: Name, initializer: IrExpression): IrExpression
  protected abstract fun transformComposableBody(function: IrSimpleFunction, block: IrBlock): IrBlock
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
    currentFunction!!.scope.createTemporaryVariable(irExpression = expression, nameHint = nameHint)
}
