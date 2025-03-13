// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.lower

import androidx.compose.compiler.plugins.kotlin.ComposeClassIds
import androidx.compose.compiler.plugins.kotlin.ComposeFqNames
import androidx.compose.compiler.plugins.kotlin.analysis.Stability
import androidx.compose.compiler.plugins.kotlin.hasComposableAnnotation
import land.sungbin.composeinvestigator.compiler.ComposeNames
import land.sungbin.composeinvestigator.compiler.HASH_CODE_FQN
import land.sungbin.composeinvestigator.compiler.MUTABLE_LIST_ADD_FQN
import land.sungbin.composeinvestigator.compiler.MUTABLE_LIST_OF_FQN
import land.sungbin.composeinvestigator.compiler.NO_INVESTIGATION_FQN
import land.sungbin.composeinvestigator.compiler.STABILITY_FQN
import land.sungbin.composeinvestigator.compiler.Stability_CERTAIN
import land.sungbin.composeinvestigator.compiler.Stability_COMBINED
import land.sungbin.composeinvestigator.compiler.Stability_PARAMETER
import land.sungbin.composeinvestigator.compiler.Stability_RUNTIME
import land.sungbin.composeinvestigator.compiler.Stability_UNKNOWN
import land.sungbin.composeinvestigator.compiler.fromFqName
import land.sungbin.composeinvestigator.compiler.struct.IrComposeInvestigator
import land.sungbin.composeinvestigator.compiler.struct.IrInvalidationLogger
import land.sungbin.composeinvestigator.compiler.struct.IrValueArgument
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrLocalDelegatedProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueDeclaration
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrSyntheticBody
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.expressions.impl.fromSymbolOwner
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.types.isNullable
import org.jetbrains.kotlin.ir.types.isNullableAny
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.types.typeWithArguments
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.Variance

/**
 * This is the parent transformer for all transformations/visits made by
 * ComposeInvestigator.
 *
 * This parent class exists to reduce common boilerplate code, and currently
 * serves the following roles:
 *
 * 1. Define commonly used [IrSymbol]s
 * 2. Provide a function to convert [Compose Stability][Stability] to ComposeInvestigator
 * Stability
 * 3. Provide a sugar syntax for creating [IrConst]s
 * 4. Enable [includeFilePathInExceptionTrace] for visited files
 * 5. Call [firstTransformComposableBody] when visiting a Composable function with
 * the conditions for ComposeInvestigator to work
 * 6. Call [lastTransformSkipToGroupEndCall] when visiting the expression
 * `composer.skipToGroupEnd()`.
 * 7. Call [firstTransformStateInitializer] when visiting a `State` or `StateObject`
 * definition
 */
