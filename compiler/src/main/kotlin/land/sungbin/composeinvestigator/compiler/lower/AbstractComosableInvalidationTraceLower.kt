/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.lower

import androidx.compose.compiler.plugins.kotlin.analysis.StabilityInferencer
import androidx.compose.compiler.plugins.kotlin.analysis.knownUnstable
import androidx.compose.compiler.plugins.kotlin.hasComposableAnnotation
import java.util.concurrent.atomic.AtomicReference
import land.sungbin.composeinvestigator.compiler.ANIMATABLE_FQN
import land.sungbin.composeinvestigator.compiler.COMPOSER_FQN
import land.sungbin.composeinvestigator.compiler.Composer_SKIPPING
import land.sungbin.composeinvestigator.compiler.Composer_SKIP_TO_GROUP_END
import land.sungbin.composeinvestigator.compiler.Composer_START_RESTART_GROUP
import land.sungbin.composeinvestigator.compiler.EMPTY_LIST_FQN
import land.sungbin.composeinvestigator.compiler.HASH_CODE_FQN
import land.sungbin.composeinvestigator.compiler.NO_INVESTIGATION_FQN
import land.sungbin.composeinvestigator.compiler.SCOPE_UPDATE_SCOPE_FQN
import land.sungbin.composeinvestigator.compiler.STACK_FQN
import land.sungbin.composeinvestigator.compiler.STATE_FQN
import land.sungbin.composeinvestigator.compiler.ScopeUpdateScope_UPDATE_SCOPE
import land.sungbin.composeinvestigator.compiler.VerboseLogger
import land.sungbin.composeinvestigator.compiler.fromFqName
import land.sungbin.composeinvestigator.compiler.origin.ComposableCallstackTracerSyntheticOrigin
import land.sungbin.composeinvestigator.compiler.origin.ComposableInvalidationTracerOrigin
import land.sungbin.composeinvestigator.compiler.struct.IrAffectedComposable
import land.sungbin.composeinvestigator.compiler.struct.IrComposableCallstackTracer
import land.sungbin.composeinvestigator.compiler.struct.IrInvalidationTraceTable
import land.sungbin.fastlist.fastAny
import land.sungbin.fastlist.fastFilterNot
import land.sungbin.fastlist.fastLastOrNull
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrLocalDelegatedProperty
import org.jetbrains.kotlin.ir.declarations.IrProperty
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
import org.jetbrains.kotlin.ir.expressions.IrSetValue
import org.jetbrains.kotlin.ir.expressions.IrStatementContainer
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrWhen
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.types.isNullableAny
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.findDeclaration
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.setDeclarationsParent
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addToStdlib.cast
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

