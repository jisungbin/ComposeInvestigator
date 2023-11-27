/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.internal.tracker

import land.sungbin.composeinvestigator.compiler.internal.COMPOSABLE_FQN
import land.sungbin.composeinvestigator.compiler.internal.COMPOSER_FQN
import land.sungbin.composeinvestigator.compiler.internal.COMPOSER_KT_FQN
import land.sungbin.composeinvestigator.compiler.internal.IS_TRACE_IN_PROGRESS
import land.sungbin.composeinvestigator.compiler.internal.SKIP_TO_GROUP_END
import land.sungbin.composeinvestigator.compiler.internal.TRACE_EVENT_END
import land.sungbin.composeinvestigator.compiler.internal.TRACE_EVENT_START
import land.sungbin.composeinvestigator.compiler.internal.origin.InvalidationTrackerOrigin
import land.sungbin.composeinvestigator.compiler.internal.tracker.table.InvalidationTrackTableCallTransformer
import land.sungbin.composeinvestigator.compiler.internal.tracker.table.IrInvalidationTrackTable
import land.sungbin.composeinvestigator.compiler.util.VerboseLogger
import land.sungbin.fastlist.fastLastOrNull
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.wasm.ir2wasm.getSourceLocation
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrSymbolOwner
import org.jetbrains.kotlin.ir.declarations.IrValueDeclaration
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrElseBranch
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrWhen
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.types.isNullableAny
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isTopLevel
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.setDeclarationsParent
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.load.kotlin.FacadeClassSource
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.utils.addToStdlib.cast
import org.jetbrains.kotlin.wasm.ir.source.location.SourceLocation

