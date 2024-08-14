/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.lower

import androidx.compose.compiler.plugins.kotlin.analysis.Stability
import androidx.compose.compiler.plugins.kotlin.hasComposableAnnotation
import androidx.compose.compiler.plugins.kotlin.lower.includeFileNameInExceptionTrace
import land.sungbin.composeinvestigator.compiler.COMPOSER_FQN
import land.sungbin.composeinvestigator.compiler.Composer_SKIP_TO_GROUP_END
import land.sungbin.composeinvestigator.compiler.EMPTY_LIST_FQN
import land.sungbin.composeinvestigator.compiler.HASH_CODE_FQN
import land.sungbin.composeinvestigator.compiler.NO_INVESTIGATION_FQN
import land.sungbin.composeinvestigator.compiler.STABILITY_FQN
import land.sungbin.composeinvestigator.compiler.STATE_FQN
import land.sungbin.composeinvestigator.compiler.Stability_CERTAIN
import land.sungbin.composeinvestigator.compiler.Stability_COMBINED
import land.sungbin.composeinvestigator.compiler.Stability_PARAMETER
import land.sungbin.composeinvestigator.compiler.Stability_RUNTIME
import land.sungbin.composeinvestigator.compiler.Stability_UNKNOWN
import land.sungbin.composeinvestigator.compiler.fromFqName
import land.sungbin.composeinvestigator.compiler.struct.IrComposableInformation
import land.sungbin.composeinvestigator.compiler.struct.IrInvalidationTraceTable
import land.sungbin.composeinvestigator.compiler.struct.IrInvalidationTraceTableHolder
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
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.types.isNullable
import org.jetbrains.kotlin.ir.types.isNullableAny
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.types.typeWithArguments
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.setDeclarationsParent
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.utils.addToStdlib.cast

