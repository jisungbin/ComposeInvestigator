/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

package land.sungbin.composeinvalidator.compiler.internal.stability

import androidx.compose.compiler.plugins.kotlin.analysis.Stability
import land.sungbin.composeinvalidator.compiler.internal.DECLARATION_STABILITY_CERTAIN_FQN
import land.sungbin.composeinvalidator.compiler.internal.DECLARATION_STABILITY_COMBINED_FQN
import land.sungbin.composeinvalidator.compiler.internal.DECLARATION_STABILITY_FQN
import land.sungbin.composeinvalidator.compiler.internal.DECLARATION_STABILITY_PARAMETER_FQN
import land.sungbin.composeinvalidator.compiler.internal.DECLARATION_STABILITY_RUNTIME_FQN
import land.sungbin.composeinvalidator.compiler.internal.DECLARATION_STABILITY_UNKNOWN_FQN
import land.sungbin.composeinvalidator.compiler.internal.irBoolean
import land.sungbin.composeinvalidator.compiler.internal.irString
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.name.ClassId

private var declarationStabilitySymbol: IrClassSymbol? = null
private var declarationStabilityCertainSymbol: IrClassSymbol? = null
private var declarationStabilityRuntimeSymbol: IrClassSymbol? = null
private var declarationStabilityUnknownSymbol: IrClassSymbol? = null
private var declarationStabilityParameterSymbol: IrClassSymbol? = null
private var declarationStabilityCombinedSymbol: IrClassSymbol? = null

private var declarationStabilityArraySymbol: IrClassSymbol? = null

internal fun Stability.toIrDeclarationStability(context: IrPluginContext): IrConstructorCall =
  when (this) {
    is Stability.Certain -> context.irDeclarationStabilityCertain(context.irBoolean(stable))
    is Stability.Runtime -> context.irDeclarationStabilityRuntime(context.irString(declaration.name.asString()))
    is Stability.Unknown -> context.irDeclarationStabilityUnknown(context.irString(declaration.name.asString()))
    is Stability.Parameter -> context.irDeclarationStabilityParameter(context.irString(parameter.name.asString()))
    is Stability.Combined -> context.irDeclarationStabilityCombined(elements.map { it.toIrDeclarationStability(context) })
  }

private fun IrPluginContext.irDeclarationStabilityCertain(stable: IrConst<Boolean>): IrConstructorCall {
  val symbol =
    declarationStabilityCertainSymbol ?: (referenceClass(ClassId.topLevel(DECLARATION_STABILITY_CERTAIN_FQN))!!
      .also { symbol -> declarationStabilityCertainSymbol = symbol })

  return IrConstructorCallImpl
    .fromSymbolOwner(
      type = symbol.owner.defaultType,
      constructorSymbol = symbol.constructors.single(),
    )
    .apply {
      putValueArgument(0, stable)
    }
}

private fun IrPluginContext.irDeclarationStabilityRuntime(declarationName: IrConst<String>): IrConstructorCall {
  val symbol =
    declarationStabilityRuntimeSymbol ?: (referenceClass(ClassId.topLevel(DECLARATION_STABILITY_RUNTIME_FQN))!!
      .also { symbol -> declarationStabilityRuntimeSymbol = symbol })

  return IrConstructorCallImpl
    .fromSymbolOwner(
      type = symbol.owner.defaultType,
      constructorSymbol = symbol.constructors.single(),
    )
    .apply {
      putValueArgument(0, declarationName)
    }
}

private fun IrPluginContext.irDeclarationStabilityUnknown(declarationName: IrConst<String>): IrConstructorCall {
  val symbol =
    declarationStabilityUnknownSymbol ?: (referenceClass(ClassId.topLevel(DECLARATION_STABILITY_UNKNOWN_FQN))!!
      .also { symbol -> declarationStabilityUnknownSymbol = symbol })

  return IrConstructorCallImpl
    .fromSymbolOwner(
      type = symbol.owner.defaultType,
      constructorSymbol = symbol.constructors.single(),
    )
    .apply {
      putValueArgument(0, declarationName)
    }
}

private fun IrPluginContext.irDeclarationStabilityParameter(parameterName: IrConst<String>): IrConstructorCall {
  val symbol =
    declarationStabilityParameterSymbol ?: (referenceClass(ClassId.topLevel(DECLARATION_STABILITY_PARAMETER_FQN))!!
      .also { symbol -> declarationStabilityParameterSymbol = symbol })

  return IrConstructorCallImpl
    .fromSymbolOwner(
      type = symbol.owner.defaultType,
      constructorSymbol = symbol.constructors.single(),
    )
    .apply {
      putValueArgument(0, parameterName)
    }
}

private fun IrPluginContext.irDeclarationStabilityCombined(elements: List<IrConstructorCall>): IrConstructorCall {
  val symbol =
    declarationStabilityCombinedSymbol ?: (referenceClass(ClassId.topLevel(DECLARATION_STABILITY_COMBINED_FQN))!!
      .also { symbol -> declarationStabilityCombinedSymbol = symbol })
  val parentSymbol =
    declarationStabilitySymbol ?: (referenceClass(ClassId.topLevel(DECLARATION_STABILITY_FQN))!!
      .also { parentSymbol -> declarationStabilitySymbol = parentSymbol })
  // val varargSymbol =
  //   declarationStabilityArraySymbol ?: (irBuiltIns.arrayClass
  //     .also { varargSymbol ->
  //       varargSymbol.owner.
  //       declarationStabilityArraySymbol = varargSymbol
  //     })

  return IrConstructorCallImpl
    .fromSymbolOwner(
      type = symbol.owner.defaultType,
      constructorSymbol = symbol.constructors.single(),
    )
    .apply {
      val vararg = IrVarargImpl(
        startOffset = UNDEFINED_OFFSET,
        endOffset = UNDEFINED_OFFSET,
        type = irBuiltIns.arrayClass.owner.defaultType,
        varargElementType = parentSymbol.owner.defaultType,
        elements = elements,
      )
      putValueArgument(0, vararg)
    }
}
