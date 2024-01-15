/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.internal.tracker

import androidx.compose.compiler.plugins.kotlin.analysis.StabilityInferencer
import androidx.compose.compiler.plugins.kotlin.analysis.normalize
import androidx.compose.compiler.plugins.kotlin.irTrace
import land.sungbin.composeinvestigator.compiler.internal.COMPOSER_FQN
import land.sungbin.composeinvestigator.compiler.internal.MUTABLE_LIST_ADD_FQN
import land.sungbin.composeinvestigator.compiler.internal.MUTABLE_LIST_OF_FQN
import land.sungbin.composeinvestigator.compiler.internal.REGISTER_STATE_OBJECT_TRACKING_FQN
import land.sungbin.composeinvestigator.compiler.internal.fromFqName
import land.sungbin.composeinvestigator.compiler.internal.key.DurableWritableSlices
import land.sungbin.composeinvestigator.compiler.internal.stability.toIrDeclarationStability
import land.sungbin.composeinvestigator.compiler.internal.tracker.affect.IrAffectedField
import land.sungbin.composeinvestigator.compiler.internal.tracker.logger.IrInvalidationLogger
import land.sungbin.composeinvestigator.compiler.origin.StateChangeTrackerOrigin
import land.sungbin.composeinvestigator.compiler.util.HandledMap
import land.sungbin.composeinvestigator.compiler.util.IrStatementContainerImpl
import land.sungbin.composeinvestigator.compiler.util.VerboseLogger
import land.sungbin.composeinvestigator.compiler.util.irString
import land.sungbin.fastlist.fastLastOrNull
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrStatementContainer
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetObjectValueImpl
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

