/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.invalidation.internal.stability

import androidx.compose.compiler.plugins.kotlin.analysis.Stability
import land.sungbin.composeinvestigator.compiler.base.DECLARATION_STABILITY_FQN
import land.sungbin.composeinvestigator.compiler.base.DeclarationStability_CERTAIN
import land.sungbin.composeinvestigator.compiler.base.DeclarationStability_COMBINED
import land.sungbin.composeinvestigator.compiler.base.DeclarationStability_PARAMETER
import land.sungbin.composeinvestigator.compiler.base.DeclarationStability_RUNTIME
import land.sungbin.composeinvestigator.compiler.base.DeclarationStability_UNKNOWN
import land.sungbin.composeinvestigator.compiler.base.irBoolean
import land.sungbin.composeinvestigator.compiler.base.irString
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

private var declarationStabilitySymbol: IrClassSymbol? = null
private var declarationStabilityCertainSymbol: IrClassSymbol? = null
private var declarationStabilityRuntimeSymbol: IrClassSymbol? = null
private var declarationStabilityUnknownSymbol: IrClassSymbol? = null
private var declarationStabilityParameterSymbol: IrClassSymbol? = null
private var declarationStabilityCombinedSymbol: IrClassSymbol? = null

public fun Stability.toIrDeclarationStability(context: IrPluginContext): IrConstructorCall {
  if (declarationStabilitySymbol == null)
    declarationStabilitySymbol = context.referenceClass(ClassId.topLevel(DECLARATION_STABILITY_FQN))!!

  return when (this) {
    is Stability.Certain -> irDeclarationStabilityCertain(context.irBoolean(stable))
    is Stability.Runtime -> irDeclarationStabilityRuntime(context.irString(declaration.name.asString()))
    is Stability.Unknown -> irDeclarationStabilityUnknown(context.irString(declaration.name.asString()))
    is Stability.Parameter -> irDeclarationStabilityParameter(context.irString(parameter.name.asString()))
    is Stability.Combined -> context.irDeclarationStabilityCombined(elements.map { it.toIrDeclarationStability(context) })
  }
}

private fun irDeclarationStabilityCertain(stable: IrConst<Boolean>): IrConstructorCall {
  val symbol = declarationStabilityCertainSymbol ?: (
    declarationStabilitySymbol!!.owner.sealedSubclasses
      .single { clz -> clz.owner.name == DeclarationStability_CERTAIN }
      .also { symbol -> declarationStabilityCertainSymbol = symbol }
    )

  return IrConstructorCallImpl.fromSymbolOwner(
    type = symbol.defaultType,
    constructorSymbol = symbol.constructors.single(),
  ).apply {
    putValueArgument(0, stable)
  }
}

private fun irDeclarationStabilityRuntime(declarationName: IrConst<String>): IrConstructorCall {
  val symbol = declarationStabilityRuntimeSymbol ?: (
    declarationStabilitySymbol!!.owner.sealedSubclasses
      .single { clz -> clz.owner.name == DeclarationStability_RUNTIME }
      .also { symbol -> declarationStabilityRuntimeSymbol = symbol }
    )

  return IrConstructorCallImpl.fromSymbolOwner(
    type = symbol.defaultType,
    constructorSymbol = symbol.constructors.single(),
  ).apply {
    putValueArgument(0, declarationName)
  }
}

private fun irDeclarationStabilityUnknown(declarationName: IrConst<String>): IrConstructorCall {
  val symbol = declarationStabilityUnknownSymbol ?: (
    declarationStabilitySymbol!!.owner.sealedSubclasses
      .single { clz -> clz.owner.name == DeclarationStability_UNKNOWN }
      .also { symbol -> declarationStabilityUnknownSymbol = symbol }
    )

  return IrConstructorCallImpl.fromSymbolOwner(
    type = symbol.defaultType,
    constructorSymbol = symbol.constructors.single(),
  ).apply {
    putValueArgument(0, declarationName)
  }
}

private fun irDeclarationStabilityParameter(parameterName: IrConst<String>): IrConstructorCall {
  val symbol = declarationStabilityParameterSymbol ?: (
    declarationStabilitySymbol!!.owner.sealedSubclasses
      .single { clz -> clz.owner.name == DeclarationStability_PARAMETER }
      .also { symbol -> declarationStabilityParameterSymbol = symbol }
    )

  return IrConstructorCallImpl.fromSymbolOwner(
    type = symbol.defaultType,
    constructorSymbol = symbol.constructors.single(),
  ).apply {
    putValueArgument(0, parameterName)
  }
}

private fun IrPluginContext.irDeclarationStabilityCombined(elements: List<IrConstructorCall>): IrConstructorCall {
  val symbol = declarationStabilityCombinedSymbol ?: (
    declarationStabilitySymbol!!.owner.sealedSubclasses
      .single { clz -> clz.owner.name == DeclarationStability_COMBINED }
      .also { symbol -> declarationStabilityCombinedSymbol = symbol }
    )

  return IrConstructorCallImpl.fromSymbolOwner(
    type = symbol.defaultType,
    constructorSymbol = symbol.constructors.single(),
  ).apply {
    val varargElementType = declarationStabilitySymbol!!.defaultType
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
