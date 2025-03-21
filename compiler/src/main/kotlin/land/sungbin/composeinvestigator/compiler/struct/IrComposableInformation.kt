// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.struct

import land.sungbin.composeinvestigator.compiler.InvestigatorClassIds
import land.sungbin.composeinvestigator.compiler.InvestigatorNames
import land.sungbin.composeinvestigator.compiler.lower.unsafeLazy
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.fir.java.enhancement.FirEmptyJavaDeclarationList.declarations
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrGetEnumValueImpl
import org.jetbrains.kotlin.ir.expressions.IrGetEnumValue
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.fromSymbolOwner
import org.jetbrains.kotlin.ir.symbols.IrEnumEntrySymbol
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.name.Name

/** Helper class to make the `ComposableInformation` class easier to handle in IR. */
public class IrComposableInformation(context: IrPluginContext) {
  private val symbol = context.referenceClass(InvestigatorClassIds.ComposableInformation)!!
  private val originSymbol = context.referenceClass(InvestigatorClassIds.ComposableInformationOrigin)!!

  private fun findOriginEntry(name: Name): IrEnumEntrySymbol =
    originSymbol.owner
      .declarations
      .filterIsInstance<IrEnumEntry>()
      .single { it.name == name }
      .symbol

  private val functionOrigin by unsafeLazy { findOriginEntry(InvestigatorNames.Function) }
  private val valueArgumentOrigin by unsafeLazy { findOriginEntry(InvestigatorNames.ValueArgument) }
  private val lambdaOrigin by unsafeLazy { findOriginEntry(InvestigatorNames.Lambda) }

  public operator fun invoke(
    fileName: IrConst,
    packageName: IrConst,
    simpleName: IrCall, // Expected to get irGetComposableName
    compoundKey: IrExpression,
    origin: IrGetEnumValue,
  ): IrConstructorCallImpl =
    IrConstructorCallImpl.fromSymbolOwner(
      type = symbol.defaultType,
      constructorSymbol = symbol.constructors.single(),
    ).apply {
      putValueArgument(0, fileName)
      putValueArgument(1, packageName)
      putValueArgument(2, simpleName)
      putValueArgument(3, compoundKey)
      putValueArgument(4, origin)
    }

  public fun irFunctionOrigin(): IrGetEnumValue =
    IrGetEnumValueImpl(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      type = originSymbol.defaultType,
      symbol = functionOrigin,
    )

  public fun irValueArgumentOrigin(): IrGetEnumValue =
    IrGetEnumValueImpl(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      type = originSymbol.defaultType,
      symbol = valueArgumentOrigin,
    )

  public fun irLambdaOrigin(): IrGetEnumValue =
    IrGetEnumValueImpl(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      type = originSymbol.defaultType,
      symbol = lambdaOrigin,
    )
}
