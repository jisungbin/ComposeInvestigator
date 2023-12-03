/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.internal.tracker.logger

import land.sungbin.composeinvestigator.compiler.internal.COMPOSABLE_INVALIDATION_TYPE_FQN
import land.sungbin.composeinvestigator.compiler.internal.COMPOSABLE_INVALIDATION_TYPE_PROCESSED_FQN
import land.sungbin.composeinvestigator.compiler.internal.COMPOSABLE_INVALIDATION_TYPE_SKIPPED_FQN
import land.sungbin.composeinvestigator.compiler.internal.COMPOSE_INVESTIGATOR_CONFIG_FQN
import land.sungbin.composeinvestigator.compiler.internal.COMPOSE_INVESTIGATOR_CONFIG_INVALIDATION_LOGGER_FQN
import land.sungbin.composeinvestigator.compiler.internal.FUNCTION_2_FQN
import land.sungbin.composeinvestigator.compiler.internal.FUNCTION_2_INVOKE_FQN
import land.sungbin.composeinvestigator.compiler.internal.fromFqName
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrDeclarationReference
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetObjectValue
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetObjectValueImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId

public object IrInvalidationLogger {
  private var loggerContainerSymbol: IrClassSymbol? = null
  private var loggerGetterSymbol: IrSimpleFunctionSymbol? = null

  private var function2Symbol: IrClassSymbol? = null
  private var function2InvokeSymbol: IrSimpleFunctionSymbol? = null

  private var invalidationTypeSymbol: IrClassSymbol? = null
  private var invalidationTypeProcessedSymbol: IrClassSymbol? = null
  private var invalidationTypeSkippedSymbol: IrClassSymbol? = null

  public val irInvalidationTypeSymbol: IrClassSymbol get() = invalidationTypeSymbol!!

  public fun init(context: IrPluginContext) {
    loggerContainerSymbol = context.referenceClass(ClassId.topLevel(COMPOSE_INVESTIGATOR_CONFIG_FQN))!!
    loggerGetterSymbol =
      context
        .referenceProperties(CallableId.fromFqName(COMPOSE_INVESTIGATOR_CONFIG_INVALIDATION_LOGGER_FQN)).single()
        .owner.getter!!.symbol

    function2Symbol = context.referenceClass(ClassId.topLevel(FUNCTION_2_FQN))!!
    function2InvokeSymbol = context.referenceFunctions(CallableId.fromFqName(FUNCTION_2_INVOKE_FQN)).single()

    invalidationTypeSymbol = context.referenceClass(ClassId.topLevel(COMPOSABLE_INVALIDATION_TYPE_FQN))!!
    invalidationTypeProcessedSymbol = context.referenceClass(ClassId.topLevel(COMPOSABLE_INVALIDATION_TYPE_PROCESSED_FQN))!!
    invalidationTypeSkippedSymbol = context.referenceClass(ClassId.topLevel(COMPOSABLE_INVALIDATION_TYPE_SKIPPED_FQN))!!
  }

  public fun irLog(
    affectedComposable: IrDeclarationReference,
    invalidationType: IrDeclarationReference,
  ): IrCall = IrCallImpl.fromSymbolOwner(
    startOffset = UNDEFINED_OFFSET,
    endOffset = UNDEFINED_OFFSET,
    symbol = function2InvokeSymbol!!,
  ).also { invokeCall ->
    invokeCall.dispatchReceiver = IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = loggerGetterSymbol!!,
    ).also { loggerGetter ->
      loggerGetter.dispatchReceiver = IrGetObjectValueImpl(
        startOffset = UNDEFINED_OFFSET,
        endOffset = UNDEFINED_OFFSET,
        type = loggerContainerSymbol!!.defaultType,
        symbol = loggerContainerSymbol!!,
      )
    }.apply {
      type = function2Symbol!!.defaultType
    }
  }.apply {
    putValueArgument(0, affectedComposable)
    putValueArgument(1, invalidationType)
  }

  public fun irInvalidationTypeProcessed(reason: IrExpression): IrConstructorCall {
    val invalidateTypeProcessedSymbol = invalidationTypeProcessedSymbol!!
    return IrConstructorCallImpl.fromSymbolOwner(
      type = invalidateTypeProcessedSymbol.defaultType,
      constructorSymbol = invalidateTypeProcessedSymbol.constructors.single(),
    ).apply {
      putValueArgument(0, reason)
    }
  }

  public fun irInvalidationTypeSkipped(): IrGetObjectValue {
    val invalidateTypeSkippedSymbol = invalidationTypeSkippedSymbol!!
    return IrGetObjectValueImpl(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      type = invalidateTypeSkippedSymbol.defaultType,
      symbol = invalidateTypeSkippedSymbol,
    )
  }
}
