// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.lower

import land.sungbin.composeinvestigator.compiler.InvestigatorClassIds
import land.sungbin.composeinvestigator.compiler.log
import land.sungbin.composeinvestigator.compiler.struct.IrComposeInvestigator
import land.sungbin.composeinvestigator.compiler.struct.irComposeInvestigator
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.setDeclarationsParent
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

/**
 * Generates code to instantiate `ComposableInvalidationTraceTable` as the top-level
 * variable of the current file, if the file is not annotated with `@file:NoInvestigation`.
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
 * val ComposeInvestigatorImpl$DisplayPlusResultKt = ComposeInvestigator()
 *
 * @Composable fun DisplayPlusResult(a: Int, b: Int) {
 *   Text((a + b).toString())
 * }
 * ```
 */
public class ComposeInvestigatorInstantiateTransformer(
  private val context: IrPluginContext,
  private val messageCollector: MessageCollector, // TODO context.createDiagnosticReporter() (Blocked: "This API is not supported for K2")
) : IrElementTransformerVoid() {
  override fun visitFile(declaration: IrFile): IrFile =
    includeFilePathInExceptionTrace(declaration) {
      if (declaration.hasAnnotation(InvestigatorClassIds.NoInvestigation))
        return declaration

      val composeInvestigator =
        IrComposeInvestigator.create(context, declaration).also {
          declaration.declarations.add(0, it.property.also { prop -> prop.setDeclarationsParent(declaration) })
        }
      declaration.irComposeInvestigator = composeInvestigator

      messageCollector.log("Instantiated ComposeInvestigator for ${declaration.name}")

      super.visitFile(declaration)
    }
}
