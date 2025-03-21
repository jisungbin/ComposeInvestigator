// Copyright 2025 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

import androidx.compose.compiler.plugins.kotlin.ComposeIrGenerationExtension
import androidx.compose.compiler.plugins.kotlin.FeatureFlags
import java.util.EnumSet
import land.sungbin.composeinvestigator.compiler.ComposeInvestigatorDirectives.COMPOSE_FEATURES
import land.sungbin.composeinvestigator.compiler.ComposeInvestigatorDirectives.INVESTIGATOR_FEATURES
import land.sungbin.composeinvestigator.compiler.ComposeInvestigatorDirectives.WITH_COMPOSE
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar.ExtensionStorage
import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.IrVerificationMode
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.config.messageCollector
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.model.singleOrZeroValue
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.TestServices

fun TestConfigurationBuilder.configureComposeInvestigatorPlugin() {
  useConfigurators(::InvestigatorExtensionRegistrarConfigurator, ::ComposeInvestigatorRuntimeEnvironmentConfigurator)
  useDirectives(ComposeInvestigatorDirectives)
  useSourcePreprocessor(::ComposeInvestigatorDefaultImportPreprocessor)
  useCustomRuntimeClasspathProviders(::ComposeInvestigatorRuntimeClasspathProvider)
}

class InvestigatorExtensionRegistrarConfigurator(services: TestServices) : EnvironmentConfigurator(services) {
  override fun ExtensionStorage.registerCompilerExtensions(module: TestModule, configuration: CompilerConfiguration) {
    configuration.languageVersionSettings = LanguageVersionSettingsImpl(LanguageVersion.KOTLIN_2_1, ApiVersion.KOTLIN_2_1)

    val investigatorFeatures =
      module.directives
        .singleOrZeroValue(INVESTIGATOR_FEATURES).orEmpty()
        .let {
          if (it.isEmpty())
            EnumSet.allOf(FeatureFlag::class.java)
          else
            EnumSet.of(it.first(), *it.toTypedArray())
        }
    val enablesComposeCompiler = WITH_COMPOSE in module.directives
    val composeFeatures = module.directives.singleOrZeroValue(COMPOSE_FEATURES).orEmpty()

    val messageCollector = object : MessageCollector {
      override fun clear() = Unit
      override fun hasErrors(): Boolean = false
      override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageSourceLocation?) {
        if (severity.isError)
          throw AssertionError("$message at $location")
        else
          println("[$severity] $message at $location")
      }
    }
    configuration.messageCollector = messageCollector

    IrGenerationExtension.registerExtension(
      ComposeInvestigatorFirstPhaseExtension(
        messageCollector,
        IrVerificationMode.ERROR,
        investigatorFeatures,
      ),
    )
    if (enablesComposeCompiler || composeFeatures.isNotEmpty()) {
      IrGenerationExtension.registerExtension(
        ComposeIrGenerationExtension(
          liveLiteralsV2Enabled = ComposeFeatureFlag.LiveLiterals in composeFeatures,
          useK2 = true,
          featureFlags = FeatureFlags(composeFeatures.map { it.name }),
          messageCollector = MessageCollector.NONE,
        ),
      )
    }
    IrGenerationExtension.registerExtension(
      ComposeInvestigatorLastPhaseExtension(
        messageCollector,
        IrVerificationMode.ERROR,
        investigatorFeatures,
      ),
    )
  }
}