public open class ComposeInvestigatorBaseLower(
  protected val context: IrPluginContext,
  protected val messageCollector: MessageCollector,
  protected val irComposableInformation: IrComposableInformation,
) : IrElementTransformerVoid() {
  private var ownStabilitySymbol: IrClassSymbol? = null
  private var ownStabilityCertainSymbol: IrClassSymbol? = null
  private var ownStabilityRuntimeSymbol: IrClassSymbol? = null
  private var ownStabilityUnknownSymbol: IrClassSymbol? = null
  private var ownStabilityParameterSymbol: IrClassSymbol? = null
  private var ownStabilityCombinedSymbol: IrClassSymbol? = null

  private val composerSymbol = context.referenceClass(ClassId.topLevel(COMPOSER_FQN))!!
  private val composerSkipToGroupEndSymbol = composerSymbol.getSimpleFunction(Composer_SKIP_TO_GROUP_END.asString())!!

  private val stateSymbol = context.referenceClass(ClassId.topLevel(STATE_FQN))!!

  private val emptyListSymbol =
    context
      .referenceFunctions(CallableId.fromFqName(EMPTY_LIST_FQN))
      .single { symbol -> symbol.owner.valueParameters.isEmpty() && symbol.owner.typeParameters.size == 1 }

  protected val emptyStringListCall: IrCall =
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

  private val tableCallTransformer: InvalidationTraceTableIntrinsicTransformer = InvalidationTraceTableIntrinsicTransformer(
    context = context,
    messageCollector = messageCollector,
    irComposableInformation = irComposableInformation,
  )

  // TODO make protected
  internal val invalidationTraceTableHolder: IrInvalidationTraceTableHolder get() = tableCallTransformer

  final override fun visitFile(declaration: IrFile): IrFile =
    includeFileNameInExceptionTrace(declaration) {
      if (declaration.hasAnnotation(NO_INVESTIGATION_FQN)) return declaration

      val table = IrInvalidationTraceTable.create(context, declaration)
      declaration.declarations.add(0, table.rawProp.also { prop -> prop.setDeclarationsParent(declaration) })
      tableCallTransformer.lower(declaration, table)

      super.visitFile(declaration)
    }

  // if (%dirty and 0b0011 != 0b0010 || !%composer.skipping) {
  //   <ENTER HERE>
  //   magic()
  // } else {
  //   $composer.skipToGroupEnd()
  // }
  final override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
    // If the function itself is @NoInvestigation, all elements contained in this function
    // will be excluded from investigation.
    if (declaration.hasAnnotation(NO_INVESTIGATION_FQN)) return declaration

    // Since some of the elements inside the function may be composable, we continue inspection.
    if (!declaration.hasComposableAnnotation()) return super.visitSimpleFunction(declaration)

    declaration.body = firstTransformComposableBody(declaration, declaration.body.cast()).apply { transformChildrenVoid() }
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
      lastTransformSkipToGroupEndCall(expression)
    else
      super.visitCall(expression)

  // val state = remember { mutableStateOf(T) }
  final override fun visitVariable(declaration: IrVariable): IrStatement {
    if (declaration.origin == IrDeclarationOrigin.PROPERTY_DELEGATE) return super.visitVariable(declaration)
    if (declaration.isValidStateDeclaration()) {
      declaration.initializer = firstTransformStateInitializer(
        name = declaration.name,
        initializer = declaration.initializer!!,
      )
    }
    return super.visitVariable(declaration)
  }

  // var state by remember { mutableStateOf(T) }
  final override fun visitLocalDelegatedProperty(declaration: IrLocalDelegatedProperty): IrStatement {
    if (declaration.delegate.isValidStateDeclaration()) {
      declaration.delegate.initializer = firstTransformStateInitializer(
        name = declaration.name,
        initializer = declaration.delegate.initializer!!,
      )
    }
    return super.visitLocalDelegatedProperty(declaration)
  }

  // (MUST) LoadingOrder.FIRST
  protected open fun firstTransformComposableBody(composable: IrSimpleFunction, body: IrBlockBody): IrBody = body

  // (MUST) LoadingOrder.FIRST
  protected open fun firstTransformStateInitializer(name: Name, initializer: IrExpression): IrExpression = initializer

  // (MUST) LoadingOrder.LAST
  protected open fun lastTransformSkipToGroupEndCall(expression: IrCall): IrExpression = expression

  private fun IrVariable.isValidStateDeclaration(): Boolean {
    val isState = type.classOrNull?.isSubtypeOfClass(stateSymbol.defaultType.classOrFail) == true
    val isTempVariable = origin == IrDeclarationOrigin.IR_TEMPORARY_VARIABLE
    val hasInitializer = initializer != null
    return !type.isNullable() && isState && !isTempVariable && hasInitializer
  }

  protected fun Stability.toIrOwnStability(): IrConstructorCall {
    if (ownStabilitySymbol == null) ownStabilitySymbol = context.referenceClass(ClassId.topLevel(STABILITY_FQN))!!

    return when (this) {
      is Stability.Certain -> irOwnStabilityCertain(irBoolean(stable))
      is Stability.Runtime -> irOwnStabilityRuntime(irString(declaration.name.asString()))
      is Stability.Unknown -> irOwnStabilityUnknown(irString(declaration.name.asString()))
      is Stability.Parameter -> irOwnStabilityParameter(irString(parameter.name.asString()))
      is Stability.Combined -> irOwnStabilityCombined(elements.map { stability -> stability.toIrOwnStability() })
    }
  }

  protected fun irString(
    value: String,
    startOffset: Int = UNDEFINED_OFFSET,
    endOffset: Int = UNDEFINED_OFFSET,
  ): IrConst<String> = context.irString(value, startOffset, endOffset)

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
  ): IrConst<Boolean> = IrConstImpl.boolean(
    startOffset = startOffset,
    endOffset = endOffset,
    type = context.irBuiltIns.booleanType,
    value = value,
  )

  private fun irOwnStabilityCertain(stable: IrConst<Boolean>): IrConstructorCall {
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

  private fun irOwnStabilityRuntime(declarationName: IrConst<String>): IrConstructorCall {
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

  private fun irOwnStabilityUnknown(declarationName: IrConst<String>): IrConstructorCall {
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

  private fun irOwnStabilityParameter(parameterName: IrConst<String>): IrConstructorCall {
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
