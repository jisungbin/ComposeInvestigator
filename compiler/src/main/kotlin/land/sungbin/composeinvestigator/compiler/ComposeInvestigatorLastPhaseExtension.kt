/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler

import land.sungbin.composeinvestigator.compiler.lower.InvalidationSkipTracingLastTransformer
import land.sungbin.composeinvestigator.compiler.lower.InvalidationTraceTableInstanceTransformer
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.validateIr
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.IrVerificationMode
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

public class ComposeInvestigatorLastPhaseExtension(
  private val messageCollector: MessageCollector,
  private val verificationMode: IrVerificationMode,
) : IrGenerationExtension {
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    val tables = InvalidationTraceTableInstanceTransformer(pluginContext, messageCollector)

    // Input check. This should always pass, else something is horribly wrong upstream.
    // Necessary because oftentimes the issue is upstream. (compiler bug, prior plugin, etc)
    validateIr(messageCollector, verificationMode) {
      performBasicIrValidation(
        moduleFragment,
        pluginContext.irBuiltIns,
        phaseName = "Before ComposeInvestigator Last Phase",
        checkProperties = true,
        checkTypes = false, // TODO re-enable checking types (KT-68663)
      )
    }

    moduleFragment.transformChildrenVoid(tables)
    moduleFragment.transformChildrenVoid(InvalidationSkipTracingLastTransformer(pluginContext, messageCollector, tables))

    // Verify that our transformations didn't break something
    validateIr(messageCollector, verificationMode) {
      performBasicIrValidation(
        moduleFragment,
        pluginContext.irBuiltIns,
        phaseName = "After ComposeInvestigator Last Phase",
        checkProperties = true,
        checkTypes = false, // There is implicit type downcasting: InvalidationType.Skipped -> InvalidationType
      )
    }
  }
}
