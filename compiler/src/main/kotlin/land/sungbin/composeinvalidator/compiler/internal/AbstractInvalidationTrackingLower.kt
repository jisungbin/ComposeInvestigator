/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package land.sungbin.composeinvalidator.compiler.internal

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueDeclaration
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.types.isNullableAny
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addToStdlib.cast

internal abstract class AbstractInvalidationTrackingLower(context: IrPluginContext) :
  IrElementTransformerVoidWithContext() {
  private val printlnSymbol: IrSimpleFunctionSymbol =
    context
      .referenceFunctions(
        CallableId(
          packageName = FqName("kotlin.io"),
          callableName = Name.identifier("println"),
        ),
      )
      .single { symbol ->
        symbol.owner.valueParameters.size == 1 &&
          symbol.owner.valueParameters.single().type.isNullableAny()
      }
  private val hashCodeSymbol: IrSimpleFunctionSymbol =
    context
      .referenceFunctions(
        CallableId(
          packageName = FqName("kotlin"),
          callableName = Name.identifier("hashCode"),
        ),
      )
      .single { symbol ->
        val extensionReceiver = symbol.owner.extensionReceiverParameter

        val isValidExtensionReceiver = extensionReceiver != null && extensionReceiver.type.isNullableAny()
        val isValidReturnType = symbol.owner.returnType.isInt()

        isValidExtensionReceiver && isValidReturnType
      }
  private val toStringSymbol: IrSimpleFunctionSymbol =
    context
      .referenceFunctions(
        CallableId(
          packageName = FqName("kotlin"),
          callableName = Name.identifier("toString"),
        ),
      )
      // [KT-44684] Duplicate results from IrPluginContext.referenceFunctions for kotlin.toString()
      .first { symbol ->
        val extensionReceiver = symbol.owner.extensionReceiverParameter

        val isValidExtensionReceiver = extensionReceiver != null && extensionReceiver.type.isNullableAny()
        val isValidReturnType = symbol.owner.returnType.isString()

        isValidExtensionReceiver && isValidReturnType
      }

  protected val currentFunctionOrNull: IrFunction?
    get() {
      return (currentFunction ?: return null).scope.scopeOwnerSymbol.cast<IrFunctionSymbol>().owner
    }

  protected val currentFunctionName: String
    get() {
      return (currentFunctionOrNull ?: return "<unknown>").name.asString()
    }

  protected fun irGetValue(value: IrValueDeclaration): IrGetValue =
    IrGetValueImpl(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = value.symbol,
    )

  protected fun irPrintln(value: IrExpression): IrCall =
    IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = printlnSymbol,
    ).apply {
      putValueArgument(0, value)
    }

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
      symbol = toStringSymbol,
    ).apply {
      extensionReceiver = value
    }

  protected fun irTmpVariableInCurrentFun(expression: IrExpression, nameHint: String? = null): IrVariable =
    currentFunction!!.scope.createTemporaryVariable(irExpression = expression, nameHint = nameHint)
}
