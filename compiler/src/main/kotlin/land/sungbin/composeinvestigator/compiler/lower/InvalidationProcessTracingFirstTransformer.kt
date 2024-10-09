// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.lower

import androidx.compose.compiler.plugins.kotlin.analysis.StabilityInferencer
import androidx.compose.compiler.plugins.kotlin.analysis.normalize
import androidx.compose.compiler.plugins.kotlin.irTrace
import land.sungbin.composeinvestigator.compiler.CURRENT_COMPOSER_FQN
import land.sungbin.composeinvestigator.compiler.analysis.DurationWritableSlices
import land.sungbin.composeinvestigator.compiler.fromFqName
import land.sungbin.composeinvestigator.compiler.log
import land.sungbin.composeinvestigator.compiler.struct.IrComposableInformation
import land.sungbin.composeinvestigator.compiler.struct.IrInvalidationTraceTable
import land.sungbin.composeinvestigator.compiler.struct.IrInvalidationTraceTableHolder
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.getCompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.Scope
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.fromSymbolOwner
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.SpecialNames

/**
 * Generate code to find and report value arguments whose values have changed
 * whenever a Composable function becomes (re)composed.
 *
 * ### Original
 *
 * ```
 * @Composable fun DisplayPlusResult(a: Int, b: Int) {
 *   Text((a + b).toString())
 * }
 * ```
 *
 * ### Transformed
 *
 * ```
 * @Composable fun DisplayPlusResult(a: Int, b: Int) {
 *   val currentValueArguments = listOf(
 *     ValueArgument(
 *       name = "a",
 *       type = "kotlin.Int",
 *       valueString = a.toString(),
 *       valueHashCode = a.hashCode(),
 *       stability = Stability.Stable,
 *     ),
 *     ValueArgument(
 *       name = "b",
 *       type = "kotlin.Int",
 *       valueString = b.toString(),
 *       valueHashCode = b.hashCode(),
 *       stability = Stability.Stable,
 *     ),
 *   )
 *   val affectedComposable = ComposableInformation(
 *     name = "DisplayPlusResult",
 *     packageName = "land.sungbin.composeinvestigator.sample",
 *     fileName = "DisplayPlusResult.kt",
 *     compoundKey = androidx.compose.runtime.currentCompositeKeyHash,
 *   )
 *   val invalidationReason = currentComposableInvalidationTracer.computeInvalidationReason(
 *     keyName = "fun-DisplayPlusResult(Int,Int)Unit... (truncated)",
 *     compoundKey = androidx.compose.runtime.currentCompositeKeyHash,
 *     arguments = currentValueArguments,
 *   )
 *   ComposeInvestigator.logger.log(affectedComposable, invalidationReason)
 *   Text((a + b).toString())
 * }
 * ```
 */
public class InvalidationProcessTracingFirstTransformer(
  context: IrPluginContext,
  messageCollector: MessageCollector,
  tables: IrInvalidationTraceTableHolder,
  private val stabilityInferencer: StabilityInferencer,
) : ComposeInvestigatorBaseLower(context, messageCollector, tables) {
  private val currentComposerSymbol: IrPropertySymbol =
    context.referenceProperties(CallableId.fromFqName(CURRENT_COMPOSER_FQN)).single()

  // TODO Should I use regular variables instead of "temporary" variables?
  override fun firstTransformComposableBody(
    composable: IrSimpleFunction,
    body: IrBody,
    table: IrInvalidationTraceTable,
  ): IrBody {
    messageCollector.log(
      "Visit composable body: ${composable.name}",
      body.getCompilerMessageLocation(composable.file),
    )

    val scope = Scope(composable.symbol)
    val currentKey = context.irTrace[DurationWritableSlices.DURABLE_FUNCTION_KEY, composable] ?: return body
    val newStatements = mutableListOf<IrStatement>()

    val currentValueArguments = scope.createTemporaryVariable(
      IrCallImpl.fromSymbolOwner(
        startOffset = UNDEFINED_OFFSET,
        endOffset = UNDEFINED_OFFSET,
        symbol = mutableListOfSymbol,
      ).apply {
        putTypeArgument(0, valueArgument.symbol.defaultType)
      },
      nameHint = "currentValueArguments",
    )
    newStatements += currentValueArguments

    // TODO Only process parameters that actually used.
    for (param in composable.valueParameters) {
      // Synthetic arguments are not handled.
      if (param.name.asString().startsWith('$')) continue

      val name = irString(param.name.asString())
      val type = irString(param.type.classFqName?.asString() ?: SpecialNames.ANONYMOUS_STRING)
      val valueString = irToString(irGetValue(param))
      val valueHashCode = irHashCode(irGetValue(param))
      val stability = stabilityInferencer.stabilityOf(irGetValue(param)).normalize().asOwnStability()

      val valueArgumentVariable = scope.createTemporaryVariable(
        valueArgument(
          name = name,
          type = type,
          valueString = valueString,
          valueHashCode = valueHashCode,
          stability = stability,
        ),
        nameHint = "${param.name.asString()}\$valueArgu",
      )
      val addValueArgumentToList = IrCallImpl.fromSymbolOwner(
        startOffset = UNDEFINED_OFFSET,
        endOffset = UNDEFINED_OFFSET,
        symbol = mutableListAddSymbol,
      ).apply {
        dispatchReceiver = irGetValue(currentValueArguments)
        putValueArgument(0, irGetValue(valueArgumentVariable))
      }

      newStatements += valueArgumentVariable
      newStatements += addValueArgumentToList
    }

    // Avoid declaring duplicate IR nodes
    fun compoundKeyHashCall(): IrCallImpl =
      IrCallImpl.fromSymbolOwner(
        startOffset = UNDEFINED_OFFSET,
        endOffset = UNDEFINED_OFFSET,
        symbol = composerCompoundKeyHashSymbol,
      ).also { fn ->
        fn.dispatchReceiver = IrCallImpl.fromSymbolOwner(
          startOffset = UNDEFINED_OFFSET,
          endOffset = UNDEFINED_OFFSET,
          symbol = currentComposerSymbol.owner.getter!!.symbol,
        )
      }

    val affectedComposable = IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = IrComposableInformation.withCompoundKeySymbol(context),
    ).apply {
      dispatchReceiver = currentKey.composable
      putValueArgument(0, compoundKeyHashCall())
    }

    val invalidationReasonVariable = scope.createTemporaryVariable(
      table.irComputeInvalidationReason(
        keyName = irString(currentKey.keyName),
        compoundKey = compoundKeyHashCall(),
        arguments = irGetValue(currentValueArguments),
      ),
      nameHint = "invalidationReason",
    )
    newStatements += invalidationReasonVariable

    val invalidationResultProcessed = irGetValue(invalidationReasonVariable)
      .apply { type = invalidationLogger.irInvalidationResultSymbol.defaultType }
    val logger = invalidationLogger.irLog(affectedComposable, result = invalidationResultProcessed)

    newStatements += logger

    val newBody = context.irFactory.createBlockBody(
      startOffset = body.startOffset,
      endOffset = body.endOffset,
    ).also { block ->
      block.statements += newStatements
      block.statements += body.statements
    }

    return newBody.also {
      messageCollector.log(
        "Transform composable body succeed: ${composable.name}",
        body.getCompilerMessageLocation(composable.file),
      )
    }
  }
}
