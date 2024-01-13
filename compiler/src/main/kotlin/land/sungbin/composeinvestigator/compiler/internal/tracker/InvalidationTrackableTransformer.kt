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
import land.sungbin.composeinvestigator.compiler.internal.COMPOSABLE_KEY_INFO_FQN
import land.sungbin.composeinvestigator.compiler.internal.COMPOSE_STATE_OBJECT_VALUE_GETTER_FQN
import land.sungbin.composeinvestigator.compiler.internal.MUTABLE_LIST_ADD_FQN
import land.sungbin.composeinvestigator.compiler.internal.MUTABLE_LIST_OF_FQN
import land.sungbin.composeinvestigator.compiler.internal.PAIR_FQN
import land.sungbin.composeinvestigator.compiler.internal.REGISTER_STATE_OBJECT_TRACKING_FQN
import land.sungbin.composeinvestigator.compiler.internal.STATE_VALUE_GETTER_FQN
import land.sungbin.composeinvestigator.compiler.internal.fromFqName
import land.sungbin.composeinvestigator.compiler.internal.irInt
import land.sungbin.composeinvestigator.compiler.internal.irString
import land.sungbin.composeinvestigator.compiler.internal.origin.StateChangeTrackerOrigin
import land.sungbin.composeinvestigator.compiler.internal.stability.toIrDeclarationStability
import land.sungbin.composeinvestigator.compiler.internal.tracker.affect.IrAffectedComposable
import land.sungbin.composeinvestigator.compiler.internal.tracker.affect.IrAffectedField
import land.sungbin.composeinvestigator.compiler.internal.tracker.key.TrackerWritableSlices
import land.sungbin.composeinvestigator.compiler.internal.tracker.logger.IrInvalidationLogger
import land.sungbin.composeinvestigator.compiler.util.VerboseLogger
import land.sungbin.composeinvestigator.compiler.util.dump
import land.sungbin.composeinvestigator.compiler.util.dumpKotlinLike
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrStatementContainer
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetObjectValueImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
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

  private val pairSymbol = context.referenceClass(ClassId.topLevel(PAIR_FQN))!!

  private val registerStateObjectTrackingSymbol =
    context.referenceFunctions(CallableId.fromFqName(REGISTER_STATE_OBJECT_TRACKING_FQN)).single()
  private val composableKeyInfoSymbol = context.referenceClass(ClassId.topLevel(COMPOSABLE_KEY_INFO_FQN))!!

  private val stateValueGetterSymbol = context.referenceClass(ClassId.topLevel(STATE_VALUE_GETTER_FQN))!!
  private val composeStateObjectValueGetterSymbol =
    context.referenceClass(ClassId.topLevel(COMPOSE_STATE_OBJECT_VALUE_GETTER_FQN))!!

  override fun transformStateInitializer(composable: IrSimpleFunction, stateName: Name, initializer: IrExpression): IrExpression {
    val functionKey = irTrace[TrackerWritableSlices.SIMPLE_FUNCTION_KEY, composable] ?: return initializer
    val functionLocation = composable.getSafelyLocation()

    val nearestComposer = composable.valueParameters.single { param ->
      param.type == composerSymbol.defaultType.makeNullable()
    }

    val composableName = composable.getFunctionNameIntercepttedAnonymous(functionKey.userProvideName)
    val composableKeyInfo = IrConstructorCallImpl.fromSymbolOwner(
      type = composableKeyInfoSymbol.defaultType,
      constructorSymbol = composableKeyInfoSymbol.constructors.single(),
    ).apply {
      putValueArgument(0, irString(composableName))
      putValueArgument(1, irString(functionKey.keyName))
    }

    val affectedComposable = IrAffectedComposable.irAffectedComposable(
      composableName = irString(composableName),
      packageName = irString(composable.kotlinFqName.asString()),
      filePath = irString(functionLocation.file),
      startLine = irInt(functionLocation.line),
      startColumn = irInt(functionLocation.column),
    )

    return IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = registerStateObjectTrackingSymbol,
    ).also { register ->
      register.extensionReceiver = composableKeyInfo
      register.type = initializer.type
      register.origin = StateChangeTrackerOrigin
    }.apply {
      putTypeArgument(0, initializer.type)

      putValueArgument(0, irGetValue(nearestComposer))
      putValueArgument(1, affectedComposable)
      putValueArgument(
        2,
        IrGetObjectValueImpl(
          startOffset = UNDEFINED_OFFSET,
          endOffset = UNDEFINED_OFFSET,
          type = stateValueGetterSymbol.defaultType,
          symbol = composeStateObjectValueGetterSymbol,
        ),
      )
      putValueArgument(
        3,
        IrConstructorCallImpl.fromSymbolOwner(
          type = pairSymbol.defaultType,
          constructorSymbol = pairSymbol.constructors.single(),
        ).apply {
          putTypeArgument(0, irBuiltIns.stringType)
          putTypeArgument(1, initializer.type)

          putValueArgument(0, irString(stateName.asString()))
          putValueArgument(1, initializer)
        }
      )
    }.also { transformed ->
      logger("[StateInitializer] transformed dump: ${transformed.dump()}")
      logger("[StateInitializer] transformed dumpKotlinLike: ${transformed.dumpKotlinLike()}")
    }
  }

  override fun transformUpdateScopeBlock(initializer: IrReturn): IrStatementContainer {
    val noChanged = IrStatementContainerImpl(statements = listOf(initializer))

    val newStatements = mutableListOf<IrStatement>()
    val target = initializer.value as? IrCall ?: return noChanged
    val targetOwner = target.symbol.owner

    val currentKey = irTrace[TrackerWritableSlices.SIMPLE_FUNCTION_KEY, targetOwner]!!
    val currentUserProvideName = currentKey.userProvideName

    val currentFunctionName = targetOwner.getFunctionNameIntercepttedAnonymous(currentUserProvideName)
    val currentFunctionLocation = targetOwner.getSafelyLocation()
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

    check(target.valueArgumentsCount == targetOwner.valueParameters.size)

    // The last two arguments are added by the Compose compiler ($composer, $changed)
    for ((index, param) in targetOwner.valueParameters.dropLast(2).withIndex()) {
      val name = irString(param.name.asString())
      val value = target.getValueArgument(index)!!
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

    newStatements += initializer

    return IrStatementContainerImpl(statements = newStatements).also { transformed ->
      logger("[invalidation processed] transformed dump: ${transformed.dump()}")
      logger("[invalidation processed] transformed dumpKotlinLike: ${transformed.dumpKotlinLike()}")
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

    return IrStatementContainerImpl(statements = listOf(callListeners, logger, initializer)).also { transformed ->
      logger("[invalidation skipped] transformed: ${initializer.dumpKotlinLike()} -> ${transformed.dumpKotlinLike()}")
    }
  }
}
