/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.internal.tracker.logger

import land.sungbin.composeinvestigator.compiler.internal.COMPOSABLE_INVALIDATION_TYPE_FQN
import land.sungbin.composeinvestigator.compiler.internal.COMPOSABLE_INVALIDATION_TYPE_PROCESSED_FQN
import land.sungbin.composeinvestigator.compiler.internal.COMPOSABLE_INVALIDATION_TYPE_SKIPPED_FQN
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

public object IrInvalidationLogger {
  private var loggerSymbol: IrSimpleFunctionSymbol? = null
  private var loggerType: LoggerType? = null

  private var invalidationTypeSymbol: IrClassSymbol? = null
  private var invalidationTypeProcessedSymbol: IrClassSymbol? = null
  private var invalidationTypeSkippedSymbol: IrClassSymbol? = null

  public val irInvalidationTypeSymbol: IrClassSymbol get() = invalidationTypeSymbol!!

  public fun init(context: IrPluginContext) {
    invalidationTypeSymbol = context.referenceClass(ClassId.topLevel(COMPOSABLE_INVALIDATION_TYPE_FQN))!!
    invalidationTypeProcessedSymbol = context.referenceClass(ClassId.topLevel(COMPOSABLE_INVALIDATION_TYPE_PROCESSED_FQN))!!
    invalidationTypeSkippedSymbol = context.referenceClass(ClassId.topLevel(COMPOSABLE_INVALIDATION_TYPE_SKIPPED_FQN))!!
  }

  internal fun getCurrentLoggerSymbolOrNull(): IrSimpleFunctionSymbol? = loggerSymbol

  public fun useDefaultLogger(context: IrPluginContext) {
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

  public fun useCustomLogger(symbol: IrSimpleFunctionSymbol) {
    loggerSymbol = symbol
    loggerType = LoggerType.Custom
  }

  public fun irLog(
    affectedComposable: IrDeclarationReference,
    invalidationType: IrDeclarationReference,
    defaultMessage: IrConst<String>,
  ): IrCall = IrCallImpl.fromSymbolOwner(
    startOffset = UNDEFINED_OFFSET,
    endOffset = UNDEFINED_OFFSET,
    symbol = loggerSymbol!!,
  ).apply {
    when (loggerType!!) {
      LoggerType.Println -> {
        putValueArgument(0, defaultMessage)
      }
      LoggerType.Custom -> {
        putValueArgument(0, affectedComposable)
        putValueArgument(1, invalidationType)
      }
    }
  }

  public fun irInvalidationTypeProcessed(reason: IrExpression): IrConstructorCall {
    val invalidateTypeProcessedSymbol = invalidationTypeProcessedSymbol!!
    return IrConstructorCallImpl.fromSymbolOwner(
      type = invalidateTypeProcessedSymbol.defaultType,
      constructorSymbol = invalidateTypeProcessedSymbol.constructors.single(),
    ).apply {
      putValueArgument(0, reason)
    }
  }

  public fun irInvalidationTypeSkipped(): IrGetObjectValue {
    val invalidateTypeSkippedSymbol = invalidationTypeSkippedSymbol!!
    return IrGetObjectValueImpl(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      type = invalidateTypeSkippedSymbol.defaultType,
      symbol = invalidateTypeSkippedSymbol,
    )
  }

  public enum class LoggerType {
    Println,
    Custom,
  }
}
