/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.internal.tracker

import androidx.compose.compiler.plugins.kotlin.hasComposableAnnotation
import land.sungbin.composeinvestigator.compiler.internal.COMPOSABLE_FQN
import land.sungbin.composeinvestigator.compiler.internal.COMPOSER_FQN
import land.sungbin.composeinvestigator.compiler.internal.Composer_SKIP_TO_GROUP_END
import land.sungbin.composeinvestigator.compiler.internal.HASH_CODE_FQN
import land.sungbin.composeinvestigator.compiler.internal.SCOPE_UPDATE_SCOPE_FQN
import land.sungbin.composeinvestigator.compiler.internal.STATE_FQN
import land.sungbin.composeinvestigator.compiler.internal.ScopeUpdateScope_UPDATE_SCOPE
import land.sungbin.composeinvestigator.compiler.internal.UNKNOWN_STRING
import land.sungbin.composeinvestigator.compiler.internal.fromFqName
import land.sungbin.composeinvestigator.compiler.internal.origin.InvalidationTrackerOrigin
import land.sungbin.composeinvestigator.compiler.internal.tracker.table.InvalidationTrackTableIntrinsicTransformer
import land.sungbin.composeinvestigator.compiler.internal.tracker.table.IrInvalidationTrackTable
import land.sungbin.composeinvestigator.compiler.util.VerboseLogger
import land.sungbin.fastlist.fastLastOrNull
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.wasm.ir2wasm.getSourceLocation
import org.jetbrains.kotlin.ir.IrElementBase
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrLocalDelegatedProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrSymbolOwner
import org.jetbrains.kotlin.ir.declarations.IrValueDeclaration
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrBlock
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
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.setDeclarationsParent
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.utils.addToStdlib.cast
import org.jetbrains.kotlin.utils.addToStdlib.safeAs
import org.jetbrains.kotlin.wasm.ir.source.location.SourceLocation

