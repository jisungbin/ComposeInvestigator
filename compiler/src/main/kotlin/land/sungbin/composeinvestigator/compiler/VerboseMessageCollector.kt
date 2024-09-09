/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

internal class VerboseMessageCollector(private val delegate: MessageCollector) : MessageCollector by delegate {
  private var verbose = false

  internal fun verbose() = apply { verbose = true }

  override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageSourceLocation?) {
    if (severity == CompilerMessageSeverity.LOGGING || !verbose) return
    delegate.report(severity, message, location)
  }
}

internal fun MessageCollector.log(message: String, location: CompilerMessageSourceLocation? = null) {
  report(CompilerMessageSeverity.LOGGING, message, location)
}

internal fun MessageCollector.error(message: String, location: CompilerMessageSourceLocation? = null) {
  report(CompilerMessageSeverity.ERROR, message, location)
}