public abstract class AbstractComosableInvalidationTraceLower(
  private val context: IrPluginContext,
  private val logger: VerboseLogger,
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

  private val stateSymbol = context.referenceClass(ClassId.topLevel(STATE_FQN))!!
  private val animatableSymbol = context.referenceClass(ClassId.topLevel(ANIMATABLE_FQN))

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
        val extensionReceiver = symbol.owner.extensionReceiverParameter

        val isValidExtensionReceiver = extensionReceiver != null && extensionReceiver.type.isNullableAny()
        val isValidReturnType = symbol.owner.returnType.isInt()

        isValidExtensionReceiver && isValidReturnType
      }

  private fun lastReachedComposable(): IrSimpleFunction? =
    allScopes
      .fastLastOrNull { scope -> scope.irElement.safeAs<IrSimpleFunction>()?.hasComposableAnnotation() == true }
      ?.irElement?.safeAs<IrSimpleFunction>()

  protected val currentInvalidationTraceTable: IrInvalidationTraceTable?
    get() = allScopes
      .fastLastOrNull { scope ->
        val element = scope.irElement
        element is IrSymbolOwnerWithData<*> && element.data is IrInvalidationTraceTable
      }
      ?.irElement?.cast<IrSymbolOwnerWithData<IrInvalidationTraceTable>>()?.data

  private val currentCallstackCallReference: AtomicReference<IrCall?> = AtomicReference()

  final override fun visitFileNew(declaration: IrFile): IrFile {
    if (currentCallstackCallReference.get() == null) {
      val callstackProp = declaration.findDeclaration<IrProperty> { prop ->
        prop.origin == ComposableCallstackTracerSyntheticOrigin && prop.backingField?.type?.classFqName == STACK_FQN
      }
      callstackProp?.let {
        val tracer = IrComposableCallstackTracer.from(context, callstackProp)
        check(currentCallstackCallReference.compareAndSet(null, tracer.irCopy())) {
          "The callstack tracer was already assigned, please report it as a project bug."
        }
      }
    }

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

    return withinScope(IrSymbolOwnerWithData(owner = declaration, data = table)) {
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

  // val state = remember { mutableStateOf(T) }
  final override fun visitVariable(declaration: IrVariable): IrStatement {
    if (declaration.origin == IrDeclarationOrigin.PROPERTY_DELEGATE || declaration.hasAnnotation(NO_INVESTIGATION_FQN))
      return super.visitVariable(declaration)
    val composable = lastReachedComposable() ?: return super.visitVariable(declaration)

    if (declaration.isValidStateDeclaration()) {
      declaration.initializer = transformStateInitializer(
        composable = composable,
        stateName = declaration.name,
        initializer = declaration.initializer!!,
      )
    }
    return super.visitVariable(declaration)
  }

  // var state by remember { mutableStateOf(T) }
  final override fun visitLocalDelegatedProperty(declaration: IrLocalDelegatedProperty): IrStatement {
    if (declaration.hasAnnotation(NO_INVESTIGATION_FQN)) return super.visitLocalDelegatedProperty(declaration)
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

  // **This logic is only executed when the composable owns an unstable parameter.**
  // @Composable fun Function($composer: Composer?, $changed: Int) {
  //   $composer = $composer.startRestartGroup()
  //   (composable content) <ENTER HERE>
  //   $composer.endRestartGroup()
  // }
  override fun visitBlockBody(body: IrBlockBody): IrBody {
    val composable = lastReachedComposable() ?: return super.visitBlockBody(body)

    val firstStatement = body.statements.firstOrNull()
    if (firstStatement is IrSetValue && firstStatement.value is IrCall) {
      val firstCall = firstStatement.value as IrCall
      if (firstCall.symbol.owner.kotlinFqName != composerStartRestartGroupSymbol.owner.kotlinFqName)
        return super.visitBlockBody(body)
    } else return super.visitBlockBody(body)

    // Synthetic arguments are not handled.
    val validParameters = composable.valueParameters.fastFilterNot { parameter ->
      parameter.name.asString().startsWith('$')
    }
    val hasUnstableParameter = validParameters.fastAny { parameter ->
      stabilityInferencer.stabilityOf(parameter.type).knownUnstable()
    }

    if (!hasUnstableParameter) return super.visitBlockBody(body)

    val transformed = transformComposableBody(composable = composable, body = body) as IrBlockBody
    return super.visitBlockBody(transformed)
  }

  // **This logic is only executed when all parameters in the composable are stable.**
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
          val assertNestedSecondCondition = run {
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

          assertNestedFirstCondition && assertNestedSecondCondition
        } else false
      }
      if (assertFirstCondition) {
        val transformed = transformComposableBody(composable = composable, body = firstResult) as IrBlock
        expression.branches[0].result = transformed
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
      origin = ComposableInvalidationTracerOrigin,
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
  protected abstract fun transformComposableBody(composable: IrSimpleFunction, body: IrStatementContainer): IrStatementContainer
  protected abstract fun transformUpdateScopeBlock(target: IrSimpleFunction, initializer: IrReturn): IrStatementContainer
  protected abstract fun transformSkipToGroupEndCall(composable: IrSimpleFunction, initializer: IrCall): IrStatementContainer

  protected fun currentCallstack(): IrCall = currentCallstackCallReference.get() ?: stringEmptyListCall

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
