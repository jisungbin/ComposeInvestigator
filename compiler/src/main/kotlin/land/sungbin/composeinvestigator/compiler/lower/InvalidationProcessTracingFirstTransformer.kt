/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.lower

import androidx.compose.compiler.plugins.kotlin.analysis.StabilityInferencer
import androidx.compose.compiler.plugins.kotlin.analysis.normalize
import androidx.compose.compiler.plugins.kotlin.irTrace
import land.sungbin.composeinvestigator.compiler.analysis.DurationWritableSlices
import land.sungbin.composeinvestigator.compiler.log
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
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.name.SpecialNames

internal class InvalidationProcessTracingFirstTransformer(
  context: IrPluginContext,
  messageCollector: MessageCollector,
  tables: IrInvalidationTraceTableHolder,
  private val stabilityInferencer: StabilityInferencer,
) : ComposeInvestigatorBaseLower(context, messageCollector, tables) {
  // TODO should I use regular variables instead of "temporary" variables?
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

    // TODO only process parameters that actually used.
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

    val compoundKeyHashCall = IrCallImpl.fromSymbolOwner(
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

    val invalidationReasonVariable = scope.createTemporaryVariable(
      table.irComputeInvalidationReason(
        keyName = irString(currentKey.keyName),
        compoundKey = compoundKeyHashCall,
        arguments = irGetValue(currentValueArguments),
      ),
      nameHint = "invalidationReason",
    )
    newStatements += invalidationReasonVariable

    val invalidationTypeProcessed = irGetValue(invalidationReasonVariable)
      .apply { type = invalidationLogger.irInvalidationTypeSymbol.defaultType }
    val logger = invalidationLogger.irLog(currentKey.composable, type = invalidationTypeProcessed)

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
