// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

/**
 * Registers the [FirAdditionalCheckersExtension]'s of ComposeInvestigator into the
 * Kotlin Compiler Plugin.
 */
public class ComposeInvestigatorFirExtensionRegistrar : FirExtensionRegistrar() {
  override fun ExtensionRegistrarContext.configurePlugin() {
    // +::InvalidationTraceTableInstantiationValidator
    // +::InvalidationTraceTableApiChecker
  }
}
