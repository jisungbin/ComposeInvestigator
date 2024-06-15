/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.struct

import land.sungbin.composeinvestigator.compiler.COMPOSABLE_INVALIDATION_TYPE_FQN
import land.sungbin.composeinvestigator.compiler.COMPOSE_INVESTIGATOR_CONFIG_FQN
import land.sungbin.composeinvestigator.compiler.ComposableInvalidationLogger_INVOKE
import land.sungbin.composeinvestigator.compiler.ComposableInvalidationType_PROCESSED
import land.sungbin.composeinvestigator.compiler.ComposableInvalidationType_SKIPPED
import land.sungbin.composeinvestigator.compiler.ComposeInvestigatorConfig_INVALIDATION_LOGGER
import land.sungbin.composeinvestigator.compiler.INVALIDATION_REASON_FQN
import land.sungbin.composeinvestigator.compiler.InvalidationReason_Invalidate
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
  private var loggerGetterSymbol = loggerContainerSymbol.getPropertyGetter(ComposeInvestigatorConfig_INVALIDATION_LOGGER.asString())!!
  private var loggerInvokerSymbol = loggerGetterSymbol.owner.returnType.classOrFail.getSimpleFunction(ComposableInvalidationLogger_INVOKE.asString())!!

  public val irInvalidateReasonSymbol: IrClassSymbol = context.referenceClass(ClassId.topLevel(INVALIDATION_REASON_FQN))!!
  public val irInvalidateReasonInvalidateSymbol: IrClassSymbol =
    irInvalidateReasonSymbol.owner.sealedSubclasses.single { clz -> clz.owner.name == InvalidationReason_Invalidate }

  public val irInvalidationTypeSymbol: IrClassSymbol = context.referenceClass(ClassId.topLevel(COMPOSABLE_INVALIDATION_TYPE_FQN))!!

  private var invalidationTypeProcessedSymbol =
    irInvalidationTypeSymbol.owner.sealedSubclasses.single { clz -> clz.owner.name == ComposableInvalidationType_PROCESSED }

  private var invalidationTypeSkippedSymbol =
    irInvalidationTypeSymbol.owner.sealedSubclasses.single { clz -> clz.owner.name == ComposableInvalidationType_SKIPPED }

  public fun irLog(
    callstack: IrDeclarationReference,
    affectedComposable: IrDeclarationReference,
    invalidationType: IrDeclarationReference,
  ): IrCall = IrCallImpl.fromSymbolOwner(
    startOffset = UNDEFINED_OFFSET,
    endOffset = UNDEFINED_OFFSET,
    symbol = loggerInvokerSymbol,
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
    putValueArgument(0, callstack)
    putValueArgument(1, affectedComposable)
    putValueArgument(2, invalidationType)
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
