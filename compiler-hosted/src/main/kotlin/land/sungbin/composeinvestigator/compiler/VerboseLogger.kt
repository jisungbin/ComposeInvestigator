/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package land.sungbin.composeinvestigator.compiler

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CompilerConfiguration

public class VerboseLogger() {
  private var messageCollector = MessageCollector.NONE

  public constructor(configuration: CompilerConfiguration) : this() {
    this.messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
  }

  public constructor(messageCollector: MessageCollector) : this() {
    this.messageCollector = messageCollector
  }

  private var verbose = false

  public fun verbose() {
    verbose = true
  }

  public fun warn(message: Any?, location: CompilerMessageSourceLocation?) {
    if (verbose) {
      messageCollector.report(CompilerMessageSeverity.WARNING, message.toString(), location)
    }
  }

  public fun error(message: Any?, location: CompilerMessageSourceLocation?) {
    if (verbose) {
      messageCollector.report(CompilerMessageSeverity.ERROR, message.toString(), location)
    }
  }

  public operator fun invoke(value: Any?, location: CompilerMessageSourceLocation? = null) {
    warn(message = value, location = location)
  }
}
