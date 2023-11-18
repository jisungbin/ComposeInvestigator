/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("unused")

package land.sungbin.composeinvestigator.compiler

import com.google.auto.service.AutoService
import org.jetbrains.annotations.VisibleForTesting
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@VisibleForTesting
public const val COMPOSE_INVESTIGATOR_PLUGIN_ID: String = "land.sungbin.composeinvestigator.compiler"

internal val KEY_VERBOSE = CompilerConfigurationKey<String>("Whether to enable verbose log")

@VisibleForTesting
public val OPTION_VERBOSE: CliOption =
  CliOption(
    optionName = "verbose",
    valueDescription = "<true | false>",
    description = KEY_VERBOSE.toString(),
    required = false,
  )

@VisibleForTesting
@AutoService(CommandLineProcessor::class)
public class ComposeInvestigatorCommandLineProcessor : CommandLineProcessor {
  override val pluginId: String = COMPOSE_INVESTIGATOR_PLUGIN_ID

  override val pluginOptions: List<CliOption> = listOf(OPTION_VERBOSE)

  override fun processOption(
    option: AbstractCliOption,
    value: String,
    configuration: CompilerConfiguration,
  ) {
    when (val optionName = option.optionName) {
      OPTION_VERBOSE.optionName -> configuration.put(KEY_VERBOSE, value)
      else -> error("Unknown plugin option: $optionName")
    }
  }
}
