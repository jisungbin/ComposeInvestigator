/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.analysis

import androidx.compose.compiler.plugins.kotlin.analysis.Stability
import land.sungbin.composeinvestigator.compiler.STABILITY_FQN
import land.sungbin.composeinvestigator.compiler.Stability_CERTAIN
import land.sungbin.composeinvestigator.compiler.Stability_COMBINED
import land.sungbin.composeinvestigator.compiler.Stability_PARAMETER
import land.sungbin.composeinvestigator.compiler.Stability_RUNTIME
import land.sungbin.composeinvestigator.compiler.Stability_UNKNOWN
import land.sungbin.composeinvestigator.compiler.util.irBoolean
import land.sungbin.composeinvestigator.compiler.util.irString
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.types.typeWithArguments
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.types.Variance

// TODO cause memory leaks?
private var ownStabilitySymbol: IrClassSymbol? = null
private var ownStabilityCertainSymbol: IrClassSymbol? = null
private var ownStabilityRuntimeSymbol: IrClassSymbol? = null
private var ownStabilityUnknownSymbol: IrClassSymbol? = null
private var ownStabilityParameterSymbol: IrClassSymbol? = null
private var ownStabilityCombinedSymbol: IrClassSymbol? = null

public fun Stability.toIrOwnStability(context: IrPluginContext): IrConstructorCall {
  if (ownStabilitySymbol == null)
    ownStabilitySymbol = context.referenceClass(ClassId.topLevel(STABILITY_FQN))!!

  return when (this) {
    is Stability.Certain -> irOwnStabilityCertain(context.irBoolean(stable))
    is Stability.Runtime -> irOwnStabilityRuntime(context.irString(declaration.name.asString()))
    is Stability.Unknown -> irOwnStabilityUnknown(context.irString(declaration.name.asString()))
    is Stability.Parameter -> irOwnStabilityParameter(context.irString(parameter.name.asString()))
    is Stability.Combined -> context.irOwnStabilityCombined(elements.map { it.toIrOwnStability(context) })
  }
}

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

private fun IrPluginContext.irOwnStabilityCombined(elements: List<IrConstructorCall>): IrConstructorCall {
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
    val genericType = irBuiltIns.arrayClass.typeWithArguments(listOf(genericTypeProjection))
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
