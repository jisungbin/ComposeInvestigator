// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.struct

import land.sungbin.composeinvestigator.compiler.InvestigatorClassIds
import land.sungbin.composeinvestigator.compiler.InvestigatorNames
import land.sungbin.composeinvestigator.compiler.lower.unsafeLazy
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.functionByName
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetObjectValue
import org.jetbrains.kotlin.ir.expressions.IrValueAccessExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetObjectValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.fromSymbolOwner
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.shallowCopy

public class IrComposeInvestigator(private val context: IrPluginContext) {
  public val symbol: IrClassSymbol by lazy { context.referenceClass(InvestigatorClassIds.ComposeInvestigator)!! }

  public val irComposeInvestigator: IrGetObjectValue by unsafeLazy {
    IrGetObjectValueImpl(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      type = symbol.defaultType,
      symbol = symbol,
    )
  }
  private val getCurrentComposableName by unsafeLazy { symbol.functionByName(InvestigatorNames.getCurrentComposableName.asString()) }
  private val registerStateObjectSymbol by unsafeLazy { symbol.functionByName(InvestigatorNames.registerStateObject.asString()) }
  private val computeInvalidationReasonSymbol by unsafeLazy { symbol.functionByName(InvestigatorNames.computeInvalidationReason.asString()) }

  public fun irGetCurrentComposableName(
    default: IrConst,
    compoundKey: IrCall, // Expected to get irCompoundKeyHash
  ): IrCall =
    IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = getCurrentComposableName,
    ).apply {
      dispatchReceiver = irComposeInvestigator.shallowCopy()
      putValueArgument(0, default)
      putValueArgument(1, compoundKey)
    }

  /** Returns an [IrCall] that invokes `ComposeInvestigator#registerStateObject`. */
  public fun irRegisterStateObject(
    expression: IrExpression,
    name: IrConst,
  ): IrCall =
    IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = registerStateObjectSymbol,
    ).apply {
      dispatchReceiver = irComposeInvestigator.shallowCopy()
      type = expression.type
      putTypeArgument(0, expression.type)
      putValueArgument(0, expression)
      putValueArgument(1, name)
    }

  /** Returns an [IrCall] that invokes `ComposableInvalidationTraceTable#computeInvalidationReason`. */
  public fun irComputeInvalidationReason(
    compoundKey: IrCall, // Expected to get irCompoundKeyHash
    arguments: IrValueAccessExpression,
  ): IrCall =
    IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = computeInvalidationReasonSymbol,
    ).apply {
      dispatchReceiver = irComposeInvestigator.shallowCopy()
      putValueArgument(0, compoundKey)
      putValueArgument(1, arguments)
    }
}
