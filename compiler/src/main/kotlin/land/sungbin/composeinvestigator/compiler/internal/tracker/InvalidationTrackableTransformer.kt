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
import land.sungbin.composeinvestigator.compiler.internal.fromFqName
import land.sungbin.composeinvestigator.compiler.internal.irInt
import land.sungbin.composeinvestigator.compiler.internal.irString
import land.sungbin.composeinvestigator.compiler.internal.stability.toIrDeclarationStability
import land.sungbin.composeinvestigator.compiler.internal.tracker.affect.IrAffectedComposable
import land.sungbin.composeinvestigator.compiler.internal.tracker.affect.IrAffectedField
import land.sungbin.composeinvestigator.compiler.internal.tracker.key.TrackerWritableSlices
import land.sungbin.composeinvestigator.compiler.internal.tracker.logger.IrInvalidationLogger
import land.sungbin.composeinvestigator.compiler.util.VerboseLogger
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrStatementContainer
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.name.CallableId
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

  override fun transformStateInitializer(stateName: Name, initializer: IrExpression): IrExpression {
    logger("transformStateInitializer: $stateName")
    return initializer
  }

  override fun transformUpdateScopeBlock(function: IrSimpleFunction, initializer: IrReturn): IrStatementContainer {
    val newStatements = mutableListOf<IrStatement>()

    val currentKey = irTrace[TrackerWritableSlices.SIMPLE_FUNCTION_KEY, function]!!
    val currentUserProvideName = currentKey.userProvideName

    val currentFunctionName = getCurrentFunctionNameIntercepttedAnonymous(currentUserProvideName)
    val currentFunctionLocation = function.getSafelyLocation()
    val currentInvalidationTrackTable = currentInvalidationTrackTable!!

    val affectedFieldList = IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = mutableListOfSymbol,
    ).apply {
      putTypeArgument(0, IrAffectedField.irAffectedFieldSymbol.defaultType)
    }
    val affectedFieldListVar = irTmpVariableInCurrentFun(affectedFieldList, nameHint = "affectFields")
    newStatements += affectedFieldListVar

    // The last two arguments are added by the Compose compiler ($composer, $changed)
    for (param in function.valueParameters.dropLast(2)) {
      val name = irString(param.name.asString())
      val valueGetter = irGetValue(param)
      val valueString = irToString(valueGetter)
      val valueHashCode = irHashCode(valueGetter)
      val stability = stabilityInferencer.stabilityOf(valueGetter).normalize().toIrDeclarationStability(context)

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
      packageName = irString(getCurrentFunctionPackage()),
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

    logger("[invalidation processed] dump: ${initializer.dump()}")
    logger("[invalidation processed] dumpKotlinLike: ${initializer.dumpKotlinLike()}")

    return IrStatementContainerImpl(statements = newStatements).also {
      logger("[invalidation processed] transformed dump: ${it.dump()}")
      logger("[invalidation processed] transformed dumpKotlinLike: ${it.dumpKotlinLike()}")
    }
  }

  override fun transformSkipToGroupEndCall(function: IrSimpleFunction, initializer: IrCall): IrStatementContainer {
    val currentKey = irTrace[TrackerWritableSlices.SIMPLE_FUNCTION_KEY, function]!!
    val currentUserProvideName = currentKey.userProvideName

    val currentFunctionName = getCurrentFunctionNameIntercepttedAnonymous(currentUserProvideName)
    val currentFunctionLocation = function.getSafelyLocation()
    val currentInvalidationTrackTable = currentInvalidationTrackTable!!

    val affectedComposable = IrAffectedComposable.irAffectedComposable(
      composableName = irString(currentFunctionName),
      packageName = irString(getCurrentFunctionPackage()),
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

    val transform = IrStatementContainerImpl(statements = listOf(initializer, callListeners, logger))

    logger("[invalidation skipped] transformed: ${initializer.dumpKotlinLike()} -> ${transform.dumpKotlinLike()}")

    return transform
  }
}