public open class ComposeInvestigatorBaseLower(
  protected val context: IrPluginContext,
  protected val messageCollector: MessageCollector, // TODO context.createDiagnosticReporter() (Blocked: "This API is not supported for K2")
) : IrElementTransformerVoidWithContext() {
  private var ownStabilitySymbol: IrClassSymbol? = null
  private var ownStabilityCertainSymbol: IrClassSymbol? = null
  private var ownStabilityRuntimeSymbol: IrClassSymbol? = null
  private var ownStabilityUnknownSymbol: IrClassSymbol? = null
  private var ownStabilityParameterSymbol: IrClassSymbol? = null
  private var ownStabilityCombinedSymbol: IrClassSymbol? = null

  private val composerSymbol = context.referenceClass(ClassId.topLevel(ComposeFqNames.Composer))!!
  private val composerSkipToGroupEndSymbol = composerSymbol.getSimpleFunction(ComposeNames.skipToGroupEnd.asString())!!
  protected val composerCompoundKeyHashSymbol: IrSimpleFunctionSymbol = composerSymbol.getPropertyGetter(ComposeNames.compoundKeyHash.asString())!!

  private val stateSymbol = context.referenceClass(ComposeClassIds.State)!!
  private val stateObjectSymbol = context.referenceClass(ClassId.topLevel(ComposeFqNames.StateObject))!!

  protected val mutableListOfSymbol: IrSimpleFunctionSymbol by unsafeLazy {
    context
      .referenceFunctions(CallableId.fromFqName(MUTABLE_LIST_OF_FQN))
      .first { fn -> fn.owner.valueParameters.isEmpty() }
  }

  protected val mutableListAddSymbol: IrSimpleFunctionSymbol by unsafeLazy {
    context
      .referenceFunctions(CallableId.fromFqName(MUTABLE_LIST_ADD_FQN))
      .first { fn -> fn.owner.valueParameters.size == 1 }
  }

  private val hashCodeSymbol: IrSimpleFunctionSymbol by unsafeLazy {
    context
      .referenceFunctions(CallableId.fromFqName(HASH_CODE_FQN))
      .first { symbol ->
        val isNullableAnyExtension = with(symbol.owner.extensionReceiverParameter) {
          this != null && type.isNullableAny()
        }
        val isIntReturn = symbol.owner.returnType.isInt()

        isNullableAnyExtension && isIntReturn
      }
  }

  protected val invalidationLogger: IrInvalidationLogger by unsafeLazy { IrInvalidationLogger(context) }
  protected val valueArgument: IrValueArgument by unsafeLazy { IrValueArgument(context) }

  final override fun visitFileNew(declaration: IrFile): IrFile =
    includeFilePathInExceptionTrace(declaration) {
      if (declaration.hasAnnotation(NO_INVESTIGATION_FQN)) return declaration
      super.visitFileNew(declaration)
    }

  // if (%dirty and 0b0011 != 0b0010 || !%composer.skipping) {
  //   <ENTER HERE>
  //   magic()
  // } else {
  //   $composer.skipToGroupEnd()
  // }
  final override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
    if (
      declaration.hasAnnotation(NO_INVESTIGATION_FQN) ||
      declaration.body == null ||
      declaration.body is IrSyntheticBody
    )
      return declaration

    // Since some of the elements inside the function may be composable, we continue inspection.
    if (!declaration.hasComposableAnnotation()) return super.visitSimpleFunction(declaration)

    declaration.body = firstTransformComposableBody(
      composable = declaration,
      body = declaration.body!!,
      table = tables[declaration.file],
    )

    return super.visitSimpleFunction(declaration)
  }

  // if (%dirty and 0b0011 != 0b0010 || !%composer.skipping) {
  //   magic()
  // } else {
  //   <ENTER HERE>
  //   $composer.skipToGroupEnd()
  // }
  final override fun visitCall(expression: IrCall): IrExpression =
    if (expression.symbol.owner.kotlinFqName == composerSkipToGroupEndSymbol.owner.kotlinFqName)
      lastTransformSkipToGroupEndCall(
        composable = allScopes.lastComposable()!!,
        expression = expression,
        table = tables[currentFile],
      )
    else
      super.visitCall(expression)

  // val state = <ENTER HERE: remember { mutableStateOf(T) } >
  final override fun visitVariable(declaration: IrVariable): IrStatement {
    if (declaration.origin == IrDeclarationOrigin.PROPERTY_DELEGATE) return super.visitVariable(declaration)
    if (declaration.isStateDeclaration()) {
      declaration.initializer = firstTransformStateInitializer(
        name = declaration.name,
        initializer = declaration.initializer!!,
        table = tables[declaration.file],
      )
    }
    return super.visitVariable(declaration)
  }

  // var state by <ENTER HERE: remember { mutableStateOf(T) } >
  final override fun visitLocalDelegatedProperty(declaration: IrLocalDelegatedProperty): IrStatement {
    if (declaration.delegate.isStateDeclaration()) {
      declaration.delegate.initializer = firstTransformStateInitializer(
        name = declaration.name,
        initializer = declaration.delegate.initializer!!,
        table = tables[declaration.file],
      )
    }
    return super.visitLocalDelegatedProperty(declaration)
  }

  // (MUST) LoadingOrder.FIRST
  protected open fun firstTransformComposableBody(
    composable: IrSimpleFunction,
    body: IrBody,
    table: IrComposeInvestigator,
  ): IrBody = body

  // (MUST) LoadingOrder.FIRST
  protected open fun firstTransformStateInitializer(
    name: Name,
    initializer: IrExpression,
    table: IrComposeInvestigator,
  ): IrExpression = initializer

  // (MUST) LoadingOrder.LAST
  protected open fun lastTransformSkipToGroupEndCall(
    composable: IrSimpleFunction,
    expression: IrCall,
    table: IrComposeInvestigator,
  ): IrExpression = expression

  private fun IrVariable.isStateDeclaration(): Boolean {
    val isState = type.classOrNull?.let { clazz ->
      clazz.isSubtypeOfClass(stateSymbol.defaultType.classOrNull ?: return false) == true ||
        clazz.isSubtypeOfClass(stateObjectSymbol.defaultType.classOrNull ?: return false) == true
    }
    val isTempVariable = origin == IrDeclarationOrigin.IR_TEMPORARY_VARIABLE
    val hasInitializer = initializer != null
    return !type.isNullable() && isState == true && !isTempVariable && hasInitializer
  }

  protected fun Stability.asOwnStability(): IrConstructorCall {
    if (ownStabilitySymbol == null) ownStabilitySymbol = context.referenceClass(ClassId.topLevel(STABILITY_FQN))!!

    return when (this) {
      is Stability.Certain -> irOwnStabilityCertain(irBoolean(stable))
      is Stability.Runtime -> irOwnStabilityRuntime(irString(declaration.name.asString()))
      is Stability.Unknown -> irOwnStabilityUnknown(irString(declaration.name.asString()))
      is Stability.Parameter -> irOwnStabilityParameter(irString(parameter.name.asString()))
      is Stability.Combined -> irOwnStabilityCombined(elements.map { stability -> stability.asOwnStability() })
    }
  }

  protected fun irString(
    value: String,
    startOffset: Int = UNDEFINED_OFFSET,
    endOffset: Int = UNDEFINED_OFFSET,
  ): IrConst = context.irString(value, startOffset, endOffset)

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

  private fun irBoolean(
    value: Boolean,
    startOffset: Int = UNDEFINED_OFFSET,
    endOffset: Int = UNDEFINED_OFFSET,
  ): IrConst = IrConstImpl.boolean(
    startOffset = startOffset,
    endOffset = endOffset,
    type = context.irBuiltIns.booleanType,
    value = value,
  )

  private fun irOwnStabilityCertain(stable: IrConst): IrConstructorCall {
    val symbol = ownStabilityCertainSymbol ?: (
      ownStabilitySymbol!!.owner.sealedSubclasses
        .single { clz -> clz.owner.name == Stability_CERTAIN }
        .also { symbol -> ownStabilityCertainSymbol = symbol }
      )

    return IrConstructorCallImpl.fromSymbolOwner(
      type = symbol.defaultType,
      constructorSymbol = symbol.constructors.single(),
    ).apply {
      putValueArgument(0, stable)
    }
  }

  private fun irOwnStabilityRuntime(declarationName: IrConst): IrConstructorCall {
    val symbol = ownStabilityRuntimeSymbol ?: (
      ownStabilitySymbol!!.owner.sealedSubclasses
        .single { clz -> clz.owner.name == Stability_RUNTIME }
        .also { symbol -> ownStabilityRuntimeSymbol = symbol }
      )

    return IrConstructorCallImpl.fromSymbolOwner(
      type = symbol.defaultType,
      constructorSymbol = symbol.constructors.single(),
    ).apply {
      putValueArgument(0, declarationName)
    }
  }

  private fun irOwnStabilityUnknown(declarationName: IrConst): IrConstructorCall {
    val symbol = ownStabilityUnknownSymbol ?: (
      ownStabilitySymbol!!.owner.sealedSubclasses
        .single { clz -> clz.owner.name == Stability_UNKNOWN }
        .also { symbol -> ownStabilityUnknownSymbol = symbol }
      )

    return IrConstructorCallImpl.fromSymbolOwner(
      type = symbol.defaultType,
      constructorSymbol = symbol.constructors.single(),
    ).apply {
      putValueArgument(0, declarationName)
    }
  }

  private fun irOwnStabilityParameter(parameterName: IrConst): IrConstructorCall {
    val symbol = ownStabilityParameterSymbol ?: (
      ownStabilitySymbol!!.owner.sealedSubclasses
        .single { clz -> clz.owner.name == Stability_PARAMETER }
        .also { symbol -> ownStabilityParameterSymbol = symbol }
      )

    return IrConstructorCallImpl.fromSymbolOwner(
      type = symbol.defaultType,
      constructorSymbol = symbol.constructors.single(),
    ).apply {
      putValueArgument(0, parameterName)
    }
  }

  private fun irOwnStabilityCombined(elements: List<IrConstructorCall>): IrConstructorCall {
    val symbol = ownStabilityCombinedSymbol ?: (
      ownStabilitySymbol!!.owner.sealedSubclasses
        .single { clz -> clz.owner.name == Stability_COMBINED }
        .also { symbol -> ownStabilityCombinedSymbol = symbol }
      )

    return IrConstructorCallImpl.fromSymbolOwner(
      type = symbol.defaultType,
      constructorSymbol = symbol.constructors.single(),
    ).apply {
      val varargElementType = ownStabilitySymbol!!.defaultType
      val genericTypeProjection = makeTypeProjection(type = varargElementType, variance = Variance.OUT_VARIANCE)
      val genericType = context.irBuiltIns.arrayClass.typeWithArguments(listOf(genericTypeProjection))
      val vararg = IrVarargImpl(
        startOffset = UNDEFINED_OFFSET,
        endOffset = UNDEFINED_OFFSET,
        type = genericType,
        varargElementType = varargElementType,
        elements = elements,
      )

      putValueArgument(0, vararg)
    }
  }
}
