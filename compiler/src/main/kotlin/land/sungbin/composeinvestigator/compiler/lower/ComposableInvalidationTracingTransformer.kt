/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.lower

import androidx.compose.compiler.plugins.kotlin.analysis.StabilityInferencer
import androidx.compose.compiler.plugins.kotlin.irTrace
import land.sungbin.composeinvestigator.compiler.HandledMap
import land.sungbin.composeinvestigator.compiler.MUTABLE_LIST_ADD_FQN
import land.sungbin.composeinvestigator.compiler.MUTABLE_LIST_OF_FQN
import land.sungbin.composeinvestigator.compiler.VerboseLogger
import land.sungbin.composeinvestigator.compiler.analysis.DurationWritableSlices
import land.sungbin.composeinvestigator.compiler.fromFqName
import land.sungbin.composeinvestigator.compiler.struct.IrAffectedComposable
import land.sungbin.composeinvestigator.compiler.struct.IrAffectedField
import land.sungbin.composeinvestigator.compiler.struct.IrInvalidationLogger
import land.sungbin.composeinvestigator.compiler.util.IrStatementContainerSimpleImpl
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrStatementContainer
import org.jetbrains.kotlin.ir.expressions.impl.IrGetObjectValueImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.name.CallableId

internal class ComposableInvalidationTracingTransformer(
  private val context: IrPluginContext,
  private val logger: VerboseLogger,
  private val stabilityInferencer: StabilityInferencer,
  private val affectedField: IrAffectedField,
  affectedComposable: IrAffectedComposable,
  private val invalidationLogger: IrInvalidationLogger,
) : AbstractComposableInvalidationTraceLower(
  context = context,
  logger = logger,
  stabilityInferencer = stabilityInferencer,
  affectedComposable = affectedComposable,
),
  IrPluginContext by context {
  private val mutableListOfSymbol =
    context
      .referenceFunctions(CallableId.fromFqName(MUTABLE_LIST_OF_FQN))
      .single { fn -> fn.owner.valueParameters.isEmpty() }
  private val mutableListAddSymbol =
    context
      .referenceFunctions(CallableId.fromFqName(MUTABLE_LIST_ADD_FQN))
      .single { fn -> fn.owner.valueParameters.size == 1 }

  private val handledUpdateScopeBlock = HandledMap()
  private val handledSkipToGroupEndCall = HandledMap()

  // TODO work on this
  /*override fun transformComposableBody(composable: IrSimpleFunction, body: IrStatementContainer): IrStatementContainer {
    val currentKey = irTrace[DurationWritableSlices.DURABLE_FUNCTION_KEY, composable] ?: return body
    if (!handledComposableBody.handle(currentKey.keyName)) return body

    val newStatements = mutableListOf<IrStatement>()
    val currentInvalidationTraceTable = currentInvalidationTraceTable!!

    val affectedFieldList = IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = mutableListOfSymbol,
    ).apply {
      putTypeArgument(0, affectedField.irAffectedField.defaultType)
    }
    val affectedFieldListVar = irTmpVariableInCurrentFun(affectedFieldList, nameHint = "affectFields")
    newStatements += affectedFieldListVar

    for (param in composable.valueParameters) {
      // Synthetic arguments are not handled.
      if (param.name.asString().startsWith('$')) continue

      val name = irString(param.name.asString())
      val typeFqName = irString(param.type.classFqName?.asString() ?: SpecialNames.ANONYMOUS_STRING)
      val value = irGetValue(param)
      val valueString = irToString(value)
      val valueHashCode = irHashCode(value)
      val stability = stabilityInferencer.stabilityOf(value).normalize().toIrOwnStability(context)

      val valueParam = affectedField.irValueParameter(
        name = name,
        typeName = typeFqName,
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

    val computeInvalidationReason = currentInvalidationTraceTable.irComputeInvalidationReason(
      composableKeyName = irString(currentKey.keyName),
      fields = irGetValue(affectedFieldListVar),
    )
    val computeInvalidationReasonVariable = irTmpVariableInCurrentFun(
      computeInvalidationReason,
      nameHint = "${composable.name.asString()}\$validationReason",
    )
    newStatements += computeInvalidationReasonVariable

    val invalidationTypeSymbol = invalidationLogger.irInvalidationTypeSymbol
    val invalidationTypeProcessed =
      invalidationLogger.irInvalidationTypeProcessed(reason = irGetValue(computeInvalidationReasonVariable))
        .apply { type = invalidationTypeSymbol.defaultType }

    val currentCallstack = currentCallstack()

    val callListeners = currentInvalidationTraceTable.irCallListeners(
      key = irString(currentKey.keyName),
      callstack = currentCallstack,
      composable = currentKey.affectedComposable,
      type = invalidationTypeProcessed,
    )
    newStatements += callListeners

    val logger = invalidationLogger.irLog(
      callstack = currentCallstack,
      affectedComposable = currentKey.affectedComposable,
      invalidationType = invalidationTypeProcessed,
    )
    newStatements += logger

    body.statements.addAll(0, newStatements)

    return body.also { transformed ->
      logger("[ComposableBody] transformed dump: ${transformed.dump()}")
      logger("[ComposableBody] transformed dumpKotlinLike: ${transformed.dumpKotlinLike()}")
    }
  }*/

  override fun transformUpdateScopeBlock(target: IrSimpleFunction, initializer: IrReturn): IrStatementContainer {
    @Suppress("LocalVariableName")
    val NO_CHANGED = IrStatementContainerSimpleImpl(statements = listOf(initializer))

    val currentKey = irTrace[DurationWritableSlices.DURABLE_FUNCTION_KEY, target] ?: return NO_CHANGED
    if (!handledUpdateScopeBlock.handle(currentKey.keyName)) return NO_CHANGED

    val newStatements = mutableListOf<IrStatement>()

    val invalidationTypeSymbol = invalidationLogger.irInvalidationTypeSymbol
    val invalidationTypeProcessed = invalidationLogger.irInvalidationTypeProcessed(
      reason = IrGetObjectValueImpl(
        startOffset = UNDEFINED_OFFSET,
        endOffset = UNDEFINED_OFFSET,
        type = invalidationLogger.irInvalidateReasonSymbol.defaultType,
        symbol = invalidationLogger.irInvalidateReasonInvalidateSymbol,
      ),
    ).apply {
      type = invalidationTypeSymbol.defaultType
    }

    val logger = invalidationLogger.irLog(
      affectedComposable = currentKey.affectedComposable,
      invalidationType = invalidationTypeProcessed,
    )
    newStatements += logger

    newStatements += initializer

    return IrStatementContainerSimpleImpl(statements = newStatements).also { transformed ->
      logger("[ComposableUpdateScope] transformed dump: ${transformed.dump()}")
      logger("[ComposableUpdateScope] transformed dumpKotlinLike: ${transformed.dumpKotlinLike()}")
    }
  }

  override fun transformSkipToGroupEndCall(composable: IrSimpleFunction, initializer: IrCall): IrStatementContainer {
    @Suppress("LocalVariableName")
    val NO_CHANGED = IrStatementContainerSimpleImpl(statements = listOf(initializer))

    val currentKey = irTrace[DurationWritableSlices.DURABLE_FUNCTION_KEY, composable] ?: return NO_CHANGED
    if (!handledSkipToGroupEndCall.handle(currentKey.keyName)) return NO_CHANGED

    val invalidationTypeSymbol = invalidationLogger.irInvalidationTypeSymbol
    val invalidationTypeSkipped = invalidationLogger.irInvalidationTypeSkipped()
      .apply { type = invalidationTypeSymbol.defaultType }

    val logger = invalidationLogger.irLog(
      affectedComposable = currentKey.affectedComposable,
      invalidationType = invalidationTypeSkipped,
    )

    return IrStatementContainerSimpleImpl(statements = listOf(logger, initializer)).also { transformed ->
      logger("[ComposableSkipToGroupEnd] transformed dump: ${transformed.dump()}")
      logger("[ComposableSkipToGroupEnd] transformed dumpKotlinLike: ${transformed.dumpKotlinLike()}")
    }
  }
}
