// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("unused")

package land.sungbin.composeinvestigator.compiler

import java.util.EnumSet
import land.sungbin.composeinvestigator.compiler.ComposeInvestigatorConfiguration.KEY_FEATURES
import land.sungbin.composeinvestigator.compiler.ComposeInvestigatorConfiguration.KEY_VERBOSE
import org.jetbrains.kotlin.buildtools.api.CompilerArgumentsParseException
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CliOptionProcessingException
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

public object ComposeInvestigatorConfiguration {
  public val KEY_FEATURES: CompilerConfigurationKey<EnumSet<FeatureFlag>> = CompilerConfigurationKey("Features to enable")
  public val KEY_VERBOSE: CompilerConfigurationKey<Boolean> = CompilerConfigurationKey("Whether to enable verbose logging")
}

public class ComposeInvestigatorCommandLineProcessor : CommandLineProcessor {
  override val pluginId: String get() = PLUGIN_ID
  override val pluginOptions: List<CliOption> = listOf(OPTION_FEATURES, OPTION_VERBOSE)

  override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
    when (val optionName = option.optionName) {
      OPTION_FEATURES.optionName -> configuration.put(KEY_FEATURES, value.toFeatureSetOrThrows())
      OPTION_VERBOSE.optionName -> configuration.put(KEY_VERBOSE, value.toBooleanOrThrows())
      else -> throw CliOptionProcessingException("Unknown plugin option: $optionName")
    }
  }

  private fun String.toFeatureSetOrThrows(): EnumSet<FeatureFlag> =
    split(',')
      .map { FeatureFlag.entries.find { flag -> flag.name == it } ?: throw CompilerArgumentsParseException("Invalid feature: $it") }
      .also { if (it.isEmpty()) return EnumSet.noneOf(FeatureFlag::class.java) }
      .let { EnumSet.of(it.first(), *it.toTypedArray()) }

  private fun String.toBooleanOrThrows(): Boolean =
    toBooleanStrictOrNull() ?: throw CompilerArgumentsParseException("Invalid value for boolean option: $this")

  public companion object {
    public const val PLUGIN_ID: String = "land.sungbin.composeinvestigator.compiler"

    public val OPTION_FEATURES: CliOption =
      CliOption(
        optionName = "features",
        valueDescription = "<true | false>",
        description = KEY_FEATURES.toString(),
        required = false,
      )

    public val OPTION_VERBOSE: CliOption =
      CliOption(
        optionName = "verbose",
        valueDescription = "<true | false>",
        description = KEY_VERBOSE.toString(),
        required = false,
      )
  }
}
