/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.internal.tracker

import androidx.compose.compiler.plugins.kotlin.analysis.StabilityInferencer
import androidx.compose.compiler.plugins.kotlin.analysis.normalize
import land.sungbin.composeinvestigator.compiler.internal.STATE_FQN
import land.sungbin.composeinvestigator.compiler.internal.STATE_VALUE_FQN
import land.sungbin.composeinvestigator.compiler.internal.irInt
import land.sungbin.composeinvestigator.compiler.internal.irString
import land.sungbin.composeinvestigator.compiler.internal.origin.InvalidationTrackerOrigin
import land.sungbin.composeinvestigator.compiler.internal.stability.toIrDeclarationStability
import land.sungbin.composeinvestigator.compiler.internal.tracker.affect.IrAffectedComposable
import land.sungbin.composeinvestigator.compiler.internal.tracker.affect.IrAffectedField
import land.sungbin.composeinvestigator.compiler.internal.tracker.key.DurableWritableSlices
import land.sungbin.composeinvestigator.compiler.internal.tracker.key.irTracee
import land.sungbin.composeinvestigator.compiler.internal.tracker.logger.IrInvalidationLogger
import land.sungbin.composeinvestigator.compiler.util.VerboseLogger
import land.sungbin.fastlist.fastForEachIndexed
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.wasm.ir2wasm.getSourceLocation
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrLocalDelegatedProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.addElement
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.types.typeWithArguments
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.wasm.ir.source.location.SourceLocation

