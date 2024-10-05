// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
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
