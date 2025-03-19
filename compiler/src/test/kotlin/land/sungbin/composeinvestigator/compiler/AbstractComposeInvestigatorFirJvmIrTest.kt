// Copyright 2025 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.builders.irHandlersStep
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives.DUMP_IR
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives.DUMP_KT_IR
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives.DUMP_SIGNATURES
import org.jetbrains.kotlin.test.directives.ConfigurationDirectives.WITH_STDLIB
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives.FULL_JDK
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives.JVM_TARGET
import org.jetbrains.kotlin.test.directives.LanguageSettingsDirectives.LINK_VIA_SIGNATURES_K1
import org.jetbrains.kotlin.test.runners.ir.AbstractFirLightTreeJvmIrTextTest

open class AbstractComposeInvestigatorFirJvmIrTest : AbstractFirLightTreeJvmIrTextTest() {
  override fun createKotlinStandardLibrariesPathProvider() = ClasspathBasedStandardLibrariesPathProvider

  override fun configure(builder: TestConfigurationBuilder) {
    super.configure(builder)
    with(builder) {
      configureComposeInvestigatorPlugin()

      irHandlersStep {
        useHandlers(::SimpleIrSourceDumpHandler)
      }

      defaultDirectives {
        JVM_TARGET with JvmTarget.JVM_17

        +FULL_JDK
        +WITH_STDLIB

        // Added in https://github.com/JetBrains/kotlin/blob/61efa6e0e2c1c50243f13c21228ee9bd43eb7197/compiler/tests-common-new/tests/org/jetbrains/kotlin/test/configuration/BaseIrTextConfiguration.kt#L32-L34
        -DUMP_IR
        -DUMP_KT_IR
        -DUMP_SIGNATURES
        -LINK_VIA_SIGNATURES_K1
      }
    }
  }
}
