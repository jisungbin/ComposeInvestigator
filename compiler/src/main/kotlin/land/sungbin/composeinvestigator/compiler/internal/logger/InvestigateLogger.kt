/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.internal.logger

import land.sungbin.composeinvestigator.compiler.internal.AFFECTED_COMPOSABLE_FQN
import land.sungbin.composeinvestigator.compiler.internal.COMPOSABLE_INVALIDATE_TYPE_FQN
import land.sungbin.composeinvestigator.compiler.internal.COMPOSABLE_INVALIDATE_TYPE_PROCESSED_FQN
import land.sungbin.composeinvestigator.compiler.internal.COMPOSABLE_INVALIDATE_TYPE_SKIPPED_FQN
import land.sungbin.composeinvestigator.compiler.internal.irInt
import land.sungbin.composeinvestigator.compiler.internal.irString
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
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
import org.jetbrains.kotlin.ir.types.isNullableAny
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.wasm.ir.source.location.SourceLocation

internal object InvestigateLogger {
  private var loggerSymbol: IrSimpleFunctionSymbol? = null
  private var loggerType: LoggerType? = null

  private var affectedComposableSymbol: IrClassSymbol? = null

  private var invalidateTypeSymbol: IrClassSymbol? = null
  private var invalidateTypeProcessedSymbol: IrClassSymbol? = null
  private var invalidateTypeSkippedSymbol: IrClassSymbol? = null

  internal fun getCurrentLoggerSymbolOrNull(): IrSimpleFunctionSymbol? = loggerSymbol

  internal fun useDefaultLogger(context: IrPluginContext) {
    val printlnSymbol: IrSimpleFunctionSymbol =
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

    loggerSymbol = printlnSymbol
    loggerType = LoggerType.Println
  }

  internal fun useCustomLogger(symbol: IrSimpleFunctionSymbol) {
    loggerSymbol = symbol
    loggerType = LoggerType.Custom
  }

  internal fun irLog(
    affectedComposable: IrDeclarationReference,
    invalidateType: IrDeclarationReference,
    originalMessage: IrConst<String>,
  ): IrCall = IrCallImpl.fromSymbolOwner(
    startOffset = UNDEFINED_OFFSET,
    endOffset = UNDEFINED_OFFSET,
    symbol = loggerSymbol!!,
  ).apply {
    when (loggerType!!) {
      LoggerType.Println -> {
        putValueArgument(0, originalMessage)
      }
      LoggerType.Custom -> {
        putValueArgument(0, affectedComposable)
        putValueArgument(1, invalidateType)
      }
    }
  }

  internal fun irAffectedComposable(
    context: IrPluginContext,
    functionName: String,
    packageName: String,
    location: SourceLocation.Location,
  ): IrConstructorCall {
    if (affectedComposableSymbol == null) {
      affectedComposableSymbol = context.referenceClass(ClassId.topLevel(AFFECTED_COMPOSABLE_FQN))
    }
    val affectedComposableSymbol = affectedComposableSymbol!!
    return IrConstructorCallImpl.fromSymbolOwner(
      type = affectedComposableSymbol.defaultType,
      constructorSymbol = affectedComposableSymbol.constructors.single(),
    ).apply {
      putValueArgument(0, context.irString(functionName))
      putValueArgument(1, context.irString(packageName))
      putValueArgument(2, context.irString(location.file))
      putValueArgument(3, context.irInt(location.line))
      putValueArgument(4, context.irInt(location.column))
    }
  }

  internal fun irInvalidateTypeSymbol(context: IrPluginContext): IrClassSymbol {
    if (invalidateTypeSymbol == null) {
      invalidateTypeSymbol = context.referenceClass(ClassId.topLevel(COMPOSABLE_INVALIDATE_TYPE_FQN))
    }
    return invalidateTypeSymbol!!
  }

  internal fun irInvalidateTypeProcessed(
    context: IrPluginContext,
    diffParams: IrExpression,
  ): IrConstructorCall {
    if (invalidateTypeProcessedSymbol == null) {
      invalidateTypeProcessedSymbol = context.referenceClass(ClassId.topLevel(COMPOSABLE_INVALIDATE_TYPE_PROCESSED_FQN))
    }
    val invalidateTypeProcessedSymbol = invalidateTypeProcessedSymbol!!
    return IrConstructorCallImpl.fromSymbolOwner(
      type = invalidateTypeProcessedSymbol.defaultType,
      constructorSymbol = invalidateTypeProcessedSymbol.constructors.single(),
    ).apply {
      putValueArgument(0, diffParams)
    }
  }

  internal fun irInvalidateTypeSkipped(context: IrPluginContext): IrGetObjectValue {
    if (invalidateTypeSkippedSymbol == null) {
      invalidateTypeSkippedSymbol = context.referenceClass(ClassId.topLevel(COMPOSABLE_INVALIDATE_TYPE_SKIPPED_FQN))
    }
    val invalidateTypeSkippedSymbol = invalidateTypeSkippedSymbol!!
    return IrGetObjectValueImpl(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      type = invalidateTypeSkippedSymbol.defaultType,
      symbol = invalidateTypeSkippedSymbol,
    )
  }

  private enum class LoggerType {
    Println,
    Custom,
  }
}
