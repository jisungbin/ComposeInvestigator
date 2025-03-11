// Copyright 2025 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.struct

import androidx.compose.compiler.plugins.kotlin.analysis.Stability
import land.sungbin.composeinvestigator.compiler.InvestigatorClassIds
import land.sungbin.composeinvestigator.compiler.InvestigatorNames
import land.sungbin.composeinvestigator.compiler.lower.irBoolean
import land.sungbin.composeinvestigator.compiler.lower.irString
import land.sungbin.composeinvestigator.compiler.lower.unsafeLazy
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.expressions.impl.fromSymbolOwner
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.types.typeWithArguments
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.Variance

public class IrRuntimeStability(private val context: IrPluginContext) {
  private val containerSymbol by lazy { context.referenceClass(InvestigatorClassIds.Stability)!! }
  private val certainSymbol by unsafeLazy { stability(InvestigatorNames.Certain) }
  private val runtimeSymbol by unsafeLazy { stability(InvestigatorNames.Runtime) }
  private val parameterSymbol by unsafeLazy { stability(InvestigatorNames.Parameter) }
  private val unknownSymbol by unsafeLazy { stability(InvestigatorNames.Unknown) }
  private val combinedSymbol by unsafeLazy { stability(InvestigatorNames.Combined) }

  public fun Stability.asRuntimeStability(): IrConstructorCall =
    when (this) {
      is Stability.Certain -> irCertainStability(context.irBoolean(stable))
      is Stability.Runtime -> irRuntimeStability(context.irString(declaration.name.asString()))
      is Stability.Parameter -> irParameterStability(context.irString(parameter.name.asString()))
      is Stability.Unknown -> irUnknownStability(context.irString(declaration.name.asString()))
      is Stability.Combined -> irCombinedStability(elements.map { stability -> stability.asRuntimeStability() })
    }

  private fun irCertainStability(stable: IrConst): IrConstructorCall =
    IrConstructorCallImpl.fromSymbolOwner(
      type = certainSymbol.defaultType,
      constructorSymbol = certainSymbol.constructors.single(),
    ).apply {
      putValueArgument(0, stable)
    }

  private fun irRuntimeStability(declarationName: IrConst): IrConstructorCall =
    IrConstructorCallImpl.fromSymbolOwner(
      type = runtimeSymbol.defaultType,
      constructorSymbol = runtimeSymbol.constructors.single(),
    ).apply {
      putValueArgument(0, declarationName)
    }

  private fun irParameterStability(declarationName: IrConst): IrConstructorCall =
    IrConstructorCallImpl.fromSymbolOwner(
      type = parameterSymbol.defaultType,
      constructorSymbol = parameterSymbol.constructors.single(),
    ).apply {
      putValueArgument(0, declarationName)
    }

  private fun irUnknownStability(parameterName: IrConst): IrConstructorCall =
    IrConstructorCallImpl.fromSymbolOwner(
      type = unknownSymbol.defaultType,
      constructorSymbol = unknownSymbol.constructors.single(),
    ).apply {
      putValueArgument(0, parameterName)
    }

  private fun irCombinedStability(elements: List<IrConstructorCall>): IrConstructorCall =
    IrConstructorCallImpl.fromSymbolOwner(
      type = combinedSymbol.defaultType,
      constructorSymbol = combinedSymbol.constructors.single(),
    ).apply {
      val varargElementType = containerSymbol.defaultType
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

  private fun stability(name: Name): IrClassSymbol =
    containerSymbol.owner.sealedSubclasses.first { it.owner.name == name }
}
