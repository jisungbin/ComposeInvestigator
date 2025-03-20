// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.lower

import androidx.compose.compiler.plugins.kotlin.ComposeCallableIds
import androidx.compose.compiler.plugins.kotlin.ComposeClassIds
import androidx.compose.compiler.plugins.kotlin.hasComposableAnnotation
import land.sungbin.composeinvestigator.compiler.ComposeNames
import land.sungbin.composeinvestigator.compiler.InvestigatorClassIds
import land.sungbin.composeinvestigator.compiler.StandardCallableIds
import land.sungbin.composeinvestigator.compiler.StateObject
import land.sungbin.composeinvestigator.compiler.skipToGroupEnd
import land.sungbin.composeinvestigator.compiler.struct.IrComposableInformation
import land.sungbin.composeinvestigator.compiler.struct.IrInvalidationLogger
import land.sungbin.composeinvestigator.compiler.struct.IrRuntimeStability
import land.sungbin.composeinvestigator.compiler.struct.IrValueArgument
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrLocalDelegatedProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueDeclaration
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.declarations.impl.IrVariableImpl
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrSyntheticBody
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.fromSymbolOwner
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.IrVariableSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrVariableSymbolImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.types.isNullableAny
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isSubtypeOfClass
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.name.Name

/**
 * This is the parent transformer for all transformations/visits made by ComposeInvestigator.
 *
 * This parent class exists to reduce common boilerplate code, and currently serves the
 * following roles:
 *
 * - Define commonly used [IrSymbol]s
 * - Provide a sugar syntax for creating [IrConst]s
 * - Enable [includeFilePathInExceptionTrace] for visited files
 * - Call [firstTransformComposableBody] when visiting a Composable function with the
 * conditions for ComposeInvestigator to work
 * - Call [lastTransformSkipToGroupEndCall] when visiting the expression `composer.skipToGroupEnd()`.
 * - Call [firstTransformStateInitializer] when visiting a `State` or `StateObject` definition
 */
