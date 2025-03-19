// Copyright 2025 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives.IGNORE_DEXING
import org.jetbrains.kotlin.test.directives.ConfigurationDirectives.WITH_STDLIB
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives.FULL_JDK
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives.JVM_TARGET
import org.jetbrains.kotlin.test.runners.codegen.AbstractFirLightTreeBlackBoxCodegenTest

abstract class AbstractComposeInvestigatorFirBlackBoxTest : AbstractFirLightTreeBlackBoxCodegenTest() {
  override fun createKotlinStandardLibrariesPathProvider() = ClasspathBasedStandardLibrariesPathProvider
  override fun configure(builder: TestConfigurationBuilder) {
    super.configure(builder)
    with(builder) {
      configureInvestigatorPlugin()
      defaultDirectives {
        +FULL_JDK
        +WITH_STDLIB
        JVM_TARGET with JvmTarget.JVM_17
        +IGNORE_DEXING // Avoids loading R8 from the classpath.
      }
    }
  }
}
