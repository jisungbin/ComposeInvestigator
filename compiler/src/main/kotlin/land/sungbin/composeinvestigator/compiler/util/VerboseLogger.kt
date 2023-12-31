/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package land.sungbin.composeinvestigator.compiler.util

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CompilerConfiguration

internal class VerboseLogger(configuration: CompilerConfiguration) {
  private val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
  private var verbose = false

  fun verbose() {
    verbose = true
  }

  fun warn(message: Any?, location: CompilerMessageSourceLocation?) {
    if (verbose) {
      messageCollector.report(CompilerMessageSeverity.WARNING, message.toString(), location)
    }
  }

  fun error(message: Any?, location: CompilerMessageSourceLocation?) {
    if (verbose) {
      messageCollector.report(CompilerMessageSeverity.ERROR, message.toString(), location)
    }
  }

  operator fun invoke(value: Any?, location: CompilerMessageSourceLocation? = null) {
    warn(message = value, location = location)
  }
}
