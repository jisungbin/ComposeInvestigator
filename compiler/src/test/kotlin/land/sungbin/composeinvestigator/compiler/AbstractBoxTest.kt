// Copyright 2025 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives.IGNORE_DEXING
import org.jetbrains.kotlin.test.directives.ConfigurationDirectives.WITH_STDLIB
import org.jetbrains.kotlin.test.runners.codegen.AbstractFirLightTreeBlackBoxCodegenTest

open class AbstractBoxTest : AbstractFirLightTreeBlackBoxCodegenTest() {
  override fun createKotlinStandardLibrariesPathProvider() = ClasspathBasedStandardLibrariesPathProvider
  override fun configure(builder: TestConfigurationBuilder) {
    super.configure(builder)
    with(builder) {
      configureInvestigatorPlugin()
      defaultDirectives {
        +WITH_STDLIB
        +IGNORE_DEXING // Avoids loading R8 from the classpath.
      }
    }
  }
}
