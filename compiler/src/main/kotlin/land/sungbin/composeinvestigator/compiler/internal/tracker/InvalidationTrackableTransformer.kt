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
import land.sungbin.composeinvestigator.compiler.internal.MUTABLE_LIST_ADD_FQN
import land.sungbin.composeinvestigator.compiler.internal.MUTABLE_LIST_OF_FQN
import land.sungbin.composeinvestigator.compiler.internal.REGISTER_STATE_OBJECT_TRACKING_FQN
import land.sungbin.composeinvestigator.compiler.internal.fromFqName
import land.sungbin.composeinvestigator.compiler.internal.irInt
import land.sungbin.composeinvestigator.compiler.internal.irString
import land.sungbin.composeinvestigator.compiler.internal.origin.StateChangeTrackerOrigin
import land.sungbin.composeinvestigator.compiler.internal.stability.toIrDeclarationStability
import land.sungbin.composeinvestigator.compiler.internal.tracker.affect.IrAffectedComposable
import land.sungbin.composeinvestigator.compiler.internal.tracker.affect.IrAffectedField
import land.sungbin.composeinvestigator.compiler.internal.tracker.key.TrackerWritableSlices
import land.sungbin.composeinvestigator.compiler.internal.tracker.logger.IrInvalidationLogger
import land.sungbin.composeinvestigator.compiler.util.HandledMap
import land.sungbin.composeinvestigator.compiler.util.IrStatementContainerImpl
import land.sungbin.composeinvestigator.compiler.util.VerboseLogger
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
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

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
    return initializer

    val functionKey = irTrace[TrackerWritableSlices.DURABLE_FUNCTION_KEY, composable] ?: return initializer
    if (!handledState.handle(functionKey.keyName, stateName)) return initializer

    val functionLocation = composable.getSafelyLocation()
    val composableName = composable.getFunctionNameIntercepttedAnonymous(functionKey.userProvideName)

    val affectedComposable = IrAffectedComposable.irAffectedComposable(
      composableName = irString(composableName),
      packageName = irString(composable.fqNameWhenAvailable?.parent()?.asString() ?: FqName.ROOT.asString()),
      filePath = irString(functionLocation.file),
      startLine = irInt(functionLocation.line),
      startColumn = irInt(functionLocation.column),
    )

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

      putValueArgument(0, affectedComposable)
      putValueArgument(1, irString(functionKey.keyName))
      putValueArgument(2, irString(stateName.asString()))
      // The arguments for index 3 have default values.
    }.also { transformed ->
      logger("[StateInitializer] transformed dump: ${transformed.dump()}")
      logger("[StateInitializer] transformed dumpKotlinLike: ${transformed.dumpKotlinLike()}")
    }
  }

  override fun transformComposableBody(function: IrSimpleFunction, block: IrBlock): IrBlock {
    val currentKey = irTrace[TrackerWritableSlices.DURABLE_FUNCTION_KEY, function] ?: return block
    if (!handledComposableBody.handle(currentKey.keyName)) return block

    val newStatements = mutableListOf<IrStatement>()

    val currentUserProvideName = currentKey.userProvideName

    val currentFunctionName = function.getFunctionNameIntercepttedAnonymous(currentUserProvideName)
    val currentFunctionLocation = function.getSafelyLocation()
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
      val value = irGetValue(param)
      val valueString = irToString(value)
      val valueHashCode = irHashCode(value)
      val stability = stabilityInferencer.stabilityOf(value).normalize().toIrDeclarationStability(context)

      val valueParam = IrAffectedField.irValueParameter(
        name = name,
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
      nameHint = "$currentFunctionName\$validationReason",
    )
    newStatements += computeInvalidationReasonVariable

    val affectedComposable = IrAffectedComposable.irAffectedComposable(
      composableName = irString(currentFunctionName),
      packageName = irString(function.fqNameWhenAvailable?.parent()?.asString() ?: FqName.ROOT.asString()),
      filePath = irString(currentFunctionLocation.file),
      startLine = irInt(currentFunctionLocation.line),
      startColumn = irInt(currentFunctionLocation.column),
    )

    val invalidationTypeSymbol = IrInvalidationLogger.irInvalidationTypeSymbol
    val invalidationTypeProcessed =
      IrInvalidationLogger.irInvalidationTypeProcessed(reason = irGetValue(computeInvalidationReasonVariable))
        .apply { type = invalidationTypeSymbol.defaultType }

    val callListeners = currentInvalidationTrackTable.irCallListeners(
      key = irString(currentKey.keyName),
      composable = affectedComposable,
      type = invalidationTypeProcessed,
    )
    newStatements += callListeners

    val logger = IrInvalidationLogger.irLog(
      affectedComposable = affectedComposable,
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

    val currentKey = irTrace[TrackerWritableSlices.DURABLE_FUNCTION_KEY, target] ?: return NO_CHANGED
    if (!handledUpdateScopeBlock.handle(currentKey.keyName)) return NO_CHANGED

    val newStatements = mutableListOf<IrStatement>()

    val currentUserProvideName = currentKey.userProvideName

    val currentFunctionName = target.getFunctionNameIntercepttedAnonymous(currentUserProvideName)
    val currentFunctionLocation = target.getSafelyLocation()
    val currentInvalidationTrackTable = currentInvalidationTrackTable!!

    val affectedComposable = IrAffectedComposable.irAffectedComposable(
      composableName = irString(currentFunctionName),
      packageName = irString(target.fqNameWhenAvailable?.parent()?.asString() ?: FqName.ROOT.asString()),
      filePath = irString(currentFunctionLocation.file),
      startLine = irInt(currentFunctionLocation.line),
      startColumn = irInt(currentFunctionLocation.column),
    )

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
      composable = affectedComposable,
      type = invalidationTypeProcessed,
    )
    newStatements += callListeners

    val logger = IrInvalidationLogger.irLog(
      affectedComposable = affectedComposable,
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

    val currentKey = irTrace[TrackerWritableSlices.DURABLE_FUNCTION_KEY, composable] ?: return NO_CHANGED
    if (!handledSkipToGroupEndCall.handle(currentKey.keyName)) return NO_CHANGED

    val currentUserProvideName = currentKey.userProvideName

    val currentFunctionName = composable.getFunctionNameIntercepttedAnonymous(currentUserProvideName)
    val currentFunctionLocation = composable.getSafelyLocation()
    val currentInvalidationTrackTable = currentInvalidationTrackTable!!

    val affectedComposable = IrAffectedComposable.irAffectedComposable(
      composableName = irString(currentFunctionName),
      packageName = irString(composable.fqNameWhenAvailable?.parent()?.asString() ?: FqName.ROOT.asString()),
      filePath = irString(currentFunctionLocation.file),
      startLine = irInt(currentFunctionLocation.line),
      startColumn = irInt(currentFunctionLocation.column),
    )

    val invalidationTypeSymbol = IrInvalidationLogger.irInvalidationTypeSymbol
    val invalidationTypeSkipped = IrInvalidationLogger.irInvalidationTypeSkipped()
      .apply { type = invalidationTypeSymbol.defaultType }

    val callListeners = currentInvalidationTrackTable.irCallListeners(
      key = irString(currentKey.keyName),
      composable = affectedComposable,
      type = invalidationTypeSkipped,
    )
    val logger = IrInvalidationLogger.irLog(
      affectedComposable = affectedComposable,
      invalidationType = invalidationTypeSkipped,
    )

    return IrStatementContainerImpl(statements = listOf(callListeners, logger, initializer)).also { transformed ->
      logger("[ComposableSkipToGroupEnd] transformed dump: ${transformed.dump()}")
      logger("[ComposableSkipToGroupEnd] transformed dumpKotlinLike: ${transformed.dumpKotlinLike()}")
    }
  }
}