internal abstract class AbstractInvalidationTrackingLower(
  private val context: IrPluginContext,
  private val logger: VerboseLogger,
) : IrElementTransformerVoidWithContext() {
  private class IrSymbolOwnerWithData<D>(private val owner: IrSymbolOwner, val data: D) : IrSymbolOwner by owner

  private val hashCodeSymbol: IrSimpleFunctionSymbol =
    context
      .referenceFunctions(
        CallableId(
          packageName = FqName("kotlin"),
          callableName = Name.identifier("hashCode"),
        ),
      )
      .single { symbol ->
        val extensionReceiver = symbol.owner.extensionReceiverParameter

        val isValidExtensionReceiver = extensionReceiver != null && extensionReceiver.type.isNullableAny()
        val isValidReturnType = symbol.owner.returnType.isInt()

        isValidExtensionReceiver && isValidReturnType
      }

  private val toStringSymbol: IrSimpleFunctionSymbol =
    context
      .referenceFunctions(
        CallableId(
          packageName = FqName("kotlin"),
          callableName = Name.identifier("toString"),
        ),
      )
      // [KT-44684] Duplicate results from IrPluginContext.referenceFunctions for kotlin.toString()
      .first { symbol ->
        val extensionReceiver = symbol.owner.extensionReceiverParameter

        val isValidExtensionReceiver = extensionReceiver != null && extensionReceiver.type.isNullableAny()
        val isValidReturnType = symbol.owner.returnType.isString()

        isValidExtensionReceiver && isValidReturnType
      }

  private val unsafeCurrentFunction: IrSimpleFunction
    get() = allScopes
      .fastLastOrNull { scope -> scope.irElement is IrSimpleFunction }
      ?.irElement
      ?.cast()
      ?: error("Cannot find current function")

  protected fun getCurrentFunctionPackage() = unsafeCurrentFunction.kotlinFqName.asString()

  protected fun getCurrentFunctionNameIntercepttedAnonymous(userProvideName: String?): String {
    if (userProvideName != null) return userProvideName
    val currentFunctionName = unsafeCurrentFunction.name
    return if (currentFunctionName == SpecialNames.ANONYMOUS) {
      try {
        val parent = currentFunction!!.irElement.cast<IrSimpleFunction>().parent
        "${SpecialNames.ANONYMOUS_STRING} in ${parent.kotlinFqName.asString()}"
      } catch (_: Exception) {
        SpecialNames.ANONYMOUS_STRING
      }
    } else currentFunctionName.asString()
  }

  protected val currentInvalidationTrackTable: IrInvalidationTrackTable?
    get() =
      allScopes
        .lastOrNull { scope ->
          val element = scope.irElement
          element is IrSymbolOwnerWithData<*> && element.data is IrInvalidationTrackTable
        }
        ?.irElement
        ?.cast<IrSymbolOwnerWithData<IrInvalidationTrackTable>>()
        ?.data

  override fun visitFileNew(declaration: IrFile): IrFile {
    val trackTable = IrInvalidationTrackTable.create(context, declaration)
    declaration.declarations.add(0, trackTable.prop.also { it.setDeclarationsParent(declaration) })
    declaration.transformChildrenVoid(
      InvalidationTrackTableCallTransformer(
        context = context,
        table = trackTable,
        logger = logger,
      ),
    )
    return withinScope(IrSymbolOwnerWithData(declaration, trackTable)) {
      super.visitFileNew(declaration)
    }
  }

  override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
    if (!declaration.hasAnnotation(COMPOSABLE_FQN)) return super.visitSimpleFunction(declaration)
    withinScope(declaration) { declaration.body?.transformChildrenVoid() }
    return super.visitSimpleFunction(declaration)
  }

  // <BLOCK> {
  //   <WHEN> when { <CALL> isTraceInProgress() -> <CALL> traceEventStart() }
  //   composable()
  //   <WHEN> when { <CALL> isTraceInProgress() -> <CALL> traceEventEnd() }
  // }
  final override fun visitBlock(expression: IrBlock): IrExpression {
    // skip if it is already transformed
    if (expression.origin == InvalidationTrackerOrigin) return super.visitBlock(expression)

    // composable ir block is always has more than 3 statements
    if (expression.statements.size < 3) return super.visitBlock(expression)

    val firstStatement = expression.statements.first()
    val lastStatement = expression.statements.last()

    // composable ir block's first and last statement must be IrWhen
    if (firstStatement !is IrWhen || lastStatement !is IrWhen) return super.visitBlock(expression)

    val isComposableIrBlock = firstStatement.isComposableTraceBranch() && lastStatement.isComposableTraceBranch()
    if (!isComposableIrBlock) return super.visitBlock(expression)

    return super.visitBlock(visitComposableBlock(unsafeCurrentFunction, expression))
  }

  // <WHEN> when {
  //   ...
  //   else -> <CALL> $composer.skipToGroupEnd()
  // }
  final override fun visitElseBranch(branch: IrElseBranch): IrElseBranch {
    val call = branch.result as? IrCall ?: return super.visitElseBranch(branch)

    val callName = call.symbol.owner.name
    val callParentFqn = call.symbol.owner.parent.kotlinFqName

    // SKIP_TO_GROUP_END is declared in 'androidx.compose.runtime.Composer'
    if (callName != SKIP_TO_GROUP_END || callParentFqn != COMPOSER_FQN) return super.visitElseBranch(branch)

    branch.result = visitSkipToGroupEndCall(unsafeCurrentFunction, call)

    return super.visitElseBranch(branch)
  }

  protected fun IrFunction.getSafelyLocation(): SourceLocation.Location =
    getSourceLocation(currentFile.fileEntry).let { location ->
      if (location is SourceLocation.Location) location.copy(line = location.line + 1)
      else SourceLocation.Location(file = "<unknown>", line = -1, column = -1)
    }

  protected abstract fun visitComposableBlock(function: IrSimpleFunction, expression: IrBlock): IrBlock
  protected abstract fun visitSkipToGroupEndCall(function: IrSimpleFunction, expression: IrCall): IrBlock

  private fun IrWhen.isComposableTraceBranch(): Boolean {
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
      symbol = toStringSymbol,
    ).apply {
      extensionReceiver = value
    }

  protected fun irTmpVariableInCurrentFun(expression: IrExpression, nameHint: String? = null): IrVariable =
    currentFunction!!.scope.createTemporaryVariable(irExpression = expression, nameHint = nameHint)
}

// TODO(multiplatform): this is jvm specific implementation
private fun IrFunction.unsafeGetTopLevelParentFqn(): FqName =
  parent.cast<IrClass>()
    .source.cast<FacadeClassSource>()
    .className.getFqNameForClassNameWithoutDollars() // JvmClassName -> FqName
