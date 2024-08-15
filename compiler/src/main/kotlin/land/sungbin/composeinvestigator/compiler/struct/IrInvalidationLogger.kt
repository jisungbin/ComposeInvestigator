/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.struct

import land.sungbin.composeinvestigator.compiler.COMPOSE_INVESTIGATOR_CONFIG_FQN
import land.sungbin.composeinvestigator.compiler.ComposableInvalidationLogger_LOG
import land.sungbin.composeinvestigator.compiler.ComposeInvestigatorConfig_LOGGER
import land.sungbin.composeinvestigator.compiler.INVALIDATION_TYPE_FQN
import land.sungbin.composeinvestigator.compiler.InvalidationType_PROCESSED
import land.sungbin.composeinvestigator.compiler.InvalidationType_SKIPPED
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrDeclarationReference
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetObjectValue
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetObjectValueImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.name.ClassId

public class IrInvalidationLogger(context: IrPluginContext) {
  private var loggerContainerSymbol = context.referenceClass(ClassId.topLevel(COMPOSE_INVESTIGATOR_CONFIG_FQN))!!
  private var loggerGetterSymbol = loggerContainerSymbol.getPropertyGetter(ComposeInvestigatorConfig_LOGGER.asString())!!
  private var loggerLogSymbol = loggerGetterSymbol.owner.returnType.classOrFail.getSimpleFunction(ComposableInvalidationLogger_LOG.asString())!!

  public val irInvalidationTypeSymbol: IrClassSymbol = context.referenceClass(ClassId.topLevel(INVALIDATION_TYPE_FQN))!!

  private var invalidationTypeProcessedSymbol =
    irInvalidationTypeSymbol.owner.sealedSubclasses.first { clz -> clz.owner.name == InvalidationType_PROCESSED }

  private var invalidationTypeSkippedSymbol =
    irInvalidationTypeSymbol.owner.sealedSubclasses.first { clz -> clz.owner.name == InvalidationType_SKIPPED }

  public fun irLog(
    composable: IrDeclarationReference,
    type: IrDeclarationReference,
  ): IrCall = IrCallImpl.fromSymbolOwner(
    startOffset = UNDEFINED_OFFSET,
    endOffset = UNDEFINED_OFFSET,
    symbol = loggerLogSymbol,
  ).also { invokeCall ->
    invokeCall.dispatchReceiver = IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = loggerGetterSymbol,
    ).also { loggerGetter ->
      loggerGetter.dispatchReceiver = IrGetObjectValueImpl(
        startOffset = UNDEFINED_OFFSET,
        endOffset = UNDEFINED_OFFSET,
        type = loggerContainerSymbol.defaultType,
        symbol = loggerContainerSymbol,
      )
    }
  }.apply {
    putValueArgument(0, composable)
    putValueArgument(1, type)
  }

  public fun irInvalidationTypeProcessed(reason: IrExpression): IrConstructorCall =
    IrConstructorCallImpl.fromSymbolOwner(
      type = invalidationTypeProcessedSymbol.defaultType,
      constructorSymbol = invalidationTypeProcessedSymbol.constructors.single(),
    ).apply {
      putValueArgument(0, reason)
    }

  public fun irInvalidationTypeSkipped(): IrGetObjectValue =
    IrGetObjectValueImpl(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      type = invalidationTypeSkippedSymbol.defaultType,
      symbol = invalidationTypeSkippedSymbol,
    )
}