public open class ComposeInvestigatorBaseLower(
  protected val context: IrPluginContext,
  protected val messageCollector: MessageCollector, // TODO context.createDiagnosticReporter() (Blocked: "This API is not supported for K2")
) : IrElementTransformerVoidWithContext() {
  protected val irRuntimeStability: IrRuntimeStability by lazy { IrRuntimeStability(context) }
  protected val irComposableInformation: IrComposableInformation by lazy { IrComposableInformation(context) }
  protected val irInvalidationLogger: IrInvalidationLogger by lazy { IrInvalidationLogger(context) }
  protected val irValueArgument: IrValueArgument by lazy { IrValueArgument(context) }

  private val currentComposerSymbol by unsafeLazy { context.referenceProperties(ComposeCallableIds.currentComposer).single() }
  private val composerSymbol by unsafeLazy { context.referenceClass(ComposeClassIds.Composer)!! }
  private val composerCompoundKeyHashSymbol by unsafeLazy {
    composerSymbol.getPropertyGetter(ComposeNames.compoundKeyHash.asString())!!
  }

  private val stateSymbol by unsafeLazy { context.referenceClass(ComposeClassIds.State)!! }
  private val stateObjectSymbol by unsafeLazy { context.referenceClass(ComposeClassIds.StateObject)!! }

  protected val mutableListOfSymbol: IrSimpleFunctionSymbol by unsafeLazy {
    context
      .referenceFunctions(StandardCallableIds.mutableListOf)
      .first { it.owner.valueParameters.isEmpty() }
  }
  protected val mutableListAddSymbol: IrSimpleFunctionSymbol by unsafeLazy {
    context
      .referenceFunctions(StandardCallableIds.mutableListAdd)
      .first { it.owner.valueParameters.size == 1 }
  }

  private val hashCodeSymbol: IrSimpleFunctionSymbol by unsafeLazy {
    context
      .referenceFunctions(StandardCallableIds.hashCode)
      .first { symbol ->
        val isNullableAnyExtension = with(symbol.owner.extensionReceiverParameter) {
          this != null && type.isNullableAny()
        }
        val isIntReturn = symbol.owner.returnType.isInt()

        isNullableAnyExtension && isIntReturn
      }
  }

  final override fun visitFileNew(declaration: IrFile): IrFile =
    includeFilePathInExceptionTrace(declaration) {
      if (declaration.hasAnnotation(InvestigatorClassIds.NoInvestigation))
        return declaration

      super.visitFileNew(declaration)
    }

  // Visit composable function declarations
  override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
    if (
      declaration.hasAnnotation(InvestigatorClassIds.NoInvestigation) ||
      declaration.body == null ||
      declaration.body is IrSyntheticBody
    )
      return declaration

    // Since some of the elements inside the function may be composable, we continue inspection.
    if (!declaration.hasComposableAnnotation())
      return super.visitSimpleFunction(declaration)

    declaration.body = firstTransformComposableBody(declaration, declaration.body!!)

    return super.visitSimpleFunction(declaration)
  }

  // Visit composable function expressions
  override fun visitFunctionExpression(expression: IrFunctionExpression): IrExpression {
    fun IrStatement.hasComposableCall(): Boolean {
      var result = false

      acceptChildrenVoid(
        object : IrVisitorVoid() {
          override fun visitElement(element: IrElement) {
            if (!result) element.acceptChildrenVoid(this)
          }

          override fun visitCall(expression: IrCall) {
            if (expression.symbol.owner.hasComposableAnnotation())
              result = true
            else
              super.visitCall(expression)
          }
        },
      )

      return result
    }

    if (expression.function.body?.statements?.any { it.hasComposableCall() } == true)
      expression.function.body = firstTransformComposableBody(expression.function, expression.function.body!!)

    return super.visitFunctionExpression(expression)
  }

  // Visit composer.skipToGroupEnd() calls
  override fun visitCall(expression: IrCall): IrExpression =
    if (expression.symbol.owner.callableIdOrNull == ComposeCallableIds.skipToGroupEnd)
      lastTransformSkipToGroupEndCall(allScopes.lastComposable()!!, expression)
    else
      super.visitCall(expression)

  // val state = <ENTER HERE: remember { mutableStateOf(T) } >
  override fun visitVariable(declaration: IrVariable): IrStatement {
    if (declaration.origin == IrDeclarationOrigin.PROPERTY_DELEGATE)
      return super.visitVariable(declaration)

    run {
      if (declaration.type.isState() && !declaration.isVar && !declaration.isLateinit) {
        val initializer = declaration.initializer ?: return@run
        declaration.initializer = firstTransformStateInitializer(declaration.name, initializer, declaration.file)
      }
    }

    return super.visitVariable(declaration)
  }

  // var state by <ENTER HERE: remember { mutableStateOf(T) } >
  override fun visitLocalDelegatedProperty(declaration: IrLocalDelegatedProperty): IrStatement {
    run {
      val delegate = declaration.delegate
      if (delegate.type.isState() && !delegate.isVar && !delegate.isLateinit) {
        val initializer = delegate.initializer ?: return@run
        delegate.initializer = firstTransformStateInitializer(declaration.name, initializer, declaration.file)
      }
    }

    return super.visitLocalDelegatedProperty(declaration)
  }

  // (MUST) LoadingOrder.FIRST
  protected open fun firstTransformComposableBody(
    composable: IrSimpleFunction,
    body: IrBody,
  ): IrBody = body

  // (MUST) LoadingOrder.FIRST
  protected open fun firstTransformStateInitializer(
    name: Name,
    initializer: IrExpression,
    file: IrFile,
  ): IrExpression = initializer

  // (MUST) LoadingOrder.LAST
  protected open fun lastTransformSkipToGroupEndCall(
    composable: IrFunction,
    expression: IrCall,
  ): IrExpression = expression

  protected fun irVariable(
    name: Name,
    initializer: IrExpression,
    parent: IrDeclarationParent,
    origin: IrDeclarationOrigin = IrDeclarationOrigin.DEFINED,
    symbol: IrVariableSymbol = IrVariableSymbolImpl(),
    type: IrType = initializer.type,
    isVar: Boolean = false,
    isConst: Boolean = false,
    isLateinit: Boolean = false,
    startOffset: Int = UNDEFINED_OFFSET,
    endOffset: Int = UNDEFINED_OFFSET,
  ): IrVariable =
    IrVariableImpl(
      name = name,
      type = type,
      symbol = symbol,
      origin = origin,
      isVar = isVar,
      isConst = isConst,
      isLateinit = isLateinit,
      startOffset = startOffset,
      endOffset = endOffset,
    ).apply {
      this.initializer = initializer
      this.parent = parent
    }

  protected fun irCurrentComposer(): IrCall =
    IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = currentComposerSymbol.owner.getter!!.symbol,
    )

  protected fun irCompoundKeyHash(composer: IrExpression): IrCall =
    IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = composerCompoundKeyHashSymbol,
    ).apply {
      dispatchReceiver = composer
    }

  protected fun irString(
    value: String,
    startOffset: Int = UNDEFINED_OFFSET,
    endOffset: Int = UNDEFINED_OFFSET,
  ): IrConst = context.irString(value, startOffset, endOffset)

  protected fun irGetValue(
    value: IrValueDeclaration,
    type: IrType = value.symbol.owner.type,
  ): IrGetValue =
    IrGetValueImpl(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = value.symbol,
    ).apply {
      this.type = type
    }

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

  private fun IrType.isState(): Boolean =
    isSubtypeOfClass(stateSymbol) || isSubtypeOfClass(stateObjectSymbol)
}
