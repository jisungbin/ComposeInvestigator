// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.lower

import androidx.compose.compiler.plugins.kotlin.analysis.StabilityInferencer
import androidx.compose.compiler.plugins.kotlin.analysis.normalize
import land.sungbin.composeinvestigator.compiler.log
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.getCompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.createBlockBody
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.fromSymbolOwner
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.name.Name.identifier
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
 *   ComposeInvestigator.Logger.log(affectedComposable, invalidationReason)
 *   Text((a + b).toString())
 * }
 * ```
 */
public class InvalidationProcessTracingFirstTransformer(
  context: IrPluginContext,
  messageCollector: MessageCollector,
  private val stabilityInferencer: StabilityInferencer,
) : ComposeInvestigatorBaseLower(context, messageCollector) {
  override fun firstTransformComposableBody(composable: IrSimpleFunction, body: IrBody): IrBody {
    messageCollector.log(
      "Visit composable body: ${composable.name}",
      body.getCompilerMessageLocation(composable.file),
    )

    val newStatements = mutableListOf<IrStatement>()

    val currentValueArguments =
      irVariable(
        identifier("currentValueArguments"),
        IrCallImpl.fromSymbolOwner(
          startOffset = UNDEFINED_OFFSET,
          endOffset = UNDEFINED_OFFSET,
          symbol = mutableListOfSymbol,
        ).apply {
          putTypeArgument(0, irValueArgument.symbol.defaultType)
        },
      )
    newStatements += currentValueArguments

    for (param in composable.valueParameters) {
      // Synthetic arguments are not handled.
      if (param.name.asString().startsWith('$')) continue

      val name = irString(param.name.asString())
      val type = irString(param.type.classFqName?.asString() ?: SpecialNames.ANONYMOUS_STRING)
      val valueString = irToString(irGetValue(param))
      val valueHashCode = irHashCode(irGetValue(param))
      val stability = stabilityInferencer.stabilityOf(irGetValue(param)).normalize()

      val valueArgumentVariable =
        irVariable(
          identifier("${param.name.asString()}\$valueArgument"),
          irValueArgument(
            name = name,
            type = type,
            valueString = valueString,
            valueHashCode = valueHashCode,
            stability = with(irRuntimeStability) { stability.asRuntimeStability() },
          ),
        )
      val addValueArgumentToList =
        IrCallImpl.fromSymbolOwner(
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

    val invalidationReasonVariable =
      irVariable(
        identifier("invalidationReason"),
        composable.file
          .irComposeInvestigator()
          .irComputeInvalidationReason(
            irCompoundKeyHashCall(irCurrentComposer()),
            irGetValue(currentValueArguments),
          ),
      )
    newStatements += invalidationReasonVariable

    val composableInformation =
      irComposableInformation(
        irString(composable.name.asString()),
        irString(composable.file.packageFqName.asString()),
        irString(composable.file.name),
        irCompoundKeyHashCall(irCurrentComposer()),
      )
    val invalidationResult =
      irGetValue(invalidationReasonVariable, type = irInvalidationLogger.invalidationResultSymbol.defaultType)

    val logger = irInvalidationLogger.irLog(composableInformation, invalidationResult)
    newStatements += logger

    return context.irFactory.createBlockBody(
      startOffset = body.startOffset,
      endOffset = UNDEFINED_OFFSET,
    ) {
      statements += newStatements
      statements += body.statements
    }
      .also {
        messageCollector.log(
          "Transform composable body succeed: ${composable.name}",
          body.getCompilerMessageLocation(composable.file),
        )
      }
  }
}
