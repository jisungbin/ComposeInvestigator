/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.internal.logger

import land.sungbin.composeinvestigator.compiler.internal.COMPOSE_INVESTIGATE_LOG_TYPE_FQN
import land.sungbin.composeinvestigator.compiler.internal.COMPOSE_INVESTIGATE_LOG_TYPE_INVALIDATION_PROCESSED_FQN
import land.sungbin.composeinvestigator.compiler.internal.COMPOSE_INVESTIGATE_LOG_TYPE_INVALIDATION_SKIPPED_FQN
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrDeclarationReference
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.isNullableAny
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal object InvestigateLogger {
  private var loggerSymbol: IrSimpleFunctionSymbol? = null
  private var loggerType: LoggerType? = null

  private var logTypeSymbol: IrClassSymbol? = null
  private var logTypeInvalidationProcessedSymbol: IrClassSymbol? = null
  private var logTypeInvalidationSkippedSymbol: IrClassSymbol? = null

  internal fun checkLoggerIsInstalled(): Boolean = loggerSymbol != null && loggerType != null

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

  internal fun makeIrCall(
    composableName: IrConst<String>,
    logType: IrDeclarationReference,
    originalMessage: IrConst<String>,
  ): IrCall =
    IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = loggerSymbol!!,
    ).apply {
      when (loggerType!!) {
        LoggerType.Println -> {
          putValueArgument(0, originalMessage)
        }
        LoggerType.Custom -> {
          putValueArgument(0, composableName)
          putValueArgument(1, logType)
        }
      }
    }

  internal fun obtainLogTypeSymbol(context: IrPluginContext): IrClassSymbol {
    if (logTypeSymbol == null) {
      logTypeSymbol = context.referenceClass(ClassId.topLevel(COMPOSE_INVESTIGATE_LOG_TYPE_FQN))
    }
    return logTypeSymbol!!
  }

  internal fun obtainLogTypeInvalidationProcessedSymbol(context: IrPluginContext): IrClassSymbol {
    if (logTypeInvalidationProcessedSymbol == null) {
      logTypeInvalidationProcessedSymbol =
        context.referenceClass(ClassId.topLevel(COMPOSE_INVESTIGATE_LOG_TYPE_INVALIDATION_PROCESSED_FQN))
    }
    return logTypeInvalidationProcessedSymbol!!
  }

  internal fun obtainLogTypeInvalidationSkippedSymbol(context: IrPluginContext): IrClassSymbol {
    if (logTypeInvalidationSkippedSymbol == null) {
      logTypeInvalidationSkippedSymbol =
        context.referenceClass(ClassId.topLevel(COMPOSE_INVESTIGATE_LOG_TYPE_INVALIDATION_SKIPPED_FQN))
    }
    return logTypeInvalidationSkippedSymbol!!
  }

  private enum class LoggerType {
    Println,
    Custom,
  }
}
