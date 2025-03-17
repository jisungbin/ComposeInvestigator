// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.lower

import androidx.compose.compiler.plugins.kotlin.ComposeClassIds
import land.sungbin.composeinvestigator.compiler.log
import land.sungbin.composeinvestigator.compiler.struct.IrComposeInvestigator
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.getCompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.classId
import org.jetbrains.kotlin.ir.util.file

/**
 * Generates code that reports whenever a Composable function is skipped during
 * recomposition due to smart recomposition.
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
 *   if (!currentComposer.skipping) {
 *     Text((a + b).toString())
 *   } else {
 *     val affectedComposable = ComposableInformation(
 *       name = "DisplayPlusResult",
 *       packageName = "land.sungbin.composeinvestigator.sample",
 *       fileName = "DisplayPlusResult.kt",
 *       compoundKey = androidx.compose.runtime.currentCompositeKeyHash,
 *     )
 *     ComposeInvestigator.Logger.log(affectedComposable, InvalidationReason.Skipped)
 *     currentComposer.skipToGroupEnd()
 *   }
 * }
 * ```
 */
public class InvalidationSkipTracingLastTransformer(
  context: IrPluginContext,
  messageCollector: MessageCollector,
) : ComposeInvestigatorBaseLower(context, messageCollector) {
  override fun lastTransformSkipToGroupEndCall(composable: IrFunction, expression: IrCall): IrExpression {
    messageCollector.log(
      "Visit skipToGroupEnd call: ${composable.name}",
      expression.getCompilerMessageLocation(composable.file),
    )

    val composer =
      composable.valueParameters
        .last { param -> param.type.classOrNull?.owner?.classId == ComposeClassIds.Composer }
        .let(::irGetValue)
    val investigator = IrComposeInvestigator(context)
    val compoundKey = irCompoundKeyHash(composer)

    val composableInformation =
      irComposableInformation(
        investigator.irGetCurrentComposableName(irString(composable.name.asString()), compoundKey),
        irString(composable.file.packageFqName.asString()),
        irString(composable.file.name),
        compoundKey,
      )
    val invalidationResult =
      irInvalidationLogger.irInvalidationResultSkipped()
        .apply { type = irInvalidationLogger.invalidationResultSymbol.defaultType }

    val logger = irInvalidationLogger.irLog(composableInformation, invalidationResult)

    return IrBlockImpl(
      startOffset = expression.startOffset,
      endOffset = expression.endOffset,
      type = context.irBuiltIns.unitType,
      statements = listOf(logger, expression),
    )
      .also {
        messageCollector.log(
          "Transform skipToGroupEnd call succeed: ${composable.name}",
          expression.getCompilerMessageLocation(composable.file),
        )
      }
  }
}
