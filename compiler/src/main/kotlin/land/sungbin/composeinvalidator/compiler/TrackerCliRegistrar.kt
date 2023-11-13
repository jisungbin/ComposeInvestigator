/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

@file:OptIn(ExperimentalCompilerApi::class)
@file:Suppress("unused")

package land.sungbin.composeinvalidator.compiler

import com.google.auto.service.AutoService
import org.jetbrains.annotations.VisibleForTesting
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@VisibleForTesting
internal const val PLUGIN_ID = "land.sungbin.composeinvalidator.compiler"

internal val KEY_VERBOSE = CompilerConfigurationKey<String>("Whether to enable verbose log")

@VisibleForTesting
internal val OPTION_VERBOSE =
  CliOption(
    optionName = "verbose",
    valueDescription = "<true | false>",
    description = KEY_VERBOSE.toString(),
    required = false,
  )

@AutoService(CommandLineProcessor::class)
internal class TrackerCliRegistrar : CommandLineProcessor {
  override val pluginId = PLUGIN_ID

  override val pluginOptions = listOf(OPTION_VERBOSE)

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
