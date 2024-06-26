/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test._compilation.compiler

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

object ErrorMessageCollector : MessageCollector {
  override fun clear() {}

  override fun report(
    severity: CompilerMessageSeverity,
    message: String,
    location: CompilerMessageSourceLocation?,
  ) {
    if (severity === CompilerMessageSeverity.ERROR) {
      throw AssertionError(
        if (location == null) message
        else "(${location.path}:${location.line}:${location.column}) $message",
      )
    }
  }

  override fun hasErrors() = false
}
