/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package land.sungbin.composeinvestigator.compiler

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.messageCollector

public class VerboseLogger(configuration: CompilerConfiguration) {
  private val messageCollector = configuration.messageCollector
  private var verbose = false

  public fun verbose(): VerboseLogger = apply { verbose = true }

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
