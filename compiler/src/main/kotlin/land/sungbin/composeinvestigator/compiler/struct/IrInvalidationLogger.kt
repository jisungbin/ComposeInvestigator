// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.struct

import land.sungbin.composeinvestigator.compiler.InvestigatorNames
import land.sungbin.composeinvestigator.compiler.fromFqName
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrDeclarationReference
import org.jetbrains.kotlin.ir.expressions.IrGetObjectValue
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetObjectValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.fromSymbolOwner
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId

/** Helper class to make the `ComposableInvalidationLogger` class easier to handle in IR. */
public class IrInvalidationLogger(context: IrPluginContext) {
  private val composeInvestigatorCompanion =
    context.referenceClass(ClassId.topLevel(InvestigatorNames.composeInvestigator))!!.owner.companionObject()!!
  private val composeInvestigatorLoggerSymbol =
    composeInvestigatorCompanion.getPropertyGetter(InvestigatorNames.composeInvestigatorLogger.shortName().asString())!!

  private val loggerLogSymbol = context.referenceFunctions(CallableId.fromFqName(InvestigatorNames.invalidationLoggerLog)).single()

  public val invalidationResultSymbol: IrClassSymbol = context.referenceClass(ClassId.topLevel(InvestigatorNames.invalidationResult))!!
  private val invalidationResultSkippedSymbol =
    invalidationResultSymbol.owner.sealedSubclasses.first { clz -> clz.owner.name == InvestigatorNames.invalidationResultSkipped }

  /**
   * Create a call to `ComposableInvalidationLogger#log`.
   *
   * @param composable A `ComposableInformation` value (first value argument)
   * @param result A `InvalidationResult` value (second value argument)
   */
  public fun irLog(
    composable: IrDeclarationReference,
    result: IrDeclarationReference,
  ): IrCall =
    IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = loggerLogSymbol,
    ).also { invokeCall ->
      invokeCall.dispatchReceiver = IrCallImpl.fromSymbolOwner(
        startOffset = UNDEFINED_OFFSET,
        endOffset = UNDEFINED_OFFSET,
        symbol = composeInvestigatorLoggerSymbol,
      ).also { loggerGetter ->
        loggerGetter.dispatchReceiver = IrGetObjectValueImpl(
          startOffset = UNDEFINED_OFFSET,
          endOffset = UNDEFINED_OFFSET,
          type = composeInvestigatorCompanion.symbol.defaultType,
          symbol = composeInvestigatorCompanion.symbol,
        )
      }
    }.apply {
      putValueArgument(0, composable)
      putValueArgument(1, result)
    }

  /** Gets the `InvalidationResult#Skipped` object call. */
  public fun irInvalidationResultSkipped(): IrGetObjectValue =
    IrGetObjectValueImpl(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      type = invalidationResultSkippedSymbol.defaultType,
      symbol = invalidationResultSkippedSymbol,
    )
}
