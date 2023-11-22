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

  internal fun makeIrCall(
    composable: IrDeclarationReference,
    type: IrDeclarationReference,
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
        putValueArgument(0, composable)
        putValueArgument(1, type)
      }
    }
  }

  internal fun obtainAffectedComposableSymbol(context: IrPluginContext): IrClassSymbol {
    if (affectedComposableSymbol == null) {
      affectedComposableSymbol =
        context.referenceClass(ClassId.topLevel(AFFECTED_COMPOSABLE_FQN))
    }
    return affectedComposableSymbol!!
  }

  internal fun obtainInvalidateTypeSymbol(context: IrPluginContext): IrClassSymbol {
    if (invalidateTypeSymbol == null) {
      invalidateTypeSymbol = context.referenceClass(ClassId.topLevel(COMPOSABLE_INVALIDATE_TYPE_FQN))
    }
    return invalidateTypeSymbol!!
  }

  internal fun obtainInvalidateTypeProcessedSymbol(context: IrPluginContext): IrClassSymbol {
    if (invalidateTypeProcessedSymbol == null) {
      invalidateTypeProcessedSymbol =
        context.referenceClass(ClassId.topLevel(COMPOSABLE_INVALIDATE_TYPE_PROCESSED_FQN))
    }
    return invalidateTypeProcessedSymbol!!
  }

  internal fun obtainInvalidateTypeSkippedSymbol(context: IrPluginContext): IrClassSymbol {
    if (invalidateTypeSkippedSymbol == null) {
      invalidateTypeSkippedSymbol =
        context.referenceClass(ClassId.topLevel(COMPOSABLE_INVALIDATE_TYPE_SKIPPED_FQN))
    }
    return invalidateTypeSkippedSymbol!!
  }

  private enum class LoggerType {
    Println,
    Custom,
  }
}