internal class InvalidationTrackableTransformer(
  private val context: IrPluginContext,
  private val logger: VerboseLogger,
  private val stabilityInferencer: StabilityInferencer,
) : AbstractInvalidationTrackingLower(context, logger), IrPluginContext by context {
  private val mutableListOfSymbol =
    context
      .referenceFunctions(CallableId.fromFqName(MUTABLE_LIST_OF_FQN))
      .single { fn -> fn.owner.valueParameters.isEmpty() }
  private val mutableListAddSymbol =
    context
      .referenceFunctions(CallableId.fromFqName(MUTABLE_LIST_ADD_FQN))
      .single { fn -> fn.owner.valueParameters.size == 1 }

  private val registerStateObjectTrackingSymbol =
    context.referenceFunctions(CallableId.fromFqName(REGISTER_STATE_OBJECT_TRACKING_FQN)).single()

  private val handledState = HandledMap()
  private val handledComposableBody = HandledMap()
  private val handledUpdateScopeBlock = HandledMap()
  private val handledSkipToGroupEndCall = HandledMap()

  override fun transformStateInitializer(composable: IrSimpleFunction, stateName: Name, initializer: IrExpression): IrExpression {
    val functionKey = irTrace[DurableWritableSlices.DURABLE_FUNCTION_KEY, composable] ?: return initializer
    if (!handledState.handle(functionKey.keyName, stateName)) return initializer

    val nearestComposer = composable.valueParameters.fastLastOrNull { param -> param.type.classFqName == COMPOSER_FQN }!!

    return IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = registerStateObjectTrackingSymbol,
    ).also { register ->
      register.extensionReceiver = initializer
      register.type = initializer.type
      register.origin = StateChangeTrackerOrigin
    }.apply {
      putTypeArgument(0, initializer.type)

      putValueArgument(0, irGetValue(nearestComposer))
      putValueArgument(1, functionKey.affectedComposable)
      putValueArgument(2, irString(functionKey.keyName))
      putValueArgument(3, irString(stateName.asString()))
      // The arguments for index 4 have default values.
    }.also { transformed ->
      logger("[StateInitializer] transformed dump: ${transformed.dump()}")
      logger("[StateInitializer] transformed dumpKotlinLike: ${transformed.dumpKotlinLike()}")
    }
  }

  override fun transformComposableBody(function: IrSimpleFunction, block: IrBlock): IrBlock {
    val currentKey = irTrace[DurableWritableSlices.DURABLE_FUNCTION_KEY, function] ?: return block
    if (!handledComposableBody.handle(currentKey.keyName)) return block

    val newStatements = mutableListOf<IrStatement>()
    val currentInvalidationTrackTable = currentInvalidationTrackTable!!

    val affectedFieldList = IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = mutableListOfSymbol,
    ).apply {
      putTypeArgument(0, IrAffectedField.irAffectedField.defaultType)
    }
    val affectedFieldListVar = irTmpVariableInCurrentFun(affectedFieldList, nameHint = "affectFields")
    newStatements += affectedFieldListVar

    for (param in function.valueParameters) {
      // Synthetic arguments are not handled.
      if (param.name.asString().startsWith('$')) continue

      val name = irString(param.name.asString())
      val typeFqName = irString(param.type.classFqName?.asString() ?: SpecialNames.ANONYMOUS_STRING)
      val value = irGetValue(param)
      val valueString = irToString(value)
      val valueHashCode = irHashCode(value)
      val stability = stabilityInferencer.stabilityOf(value).normalize().toIrDeclarationStability(context)

      val valueParam = IrAffectedField.irValueParameter(
        name = name,
        typeFqName = typeFqName,
        valueString = valueString,
        valueHashCode = valueHashCode,
        stability = stability,
      )
      val valueParamVariable = irTmpVariableInCurrentFun(valueParam, nameHint = "${param.name.asString()}\$valueParam")
      val addValueParamToList = IrCallImpl.fromSymbolOwner(
        startOffset = UNDEFINED_OFFSET,
        endOffset = UNDEFINED_OFFSET,
        symbol = mutableListAddSymbol,
      ).apply {
        dispatchReceiver = irGetValue(affectedFieldListVar)
        putValueArgument(0, irGetValue(valueParamVariable))
      }

      newStatements += valueParamVariable
      newStatements += addValueParamToList
    }

    val computeInvalidationReason = currentInvalidationTrackTable.irComputeInvalidationReason(
      composableKeyName = irString(currentKey.keyName),
      fields = irGetValue(affectedFieldListVar),
    )
    val computeInvalidationReasonVariable = irTmpVariableInCurrentFun(
      computeInvalidationReason,
      nameHint = "${function.name.asString()}\$validationReason",
    )
    newStatements += computeInvalidationReasonVariable

    val invalidationTypeSymbol = IrInvalidationLogger.irInvalidationTypeSymbol
    val invalidationTypeProcessed =
      IrInvalidationLogger.irInvalidationTypeProcessed(reason = irGetValue(computeInvalidationReasonVariable))
        .apply { type = invalidationTypeSymbol.defaultType }

    val callListeners = currentInvalidationTrackTable.irCallListeners(
      key = irString(currentKey.keyName),
      composable = currentKey.affectedComposable,
      type = invalidationTypeProcessed,
    )
    newStatements += callListeners

    val logger = IrInvalidationLogger.irLog(
      affectedComposable = currentKey.affectedComposable,
      invalidationType = invalidationTypeProcessed,
    )
    newStatements += logger

    block.statements.addAll(0, newStatements)

    return block.also { transformed ->
      logger("[ComposableBody] transformed dump: ${transformed.dump()}")
      logger("[ComposableBody] transformed dumpKotlinLike: ${transformed.dumpKotlinLike()}")
    }
  }

  override fun transformUpdateScopeBlock(target: IrSimpleFunction, initializer: IrReturn): IrStatementContainer {
    @Suppress("LocalVariableName")
    val NO_CHANGED = IrStatementContainerImpl(statements = listOf(initializer))

    val currentKey = irTrace[DurableWritableSlices.DURABLE_FUNCTION_KEY, target] ?: return NO_CHANGED
    if (!handledUpdateScopeBlock.handle(currentKey.keyName)) return NO_CHANGED

    val newStatements = mutableListOf<IrStatement>()
    val currentInvalidationTrackTable = currentInvalidationTrackTable!!

    val invalidationTypeSymbol = IrInvalidationLogger.irInvalidationTypeSymbol
    val invalidationTypeProcessed = IrInvalidationLogger.irInvalidationTypeProcessed(
      reason = IrGetObjectValueImpl(
        startOffset = UNDEFINED_OFFSET,
        endOffset = UNDEFINED_OFFSET,
        type = IrInvalidationLogger.irInvalidateReasonSymbol.defaultType,
        symbol = IrInvalidationLogger.irInvalidateReasonInvalidateSymbol,
      ),
    ).apply {
      type = invalidationTypeSymbol.defaultType
    }

    val callListeners = currentInvalidationTrackTable.irCallListeners(
      key = irString(currentKey.keyName),
      composable = currentKey.affectedComposable,
      type = invalidationTypeProcessed,
    )
    newStatements += callListeners

    val logger = IrInvalidationLogger.irLog(
      affectedComposable = currentKey.affectedComposable,
      invalidationType = invalidationTypeProcessed,
    )
    newStatements += logger

    newStatements += initializer

    return IrStatementContainerImpl(statements = newStatements).also { transformed ->
      logger("[ComposableUpdateScope] transformed dump: ${transformed.dump()}")
      logger("[ComposableUpdateScope] transformed dumpKotlinLike: ${transformed.dumpKotlinLike()}")
    }
  }

  override fun transformSkipToGroupEndCall(composable: IrSimpleFunction, initializer: IrCall): IrStatementContainer {
    @Suppress("LocalVariableName")
    val NO_CHANGED = IrStatementContainerImpl(statements = listOf(initializer))

    val currentKey = irTrace[DurableWritableSlices.DURABLE_FUNCTION_KEY, composable] ?: return NO_CHANGED
    if (!handledSkipToGroupEndCall.handle(currentKey.keyName)) return NO_CHANGED

    val currentInvalidationTrackTable = currentInvalidationTrackTable!!

    val invalidationTypeSymbol = IrInvalidationLogger.irInvalidationTypeSymbol
    val invalidationTypeSkipped = IrInvalidationLogger.irInvalidationTypeSkipped()
      .apply { type = invalidationTypeSymbol.defaultType }

    val callListeners = currentInvalidationTrackTable.irCallListeners(
      key = irString(currentKey.keyName),
      composable = currentKey.affectedComposable,
      type = invalidationTypeSkipped,
    )
    val logger = IrInvalidationLogger.irLog(
      affectedComposable = currentKey.affectedComposable,
      invalidationType = invalidationTypeSkipped,
    )

    return IrStatementContainerImpl(statements = listOf(callListeners, logger, initializer)).also { transformed ->
      logger("[ComposableSkipToGroupEnd] transformed dump: ${transformed.dump()}")
      logger("[ComposableSkipToGroupEnd] transformed dumpKotlinLike: ${transformed.dumpKotlinLike()}")
    }
  }
}
