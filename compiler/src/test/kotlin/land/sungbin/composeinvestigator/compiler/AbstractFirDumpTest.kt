// Copyright 2025 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives.FIR_DUMP
import org.jetbrains.kotlin.test.runners.AbstractFirLightTreeDiagnosticsTest

open class AbstractFirDumpTest : AbstractFirLightTreeDiagnosticsTest() {
  override fun createKotlinStandardLibrariesPathProvider() = ClasspathBasedStandardLibrariesPathProvider
  override fun configure(builder: TestConfigurationBuilder) {
    super.configure(builder)
    with(builder) {
      configureInvestigatorPlugin()
      defaultDirectives { +FIR_DUMP }
    }
  }
}
