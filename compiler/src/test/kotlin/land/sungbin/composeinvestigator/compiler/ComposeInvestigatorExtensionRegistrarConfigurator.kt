// Copyright 2025 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

import androidx.compose.compiler.plugins.kotlin.ComposeIrGenerationExtension
import androidx.compose.compiler.plugins.kotlin.FeatureFlags
import java.util.EnumSet
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar.ExtensionStorage
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.IrVerificationMode
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives.FULL_JDK
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives.JVM_TARGET
import org.jetbrains.kotlin.test.directives.model.singleOrZeroValue
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.TestServices

fun TestConfigurationBuilder.configureInvestigatorPlugin() {
  useConfigurators(::InvestigatorExtensionRegistrarConfigurator, ::RuntimeEnvironmentConfigurator)
  useDirectives(ComposeInvestigatorDirectives)
  useSourcePreprocessor(::ComposeInvestigatorDefaultImportPreprocessor)
  useCustomRuntimeClasspathProviders(::ComposeInvestigatorRuntimeClasspathProvider)

  defaultDirectives {
    JVM_TARGET.with(JvmTarget.JVM_17)
    +FULL_JDK
  }
}

class InvestigatorExtensionRegistrarConfigurator(service: TestServices) : EnvironmentConfigurator(service) {
  override fun ExtensionStorage.registerCompilerExtensions(module: TestModule, configuration: CompilerConfiguration) {
    val investigatorFeatures =
      module.directives
        .singleOrZeroValue(ComposeInvestigatorDirectives.INVESTIGATOR_FEATURE_FLAG).orEmpty()
        .let {
          if (it.isEmpty())
            EnumSet.allOf(FeatureFlag::class.java)
          else
            EnumSet.of(it.first(), *it.toTypedArray())
        }
    val composeFeatures =
      module.directives
        .singleOrZeroValue(ComposeInvestigatorDirectives.COMPOSE_FEATURE_FLAG)
        .orEmpty()

    val messageCollector = object : MessageCollector {
      override fun clear() = Unit
      override fun hasErrors(): Boolean = false
      override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageSourceLocation?) {
        if (severity == CompilerMessageSeverity.ERROR)
          kotlin.error("$message at $location")
        else
          println("[$severity] $message at $location")
      }
    }

    FirExtensionRegistrarAdapter.registerExtension(ComposeInvestigatorFirExtensionRegistrar())
    IrGenerationExtension.registerExtension(
      ComposeInvestigatorFirstPhaseExtension(
        messageCollector,
        IrVerificationMode.ERROR,
        investigatorFeatures,
      ),
    )
    IrGenerationExtension.registerExtension(
      ComposeIrGenerationExtension(
        liveLiteralsV2Enabled = ComposeFeatureFlag.LiveLiterals in composeFeatures,
        useK2 = true,
        featureFlags = FeatureFlags(composeFeatures.map { it.name }),
        messageCollector = MessageCollector.NONE,
      ),
    )
    IrGenerationExtension.registerExtension(
      ComposeInvestigatorLastPhaseExtension(
        messageCollector,
        IrVerificationMode.ERROR,
        investigatorFeatures,
      ),
    )
  }
}