internal class InvalidationTrackableTransformer(
  private val context: IrPluginContext,
  private val logger: VerboseLogger,
  private val stabilityInferencer: StabilityInferencer,
) : AbstractInvalidationTrackingLower(context, logger), IrPluginContext by context {
  private val stateClassType = context.referenceClass(ClassId.topLevel(STATE_FQN))!!.starProjectedType
  private val stateValueGetterSymbol =
    context
      .referenceProperties(
        CallableId(
          packageName = STATE_VALUE_FQN.parent(),
          callableName = STATE_VALUE_FQN.shortName(),
        ),
      )
      .single()
      .owner
      .getter!!
      .symbol

  override fun visitComposableBlock(function: IrSimpleFunction, block: IrBlock): IrBlock {
    val newStatements = mutableListOf<IrStatement>()

    val currentKey = irTracee[DurableWritableSlices.DURABLE_FUNCTION_KEY, function]!!
    val currentUserProvideName = currentKey.userProvideName

    val currentFunctionName = currentUserProvideName ?: function.name.asString()
    val currentFunctionLocation = function.getSafelyLocation()
    val currentInvalidationTrackTable = currentInvalidationTrackTable!!

    val affectedFieldType = IrAffectedField.irAffectedFieldSymbol.defaultType
    val affectedFieldGenericTypeProjection = makeTypeProjection(type = affectedFieldType, variance = Variance.OUT_VARIANCE)
    val affectedFieldGenericType = irBuiltIns.arrayClass.typeWithArguments(listOf(affectedFieldGenericTypeProjection))
    val affectedFields = IrVarargImpl(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      type = affectedFieldGenericType,
      varargElementType = affectedFieldType,
    )

    // The last two arguments are added by the Compose compiler ($composer, $changed)
    val validValueParamters = function.valueParameters.dropLast(2)
    for (param in validValueParamters) {
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
      ).apply { type = affectedFieldType }
      val valueParamVariable = irTmpVariableInCurrentFun(valueParam, nameHint = "${param.name.asString()}\$valueParam")

      newStatements += valueParamVariable
      affectedFields.addElement(irGetValue(valueParamVariable))
    }

    block
      .obtainStateProperties()
      .onEach { prop -> prop.type = affectedFieldType }
      .fastForEachIndexed { index, prop ->
        val propVariable = irTmpVariableInCurrentFun(prop, nameHint = "stateProperty\$$index")

        newStatements += propVariable
        affectedFields.addElement(irGetValue(propVariable))
      }

    val computeInvalidationReason =
      currentInvalidationTrackTable.irComputeInvalidationReason(
        composableKeyName = irString(currentKey.keyName),
        fields = affectedFields,
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
    val logger = IrInvalidationLogger.irLog(
      affectedComposable = affectedComposable,
      invalidationType = invalidationTypeProcessed,
    )
    newStatements += listOf(callListeners, logger)

    block.statements.addAll(block.statements.lastIndex, newStatements)
    block.origin = InvalidationTrackerOrigin

    logger("[invalidation processed] dump: ${block.dump()}")
    logger("[invalidation processed] dumpKotlinLike: ${block.dumpKotlinLike()}")

    return block
  }

  override fun visitSkipToGroupEndCall(function: IrSimpleFunction, call: IrCall): IrBlock {
    val currentKey = irTracee[DurableWritableSlices.DURABLE_FUNCTION_KEY, function]!!
    val currentUserProvideName = currentKey.userProvideName

    val currentFunctionName = currentUserProvideName ?: function.name.asString()
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

    val block = IrBlockImpl(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      type = irBuiltIns.unitType,
      origin = InvalidationTrackerOrigin,
      statements = listOf(call, callListeners, logger),
    )

    logger("[invalidation skipped] transformed: ${call.dumpKotlinLike()} -> ${block.dumpKotlinLike()}")

    return block
  }

  private fun IrExpression.getStateValue(): IrExpression =
    IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = stateValueGetterSymbol,
    ).apply {
      dispatchReceiver = this@getStateValue.apply { type = stateClassType }
    }

  private fun IrBlock.obtainStateProperties(): List<IrConstructorCall> {
    val result = mutableListOf<IrConstructorCall>()
    val handledLocations = mutableMapOf<SourceLocation, Unit>()
    val collector = object : IrElementTransformerVoid() {
      // val state = remember { mutableStateOf(T) }
      override fun visitVariable(declaration: IrVariable): IrStatement {
        if (declaration.origin == IrDeclarationOrigin.PROPERTY_DELEGATE) return super.visitVariable(declaration)
        if (declaration.type.isComposeState()) {
          val name = irString(declaration.name.asString())
          val valueGetter = irGetValue(declaration)
          val stateValue = valueGetter.getStateValue()
          val valueString = irToString(stateValue)
          val valueHashCode = irHashCode(stateValue)

          val location = declaration.getSourceLocation(currentFile.fileEntry)

          if (!handledLocations.containsKey(location)) {
            result += IrAffectedField.irStateProperty(
              name = name,
              valueString = valueString,
              valueHashCode = valueHashCode,
            )
            handledLocations[location] = Unit
          }
        }
        return super.visitVariable(declaration)
      }

      // var state by remember { mutableStateOf(T) }
      override fun visitLocalDelegatedProperty(declaration: IrLocalDelegatedProperty): IrStatement {
        if (declaration.delegate.type.isComposeState()) {
          val name = irString(declaration.name.asString())
          val stateValue = IrCallImpl.fromSymbolOwner(
            startOffset = UNDEFINED_OFFSET,
            endOffset = UNDEFINED_OFFSET,
            symbol = declaration.getter.symbol,
          )
          val valueString = irToString(stateValue)
          val valueHashCode = irHashCode(stateValue)

          val location = declaration.delegate.getSourceLocation(currentFile.fileEntry)

          if (!handledLocations.containsKey(location)) {
            result += IrAffectedField.irStateProperty(
              name = name,
              valueString = valueString,
              valueHashCode = valueHashCode,
            )
            handledLocations[location] = Unit
          }
        }
        return super.visitLocalDelegatedProperty(declaration)
      }
    }

    transform(collector, null)
    return result
  }

  private fun IrType.isComposeState(): Boolean =
    classOrNull?.isSubtypeOfClass(stateClassType.classOrFail) ?: false
}
