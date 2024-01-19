/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("unused")

package land.sungbin.composeinvestigator.compiler.callstack

import com.google.auto.service.AutoService
import land.sungbin.composeinvestigator.compiler.callstack.ComposableCallstackTrackerConfiguration.KEY_VERBOSE
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CliOptionProcessingException
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

public object ComposableCallstackTrackerConfiguration {
  public val KEY_VERBOSE: CompilerConfigurationKey<String> = CompilerConfigurationKey<String>("Whether to enable verbose log")
}

@AutoService(CommandLineProcessor::class)
public class ComposableCallstackTrackerCommandLineProcessor : CommandLineProcessor {
  override val pluginId: String = PLUGIN_ID
  override val pluginOptions: List<CliOption> = listOf(OPTION_VERBOSE)

  override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
    when (val optionName = option.optionName) {
      OPTION_VERBOSE.optionName -> configuration.put(KEY_VERBOSE, value)
      else -> throw CliOptionProcessingException("Unknown plugin option: $optionName")
    }
  }

  public companion object {
    public const val PLUGIN_ID: String = "land.sungbin.composeinvestigator.compiler.callstack"

    public val OPTION_VERBOSE: CliOption = CliOption(
      optionName = "verbose",
      valueDescription = "<true | false>",
      description = KEY_VERBOSE.toString(),
      required = false,
    )
  }
}
