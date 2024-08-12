/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("unused")

package land.sungbin.composeinvestigator.compiler

import land.sungbin.composeinvestigator.compiler.ComposeInvestigatorConfiguration.KEY_ENABLED
import land.sungbin.composeinvestigator.compiler.ComposeInvestigatorConfiguration.KEY_VERBOSE
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CliOptionProcessingException
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

public object ComposeInvestigatorConfiguration {
  public val KEY_ENABLED: CompilerConfigurationKey<Boolean> = CompilerConfigurationKey<Boolean>("Whether to enable compiler plugin")
  public val KEY_VERBOSE: CompilerConfigurationKey<Boolean> = CompilerConfigurationKey<Boolean>("Whether to enable verbose logging")
}

public class ComposeInvestigatorCommandLineProcessor : CommandLineProcessor {
  override val pluginId: String = PLUGIN_ID
  override val pluginOptions: List<CliOption> = listOf(OPTION_ENABLED, OPTION_VERBOSE)

  override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
    when (val optionName = option.optionName) {
      OPTION_ENABLED.optionName -> configuration.put(KEY_ENABLED, value.toBoolean())
      OPTION_VERBOSE.optionName -> configuration.put(KEY_VERBOSE, value.toBoolean())
      else -> throw CliOptionProcessingException("Unknown plugin option: $optionName")
    }
  }

  public companion object {
    public const val PLUGIN_ID: String = "land.sungbin.composeinvestigator.compiler"

    public val OPTION_ENABLED: CliOption = CliOption(
      optionName = "enabled",
      valueDescription = "<true | false>",
      description = KEY_ENABLED.toString(),
      required = false,
    )

    public val OPTION_VERBOSE: CliOption = CliOption(
      optionName = "verbose",
      valueDescription = "<true | false>",
      description = KEY_VERBOSE.toString(),
      required = false,
    )
  }
}
