/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.internal.tracker

import androidx.compose.compiler.plugins.kotlin.analysis.StabilityInferencer
import androidx.compose.compiler.plugins.kotlin.analysis.normalize
import land.sungbin.composeinvestigator.compiler.internal.MUTABLE_LIST_ADD_FQN
import land.sungbin.composeinvestigator.compiler.internal.MUTABLE_LIST_OF_FQN
import land.sungbin.composeinvestigator.compiler.internal.OBTAIN_STATE_PROPERTY_AND_ADD_FQN
import land.sungbin.composeinvestigator.compiler.internal.STATE_FQN
import land.sungbin.composeinvestigator.compiler.internal.fromFqName
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
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.wasm.ir.source.location.SourceLocation

internal class InvalidationTrackableTransformer(
  private val context: IrPluginContext,
  private val logger: VerboseLogger,
  private val stabilityInferencer: StabilityInferencer,
) : AbstractInvalidationTrackingLower(context, logger), IrPluginContext by context {
  private val stateClassType = context.referenceClass(ClassId.topLevel(STATE_FQN))!!.starProjectedType

  private val mutableListOfSymbol =
    context
      .referenceFunctions(CallableId.fromFqName(MUTABLE_LIST_OF_FQN))
      .single { fn -> fn.owner.valueParameters.isEmpty() }
  private val mutableListAddSymbol =
    context
      .referenceFunctions(CallableId.fromFqName(MUTABLE_LIST_ADD_FQN))
      .single { fn -> fn.owner.valueParameters.size == 1 }

  private val obtainStatePropertyAndAddSymbol =
    context.referenceFunctions(CallableId.fromFqName(OBTAIN_STATE_PROPERTY_AND_ADD_FQN)).single()

  override fun visitComposableBlock(function: IrSimpleFunction, block: IrBlock): IrBlock {
    val newFirstStatements = mutableListOf<IrStatement>()
    val newLastStatements = mutableListOf<IrStatement>()

    val currentKey = irTracee[DurableWritableSlices.DURABLE_FUNCTION_KEY, function]!!
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
    newFirstStatements += affectedFieldListVar

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

      newFirstStatements += valueParamVariable
      newFirstStatements += addValueParamToList
    }

    block.obtainStateProperties(statePropListVar = affectedFieldListVar)

    val computeInvalidationReason =
      currentInvalidationTrackTable.irComputeInvalidationReason(
        composableKeyName = irString(currentKey.keyName),
        fields = irGetValue(affectedFieldListVar),
      )
    val computeInvalidationReasonVariable = irTmpVariableInCurrentFun(
      computeInvalidationReason,
      nameHint = "$currentFunctionName\$validationReason",
    )
    newLastStatements += computeInvalidationReasonVariable

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
    newLastStatements += callListeners

    val logger = IrInvalidationLogger.irLog(
      affectedComposable = affectedComposable,
      invalidationType = invalidationTypeProcessed,
    )
    newLastStatements += logger

    block.statements.addAll(1, newFirstStatements)
    block.statements.addAll(block.statements.lastIndex, newLastStatements)
    block.origin = InvalidationTrackerOrigin

    logger("[invalidation processed] dump: ${block.dump()}")
    logger("[invalidation processed] dumpKotlinLike: ${block.dumpKotlinLike()}")

    return block
  }

  override fun visitSkipToGroupEndCall(function: IrSimpleFunction, call: IrCall): IrBlock {
    val currentKey = irTracee[DurableWritableSlices.DURABLE_FUNCTION_KEY, function]!!
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

  private fun IrBlock.obtainStateProperties(statePropListVar: IrVariable) {
    val handledLocations = mutableMapOf<SourceLocation, Unit>()

    transform(
      object : IrElementTransformerVoid() {
        // val state = remember { mutableStateOf(T) }
        override fun visitVariable(declaration: IrVariable): IrStatement {
          if (declaration.origin == IrDeclarationOrigin.PROPERTY_DELEGATE) return super.visitVariable(declaration)
          if (declaration.type.isComposeState()) {
            val name = irString(declaration.name.asString())
            val location = declaration.getSourceLocation(currentFile.fileEntry)

            if (!handledLocations.containsKey(location)) {
              val statePropAddingCall = IrCallImpl.fromSymbolOwner(
                startOffset = UNDEFINED_OFFSET,
                endOffset = UNDEFINED_OFFSET,
                symbol = obtainStatePropertyAndAddSymbol,
              ).apply {
                extensionReceiver = declaration.initializer!!
                putValueArgument(0, name)
                putValueArgument(1, irGetValue(statePropListVar))
              }

              declaration.initializer = statePropAddingCall
              handledLocations[location] = Unit
            }
          }
          return super.visitVariable(declaration)
        }

        // var state by remember { mutableStateOf(T) }
        override fun visitLocalDelegatedProperty(declaration: IrLocalDelegatedProperty): IrStatement {
          if (declaration.delegate.type.isComposeState()) {
            val name = irString(declaration.name.asString())
            val location = declaration.delegate.getSourceLocation(currentFile.fileEntry)

            if (!handledLocations.containsKey(location)) {
              val statePropAddingCall = IrCallImpl.fromSymbolOwner(
                startOffset = UNDEFINED_OFFSET,
                endOffset = UNDEFINED_OFFSET,
                symbol = obtainStatePropertyAndAddSymbol,
              ).apply {
                extensionReceiver = declaration.delegate.initializer!!
                putValueArgument(0, name)
                putValueArgument(1, irGetValue(statePropListVar))
                putTypeArgument(0, declaration.type)
              }

              declaration.delegate.initializer = statePropAddingCall
              handledLocations[location] = Unit
            }
          }
          return super.visitLocalDelegatedProperty(declaration)
        }
      },
      null,
    )
  }

  private fun IrType.isComposeState(): Boolean = classOrNull?.isSubtypeOfClass(stateClassType.classOrFail) ?: false
}
