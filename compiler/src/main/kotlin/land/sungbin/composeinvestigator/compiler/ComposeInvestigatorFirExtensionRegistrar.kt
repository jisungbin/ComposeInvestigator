/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler

import land.sungbin.composeinvestigator.compiler.frontend.InvalidationTraceTableApiChecker
import land.sungbin.composeinvestigator.compiler.frontend.InvalidationTraceTableInstantiationValidator
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

public class ComposeInvestigatorFirExtensionRegistrar : FirExtensionRegistrar() {
  override fun ExtensionRegistrarContext.configurePlugin() {
    +::InvalidationTraceTableInstantiationValidator
    +::InvalidationTraceTableApiChecker
  }
}