internal abstract class AbstractInvalidationTrackingLower(
  private val context: IrPluginContext,
  private val logger: VerboseLogger,
) : IrElementTransformerVoidWithContext() {
  private class IrSymbolOwnerWithData<D>(private val owner: IrSymbolOwner, val data: D) : IrSymbolOwner by owner

  protected val composerSymbol = context.referenceClass(ClassId.topLevel(COMPOSER_FQN))!!
  private val skipToGroupEndSymbol = composerSymbol.getSimpleFunction(Composer_SKIP_TO_GROUP_END.asString())!!

  private val scopeUpdateScopeSymbol = context.referenceClass(ClassId.topLevel(SCOPE_UPDATE_SCOPE_FQN))!!
  private val scopeUpdateScopeUpdateScopeSymbol = scopeUpdateScopeSymbol.getSimpleFunction(ScopeUpdateScope_UPDATE_SCOPE.asString())!!
  private val scopeUpdateScopeUpdateScopeBlockType =
    context.irBuiltIns.functionN(2).typeWith(
      composerSymbol.defaultType.makeNullable(),
      context.irBuiltIns.intType,
      context.irBuiltIns.unitType,
    )

  private val stateSymbol = context.referenceClass(ClassId.topLevel(STATE_FQN))!!

  private val hashCodeSymbol =
    context
      .referenceFunctions(CallableId.fromFqName(HASH_CODE_FQN))
      .single { symbol ->
        val extensionReceiver = symbol.owner.extensionReceiverParameter

        val isValidExtensionReceiver = extensionReceiver != null && extensionReceiver.type.isNullableAny()
        val isValidReturnType = symbol.owner.returnType.isInt()

        isValidExtensionReceiver && isValidReturnType
      }

  protected fun lastReachedComposable(): IrSimpleFunction? =
    allScopes
      .fastLastOrNull { scope ->
        val element = scope.irElement
        if (element is IrFunction) element.hasComposableAnnotation() else false
      }
      ?.irElement?.safeAs<IrSimpleFunction>()

  private val unsafeCurrentFunction: IrSimpleFunction
    get() = allScopes
      .fastLastOrNull { scope -> scope.irElement is IrSimpleFunction }
      ?.irElement?.cast() ?: error("Cannot find current function")

  protected fun getCurrentFunctionPackage() = unsafeCurrentFunction.kotlinFqName.asString()

  protected fun getCurrentFunctionNameIntercepttedAnonymous(userProvideName: String?) =
    unsafeCurrentFunction.getFunctionNameIntercepttedAnonymous(userProvideName)

  protected fun IrFunction.getFunctionNameIntercepttedAnonymous(userProvideName: String?): String {
    if (userProvideName != null) return userProvideName
    val currentFunctionName = name
    return if (currentFunctionName == SpecialNames.ANONYMOUS) {
      try {
        val parent = cast<IrSimpleFunction>().parent
        "${SpecialNames.ANONYMOUS_STRING} in ${parent.kotlinFqName.asString()}"
      } catch (_: Exception) {
        SpecialNames.ANONYMOUS_STRING
      }
    } else currentFunctionName.asString()
  }

  protected val currentInvalidationTrackTable: IrInvalidationTrackTable?
    get() =
      allScopes
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
    if (!declaration.hasAnnotation(COMPOSABLE_FQN)) return super.visitSimpleFunction(declaration)
    withinScope(declaration) { declaration.body?.transformChildrenVoid() }
    return super.visitSimpleFunction(declaration)
  }

  // State properties transformer
  // TODO: Supports NonComposable state
  final override fun visitBlock(expression: IrBlock): IrExpression {
    fun IrVariable.isValidStateDeclaration(): Boolean {
      val isState = type.classOrNull?.isSubtypeOfClass(stateSymbol.starProjectedType.classOrFail) ?: false
      val isTempVariable = origin == IrDeclarationOrigin.IR_TEMPORARY_VARIABLE
      val hasInitializer = initializer != null
      return isState && !isTempVariable && hasInitializer
    }

    expression.transformChildrenVoid(
      object : IrElementTransformerVoidWithContext() {
        // val state = remember { mutableStateOf(T) }
        override fun visitVariable(declaration: IrVariable): IrStatement {
          val composable = lastReachedComposable() ?: return super.visitVariable(declaration)
          if (declaration.origin == IrDeclarationOrigin.PROPERTY_DELEGATE) return super.visitVariable(declaration)
          if (declaration.isValidStateDeclaration()) {
            // TODO: Set origin to InvalidationTrackerOrigin
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
          val composable = lastReachedComposable() ?: return super.visitLocalDelegatedProperty(declaration)
          if (declaration.delegate.isValidStateDeclaration()) {
            // TODO: Set origin to InvalidationTrackerOrigin
            declaration.delegate.initializer = transformStateInitializer(
              composable = composable,
              stateName = declaration.name,
              initializer = declaration.delegate.initializer!!,
            )
          }
          return super.visitLocalDelegatedProperty(declaration)
        }
      },
    )

    return super.visitBlock(expression)
  }

  // when {
  //   when {
  //     EQEQ(arg0 = $dirty.and(other = 11), arg1 = 2).not() -> true
  //     else -> $composer.<get-skipping>().not()
  //   } -> { // BLOCK
  //     <ENTER HERE> (composable content)
  //   }
  //   else -> $composer.skipToGroupEnd()
  // }
  final override fun visitWhen(expression: IrWhen): IrExpression {
    if (expression.branches.size == 2) {
      val firstBranch = expression.branches.first()
      if (
        firstBranch.condition.safeAs<IrCall>()?.symbol?.owner?.kotlinFqName ==
        context.irBuiltIns.eqeqSymbol.owner.kotlinFqName &&
        firstBranch.result.safeAt<>
      ) {

      }
    }

    return super.visitWhen(expression)
  }

  // tmp0_safe_receiver.updateScope(block = local fun <anonymous>($composer: Composer?, $force: Int) {
  //   <ENTER HERE>
  //   return Composable($composer = $composer, $changed = updateChangedFlags(flags = $changed.or(other = 1)))
  // })
  final override fun visitCall(expression: IrCall): IrExpression {
    if (
      expression.dispatchReceiver?.type == scopeUpdateScopeSymbol.defaultType.makeNullable() &&
      expression.symbol == scopeUpdateScopeUpdateScopeSymbol &&
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

              logger("visitCall blockBody: ${body.dumpKotlinLike()}")

              val transformed = transformUpdateScopeBlock(initializer = returnCall)
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

  // <WHEN> when {
  //   ...
  //   else -> <CALL> $composer.skipToGroupEnd() <ENTER HERE>
  // }
  final override fun visitElseBranch(branch: IrElseBranch): IrElseBranch {
    val call = branch.result as? IrCall ?: return super.visitElseBranch(branch)

    if (call.symbol.owner.kotlinFqName != skipToGroupEndSymbol.owner.kotlinFqName)
      return super.visitElseBranch(branch)

    val transformed = transformSkipToGroupEndCall(function = unsafeCurrentFunction, initializer = call)
    branch.result = IrBlockImpl(
      startOffset = transformed.startOffset,
      endOffset = transformed.endOffset,
      type = context.irBuiltIns.unitType,
      origin = InvalidationTrackerOrigin,
      statements = transformed.statements,
    )

    return super.visitElseBranch(branch)
  }

  protected fun IrFunction.getSafelyLocation(): SourceLocation.Location =
    getSourceLocation(currentFile.fileEntry).let { location ->
      if (location is SourceLocation.Location) location.copy(line = location.line + 1) // Humans read from 1.
      else SourceLocation.Location(file = SpecialNames.UNKNOWN_STRING, line = UNDEFINED_OFFSET, column = UNDEFINED_OFFSET)
    }

  protected abstract fun transformStateInitializer(composable: IrSimpleFunction, stateName: Name, initializer: IrExpression): IrExpression
  protected abstract fun transformComposableBlock(function: IrSimpleFunction, block: IrBlock): IrBlock
  protected abstract fun transformUpdateScopeBlock(initializer: IrReturn): IrStatementContainer
  protected abstract fun transformSkipToGroupEndCall(function: IrSimpleFunction, initializer: IrCall): IrStatementContainer

  @Suppress("FunctionName")
  protected fun IrStatementContainerImpl(
    statements: List<IrStatement>,
    startOffset: Int = UNDEFINED_OFFSET,
    endOffset: Int = UNDEFINED_OFFSET,
  ): IrStatementContainer = object : IrStatementContainer, IrElementBase() {
    override val startOffset = startOffset
    override val endOffset = endOffset
    override val statements = statements.toMutableList()
    override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D) = visitor.visitElement(this, data)
  }

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
